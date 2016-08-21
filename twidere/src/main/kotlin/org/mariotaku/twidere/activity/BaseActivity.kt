/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Rect
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.TwidereActionMenuView
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.ATEActivity
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.customizers.ATEStatusBarCustomizer
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer
import com.squareup.otto.Bus
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.activity.iface.IExtendedActivity
import org.mariotaku.twidere.activity.iface.IThemedActivity
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback
import org.mariotaku.twidere.preference.iface.IDialogPreference
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.iface.IExtendedView.OnFitSystemWindowsListener
import java.lang.reflect.InvocationTargetException
import java.util.*
import javax.inject.Inject

@SuppressLint("Registered")
open class BaseActivity : ATEActivity(), Constants, IExtendedActivity, IThemedActivity,
        IControlBarActivity, OnFitSystemWindowsListener, SystemWindowsInsetsCallback,
        KeyboardShortcutCallback, OnPreferenceDisplayDialogCallback, ATEToolbarCustomizer,
        ATEStatusBarCustomizer {
    @Inject
    lateinit var keyboardShortcutsHandler: KeyboardShortcutsHandler
    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var permissionsManager: PermissionsManager

    private val actionHelper = IExtendedActivity.ActionHelper(this)

    // Registered listeners
    private val mControlBarOffsetListeners = ArrayList<IControlBarActivity.ControlBarOffsetListener>()

    // Data fields
    private var mSystemWindowsInsets: Rect? = null
    var keyMetaState: Int = 0
        private set

    override fun getSystemWindowsInsets(insets: Rect): Boolean {
        if (mSystemWindowsInsets == null) return false
        insets.set(mSystemWindowsInsets)
        return true
    }

    override fun onFitSystemWindows(insets: Rect) {
        if (mSystemWindowsInsets == null)
            mSystemWindowsInsets = Rect(insets)
        else {
            mSystemWindowsInsets!!.set(insets)
        }
        notifyControlBarOffsetChanged()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (KeyEvent.isModifierKey(keyCode)) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                keyMetaState = keyMetaState or KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode)
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                keyMetaState = keyMetaState and KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode).inv()
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (handleKeyboardShortcutSingle(keyboardShortcutsHandler, keyCode, event, keyMetaState))
            return true
        return isKeyboardShortcutHandled(keyboardShortcutsHandler, keyCode, event, keyMetaState) || super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (handleKeyboardShortcutRepeat(keyboardShortcutsHandler, keyCode, event.repeatCount, event, keyMetaState))
            return true
        return isKeyboardShortcutHandled(keyboardShortcutsHandler, keyCode, event, keyMetaState) || super.onKeyDown(keyCode, event)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        return false
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        return false
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
            StrictModeUtils.detectAllThreadPolicy()
        }
        ThemeUtils.applyDayNight(getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE), delegate)
        super.onCreate(savedInstanceState)
        GeneralComponentHelper.build(this).inject(this)
    }

    override fun onResume() {
        super.onResume()
        val adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter != null && adapter.isEnabled) {

            val handlerFilter = IntentUtils.getWebLinkIntentFilter(this)
            if (handlerFilter != null) {
                val linkIntent = Intent(this, WebLinkHandlerActivity::class.java)
                val intent = PendingIntent.getActivity(this, 0, linkIntent, 0)
                val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
                for (i in 0 until handlerFilter.countDataSchemes()) {
                    intentFilter.addDataScheme(handlerFilter.getDataScheme(i))
                }
                for (i in 0 until handlerFilter.countDataAuthorities()) {
                    val authorityEntry = handlerFilter.getDataAuthority(i)
                    val port = authorityEntry.port
                    intentFilter.addDataAuthority(authorityEntry.host, if (port < 0) null else Integer.toString(port))
                }
                try {
                    adapter.enableForegroundDispatch(this, intent, arrayOf(intentFilter), null)
                } catch (e: Exception) {
                    // Ignore if blocked by modified roms
                }

            }
        }
    }

    override fun onPause() {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter != null && adapter.isEnabled) {
            try {
                adapter.disableForegroundDispatch(this)
            } catch (e: Exception) {
                // Ignore if blocked by modified roms
            }

        }
        actionHelper.dispatchOnPause()
        super.onPause()
    }

    override fun setControlBarOffset(offset: Float) {

    }

    override fun setControlBarVisibleAnimate(visible: Boolean) {

    }

    override fun setControlBarVisibleAnimate(visible: Boolean, listener: IControlBarActivity.ControlBarShowHideHelper.ControlBarAnimationListener) {

    }

    override fun getControlBarOffset(): Float {
        return 0f
    }

    override fun getControlBarHeight(): Int {
        return 0
    }

    override fun notifyControlBarOffsetChanged() {
        val offset = controlBarOffset
        for (l in mControlBarOffsetListeners) {
            l.onControlBarOffsetChanged(this, offset)
        }
    }

    override fun registerControlBarOffsetListener(listener: IControlBarActivity.ControlBarOffsetListener) {
        mControlBarOffsetListeners.add(listener)
    }

    override fun unregisterControlBarOffsetListener(listener: IControlBarActivity.ControlBarOffsetListener) {
        mControlBarOffsetListeners.remove(listener)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        actionHelper.dispatchOnResumeFragments()
    }

    override fun executeAfterFragmentResumed(action: (IExtendedActivity) -> Unit) {
        actionHelper.executeAfterFragmentResumed(action)
    }

    override final val currentThemeBackgroundAlpha by lazy {
        themeBackgroundAlpha
    }

    override final val currentThemeBackgroundOption by lazy {
        themeBackgroundOption
    }

    override val themeBackgroundAlpha: Int
        get() = ThemeUtils.getUserThemeBackgroundAlpha(this)


    override val themeBackgroundOption: String
        get() = ThemeUtils.getThemeBackgroundOption(this)


    override fun getATEKey(): String? {
        return ThemeUtils.getATEKey(this)
    }

    override fun onApplyThemeResource(theme: Resources.Theme, resId: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resId, first)
        if (window != null && shouldApplyWindowBackground) {
            ThemeUtils.applyWindowBackground(this, window, themeBackgroundOption,
                    themeBackgroundAlpha)
        }
    }

    protected val shouldApplyWindowBackground: Boolean
        get() {
            return true
        }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        // Fix for https://github.com/afollestad/app-theme-engine/issues/109
        if (context !== this) {
            val delegate = delegate
            var view: View? = delegate.createView(parent, name, context, attrs)
            if (view == null) {
                view = newInstance(name, context, attrs)
            }
            if (view == null) {
                view = newInstance(name, context, attrs)
            }
            if (view != null) {
                return view
            }
        }
        if (parent is TwidereActionMenuView) {
            val cls = findClass(name)
            if (cls != null && ActionMenuItemView::class.java.isAssignableFrom(cls)) {
                return parent.createActionMenuView(context, attrs)
            }
        }
        return super.onCreateView(parent, name, context, attrs)
    }

    private fun findClass(name: String): Class<*>? {
        var cls: Class<*>? = null
        try {
            cls = Class.forName(name)
        } catch (e: ClassNotFoundException) {
            // Ignore
        }

        if (cls != null) return cls
        for (prefix in sClassPrefixList) {
            try {
                cls = Class.forName(prefix + name)
            } catch (e: ClassNotFoundException) {
                // Ignore
            }

            if (cls != null) return cls
        }
        return null
    }

    private fun newInstance(name: String, context: Context, attrs: AttributeSet): View? {
        try {
            val cls = findClass(name) ?: throw ClassNotFoundException(name)
            val constructor = cls.getConstructor(Context::class.java, AttributeSet::class.java)
            return constructor.newInstance(context, attrs) as View
        } catch (e: InstantiationException) {
            return null
        } catch (e: IllegalAccessException) {
            return null
        } catch (e: InvocationTargetException) {
            return null
        } catch (e: NoSuchMethodException) {
            return null
        } catch (e: ClassNotFoundException) {
            return null
        }

    }

    override fun onPreferenceDisplayDialog(fragment: PreferenceFragmentCompat, preference: Preference): Boolean {
        if (preference is IDialogPreference) {
            preference.displayDialog(fragment)
            return true
        }
        return false
    }

    override fun getStatusBarColor(): Int {
        return ATE.USE_DEFAULT
    }

    override fun getToolbarColor(toolbar: Toolbar?): Int {
        return ATE.USE_DEFAULT
    }

    override fun getLightStatusBarMode(): Int {
        //noinspection WrongConstant
        return ThemeUtils.getLightStatusBarMode(Config.statusBarColor(this, ateKey))
    }

    override fun getLightToolbarMode(toolbar: Toolbar?): Int {
        //noinspection WrongConstant
        return ThemeUtils.getLightToolbarMode(Config.toolbarColor(this, ateKey, toolbar))
    }

    companion object {

        private val sClassPrefixList = arrayOf("android.widget.", "android.view.", "android.webkit.")
    }
}
