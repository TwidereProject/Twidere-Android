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

package org.mariotaku.twidere.activity.support;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.ThemedAppCompatDelegate;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;

public abstract class ThemedAppCompatActivity extends AppCompatActivity implements Constants, IThemedActivity {

    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    @ShapeStyle
    private int mProfileImageStyle;
    private String mCurrentThemeBackgroundOption;

    private AppCompatDelegate mDelegate;

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
    public String getThemeBackgroundOption() {
        return ThemeUtils.getThemeBackgroundOption(this);
    }

    @Override
    public String getThemeFontFamily() {
        return ThemeUtils.getThemeFontFamily(this);
    }

    @Override
    public int getCurrentProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final void restart() {
        Utils.restartActivity(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (Utils.isDebugBuild()) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        setupTheme();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSupportActionModeStarted(android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        ThemeUtils.applySupportActionModeColor(mode, this, getCurrentThemeResourceId(),
                getCurrentThemeColor(), getThemeBackgroundOption(), true);
    }

    @Override
    public AppCompatDelegate getDelegate() {
        if (mDelegate != null) return mDelegate;
        return mDelegate = ThemedAppCompatDelegate.create(this, this);
    }

    private void setupTheme() {
        mCurrentThemeResource = getThemeResourceId();
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        setTheme(mCurrentThemeResource);
        ThemeUtils.applyWindowBackground(this, getWindow(), mCurrentThemeResource,
                mCurrentThemeBackgroundOption, mCurrentThemeBackgroundAlpha);
    }

}
