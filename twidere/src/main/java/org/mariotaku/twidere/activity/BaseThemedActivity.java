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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ActivityTracker;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;
import org.mariotaku.twidere.view.ShapedImageView;

import javax.inject.Inject;

public abstract class BaseThemedActivity extends Activity implements IThemedActivity {

    private int mCurrentThemeResource;
    private int mCurrentThemeColor;
    private int mCurrentThemeBackgroundAlpha;
    private String mCurrentThemeFontFamily;
    private String mCurrentThemeBackgroundOption;
    private int mProfileImageStyle;
    @Inject
    protected ActivityTracker mActivityTracker;
    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutHandler;

    @Override
    protected void onStart() {
        super.onStart();
        mActivityTracker.dispatchStart(this);
    }

    @Override
    protected void onStop() {
        mActivityTracker.dispatchStop(this);
        super.onStop();
    }

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
    public final int getCurrentThemeResourceId() {
        return mCurrentThemeResource;
    }

    @Override
    public int getThemeBackgroundAlpha() {
        return ThemeUtils.getUserThemeBackgroundAlpha(this);
    }

    @Override
    public abstract int getThemeColor();

    @Override
    public String getThemeFontFamily() {
        return ThemeUtils.getThemeFontFamily(this);
    }

    @Override
    public abstract int getThemeResourceId();

    @Override
    @ShapedImageView.ShapeStyle
    public int getCurrentProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final void restart() {
        Utils.restartActivity(this);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        super.onCreate(savedInstanceState);
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(this)).build().inject(this);
        setActionBarBackground();
    }

    private void setActionBarBackground() {
    }

    @Override
    public void setTheme(int resId) {
        final int themeResourceId = getThemeResourceId();
        super.setTheme(mCurrentThemeResource = themeResourceId != 0 ? themeResourceId : resId);
        if (shouldApplyWindowBackground()) {
            ThemeUtils.applyWindowBackground(this, getWindow(), mCurrentThemeResource,
                    mCurrentThemeBackgroundOption, mCurrentThemeBackgroundAlpha);
        }
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resId, boolean first) {
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeFontFamily = getThemeFontFamily();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        super.onApplyThemeResource(theme, resId, first);
    }

    protected boolean shouldApplyWindowBackground() {
        return true;
    }

}
