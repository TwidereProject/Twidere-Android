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
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;
import org.mariotaku.twidere.view.TintedStatusFrameLayout;

public abstract class BasePreferenceActivity extends AppCompatPreferenceActivity implements Constants,
        IThemedActivity, KeyboardShortcutsHandler.KeyboardShortcutCallback {

    private TintedStatusFrameLayout mMainContent;

    private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;
    @ShapeStyle
    private int mProfileImageStyle;
    private String mCurrentThemeBackgroundOption;
    private KeyboardShortcutsHandler mKeyboardShortcutsHandler;
    private String mCurrentThemeFontFamily;

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
    public void onContentChanged() {
        super.onContentChanged();
        mMainContent = (TintedStatusFrameLayout) findViewById(R.id.main_content);
        setupTintStatusBar();
    }

    @Override
    public void onSupportActionModeStarted(android.support.v7.view.ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        ThemeUtils.applySupportActionModeColor(mode, this, getCurrentThemeResourceId(),
                getCurrentThemeColor(), getThemeBackgroundOption(), true);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event) {
        return false;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event) {
        return false;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (Utils.isDebugBuild()) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        setupWindow();
        super.onCreate(savedInstanceState);
        mKeyboardShortcutsHandler = TwidereApplication.getInstance(this).getKeyboardShortcutsHandler();
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (handleKeyboardShortcutSingle(mKeyboardShortcutsHandler, keyCode, event)) return true;
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (handleKeyboardShortcutRepeat(mKeyboardShortcutsHandler, keyCode, event.getRepeatCount(), event))
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        final FrameLayout mainContent = initMainContent();
        getLayoutInflater().inflate(layoutResID, mainContent, true);
        super.setContentView(mainContent);
    }

    @Override
    public void setContentView(View view) {
        final FrameLayout mainContent = initMainContent();
        mainContent.removeAllViews();
        mainContent.addView(view);
        super.setContentView(mainContent);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        final FrameLayout mainContent = initMainContent();
        mainContent.removeAllViews();
        mainContent.addView(view, params);
        super.setContentView(mainContent);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        FrameLayout mainContent = (FrameLayout) findViewById(R.id.main_content);
        if (mainContent == null) {
            @SuppressLint("InflateParams")
            final View mainLayout = getLayoutInflater().inflate(R.layout.activity_settings, null);
            mainContent = (FrameLayout) mainLayout.findViewById(R.id.main_content);
        }
        mainContent.addView(view, params);
        onContentChanged();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(title);
        final int themeResId = getCurrentThemeResourceId();
        final int themeColor = getThemeColor();
        final int contrastColor = ThemeUtils.getContrastActionBarTitleColor(this, themeResId, themeColor);
        if (!ThemeUtils.isDarkTheme(themeResId)) {
            builder.setSpan(new ForegroundColorSpan(contrastColor), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        super.onTitleChanged(title, color);
    }

    protected boolean isActionBarOutlineEnabled() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean result = super.onPrepareOptionsMenu(menu);
        if (!shouldSetActionItemColor()) return result;
        final View actionBarView = getWindow().findViewById(android.support.v7.appcompat.R.id.action_bar);
        if (actionBarView instanceof Toolbar) {
            final int themeColor = getCurrentThemeColor();
            final int themeId = getCurrentThemeResourceId();
            final int itemColor = ThemeUtils.getContrastActionBarItemColor(this, themeId, themeColor);
            final Toolbar toolbar = (Toolbar) actionBarView;
            ThemeUtils.setActionBarOverflowColor(toolbar, itemColor);
            final int popupColor = ThemeUtils.getThemeForegroundColor(toolbar.getContext(), toolbar.getPopupTheme());
            ThemeUtils.wrapToolbarMenuIcon(ViewSupport.findViewByType(actionBarView, ActionMenuView.class), itemColor, popupColor);
        }
        return result;
    }

    protected boolean shouldSetActionItemColor() {
        return true;
    }

    private FrameLayout initMainContent() {
        final FrameLayout mainContent = (FrameLayout) findViewById(R.id.main_content);
        if (mainContent != null) {
            return mainContent;
        }
        @SuppressLint("InflateParams")
        final View view = getLayoutInflater().inflate(R.layout.activity_settings, null);
        return (FrameLayout) view.findViewById(R.id.main_content);
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resid, boolean first) {
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mProfileImageStyle = Utils.getProfileImageStyle(this);
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        mCurrentThemeFontFamily = getThemeFontFamily();
        ThemeUtils.applyWindowBackground(this, getWindow(), mCurrentThemeResource, mCurrentThemeBackgroundOption, mCurrentThemeBackgroundAlpha);
        super.onApplyThemeResource(theme, resid, first);
    }

    @Override
    public void setTheme(int resid) {
        super.setTheme(mCurrentThemeResource = getThemeResourceId());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        final int themeColor = getCurrentThemeColor();
        final int themeId = getCurrentThemeResourceId();
        final String option = getThemeBackgroundOption();
        final int titleColor = ThemeUtils.getContrastActionBarTitleColor(this, themeId, themeColor);
        final int actionBarItemsColor = ThemeUtils.getContrastActionBarItemColor(this, themeId, themeColor);
        ThemeUtils.applyActionBarBackground(actionBar, this, themeId, themeColor, option, isActionBarOutlineEnabled());
        ThemeUtils.setActionBarColor(getWindow(), actionBar, titleColor, actionBarItemsColor);
    }

    private void setupTintStatusBar() {
        if (mMainContent == null) return;

        final int color = getCurrentThemeColor();
        final int alpha = ThemeUtils.isTransparentBackground(getThemeBackgroundOption()) ? getCurrentThemeBackgroundAlpha() : 0xFF;
        if (ThemeUtils.isDarkTheme(getCurrentThemeResourceId())) {
            mMainContent.setColor(getResources().getColor(R.color.background_color_action_bar_dark), alpha);
        } else {
            mMainContent.setColor(color, alpha);
        }

        mMainContent.setDrawShadow(false);
        mMainContent.setDrawColor(true);
        mMainContent.setFactor(1);
    }

    private void setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
//        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
//        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_MODE_OVERLAY);
    }

}
