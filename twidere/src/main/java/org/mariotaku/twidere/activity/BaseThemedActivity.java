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
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.util.Utils.restartActivity;

public abstract class BaseThemedActivity extends Activity implements IThemedActivity {

    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    private String mCurrentThemeFontFamily;
    private Theme mTheme;

    @Override
    public Resources getDefaultResources() {
        return super.getResources();
    }

    @Override
    public final int getCurrentThemeResourceId() {
        return mCurrentThemeResource;
    }

    @Override
    public Theme getTheme() {
        if (mTheme == null) {
            mTheme = getResources().newTheme();
            mTheme.setTo(super.getTheme());
            final int getThemeResourceId = getThemeResourceId();
            if (getThemeResourceId != 0) {
                mTheme.applyStyle(getThemeResourceId, true);
            }
        }
        return mTheme;
    }

    @Override
    public int getThemeBackgroundAlpha() {
        return ThemeUtils.isTransparentBackground(this) ? ThemeUtils.getUserThemeBackgroundAlpha(this) : 0xff;
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
    public void navigateUpFromSameTask() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public final void restart() {
        restartActivity(this);
    }

    protected final boolean isThemeChanged() {
        return getThemeResourceId() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor
                || !CompareUtils.objectEquals(getThemeFontFamily(), mCurrentThemeFontFamily)
                || getThemeBackgroundAlpha() != mCurrentThemeBackgroundAlpha;
    }

    @Override
    public int getCurrentThemeBackgroundAlpha() {
        return mCurrentThemeBackgroundAlpha;
    }

    @Override
    public int getCurrentThemeColor() {
        return mCurrentThemeColor;
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
        } else {
            ThemeUtils.notifyStatusBarColorChanged(this, mCurrentThemeResource, mCurrentThemeColor,
                    mCurrentThemeBackgroundAlpha);
        }
    }

    private final void setActionBarBackground() {
        ThemeUtils.applyActionBarBackground(getActionBar(), this, mCurrentThemeResource);
    }

    private final void setTheme() {
        mCurrentThemeResource = getThemeResourceId();
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeFontFamily = getThemeFontFamily();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        ThemeUtils.notifyStatusBarColorChanged(this, mCurrentThemeResource, mCurrentThemeColor,
                mCurrentThemeBackgroundAlpha);
        setTheme(mCurrentThemeResource);
        if (ThemeUtils.isTransparentBackground(mCurrentThemeResource)) {
            getWindow().setBackgroundDrawable(ThemeUtils.getWindowBackground(this));
        }
    }
}
