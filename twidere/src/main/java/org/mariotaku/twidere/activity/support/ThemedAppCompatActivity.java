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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ThemedAppCompatDelegateFactory;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IAppCompatActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;

public abstract class ThemedAppCompatActivity extends AppCompatActivity implements Constants,
        IThemedActivity, IAppCompatActivity {

    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    @ShapeStyle
    private int mProfileImageStyle;
    private String mCurrentThemeBackgroundOption;
    private String mCurrentThemeFontFamily;

    private ThemedAppCompatDelegateFactory.ThemedAppCompatDelegate mDelegate;
    private Toolbar mToolbar;

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
    protected void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSupportActionModeStarted(android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        ThemeUtils.applySupportActionModeColor(mode, this, getCurrentThemeResourceId(),
                getCurrentThemeColor(), getThemeBackgroundOption(), true);
    }

    @Override
    public ThemedAppCompatDelegateFactory.ThemedAppCompatDelegate getDelegate() {
        if (mDelegate != null) return mDelegate;
        return mDelegate = ThemedAppCompatDelegateFactory.create(this, this);
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(mCurrentThemeResource = getThemeResourceId());
        if (shouldApplyWindowBackground()) {
            ThemeUtils.applyWindowBackground(this, getWindow(), mCurrentThemeResource, mCurrentThemeBackgroundOption,
                    mCurrentThemeBackgroundAlpha);
        }
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resid, boolean first) {
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        mCurrentThemeFontFamily = getThemeFontFamily();
        super.onApplyThemeResource(theme, resid, first);
    }

    @Override
    public void setSupportActionBar(Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        mToolbar = toolbar;
    }

    @Nullable
    public final Toolbar peekActionBarToolbar() {
        return mToolbar;
    }

    @Nullable
    public final Toolbar getActionBarToolbar() {
        if (mToolbar != null) return mToolbar;
        final View actionBarView = getWindow().findViewById(android.support.v7.appcompat.R.id.action_bar);
        if (actionBarView instanceof Toolbar) {
            return (Toolbar) actionBarView;
        }
        return null;
    }

    protected boolean shouldApplyWindowBackground() {
        return true;
    }

}
