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
import android.content.*
import android.content.res.Resources
import android.graphics.Rect
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.appcompat.app.TwilightManagerAccessor
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.TwidereActionMenuView
import androidx.core.graphics.ColorUtils
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonActivity
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.activityLabel
import org.mariotaku.ktextension.getSystemWindowInsets
import org.mariotaku.ktextension.unregisterReceiverSafe
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.activity.iface.IBaseActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.activity.iface.IThemedActivity
import org.mariotaku.twidere.annotation.NavbarStyle
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.extension.defaultSharedPreferences
import org.mariotaku.twidere.extension.firstLanguage
import org.mariotaku.twidere.extension.overriding
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowInsetsCallback
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.preference.iface.IDialogPreference
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.gifshare.GifShareProvider
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.util.schedule.StatusScheduleProvider
import org.mariotaku.twidere.util.support.ActivitySupport
import org.mariotaku.twidere.util.support.ActivitySupport.TaskDescriptionCompat
import org.mariotaku.twidere.util.support.WindowSupport
import org.mariotaku.twidere.util.sync.TimelineSyncManager
import org.mariotaku.twidere.util.theme.TwidereAppearanceCreator
import org.mariotaku.twidere.util.theme.getCurrentThemeResource
import java.lang.reflect.InvocationTargetException
import java.util.*
import javax.inject.Inject

@SuppressLint("Registered")
open class BaseActivity : ChameleonActivity(), IBaseActivity<BaseActivity>, IThemedActivity,
        IControlBarActivity, OnApplyWindowInsetsListener, SystemWindowInsetsCallback,
        KeyboardShortcutCallback, OnPreferenceDisplayDialogCallback {

    @Inject
    lateinit var keyboardShortcutsHandler: KeyboardShortcutsHandler
    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var kPreferences: KPreferences
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var permissionsManager: PermissionsManager
    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit var statusScheduleProviderFactory: StatusScheduleProvider.Factory
    @Inject
    lateinit var timelineSyncManagerFactory: TimelineSyncManager.Factory
    @Inject
    lateinit var gifShareProviderFactory: GifShareProvider.Factory
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
    @Inject
    lateinit var restHttpClient: RestHttpClient
    @Inject
    lateinit var mastodonApplicationRegistry: MastodonApplicationRegistry
    @Inject
    lateinit var taskServiceRunner: TaskServiceRunner
    @Inject
    lateinit var promotionService: PromotionService

    lateinit var requestManager: RequestManager
        private set

    protected val statusScheduleProvider: StatusScheduleProvider?
        get() = statusScheduleProviderFactory.newInstance(this)

    protected val timelineSyncManager: TimelineSyncManager?
        get() = timelineSyncManagerFactory.get()

    protected val gifShareProvider: GifShareProvider?
        get() = gifShareProviderFactory.newInstance(this)

    protected val isDialogTheme: Boolean
        get() = ThemeUtils.getBooleanFromAttribute(this, R.attr.isDialogTheme)

    final override val currentThemeBackgroundAlpha by lazy {
        themeBackgroundAlpha
    }

    final override val currentThemeBackgroundOption by lazy {
        themeBackgroundOption
    }

    override val themeBackgroundAlpha: Int
        get() = themePreferences[themeBackgroundAlphaKey]


    override val themeBackgroundOption: String
        get() = themePreferences[themeBackgroundOptionKey]

    open val themeNavigationStyle: String
        get() = themePreferences[navbarStyleKey]

    private var isNightBackup: Int = TwilightManagerAccessor.UNSPECIFIED

    private val actionHelper = IBaseActivity.ActionHelper<BaseActivity>()

    private val themePreferences by lazy {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    // Registered listeners
    private val controlBarOffsetListeners = ArrayList<IControlBarActivity.ControlBarOffsetListener>()

    private val userTheme: Chameleon.Theme by lazy {
        return@lazy ThemeUtils.getUserTheme(this, themePreferences)
    }

    private val nightTimeChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_TICK, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    if (!isFinishing) {
                        updateNightMode()
                    }
                }
            }
        }
    }

    // Data fields
    protected var systemWindowsInsets: Rect? = null
        private set
    var keyMetaState: Int = 0
        private set

    override fun getSystemWindowInsets(caller: Fragment, insets: Rect): Boolean {
        return systemWindowsInsets?.let {
            insets.set(it)
            true
        } ?: false
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        if (systemWindowsInsets == null) {
            systemWindowsInsets = Rect(insets.systemWindowInsets.left,
                    insets.systemWindowInsets.top,
                    insets.systemWindowInsets.right,
                    insets.systemWindowInsets.bottom)
        } else {
            insets.getSystemWindowInsets(systemWindowsInsets!!)
        }
        notifyControlBarOffsetChanged()
        return insets
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
        val themeColor = themePreferences[themeColorKey]
        val themeResource = getThemeResource(themePreferences, themePreferences[themeKey], themeColor)
        if (themeResource != 0) {
            setTheme(themeResource)
        }
        onApplyNavigationStyle(themeNavigationStyle, themeColor)
        super.onCreate(savedInstanceState)
        title = activityLabel
        requestManager = Glide.with(this)
        ActivitySupport.setTaskDescription(this, TaskDescriptionCompat(title.toString(), null,
                ColorUtils.setAlphaComponent(overrideTheme.colorToolbar, 0xFF)))
        GeneralComponent.get(this).inject(this)
    }

    override fun onStart() {
        super.onStart()
        requestManager.onStart()
    }

    override fun onStop() {
        requestManager.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        requestManager.onDestroy()
        super.onDestroy()
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
                    intentFilter.addDataAuthority(authorityEntry.host, if (port < 0) null else port.toString())
                }
                try {
                    adapter.enableForegroundDispatch(this, intent, arrayOf(intentFilter), null)
                } catch (e: Exception) {
                    // Ignore if blocked by modified roms
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        registerReceiver(nightTimeChangedReceiver, filter)

        updateNightMode()
    }

    override fun onPause() {

        unregisterReceiverSafe(nightTimeChangedReceiver)

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

    override fun notifyControlBarOffsetChanged() {
        val offset = controlBarOffset
        for (l in controlBarOffsetListeners) {
            l.onControlBarOffsetChanged(this, offset)
        }
    }

    override fun registerControlBarOffsetListener(listener: IControlBarActivity.ControlBarOffsetListener) {
        controlBarOffsetListeners.add(listener)
    }

    override fun unregisterControlBarOffsetListener(listener: IControlBarActivity.ControlBarOffsetListener) {
        controlBarOffsetListeners.remove(listener)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        actionHelper.dispatchOnResumeFragments(this)
    }

    override fun attachBaseContext(newBase: Context) {
        val locale = newBase.defaultSharedPreferences[overrideLanguageKey] ?: Resources.getSystem()
                .firstLanguage
        if (locale == null) {
            super.attachBaseContext(newBase)
            return
        }
        val newContext = newBase.overriding(locale)
        super.attachBaseContext(newContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applyOverrideConfiguration(newContext.resources.configuration)
        }
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (BaseActivity) -> Unit): Promise<Unit, Exception> {
        return actionHelper.executeAfterFragmentResumed(this, useHandler, action)
    }


    protected open val shouldApplyWindowBackground: Boolean
        get() {
            return true
        }

    override fun onApplyThemeResource(theme: Resources.Theme, resId: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resId, first)
        if (window != null && shouldApplyWindowBackground) {
            ThemeUtils.applyWindowBackground(this, window, themeBackgroundOption,
                    themeBackgroundAlpha)
        }
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        // Fix for https://github.com/afollestad/app-theme-engine/issues/109
        if (context != this) {
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

    override fun onPreferenceDisplayDialog(fragment: PreferenceFragmentCompat, preference: Preference): Boolean {
        if (preference is IDialogPreference) {
            preference.displayDialog(fragment)
            return true
        }
        return false
    }

    override fun getOverrideTheme(): Chameleon.Theme {
        return userTheme
    }

    override fun onCreateAppearanceCreator(): Chameleon.AppearanceCreator? {
        return TwidereAppearanceCreator
    }

    @StyleRes
    protected open fun getThemeResource(preferences: SharedPreferences, theme: String, themeColor: Int): Int {
        return getCurrentThemeResource(this, theme)
    }

    private fun onApplyNavigationStyle(navbarStyle: String, themeColor: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || isDialogTheme) return
        when (navbarStyle) {
            NavbarStyle.TRANSPARENT -> {
                if (resources.getBoolean(R.bool.support_translucent_navigation)) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                }
            }
            NavbarStyle.COLORED -> {
                WindowSupport.setNavigationBarColor(window, themeColor)
            }
        }
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
        return try {
            val cls = findClass(name) ?: throw ClassNotFoundException(name)
            val constructor = cls.getConstructor(Context::class.java, AttributeSet::class.java)
            constructor.newInstance(context, attrs) as View
        } catch (e: InstantiationException) {
            null
        } catch (e: IllegalAccessException) {
            null
        } catch (e: InvocationTargetException) {
            null
        } catch (e: NoSuchMethodException) {
            null
        } catch (e: ClassNotFoundException) {
            null
        }

    }

    private fun updateNightMode() {
        val nightState = TwilightManagerAccessor.getNightState(this)
        if (isNightBackup != TwilightManagerAccessor.UNSPECIFIED && nightState != isNightBackup) {
            recreate()
            return
        }
        isNightBackup = nightState
    }

    companion object {

        private val sClassPrefixList = arrayOf("android.widget.", "android.view.", "android.webkit.")
    }
}

