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

import android.app.ActionBar;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.menu.TwidereMenuInflater;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.util.Utils.restartActivity;

public abstract class BaseSupportThemedActivity extends FragmentActivity implements Constants, IThemedActivity {

    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    private TwidereMenuInflater mMenuInflater;

    @Override
    public void finish() {
        super.finish();
        overrideCloseAnimationIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu, TwidereMenuInflater inflater) {
        return false;
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        return onCreateOptionsMenu(menu, getTwidereMenuInflater());
    }

    @Override
    public Resources getDefaultResources() {
        return super.getResources();
    }

    @Override
    public TwidereMenuInflater getTwidereMenuInflater() {
        if (mMenuInflater != null) return mMenuInflater;
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            return mMenuInflater = new TwidereMenuInflater(actionBar.getThemedContext());
        }
        return mMenuInflater = new TwidereMenuInflater(this);
    }

    @Override
    public final int getCurrentThemeResourceId() {
        return mCurrentThemeResource;
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
    public boolean isDarkDrawerEnabled() {
        return ThemeUtils.isDarkDrawerEnabled(this);
    }

    @Override
    public void navigateUpFromSameTask() {
        NavUtils.navigateUpFromSameTask(this);
        overrideCloseAnimationIfNeeded();
    }

    @Override
    public void overrideCloseAnimationIfNeeded() {
        if (shouldOverrideActivityAnimation()) {
            ThemeUtils.overrideActivityCloseAnimation(this);
        } else {
            ThemeUtils.overrideNormalActivityCloseAnimation(this);
        }
    }


    @Override
    public final void restart() {
        restartActivity(this);
    }

    @Override
    public boolean shouldOverrideActivityAnimation() {
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (Utils.isDebugBuild()) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        if (shouldOverrideActivityAnimation()) {
            ThemeUtils.overrideActivityOpenAnimation(this);
        }
        setTheme();
        super.onCreate(savedInstanceState);
        setActionBarBackground();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(title);
        super.onTitleChanged(title, color);
        final int themeResId = getCurrentThemeResourceId();
        final int themeColor = getThemeColor(), contrastColor = Utils.getContrastYIQ(themeColor, 192);
        if (ThemeUtils.isColoredActionBar(themeResId)) {
            builder.setSpan(new ForegroundColorSpan(contrastColor), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            final int titleColor = ThemeUtils.isLightActionBar(themeResId) ? Color.BLACK : Color.WHITE;
            builder.setSpan(new ForegroundColorSpan(titleColor), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected boolean shouldSetWindowBackground() {
        return true;
    }

    private final void setActionBarBackground() {
        ThemeUtils.applyActionBarBackground(getActionBar(), this, mCurrentThemeResource);
    }

    private final void setTheme() {
        mCurrentThemeResource = getThemeResourceId();
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        ThemeUtils.notifyStatusBarColorChanged(this, mCurrentThemeResource, mCurrentThemeColor,
                mCurrentThemeBackgroundAlpha);
        setTheme(mCurrentThemeResource);
        if (shouldSetWindowBackground() && ThemeUtils.isTransparentBackground(mCurrentThemeResource)) {
            getWindow().setBackgroundDrawable(ThemeUtils.getWindowBackground(this));
        }
    }
}
