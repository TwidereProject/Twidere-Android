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
import android.os.Bundle;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

public abstract class BaseThemedActivity extends Activity implements IThemedActivity {

    private int mCurrentThemeResource;
    private int mCurrentThemeColor;
    private int mCurrentThemeBackgroundAlpha;
    private String mCurrentThemeFontFamily;
    private String mCurrentThemeBackgroundOption;
    private int mProfileImageStyle;

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
    public int getCurrentProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final void restart() {
        Utils.restartActivity(this);
    }

    protected final boolean isThemeChanged() {
        return getThemeResourceId() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor
                || !CompareUtils.objectEquals(getThemeFontFamily(), mCurrentThemeFontFamily)
                || getThemeBackgroundAlpha() != mCurrentThemeBackgroundAlpha;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (Utils.isDebugBuild()) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }

        setTheme();
        super.onCreate(savedInstanceState);
        setActionBarBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isThemeChanged()) {
            restart();
        }
    }

    private void setActionBarBackground() {
    }

    private void setTheme() {
        mCurrentThemeResource = getThemeResourceId();
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeFontFamily = getThemeFontFamily();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        setTheme(mCurrentThemeResource);
        ThemeUtils.applyWindowBackground(this, getWindow(), mCurrentThemeResource, mCurrentThemeBackgroundOption, mCurrentThemeBackgroundAlpha);
    }
}
