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

package org.mariotaku.twidere.activity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TwidereActionMenuView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEStatusBarCustomizer;
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer;
import com.squareup.otto.Bus;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IAppCompatActivity;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.activity.iface.IExtendedActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import org.mariotaku.twidere.preference.iface.IDialogPreference;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.NotificationManagerWrapper;
import org.mariotaku.twidere.util.PermissionsManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.view.iface.IExtendedView.OnFitSystemWindowsListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.inject.Inject;

@SuppressLint("Registered")
public class BaseActivity extends ATEActivity implements Constants, IExtendedActivity,
        IThemedActivity, IAppCompatActivity, IControlBarActivity, OnFitSystemWindowsListener,
        SystemWindowsInsetsCallback, KeyboardShortcutCallback, OnPreferenceDisplayDialogCallback,
        ATEToolbarCustomizer, ATEStatusBarCustomizer {

    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.view.",
            "android.webkit."
    };
    // Utility classes
    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutsHandler;
    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    protected ReadStateManager mReadStateManager;
    @Inject
    protected Bus mBus;
    @Inject
    protected SharedPreferencesWrapper mPreferences;
    @Inject
    protected NotificationManagerWrapper mNotificationManager;
    @Inject
    protected MediaLoaderWrapper mMediaLoader;
    @Inject
    protected UserColorNameManager mUserColorNameManager;
    @Inject
    protected PermissionsManager mPermissionsManager;

    private ActionHelper mActionHelper = new ActionHelper(this);

    // Registered listeners
    private ArrayList<ControlBarOffsetListener> mControlBarOffsetListeners = new ArrayList<>();

    // Data fields
    private Rect mSystemWindowsInsets;
    private int mKeyMetaState;
    // Data fields
    private int mCurrentThemeBackgroundAlpha;
    private String mCurrentThemeBackgroundOption;

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        if (mSystemWindowsInsets == null) return false;
        insets.set(mSystemWindowsInsets);
        return true;
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        if (mSystemWindowsInsets == null)
            mSystemWindowsInsets = new Rect(insets);
        else {
            mSystemWindowsInsets.set(insets);
        }
        notifyControlBarOffsetChanged();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (KeyEvent.isModifierKey(keyCode)) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                mKeyMetaState |= KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mKeyMetaState &= ~KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (handleKeyboardShortcutSingle(mKeyboardShortcutsHandler, keyCode, event, mKeyMetaState))
            return true;
        return isKeyboardShortcutHandled(mKeyboardShortcutsHandler, keyCode, event, mKeyMetaState) || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (handleKeyboardShortcutRepeat(mKeyboardShortcutsHandler, keyCode, event.getRepeatCount(), event, mKeyMetaState))
            return true;
        return isKeyboardShortcutHandled(mKeyboardShortcutsHandler, keyCode, event, mKeyMetaState) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return false;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return false;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        super.onCreate(savedInstanceState);
        GeneralComponentHelper.build(this).inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            final Intent linkIntent = new Intent(this, WebLinkHandlerActivity.class);
            final PendingIntent intent = PendingIntent.getActivity(this, 0, linkIntent, 0);
            final IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            intentFilter.addDataScheme("http");
            intentFilter.addDataScheme("https");
            intentFilter.addDataAuthority("twitter.com", null);
            intentFilter.addDataAuthority("www.twitter.com", null);
            intentFilter.addDataAuthority("mobile.twitter.com", null);
            intentFilter.addDataAuthority("fanfou.com", null);
            try {
                adapter.enableForegroundDispatch(this, intent, new IntentFilter[]{intentFilter}, null);
            } catch (SecurityException e) {
                // Ignore if blocked by modified roms
            }
        }
    }

    @Override
    protected void onPause() {
        final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            try {
                adapter.disableForegroundDispatch(this);
            } catch (SecurityException e) {
                // Ignore if blocked by modified roms
            }
        }
        mActionHelper.dispatchOnPause();
        super.onPause();
    }

    @Override
    public void setControlBarOffset(float offset) {

    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {

    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible, ControlBarShowHideHelper.ControlBarAnimationListener listener) {

    }

    @Override
    public float getControlBarOffset() {
        return 0;
    }

    @Override
    public int getControlBarHeight() {
        return 0;
    }

    @Override
    public void notifyControlBarOffsetChanged() {
        final float offset = getControlBarOffset();
        for (final ControlBarOffsetListener l : mControlBarOffsetListeners) {
            l.onControlBarOffsetChanged(this, offset);
        }
    }

    @Override
    public void registerControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.add(listener);
    }

    @Override
    public void unregisterControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.remove(listener);
    }

    public int getKeyMetaState() {
        return mKeyMetaState;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mActionHelper.dispatchOnResumeFragments();
    }

    @Override
    public void executeAfterFragmentResumed(Action action) {
        mActionHelper.executeAfterFragmentResumed(action);
    }

    @Override
    public int getCurrentThemeBackgroundAlpha() {
        return mCurrentThemeBackgroundAlpha;
    }

    @Override
    public String getCurrentThemeBackgroundOption() {
        return mCurrentThemeBackgroundOption;
    }

    @Override
    public int getThemeBackgroundAlpha() {
        return ThemeUtils.getUserThemeBackgroundAlpha(this);
    }

    @Override
    public String getThemeBackgroundOption() {
        return ThemeUtils.getThemeBackgroundOption(this);
    }

    @Nullable
    @Override
    public String getATEKey() {
        return ThemeUtils.getATEKey(this);
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resId, boolean first) {
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        super.onApplyThemeResource(theme, resId, first);
        final Window window = getWindow();
        if (window != null && shouldApplyWindowBackground()) {
            ThemeUtils.applyWindowBackground(this, window, getThemeBackgroundOption(),
                    getThemeBackgroundAlpha());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ThemeUtils.fixNightMode(getResources(), newConfig);
        super.onConfigurationChanged(newConfig);
    }

    protected boolean shouldApplyWindowBackground() {
        return true;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        // Fix for https://github.com/afollestad/app-theme-engine/issues/109
        if (context != this) {
            final AppCompatDelegate delegate = getDelegate();
            View view = delegate.createView(parent, name, context, attrs);
            if (view == null) {
                view = newInstance(name, context, attrs);
            }
            if (view == null) {
                view = newInstance(name, context, attrs);
            }
            if (view != null) {
                return view;
            }
        }
        if (parent instanceof TwidereActionMenuView) {
            final Class<?> cls = findClass(name);
            if (cls != null && ActionMenuItemView.class.isAssignableFrom(cls)) {
                return ((TwidereActionMenuView) parent).createActionMenuView(context, attrs);
            }
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    private Class<?> findClass(String name) {
        Class<?> cls = null;
        try {
            cls = Class.forName(name);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        if (cls != null) return cls;
        for (String prefix : sClassPrefixList) {
            try {
                cls = Class.forName(prefix + name);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
            if (cls != null) return cls;
        }
        return null;
    }

    private View newInstance(String name, Context context, AttributeSet attrs) {
        try {
            final Class<?> cls = findClass(name);
            if (cls == null) throw new ClassNotFoundException(name);
            final Constructor<?> constructor = cls.getConstructor(Context.class, AttributeSet.class);
            return (View) constructor.newInstance(context, attrs);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat fragment, Preference preference) {
        if (preference instanceof IDialogPreference) {
            ((IDialogPreference) preference).displayDialog(fragment);
            return true;
        }
        return false;
    }

    @Override
    public int getStatusBarColor() {
        return ATE.USE_DEFAULT;
    }

    @Override
    public int getToolbarColor(@Nullable Toolbar toolbar) {
        return ATE.USE_DEFAULT;
    }

    @Override
    public int getLightStatusBarMode() {
        //noinspection WrongConstant
        return ThemeUtils.getLightStatusBarMode(Config.statusBarColor(this, getATEKey()));
    }

    @Override
    public int getLightToolbarMode(@Nullable Toolbar toolbar) {
        //noinspection WrongConstant
        return ThemeUtils.getLightToolbarMode(Config.toolbarColor(this, getATEKey(), toolbar));
    }
}
