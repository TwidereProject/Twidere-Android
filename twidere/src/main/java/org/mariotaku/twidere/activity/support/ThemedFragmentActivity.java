/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ColorUtils;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;

import static org.mariotaku.twidere.util.Utils.restartActivity;

public abstract class ThemedFragmentActivity extends FragmentActivity implements Constants, IThemedActivity {

    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    @ShapeStyle
    private int mProfileImageStyle;

    @Override
    public Resources getDefaultResources() {
        return super.getResources();
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
    public int getCurrentThemeBackgroundAlpha() {
        return mCurrentThemeBackgroundAlpha;
    }

    @Override
    public int getCurrentThemeColor() {
        return mCurrentThemeColor;
    }

    @Override
    public String getThemeFontFamily() {
        return ThemeUtils.getThemeFontFamily(this);
    }

    @Override
    public void navigateUpFromSameTask() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public final void restart() {
        restartActivity(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (Utils.isDebugBuild()) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        setTheme();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(title);
        final int themeResId = getCurrentThemeResourceId();
        final int themeColor = getThemeColor(), contrastColor = ColorUtils.getContrastYIQ(themeColor, 192);
        if (!ThemeUtils.isDarkTheme(themeResId)) {
            builder.setSpan(new ForegroundColorSpan(contrastColor), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.onTitleChanged(title, color);
    }

    @Override
    public View onCreateView(String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        final View view = ThemeUtils.createView(name, context, attrs, mCurrentThemeColor);
        if (view instanceof ShapedImageView) {
            final ShapedImageView shapedImageView = (ShapedImageView) view;
            shapedImageView.setStyle(mProfileImageStyle);
        }
        if (view != null) return view;
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected boolean shouldSetWindowBackground() {
        return true;
    }

    private void setTheme() {
        mCurrentThemeResource = getThemeResourceId();
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        setTheme(mCurrentThemeResource);
        if (shouldSetWindowBackground() && ThemeUtils.isTransparentBackground(mCurrentThemeResource)) {
            getWindow().setBackgroundDrawable(ThemeUtils.getWindowBackground(this));
        }
    }
}
