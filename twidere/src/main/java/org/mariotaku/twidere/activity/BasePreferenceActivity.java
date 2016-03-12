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

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ActivityTracker;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;

import javax.inject.Inject;

public abstract class BasePreferenceActivity extends PreferenceActivity implements Constants,
        IThemedActivity, KeyboardShortcutsHandler.KeyboardShortcutCallback {

    // Data fields
    private int mCurrentThemeColor;
    private int mCurrentThemeBackgroundAlpha;
    @ShapeStyle
    private int mProfileImageStyle;
    private String mCurrentThemeBackgroundOption;
    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutsHandler;
    private String mCurrentThemeFontFamily;
    @Inject
    protected ActivityTracker mActivityTracker;
    private int mMetaState;

    @Override
    public String getCurrentThemeFontFamily() {
        return mCurrentThemeFontFamily;
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
    public int getCurrentThemeColor() {
        return mCurrentThemeColor;
    }

    @Override
    public int getThemeBackgroundAlpha() {
        return ThemeUtils.getUserThemeBackgroundAlpha(this);
    }

    @Override
    public String getThemeBackgroundOption() {
        return ThemeUtils.getThemeBackgroundOption(this);
    }

    @Override
    public String getThemeFontFamily() {
        return ThemeUtils.getThemeFontFamily(this);
    }

    @Override
    @ShapeStyle
    public int getCurrentProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final void restart() {
        Utils.restartActivity(this);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
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
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (KeyEvent.isModifierKey(keyCode)) {
            mMetaState &= ~KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
        }
        if (handleKeyboardShortcutSingle(mKeyboardShortcutsHandler, keyCode, event, mMetaState))
            return true;
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (KeyEvent.isModifierKey(keyCode)) {
            mMetaState |= KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
        }
        if (handleKeyboardShortcutRepeat(mKeyboardShortcutsHandler, keyCode, event.getRepeatCount(), event, mMetaState))
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resId, boolean first) {
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        mCurrentThemeFontFamily = getThemeFontFamily();
        super.onApplyThemeResource(theme, resId, first);

        if (shouldApplyWindowBackground()) {
            ThemeUtils.applyWindowBackground(this, getWindow(),
                    mCurrentThemeBackgroundOption, mCurrentThemeBackgroundAlpha);
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


}
