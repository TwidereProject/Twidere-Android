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

package org.mariotaku.twidere.util;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.internal.app.WindowDecorActionBar;
import android.support.v7.internal.app.WindowDecorActionBar.ActionModeImpl;
import android.support.v7.internal.view.SupportActionModeWrapper;
import android.support.v7.internal.view.SupportActionModeWrapperTrojan;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.support.v7.internal.widget.ContentFrameLayout;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.graphic.ActionBarColorDrawable;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.text.ParagraphSpacingSpan;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.view.TabPagerIndicator;
import org.mariotaku.twidere.view.iface.IThemedView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ThemeUtils implements Constants {

    private static final int[] ANIM_OPEN_STYLE_ATTRS = {android.R.attr.activityOpenEnterAnimation,
            android.R.attr.activityOpenExitAnimation};
    private static final int[] ANIM_CLOSE_STYLE_ATTRS = {android.R.attr.activityCloseEnterAnimation,
            android.R.attr.activityCloseExitAnimation};

    private static final String[] sClassPrefixList = {"android.widget.", "android.webkit.", "org.mariotaku.twidere.view"};

    private ThemeUtils() {
        throw new AssertionError();
    }


    public static void applyActionBarBackground(final ActionBar actionBar, final Context context,
                                                final int themeRes, final int accentColor,
                                                final String backgroundOption, boolean outlineEnabled) {
        if (actionBar == null || context == null) return;
        actionBar.setBackgroundDrawable(getActionBarBackground(context, themeRes, accentColor, backgroundOption, outlineEnabled));
        actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, themeRes));
        actionBar.setStackedBackgroundDrawable(getActionBarBackground(context, themeRes, accentColor, backgroundOption, outlineEnabled));
    }


    public static void applyActionBarBackground(final android.support.v7.app.ActionBar actionBar, final Context context,
                                                final int themeRes, final int accentColor, String backgroundOption, boolean outlineEnabled) {
        if (actionBar == null || context == null) return;
        actionBar.setBackgroundDrawable(getActionBarBackground(context, themeRes, accentColor, backgroundOption, outlineEnabled));
        actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, themeRes));
        actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, themeRes, accentColor, outlineEnabled));
    }


    public static void applyBackground(final View view) {
        if (view == null) return;
        applyBackground(view, getUserAccentColor(view.getContext()));
    }

    public static void applyBackground(final View view, final int color) {
        if (view == null) return;
        try {
            final Drawable bg = view.getBackground();
            if (bg == null) return;
            final Drawable mutated = bg.mutate();
            if (mutated == null) return;
            mutated.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            view.invalidate();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyColorFilterToMenuIcon(Activity activity, Menu menu) {
        final ActionBar actionBar = activity.getActionBar();
        final Context context = actionBar != null ? actionBar.getThemedContext() : activity;
        final int color = getThemeForegroundColor(context);
        final int popupTheme = getActionBarPopupThemeRes(context);
        final int popupColor = getThemeForegroundColor(context, popupTheme);
        final int highlightColor = getUserAccentColor(activity);
        applyColorFilterToMenuIcon(menu, color, popupColor, highlightColor, Mode.SRC_ATOP);
    }

    public static void applyColorFilterToMenuIcon(final Menu menu, final int color,
                                                  final int highlightColor, final Mode mode,
                                                  final int... excludedGroups) {
        applyColorFilterToMenuIcon(menu, color, color, highlightColor, mode, excludedGroups);
    }

    public static void applyColorFilterToMenuIcon(final Menu menu, final int color, final int popupColor,
                                                  final int highlightColor, final Mode mode,
                                                  final int... excludedGroups) {
        for (int i = 0, j = menu.size(); i < j; i++) {
            final MenuItem item = menu.getItem(i);
            final Drawable icon = item.getIcon();
            final ContextMenuInfo info = item.getMenuInfo();
            if (icon != null && !ArrayUtils.contains(excludedGroups, item.getGroupId())) {
                icon.mutate();
                if (info instanceof TwidereMenuInfo) {
                    final TwidereMenuInfo sInfo = (TwidereMenuInfo) info;
                    icon.setColorFilter(sInfo.isHighlight() ?
                            sInfo.getHighlightColor(highlightColor) : color, mode);
                } else {
                    icon.setColorFilter(color, mode);
                }
            }
            if (item.hasSubMenu()) {
                // SubMenu item is always in popup
                applyColorFilterToMenuIcon(item.getSubMenu(), popupColor, popupColor, highlightColor, mode, excludedGroups);
            }
        }
    }

    public static void applyParagraphSpacing(TextView textView, float multiplier) {
        final SpannableStringBuilder builder = SpannableStringBuilder.valueOf(textView.getText());
        int prevLineBreak, currLineBreak = 0;
        for (int i = 0, j = builder.length(); i < j; i++) {
            if (builder.charAt(i) == '\n') {
                prevLineBreak = currLineBreak;
                currLineBreak = i;
                if (currLineBreak > 0) {
                    builder.setSpan(new ParagraphSpacingSpan(multiplier), prevLineBreak, currLineBreak,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
        textView.setText(builder);
    }

    public static void applySupportActionModeColor(ActionMode mode, FragmentActivity activity,
                                                   int themeRes, int accentColor,
                                                   String backgroundOption, boolean outlineEnabled) {
        // Very dirty implementation
        if (!(mode instanceof SupportActionModeWrapper) || !(activity instanceof IThemedActivity))
            return;
        final android.support.v7.view.ActionMode modeCompat = SupportActionModeWrapperTrojan.getWrappedObject((SupportActionModeWrapper) mode);
        applySupportActionModeColor(modeCompat, activity, themeRes, accentColor, backgroundOption, outlineEnabled);
    }

    public static void applySupportActionModeColor(android.support.v7.view.ActionMode modeCompat,
                                                   FragmentActivity activity, int themeRes,
                                                   int accentColor, String backgroundOption,
                                                   boolean outlineEnabled) {
        // Very dirty implementation
        if (!(modeCompat instanceof ActionModeImpl)) return;
        try {
            WindowDecorActionBar actionBar = null;
            final Field[] fields = ActionModeImpl.class.getDeclaredFields();
            for (Field field : fields) {
                if (WindowDecorActionBar.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    actionBar = (WindowDecorActionBar) field.get(modeCompat);
                    break;
                }
            }
            if (actionBar == null) return;
            final Context context = actionBar.getThemedContext();
            final Field contextViewField = WindowDecorActionBar.class.getDeclaredField("mContextView");
            contextViewField.setAccessible(true);
            final View contextView = (View) contextViewField.get(actionBar);
            if (!(contextView instanceof ActionBarContextView)) return;
            final ActionBarContextView actionBarContextView = (ActionBarContextView) contextView;
            final TextView actionBarTitleView = (TextView) contextView.findViewById(android.support.v7.appcompat.R.id.action_bar_title);
            final TextView actionBarSubtitleView = (TextView) contextView.findViewById(android.support.v7.appcompat.R.id.action_bar_subtitle);
            final ImageView actionModeCloseButton = (ImageView) contextView.findViewById(android.support.v7.appcompat.R.id.action_mode_close_button);
            final ActionMenuView menuView = ViewUtils.findViewByType(contextView, ActionMenuView.class);
            if (actionBarTitleView == null || actionBarSubtitleView == null || actionModeCloseButton == null || menuView == null)
                return;
            final int actionBarColor;
            if (isDarkTheme(themeRes)) {
                actionBarColor = context.getResources().getColor(R.color.background_color_action_bar_dark);
            } else {
                actionBarColor = accentColor;
            }
            final int titleColor = getContrastActionBarTitleColor(context, themeRes, actionBarColor);
            final int itemColor = getContrastActionBarItemColor(context, themeRes, actionBarColor);
            actionBarTitleView.setTextColor(titleColor);
            actionBarSubtitleView.setTextColor(titleColor);
            actionModeCloseButton.setColorFilter(itemColor, Mode.SRC_ATOP);
            setActionBarOverflowColor(menuView, itemColor);
            ThemeUtils.wrapToolbarMenuIcon(menuView, itemColor, itemColor);
            ViewUtils.setBackground(contextView, getActionBarBackground(activity, themeRes, accentColor, backgroundOption, outlineEnabled));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyWindowBackground(Context context, Window window, int theme, String option, int alpha) {
        if (isWindowFloating(context, theme)) return;
        if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(option)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
            window.setBackgroundDrawable(ThemeUtils.getWindowBackgroundApplyAlpha(context, alpha));
        } else if (VALUE_THEME_BACKGROUND_SOLID.equals(option)) {
            window.setBackgroundDrawable(new ColorDrawable(isDarkTheme(theme) ? Color.BLACK : Color.WHITE));
        }
    }

    public static void applyWindowBackground(Context context, View window, int theme, String option, int alpha) {
        if (isWindowFloating(context, theme)) return;
        if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(option)) {
            ViewUtils.setBackground(window, ThemeUtils.getWindowBackgroundApplyAlpha(context, alpha));
        } else if (VALUE_THEME_BACKGROUND_SOLID.equals(option)) {
            ViewUtils.setBackground(window, new ColorDrawable(isDarkTheme(theme) ? Color.BLACK : Color.WHITE));
        } else {
            ViewUtils.setBackground(window, ThemeUtils.getWindowBackground(context));
        }
    }

    public static View createView(final String name, final Context context,
                                  final AttributeSet attrs) {
        return createView(name, context, attrs, 0);
    }

    public static View createView(final String name, final Context context,
                                  final AttributeSet attrs, final int tintColor) {
        View view = null;
        try {
            view = newViewInstance(name, context, attrs);
        } catch (final Exception e) {
            // In this case we want to let the base class take a crack
            // at it.
        }
        for (final String prefix : sClassPrefixList) {
            try {
                view = newViewInstance(prefix + name, context, attrs);
            } catch (final Exception e) {
                // In this case we want to let the base class take a crack
                // at it.
            }
        }
        if (view != null) {
            applyColorTintForView(view, tintColor);
        }
        return view;
    }

    @Deprecated
    public static Drawable getActionBarBackground(final Context context, final int themeRes) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray array = context.obtainStyledAttributes(null, new int[]{android.R.attr.background},
                android.R.attr.actionBarStyle, themeRes);
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }

    @NonNull
    public static Drawable getActionBarBackground(final Context context, final int themeRes,
                                                  final int accentColor, final String backgroundOption,
                                                  final boolean outlineEnabled) {
        final int actionBarColor;
        if (isDarkTheme(themeRes)) {
            actionBarColor = context.getResources().getColor(R.color.background_color_action_bar_dark);
        } else {
            actionBarColor = accentColor;
        }
        return ActionBarColorDrawable.create(actionBarColor, outlineEnabled);
    }

    public static Context getActionBarContext(final Context context) {
        @SuppressLint("InlinedApi")
        final TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.actionBarTheme,
                R.attr.actionBarWidgetTheme, android.R.attr.actionBarTheme,
                android.R.attr.actionBarWidgetTheme});
        final int resId;
        if (a.hasValue(0) || a.hasValue(1)) {
            resId = a.hasValue(0) ? a.getResourceId(0, 0) : a.getResourceId(1, 0);
        } else {
            resId = a.hasValue(2) ? a.getResourceId(2, 0) : a.getResourceId(3, 0);
        }
        a.recycle();
        if (resId == 0) return context;
        return new ContextThemeWrapper(context, resId);
    }

    public static float getActionBarElevation(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
        @SuppressLint("InlinedApi")
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.elevation},
                android.R.attr.actionBarStyle, 0);
        try {
            return a.getDimension(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static Drawable getActionBarHomeAsUpIndicator(android.support.v7.app.ActionBar actionBar) {
        final Context context = actionBar.getThemedContext();
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        try {
            return a.getDrawable(R.styleable.ActionBar_homeAsUpIndicator);
        } finally {
            a.recycle();
        }
    }

    public static int getActionBarPopupThemeRes(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
        @SuppressLint("InlinedApi")
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.actionBarPopupTheme});
        try {
            return a.getResourceId(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static Drawable getActionBarSplitBackground(final Context context, final int themeRes) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.backgroundSplit},
                android.R.attr.actionBarStyle, themeRes);
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }

    @NonNull
    public static Drawable getActionBarStackedBackground(final Context context, final int themeRes,
                                                         final int accentColor, boolean outlineEnabled) {
        final int actionBarColor;
        if (isDarkTheme(themeRes)) {
            actionBarColor = context.getResources().getColor(R.color.background_color_action_bar_dark);
        } else {
            actionBarColor = accentColor;
        }
        return ActionBarColorDrawable.create(actionBarColor, outlineEnabled);
    }

    public static int getCardBackgroundColor(final Context context, String backgroundOption, int themeAlpha) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.cardItemBackgroundColor});
        final int color = a.getColor(0, Color.TRANSPARENT);
        a.recycle();
        if (isTransparentBackground(backgroundOption)) {
            return themeAlpha << 24 | (0x00FFFFFF & color);
        } else if (isSolidBackground(backgroundOption)) {
            return ColorUtils.getContrastYIQ(color, Color.WHITE, Color.BLACK);
        } else {
            return color;
        }
    }

    public static Drawable getCompatToolbarOverlay(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return null;
        final Window window = activity.getWindow();
        final View view = window.findViewById(android.support.v7.appcompat.R.id.decor_content_parent);
        if (!(view instanceof ActionBarOverlayLayout)) {
            final View contentLayout = window.findViewById(android.support.v7.appcompat.R.id.action_bar_activity_content);
            if (contentLayout instanceof ContentFrameLayout) {
                return ((ContentFrameLayout) contentLayout).getForeground();
            }
            return null;
        }
        try {
            final Field field = ActionBarOverlayLayout.class.getDeclaredField("mWindowContentOverlay");
            field.setAccessible(true);
            return (Drawable) field.get(view);
        } catch (Exception ignore) {
        }
        return null;
    }

    public static int getComposeThemeResource(final Context context) {
        return getComposeThemeResource(getThemeNameOption(context));
    }

    public static int getComposeThemeResource(final String name) {
        if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Compose;
        return R.style.Theme_Twidere_Light_Compose;
    }

    public static int getContrastActionBarItemColor(Context context, int theme, int color) {
        if (isDarkTheme(theme) || ColorUtils.getYIQLuminance(color) < 192) {
            //return light text color
            return Color.WHITE;
        }
        //return dark text color
        return Color.BLACK;
    }

    public static int getContrastActionBarTitleColor(Context context, int theme, int color) {
        if (isDarkTheme(theme) || ColorUtils.getYIQLuminance(color) < 192) {
            //return light text color
            return Color.WHITE;
        }
        //return dark text color
        return Color.BLACK;
    }

    public static int getDialogThemeResource(final Context context) {
        return getDialogThemeResource(getThemeNameOption(context));
    }

    public static int getDialogThemeResource(final String name) {
        if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Dialog;
        return R.style.Theme_Twidere_Light_Dialog;
    }

    public static Context getDialogThemedContext(final Context context) {
        return new ContextThemeWrapper(context, getDialogThemeResource(context));
    }

    public static int getDialogWhenLargeThemeResource(final Context context) {
        return getDialogWhenLargeThemeResource(getThemeNameOption(context));
    }

    public static int getDialogWhenLargeThemeResource(final String name) {
        if (VALUE_THEME_NAME_DARK.equals(name)) {
            return R.style.Theme_Twidere_Dark_DialogWhenLarge;
        }
        return R.style.Theme_Twidere_Light_DialogWhenLarge;
    }

    public static int getDrawerThemeResource(final Context context) {
        return getDrawerThemeResource(getThemeResource(context));
    }

    public static int getDrawerThemeResource(final int themeRes) {
        return R.style.Theme_Twidere_Drawer_Dark;
    }

    public static Drawable getImageHighlightDrawable(final Context context) {
        final Drawable d = getSelectableItemBackgroundDrawable(context);
        if (d != null) {
            d.setAlpha(0x80);
        }
        return d;
    }

    public static int getNoDisplayThemeResource(final Context context) {
        if (context == null) return R.style.Theme_Twidere_Dark_NoDisplay;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final String theme = pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE);
        if (VALUE_THEME_NAME_DARK.equals(theme)) return R.style.Theme_Twidere_Dark_NoDisplay;
        return R.style.Theme_Twidere_Light_NoDisplay;
    }

    public static int getOptimalLinkColor(int linkColor, int color) {
        final int[] yiq = new int[3];
        ColorUtils.colorToYIQ(color, yiq);
        final int y = yiq[0];
        ColorUtils.colorToYIQ(linkColor, yiq);
        if (y < 32 && yiq[0] < 192) {
            return linkColor;
        } else if (y > 192 && yiq[0] > 32) {
            return linkColor;
        }
        yiq[0] = yiq[0] + (y - yiq[0]) / 2;
        return ColorUtils.YIQToColor(Color.alpha(linkColor), yiq);
    }

    public static int getQuickSearchBarThemeResource(final Context context) {
        return getQuickSearchBarThemeResource(getThemeNameOption(context));
    }

    public static int getQuickSearchBarThemeResource(final String name) {
        if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_QuickSearchBar;
        return R.style.Theme_Twidere_Light_QuickSearchBar;
    }

    public static Resources getResources(final Context context) {
        return context.getResources();
    }

    public static Drawable getSelectableItemBackgroundDrawable(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }

    public static int getSettingsThemeResource(final Context context) {
        return getSettingsThemeResource(getThemeNameOption(context));
    }

    public static int getSettingsThemeResource(final String name) {
        if (VALUE_THEME_NAME_TWIDERE.equals(name) || VALUE_THEME_NAME_LIGHT.equals(name))
            return R.style.Theme_Twidere_Settings_Light_DarkActionBar;
        else if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Settings_Dark;
        return R.style.Theme_Twidere_Settings_Light_DarkActionBar;
    }

    public static Drawable getSupportActionBarBackground(final Context context, final int themeRes) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray array = context.obtainStyledAttributes(null, new int[]{android.R.attr.background},
                R.attr.actionBarStyle, themeRes);
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }

    public static float getSupportActionBarElevation(final Context context) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{R.attr.elevation}, R.attr.actionBarStyle, 0);
        try {
            return a.getDimension(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static int getTextAppearanceLarge(final Context context) {
        // final Context wrapped = getThemedContext(context,
        // getResources(context));
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textAppearanceLarge});
        final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo_Large);
        a.recycle();
        return textAppearance;
    }

    public static int getTextColorPrimary(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        try {
            return a.getColor(0, Color.TRANSPARENT);
        } finally {
            a.recycle();
        }
    }

    public static int getTextColorSecondary(final Context context) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary});
        try {
            return a.getColor(0, Color.TRANSPARENT);
        } finally {
            a.recycle();
        }
    }

    public static int getThemeBackgroundColor(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.colorBackground});
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static int getThemeBackgroundColor(final Context context, int themeRes) {
        if (themeRes == 0) {
            return getThemeBackgroundColor(context);
        }
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.colorBackground},
                0, themeRes);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static String getThemeBackgroundOption(final Context context) {
        if (context == null) return VALUE_THEME_BACKGROUND_DEFAULT;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getString(KEY_THEME_BACKGROUND, VALUE_THEME_BACKGROUND_DEFAULT);
    }

    public static int getThemeColor(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.colorActivatedHighlight});
        try {
            final Resources resources = context.getResources();
            return a.getColor(0, resources.getColor(R.color.material_light_blue));
        } finally {
            a.recycle();
        }
    }

    public static int getThemeColor(Context context, int themeResourceId) {
        final Context appContext = context.getApplicationContext();
        final Resources res = appContext.getResources();
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = appContext.obtainStyledAttributes(null,
                new int[]{android.R.attr.colorActivatedHighlight}, 0, themeResourceId);
        try {
            return a.getColor(0, res.getColor(R.color.material_light_blue));
        } finally {
            a.recycle();
        }
    }

    public static String getThemeFontFamily(final Context context) {
        if (context == null) return VALUE_THEME_FONT_FAMILY_REGULAR;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final String fontFamily = pref.getString(KEY_THEME_FONT_FAMILY, VALUE_THEME_FONT_FAMILY_REGULAR);
        if (!TextUtils.isEmpty(fontFamily)) return fontFamily;
        return VALUE_THEME_FONT_FAMILY_REGULAR;
    }

    public static int getThemeForegroundColor(final Context context) {
        return getThemeForegroundColor(context, 0);
    }

    public static int getThemeForegroundColor(final Context context, int theme) {
        final Context wrapped = theme != 0 ? new ContextThemeWrapper(context, theme) : context;
        final TypedArray a = wrapped.obtainStyledAttributes(new int[]{android.R.attr.colorForeground});
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static String getThemeNameOption(final Context context) {
        if (context == null) return VALUE_THEME_NAME_TWIDERE;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE);
    }

    public static int getThemeResource(final Context context) {
        return getThemeResource(getThemeNameOption(context));
    }

    public static int getThemeResource(final String name) {
        if (VALUE_THEME_NAME_DARK.equals(name)) {
            return R.style.Theme_Twidere_Dark;
        }
        return R.style.Theme_Twidere_Light;
    }

    public static Context getThemedContext(final Context context) {
        return context;
    }

    public static Context getThemedContext(final Context context, final Resources res) {
        return context;
    }

    public static int getTitleTextAppearance(final Context context) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.titleTextStyle},
                android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
        final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo);
        a.recycle();
        return textAppearance;
    }

    public static int getUserAccentColor(final Context context) {
        if (context == null) return Color.TRANSPARENT;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final Resources res = context.getResources();
        final int def = res.getColor(R.color.branding_color);
        return pref.getInt(KEY_THEME_COLOR, def);
    }

    public static int getUserHighlightColor(final Context context) {
        final int color = getUserLinkTextColor(context);
        final int red = Color.red(color), green = Color.green(color), blue = Color.blue(color);
        return Color.argb(0x66, red, green, blue);
    }

    public static int getUserLinkTextColor(final Context context) {
        if (context == null) return new TextPaint().linkColor;
        final int themeColor = getUserAccentColor(context);
        final float[] hsv = new float[3];
        Color.colorToHSV(themeColor, hsv);
        if (isDarkTheme(context)) {
            hsv[2] = MathUtils.clamp(hsv[2], 1, 0.5f);
        } else {
            hsv[2] = MathUtils.clamp(hsv[2], 0.1f, 0.75f);
        }
        return Color.HSVToColor(hsv);
    }

    public static int getUserThemeBackgroundAlpha(final Context context) {
        if (context == null) return DEFAULT_THEME_BACKGROUND_ALPHA;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA);
    }

    public static Typeface getUserTypeface(final Context context, final Typeface defTypeface) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return Typeface.DEFAULT;
        final int fontStyle = defTypeface != null ? defTypeface.getStyle() : Typeface.NORMAL;
        final String fontFamily = getThemeFontFamily(context);
        final Typeface tf = Typeface.create(fontFamily, fontStyle);
        if (tf != null) return tf;
        return Typeface.create(Typeface.DEFAULT, fontStyle);
    }

    public static int getViewerThemeResource(final Context context) {
        return R.style.Theme_Twidere_Viewer;
    }

    public static Drawable getWindowBackground(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }

    public static Drawable getWindowBackgroundApplyAlpha(final Context context, final int alpha) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        final Drawable d = a.getDrawable(0);
        a.recycle();
        if (d != null) {
            d.setAlpha(alpha);
        }
        return d;
    }

    public static Drawable getWindowBackgroundFromTheme(final Context context, int theme) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.windowBackground}, 0, theme);
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }

    public static Drawable getWindowContentOverlay(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowContentOverlay});
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }

    public static void initPagerIndicatorAsActionBarTab(FragmentActivity activity, TabPagerIndicator indicator) {
        final float supportActionBarElevation = getSupportActionBarElevation(activity);
        ViewCompat.setElevation(indicator, supportActionBarElevation);
        if (!(activity instanceof IThemedActivity)) return;
        final int themeRes = ((IThemedActivity) activity).getCurrentThemeResourceId();
        final int themeColor = ((IThemedActivity) activity).getCurrentThemeColor();
        final int contrastColor = ColorUtils.getContrastYIQ(themeColor, 192);
        ViewUtils.setBackground(indicator, getActionBarStackedBackground(activity, themeRes, themeColor, true));
        if (isDarkTheme(themeRes)) {
            final int foregroundColor = getThemeForegroundColor(activity);
            indicator.setIconColor(foregroundColor);
            indicator.setLabelColor(foregroundColor);
            indicator.setStripColor(themeColor);
        } else {
            indicator.setIconColor(contrastColor);
            indicator.setLabelColor(contrastColor);
            indicator.setStripColor(contrastColor);
        }
        indicator.updateAppearance();
    }

    public static void initTextView(TextView view) {
        if (view.isInEditMode()) return;
        final Context context = view.getContext();
//        view.setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
//        view.setHighlightColor(ThemeUtils.getUserHighlightColor(context));
        view.setTypeface(ThemeUtils.getUserTypeface(context, view.getTypeface()));
    }

    public static boolean isColoredActionBar(int themeRes) {
        return !isDarkTheme(themeRes);
    }

    public static boolean isDarkTheme(final Context context) {
        return isDarkTheme(getThemeResource(context));
    }

    public static boolean isDarkTheme(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Dark:
            case R.style.Theme_Twidere_Dark_Dialog:
            case R.style.Theme_Twidere_Dark_DialogWhenLarge:
            case R.style.Theme_Twidere_Dark_Compose:
                return true;
        }
        return false;
    }

    public static boolean isTransparentBackground(final Context context) {
        return isTransparentBackground(getThemeBackgroundOption(context));
    }

    public static boolean isTransparentBackground(final String option) {
        return VALUE_THEME_BACKGROUND_TRANSPARENT.equals(option);
    }

    public static boolean isSolidBackground(final String option) {
        return VALUE_THEME_BACKGROUND_SOLID.equals(option);
    }

    public static boolean isWindowFloating(Context context, int theme) {
        final TypedArray a;
        if (theme != 0) {
            //noinspection ConstantConditions
            a = context.obtainStyledAttributes(null, new int[]{android.R.attr.windowIsFloating}, 0, theme);
        } else {
            a = context.obtainStyledAttributes(new int[]{android.R.attr.windowIsFloating});
        }
        try {
            return a.getBoolean(0, false);
        } finally {
            a.recycle();
        }
    }

    public static void overrideActivityCloseAnimation(final Activity activity) {
        TypedArray a = activity.obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        final int windowAnimationStyleResId = a.getResourceId(0, 0);
        a.recycle();
        // Now retrieve the resource ids of the actual animations used in the
        // animation style pointed to by
        // the window animation resource id.
        a = activity.obtainStyledAttributes(windowAnimationStyleResId, ANIM_CLOSE_STYLE_ATTRS);
        final int activityCloseEnterAnimation = a.getResourceId(0, 0);
        final int activityCloseExitAnimation = a.getResourceId(1, 0);
        a.recycle();
        activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }

    public static void overrideActivityOpenAnimation(final Activity activity) {

        TypedArray a = activity.obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        final int windowAnimationStyleResId = a.getResourceId(0, 0);
        a.recycle();
        // Now retrieve the resource ids of the actual animations used in the
        // animation style pointed to by
        // the window animation resource id.
        a = activity.obtainStyledAttributes(windowAnimationStyleResId, ANIM_OPEN_STYLE_ATTRS);
        final int activityOpenEnterAnimation = a.getResourceId(0, 0);
        final int activityOpenExitAnimation = a.getResourceId(1, 0);
        a.recycle();
        activity.overridePendingTransition(activityOpenEnterAnimation, activityOpenExitAnimation);
    }

    public static void overrideNormalActivityCloseAnimation(final Activity activity) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = activity.obtainStyledAttributes(null, ANIM_CLOSE_STYLE_ATTRS,
                0, android.R.style.Animation_Activity);
        final int activityCloseEnterAnimation = a.getResourceId(0, 0);
        final int activityCloseExitAnimation = a.getResourceId(1, 0);
        a.recycle();
        activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }

    public static void resetCheatSheet(ActionMenuView menuView) {
        final OnLongClickListener listener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (((ActionMenuItemView) v).hasText()) return false;
                final MenuItem menuItem = ((ActionMenuItemView) v).getItemData();
                Utils.showMenuItemToast(v, menuItem.getTitle(), true);
                return true;
            }
        };
        for (int i = 0, j = menuView.getChildCount(); i < j; i++) {
            final View child = menuView.getChildAt(i);
            if (!(child instanceof ActionMenuItemView)) continue;
            final ActionMenuItemView itemView = (ActionMenuItemView) child;
            final MenuItem menuItem = itemView.getItemData();
            if (menuItem.hasSubMenu()) continue;
            itemView.setOnLongClickListener(listener);
        }
    }

    public static void setActionBarItemsColor(Window window, android.support.v7.app.ActionBar actionBar, int itemColor) {
        final Drawable drawable = getActionBarHomeAsUpIndicator(actionBar);
        if (drawable != null) {
            drawable.setColorFilter(itemColor, Mode.SRC_ATOP);
        }
        actionBar.setHomeAsUpIndicator(drawable);
        setActionBarTitleTextColor(window, itemColor);
    }

    public static void setActionBarOverflowColor(Toolbar toolbar, int itemColor) {
        if (toolbar == null) return;
        final ActionMenuView actionMenuView = ViewUtils.findViewByType(toolbar, ActionMenuView.class);
        if (actionMenuView == null) return;
        View overflowView = null;
        for (int i = 0, j = actionMenuView.getChildCount(); i < j; i++) {
            final View child = actionMenuView.getChildAt(i);
            final ActionMenuView.LayoutParams lp = (ActionMenuView.LayoutParams) child.getLayoutParams();
            if (lp.isOverflowButton) {
                overflowView = child;
                break;
            }
        }
        if (!(overflowView instanceof ImageView)) return;
        final Drawable drawable = ((ImageView) overflowView).getDrawable();
        if (drawable == null) return;
        drawable.setColorFilter(itemColor, Mode.SRC_ATOP);
    }

    public static void setActionBarOverflowColor(ActionMenuView actionMenuView, int itemColor) {
        if (actionMenuView == null) return;
        View overflowView = null;
        for (int i = 0, j = actionMenuView.getChildCount(); i < j; i++) {
            final View child = actionMenuView.getChildAt(i);
            final ActionMenuView.LayoutParams lp = (ActionMenuView.LayoutParams) child.getLayoutParams();
            if (lp.isOverflowButton) {
                overflowView = child;
                break;
            }
        }
        if (!(overflowView instanceof ImageView)) return;
        final Drawable drawable = ((ImageView) overflowView).getDrawable();
        if (drawable == null) return;
        drawable.setColorFilter(itemColor, Mode.SRC_ATOP);
    }

    public static void setActionBarSubtitleTextColor(Window window, int itemColor) {
        final View actionBarView = window.findViewById(android.support.v7.appcompat.R.id.action_bar);
        if (actionBarView instanceof Toolbar) {
            ((Toolbar) actionBarView).setSubtitleTextColor(itemColor);
        }
    }

    public static void setActionBarTitleTextColor(Window window, int itemColor) {
        final View actionBarView = window.findViewById(android.support.v7.appcompat.R.id.action_bar);
        if (actionBarView instanceof Toolbar) {
            ((Toolbar) actionBarView).setTitleTextColor(itemColor);
        }
    }

    public static void setCompatToolbarOverlay(Activity activity, Drawable overlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        final Window window = activity.getWindow();
        final View view = window.findViewById(android.support.v7.appcompat.R.id.decor_content_parent);
        if (!(view instanceof ActionBarOverlayLayout)) {
            View contentLayout = window.findViewById(android.support.v7.appcompat.R.id.action_bar_activity_content);
            if (contentLayout == null) {
                contentLayout = window.findViewById(android.R.id.content);
            }
            if (contentLayout instanceof ContentFrameLayout) {
                ((ContentFrameLayout) contentLayout).setForeground(overlay);
            }
            return;
        }
        try {
            final Field field = ActionBarOverlayLayout.class.getDeclaredField("mWindowContentOverlay");
            field.setAccessible(true);
            field.set(view, overlay);
        } catch (Exception ignore) {
        }
    }

    public static void setupDrawerBackground(Context context, View view) {
        if (!(context instanceof IThemedActivity)) return;
        final int themeRes = ((IThemedActivity) context).getCurrentThemeResourceId();
        final int drawerThemeRes = getDrawerThemeResource(themeRes);
        final String backgroundOption = ((IThemedActivity) context).getThemeBackgroundOption();
        final int alpha = ((IThemedActivity) context).getCurrentThemeBackgroundAlpha();
        final Drawable d = getWindowBackgroundFromTheme(context, drawerThemeRes);
        if (d != null && isTransparentBackground(backgroundOption)) {
            d.setAlpha(alpha);
        }
        ViewUtils.setBackground(view, d);
    }

    public static void wrapMenuIcon(@NonNull Menu menu, int itemColor, int subItemColor, int... excludeGroups) {
        for (int i = 0, j = menu.size(); i < j; i++) {
            final MenuItem item = menu.getItem(i);
            wrapMenuItemIcon(item, itemColor, excludeGroups);
            if (item.hasSubMenu()) {
                wrapMenuIcon(item.getSubMenu(), subItemColor, subItemColor, excludeGroups);
            }
        }
    }

    public static void wrapMenuIcon(ActionMenuView view, int... excludeGroups) {
        final Resources resources = view.getResources();
        final int colorDark = resources.getColor(R.color.action_icon_dark);
        final int colorLight = resources.getColor(R.color.action_icon_light);
        wrapMenuIcon(view, colorDark, colorLight, excludeGroups);
    }

    public static void wrapMenuIcon(ActionMenuView view, int colorDark, int colorLight, int... excludeGroups) {
        final int itemBackgroundColor = ThemeUtils.getThemeBackgroundColor(view.getContext());
        final int popupItemBackgroundColor = ThemeUtils.getThemeBackgroundColor(view.getContext(), view.getPopupTheme());
        final int itemColor = ColorUtils.getContrastYIQ(itemBackgroundColor, colorDark, colorLight);
        final int popupItemColor = ColorUtils.getContrastYIQ(popupItemBackgroundColor, colorDark, colorLight);
        final Menu menu = view.getMenu();
        final int childCount = view.getChildCount();
        for (int i = 0, j = menu.size(), k = 0; i < j; i++) {
            final MenuItem item = menu.getItem(i);
            wrapMenuItemIcon(item, itemColor, excludeGroups);
            if (item.hasSubMenu()) {
                wrapMenuIcon(menu, popupItemColor, popupItemColor, excludeGroups);
            }
            if (item.isVisible()) {
                k++;
            }
        }
    }

    public static void wrapMenuIcon(Context context, Menu menu, int... excludeGroups) {
        final int backgroundColor = ThemeUtils.getThemeBackgroundColor(context);
        wrapMenuIcon(context, backgroundColor, backgroundColor, menu, excludeGroups);
    }

    public static void wrapMenuIcon(Context context, int backgroundColor, int popupBackgroundColor,
                                    Menu menu, int... excludeGroups) {
        final Resources resources = context.getResources();
        final int colorDark = resources.getColor(R.color.action_icon_dark);
        final int colorLight = resources.getColor(R.color.action_icon_light);
        final int itemColor = ColorUtils.getContrastYIQ(backgroundColor, colorDark, colorLight);
        final int popupItemColor = ColorUtils.getContrastYIQ(popupBackgroundColor, colorDark, colorLight);
        for (int i = 0, j = menu.size(), k = 0; i < j; i++) {
            final MenuItem item = menu.getItem(i);
            wrapMenuItemIcon(item, itemColor, excludeGroups);
            if (item.hasSubMenu()) {
                wrapMenuIcon(menu, popupItemColor, popupItemColor, excludeGroups);
            }
            if (item.isVisible()) {
                k++;
            }
        }
    }

    public static void wrapMenuItemIcon(@NonNull MenuItem item, int itemColor, int... excludeGroups) {
        if (ArrayUtils.contains(excludeGroups, item.getGroupId())) return;
        final Drawable icon = item.getIcon();
        if (icon == null) return;
        if (icon instanceof ActionIconDrawable) {
            ((ActionIconDrawable) icon).setDefaultColor(itemColor);
            item.setIcon(icon);
            return;
        }
        icon.mutate();
        final Drawable.Callback callback = icon.getCallback();
        final ActionIconDrawable newIcon = new ActionIconDrawable(icon, itemColor);
        newIcon.setCallback(callback);
        item.setIcon(newIcon);
    }

    public static void wrapToolbarMenuIcon(@Nullable ActionMenuView view, int itemColor, int popupItemColor, int... excludeGroups) {
        if (view == null) return;
        final Menu menu = view.getMenu();
        final int childCount = view.getChildCount();
        for (int i = 0, j = menu.size(), k = 0; i < j; i++) {
            final MenuItem item = menu.getItem(i);
            wrapMenuItemIcon(item, itemColor, excludeGroups);
            if (item.hasSubMenu()) {
                wrapMenuIcon(menu, popupItemColor, popupItemColor, excludeGroups);
            }
            if (item.isVisible()) {
                k++;
            }
        }
    }

    private static void applyColorTintForView(View view, int tintColor) {
        if (view instanceof IThemedView) {
            final ColorStateList tintList = ColorStateList.valueOf(tintColor);
            ((IThemedView) view).setThemeTintColor(tintList);
        } else if (view instanceof ProgressBar) {
            final ColorStateList tintList = ColorStateList.valueOf(tintColor);
            final ProgressBar progressBar = (ProgressBar) view;
            ViewUtils.setProgressTintList(progressBar, tintList);
            ViewUtils.setProgressBackgroundTintList(progressBar, tintList);
            ViewUtils.setIndeterminateTintList(progressBar, tintList);
        } else if (view instanceof CompoundButton) {
            final ColorStateList tintList = ColorStateList.valueOf(tintColor);
            final CompoundButton compoundButton = (CompoundButton) view;
            ViewUtils.setButtonTintList(compoundButton, tintList);
        }
        // TODO support TintableBackgroundView
    }

    @NonNull
    private static SharedPreferencesWrapper getSharedPreferencesWrapper(Context context) {
        final Context appContext = context.getApplicationContext();
        return SharedPreferencesWrapper.getInstance(appContext, SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    private static View newViewInstance(final String className, final Context context, final AttributeSet attrs)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final Class<?> viewCls = Class.forName(className);
        final Constructor<?> constructor = viewCls.getConstructor(Context.class, AttributeSet.class);
        return (View) constructor.newInstance(context, attrs);
    }
}
