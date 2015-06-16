/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.activity;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.WindowManager;

import com.soundcloud.android.crop.CropImageActivity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;

/**
 * Created by mariotaku on 15/6/16.
 */
public class ImageCropperActivity extends CropImageActivity implements IThemedActivity {

    // Data fields
    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    @ShapedImageView.ShapeStyle
    private int mProfileImageStyle;
    private String mCurrentThemeBackgroundOption;
    private String mCurrentThemeFontFamily;


    private TintedStatusFrameLayout mMainContent;
    private View mDoneCancelBar;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mMainContent = (TintedStatusFrameLayout) findViewById(R.id.main_content);
        mDoneCancelBar = findViewById(R.id.done_cancel_bar);
    }

    @Override
    public void onCreate(final Bundle icicle) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(icicle);
        final int themeColor = getThemeColor();
        final int themeResId = getCurrentThemeResourceId();
        final String backgroundOption = getCurrentThemeBackgroundOption();
        mMainContent.setDrawColor(true);
        mMainContent.setDrawShadow(false);
        mMainContent.setColor(themeColor);
        ViewSupport.setBackground(mDoneCancelBar, ThemeUtils.getActionBarBackground(this, themeResId,
                themeColor, backgroundOption, true));
        ViewCompat.setElevation(mDoneCancelBar, ThemeUtils.getSupportActionBarElevation(this));
        final View windowOverlay = findViewById(R.id.window_overlay);
        ViewSupport.setBackground(windowOverlay, ThemeUtils.getNormalWindowContentOverlay(this, themeResId));
    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(R.layout.activity_image_cropper);
    }


    @Override
    public void setTheme(final int resid) {
        super.setTheme(mCurrentThemeResource = getThemeResourceId());
        ThemeUtils.applyWindowBackground(this, getWindow(), mCurrentThemeResource,
                mCurrentThemeBackgroundOption, mCurrentThemeBackgroundAlpha);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getNoActionBarThemeResource(this);
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
    public String getThemeBackgroundOption() {
        return ThemeUtils.getThemeBackgroundOption(this);
    }

    @Override
    public String getThemeFontFamily() {
        return ThemeUtils.getThemeFontFamily(this);
    }

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
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resId, boolean first) {
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeFontFamily = getThemeFontFamily();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        super.onApplyThemeResource(theme, resId, first);
    }

}
