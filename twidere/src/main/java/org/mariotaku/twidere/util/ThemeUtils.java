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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.graphic.ActionBarColorDrawable;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.text.ParagraphSpacingSpan;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.view.iface.IThemedView;

import java.lang.reflect.Constructor;
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

    @Deprecated
    public static void applyActionBarBackground(final ActionBar actionBar, final Context context) {
        applyActionBarBackground(actionBar, context, true);
    }

    @Deprecated
    public static void applyActionBarBackground(final ActionBar actionBar, final Context context,
                                                final boolean applyAlpha) {
        if (actionBar == null || context == null) return;
        actionBar.setBackgroundDrawable(getActionBarBackground(context, applyAlpha));
        actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, applyAlpha));
        actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, applyAlpha));
    }

    public static void applyActionBarBackground(final ActionBar actionBar, final Context context, final int themeRes) {
        if (actionBar == null || context == null) return;
        actionBar.setBackgroundDrawable(getActionBarBackground(context, themeRes));
        actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, themeRes));
        actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, themeRes));
    }


    public static void applyActionBarBackground(final ActionBar actionBar, final Context context,
                                                final int themeRes, final int accentColor) {
        if (actionBar == null || context == null) return;
        actionBar.setBackgroundDrawable(getActionBarBackground(context, themeRes, accentColor));
        actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, themeRes));
        actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, themeRes));
    }


    public static void applyActionBarBackground(final android.support.v7.app.ActionBar actionBar, final Context context,
                                                final int themeRes, final int accentColor) {
        if (actionBar == null || context == null) return;
        actionBar.setBackgroundDrawable(getActionBarBackground(context, themeRes, accentColor));
        actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, themeRes));
        actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, themeRes));
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

    public static void wrapMenuIcon(@NonNull Menu menu, int itemColor, int subItemColor, int... excludeGroups) {
        for (int i = 0, j = menu.size(); i < j; i++) {
            final MenuItem item = menu.getItem(i);
            wrapMenuItemIcon(item, itemColor, excludeGroups);
            if (item.hasSubMenu()) {
                wrapMenuIcon(item.getSubMenu(), subItemColor, subItemColor, excludeGroups);
            }
        }
    }

    public static void wrapMenuItemIcon(@NonNull MenuItem item, int itemColor, int... excludeGroups) {
        if (ArrayUtils.contains(excludeGroups, item.getGroupId())) return;
        final Drawable icon = item.getIcon();
        if (icon == null) return;
        icon.mutate();
        final Drawable.Callback callback = icon.getCallback();
        final ActionIconDrawable newIcon = new ActionIconDrawable(icon, itemColor);
        newIcon.setCallback(callback);
        item.setIcon(newIcon);
    }

    public static void wrapMenuIcon(ActionMenuView view, int... excludeGroups) {
        final int itemBackgroundColor = ThemeUtils.getThemeBackgroundColor(view.getContext());
        final int popupItemBackgroundColor = ThemeUtils.getThemeBackgroundColor(view.getContext(), view.getPopupTheme());
        final Resources resources = view.getResources();
        final int colorDark = resources.getColor(R.color.action_icon_dark);
        final int colorLight = resources.getColor(R.color.action_icon_light);
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

    public static void applyThemeAlphaToDrawable(final Context context, final Drawable d) {
        if (context == null || d == null) return;
        d.setAlpha(getThemeAlpha(getThemeResource(context)));
    }

    public static void applyThemeBackgroundAlphaToDrawable(final Context context, final Drawable d) {
        if (context == null || d == null) return;
        d.setAlpha(getUserThemeBackgroundAlpha(context));
    }


    public static int getActionBarPopupThemeRes(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return 0;
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.actionBarPopupTheme});
        try {
            return a.getResourceId(0, 0);
        } finally {
            a.recycle();
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

    private static void applyColorTintForView(View view, int tintColor) {
        if (view instanceof IThemedView) {
            final ColorStateList tintList = ColorStateList.valueOf(tintColor);
            ((IThemedView) view).setThemeTintColor(tintList);
        } else if (view instanceof ProgressBar) {
            final ColorStateList tintList = ColorStateList.valueOf(tintColor);
            final ProgressBar progressBar = (ProgressBar) view;
            ViewAccessor.setProgressTintList(progressBar, tintList);
            ViewAccessor.setProgressBackgroundTintList(progressBar, tintList);
            ViewAccessor.setIndeterminateTintList(progressBar, tintList);
        } else if (view instanceof CompoundButton) {
            final ColorStateList tintList = ColorStateList.valueOf(tintColor);
            final CompoundButton compoundButton = (CompoundButton) view;
            ViewAccessor.setButtonTintList(compoundButton, tintList);
        }
    }


    @Deprecated
    public static Drawable getActionBarBackground(final Context context, final boolean applyAlpha) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.background},
                android.R.attr.actionBarStyle, 0);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return applyActionBarDrawable(context, d, applyAlpha);
    }

    public static Drawable getActionBarBackground(final Context context, final int themeRes) {
        final TypedArray a1 = context.obtainStyledAttributes(null, new int[]{android.R.attr.background},
                R.attr.actionBarStyle, themeRes);
        try {
            if (a1.hasValue(0)) {
                return applyActionBarDrawable(context, a1.getDrawable(0), isTransparentBackground(themeRes));
            }
        } finally {
            a1.recycle();
        }
        final TypedArray a2 = context.obtainStyledAttributes(null, new int[]{android.R.attr.background},
                android.R.attr.actionBarStyle, themeRes);
        try {
            if (a2.hasValue(0)) {
                return applyActionBarDrawable(context, a2.getDrawable(0), isTransparentBackground(themeRes));
            }
        } finally {
            a2.recycle();
        }
        return null;
    }

    public static Drawable getActionBarBackground(final Context context, final int themeRes,
                                                  final int accentColor) {
        if (!isDarkTheme(themeRes)) {
            final ColorDrawable d = new ActionBarColorDrawable(accentColor);
            return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
        }
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.background},
                android.R.attr.actionBarStyle, themeRes);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
    }

    public static int getActionBarThemeResource(final Context context) {
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
        return resId;
    }

    public static Context getActionBarContext(final Context context) {
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

    @Deprecated
    public static Drawable getActionBarSplitBackground(final Context context, final boolean applyAlpha) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.backgroundSplit},
                android.R.attr.actionBarStyle, 0);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return applyActionBarDrawable(context, d, applyAlpha);
    }

    public static Drawable getActionBarSplitBackground(final Context context, final int themeRes) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.backgroundSplit},
                android.R.attr.actionBarStyle, themeRes);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
    }

    @Deprecated
    public static Drawable getActionBarStackedBackground(final Context context, final boolean applyAlpha) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.backgroundStacked},
                android.R.attr.actionBarStyle, 0);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return applyActionBarDrawable(context, d, applyAlpha);
    }

    public static Drawable getActionBarStackedBackground(final Context context, final int themeRes) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.backgroundStacked},
                android.R.attr.actionBarStyle, themeRes);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
    }

    public static int getActionIconColor(final Context context) {
        return getActionIconColor(getThemeResource(context));
    }

    public static int getActionIconColor(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Light:
            case R.style.Theme_Twidere_Light_SolidBackground:
            case R.style.Theme_Twidere_Light_Transparent:
            case R.style.Theme_Twidere_Light_Compose:
            case R.style.Theme_Twidere_Light_Dialog:
            case R.style.Theme_Twidere_ActionBar_Colored_Light:
            case R.style.Theme_Twidere_Settings_Light:
            case R.style.Theme_Twidere_Drawer_Light:
            case R.style.Theme_Twidere_Drawer_Light_Transparent:
            case R.style.Theme_Twidere_Settings_Light_DarkActionBar_DarkIcon:
                return 0x99333333;
        }
        return 0xCCFFFFFF;
    }

    public static int getColorBackgroundCacheHint(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.colorBackgroundCacheHint});
        final int color = a.getColor(0, Color.TRANSPARENT);
        a.recycle();
        return color;
    }

    public static int getCardBackgroundColor(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.cardItemBackgroundColor});
        final int color = a.getColor(0, Color.TRANSPARENT);
        a.recycle();
        final int themeAlpha = getThemeAlpha(context);
        return themeAlpha << 24 | (0x00FFFFFF & color);
    }

    public static int getComposeThemeResource(final Context context) {
        return getComposeThemeResource(getThemeNameOption(context), getDarkActionBarOption(context));
    }

    public static int getComposeThemeResource(final String name, final boolean darkActionBar) {
        if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Compose;
        return R.style.Theme_Twidere_Light_Compose;
    }

    public static boolean getDarkActionBarOption(final Context context) {
        if (context == null) return true;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getBoolean(KEY_THEME_DARK_ACTIONBAR, true);
    }

    public static Context getDialogThemedContext(final Context context) {
        return new ContextThemeWrapper(context, getDialogThemeResource(context));
    }

    public static int getDialogThemeResource(final Context context) {
        return getDialogThemeResource(getThemeNameOption(context));
    }

    public static int getDialogThemeResource(final String name) {
        if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Dialog;
        return R.style.Theme_Twidere_Light_Dialog;
    }

    public static int getDrawerThemeResource(final Context context) {
        return getDrawerThemeResource(getThemeResource(context));
    }

    public static int getDrawerThemeResource(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Dark_Transparent:
            case R.style.Theme_Twidere_Light_Transparent:
                return R.style.Theme_Twidere_Drawer_Dark_Transparent;
        }
        return R.style.Theme_Twidere_Drawer_Dark;
    }

    public static int getLightDrawerThemeResource(final Context context) {
        return getLightDrawerThemeResource(getThemeResource(context));
    }

    public static int getLightDrawerThemeResource(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Light_Transparent:
                return R.style.Theme_Twidere_Drawer_Light_Transparent;
        }
        return R.style.Theme_Twidere_Drawer_Light;
    }

    public static Drawable getImageHighlightDrawable(final Context context) {
        final Drawable d = getSelectableItemBackgroundDrawable(context);
        if (d != null) {
            d.setAlpha(0x80);
        }
        return d;
    }

    public static int getMenuIconColor(final Context context) {
        return getMenuIconColor(getThemeResource(context));
    }

    public static int getMenuIconColor(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Light:
            case R.style.Theme_Twidere_Light_SolidBackground:
            case R.style.Theme_Twidere_Light_Transparent:
            case R.style.Theme_Twidere_Light_Compose:
            case R.style.Theme_Twidere_Light_Dialog:
            case R.style.Theme_Twidere_ActionBar_Colored_Light:
            case R.style.Theme_Twidere_Settings_Light:
            case R.style.Theme_Twidere_Settings_Light_DarkActionBar_DarkIcon:
                return 0x99333333;
        }
        return 0xCCFFFFFF;
    }

    public static int getNoDisplayThemeResource(final Context context) {
        if (context == null) return R.style.Theme_Twidere_Dark_NoDisplay;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final String theme = pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE);
        if (VALUE_THEME_NAME_DARK.equals(theme)) return R.style.Theme_Twidere_Dark_NoDisplay;
        return R.style.Theme_Twidere_Light_NoDisplay;
    }

    public static Resources getResources(final Context context) {
        if (context instanceof IThemedActivity) {
            return ((IThemedActivity) context).getDefaultResources();
        }
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
        return getSettingsThemeResource(getThemeNameOption(context), getDarkActionBarOption(context));
    }

    public static int getSettingsThemeResource(final String name, final boolean darkActionBar) {
        if (VALUE_THEME_NAME_TWIDERE.equals(name) || VALUE_THEME_NAME_LIGHT.equals(name))
            return darkActionBar ? R.style.Theme_Twidere_Settings_Light_DarkActionBar
                    : R.style.Theme_Twidere_Settings_Light;
        else if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Settings_Dark;
        return R.style.Theme_Twidere_Settings_Light_DarkActionBar;
    }

    public static int getSettingsWizardThemeResource(final Context context) {
        return R.style.Theme_Twidere_Settings_Light;
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
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary});
        try {
            return a.getColor(0, Color.TRANSPARENT);
        } finally {
            a.recycle();
        }
    }

    public static int getThemeAlpha(final Context context) {
        return getThemeAlpha(getThemeResource(context));
    }

    public static int getThemeAlpha(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Dark_Transparent:
            case R.style.Theme_Twidere_Light_Transparent:
                return 0xa0;
        }
        return 0xff;
    }

    public static String getThemeBackgroundOption(final Context context) {
        if (context == null) return VALUE_THEME_BACKGROUND_DEFAULT;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getString(KEY_THEME_BACKGROUND, VALUE_THEME_BACKGROUND_DEFAULT);
    }

    public static int getThemeColor(final Context context) {
        final Resources res = getResources(context);
        final Context wrapped = getThemedContext(context, res);
        final TypedArray a = wrapped.obtainStyledAttributes(new int[]{android.R.attr.colorActivatedHighlight});
        try {
            return a.getColor(0, res.getColor(R.color.material_light_blue));
        } finally {
            a.recycle();
        }
    }

    public static Context getThemedContext(final Context context) {
        return context;
    }

    public static Context getThemedContext(final Context context, final Resources res) {
        return context;
    }

    public static String getThemeFontFamily(final Context context) {
        if (context == null) return VALUE_THEME_FONT_FAMILY_REGULAR;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final String fontFamily = pref.getString(KEY_THEME_FONT_FAMILY, VALUE_THEME_FONT_FAMILY_REGULAR);
        if (!TextUtils.isEmpty(fontFamily)) return fontFamily;
        return VALUE_THEME_FONT_FAMILY_REGULAR;
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
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.colorBackground},
                0, themeRes);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
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


    @NonNull
    private static SharedPreferencesWrapper getSharedPreferencesWrapper(Context context) {
        final Context appContext = context.getApplicationContext();
        return SharedPreferencesWrapper.getInstance(appContext, SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    public static String getThemeNameOption(final Context context) {
        if (context == null) return VALUE_THEME_NAME_TWIDERE;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE);
    }

    public static int getThemeResource(final Context context) {
        return getThemeResource(getThemeNameOption(context), getThemeBackgroundOption(context),
                getDarkActionBarOption(context));
    }

    public static int getNoActionBarThemeResource(final Context context) {
        return getNoActionBarThemeResource(getThemeNameOption(context), getThemeBackgroundOption(context),
                getDarkActionBarOption(context));
    }

    public static int getNoActionBarThemeResource(final String name, final String background, final boolean darkActionBar) {
        if (VALUE_THEME_NAME_LIGHT.equals(name)) {
            if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
                return R.style.Theme_Twidere_Light_SolidBackground_NoActionBar;
            else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
                return R.style.Theme_Twidere_Light_Transparent_NoActionBar;
            return R.style.Theme_Twidere_Light_NoActionBar;
        } else if (VALUE_THEME_NAME_DARK.equals(name)) {
            if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
                return R.style.Theme_Twidere_Dark_SolidBackground_NoActionBar;
            else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
                return R.style.Theme_Twidere_Dark_Transparent_NoActionBar;
            return R.style.Theme_Twidere_Dark_NoActionBar;
        }
        return R.style.Theme_Twidere_Light_NoActionBar;
    }

    public static int getThemeResource(final String name, final String background, final boolean darkActionBar) {
        if (VALUE_THEME_NAME_LIGHT.equals(name)) {
            if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
                return R.style.Theme_Twidere_Light_SolidBackground;
            else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
                return R.style.Theme_Twidere_Light_Transparent;
            return R.style.Theme_Twidere_Light;

        } else if (VALUE_THEME_NAME_DARK.equals(name)) {
            if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
                return R.style.Theme_Twidere_Dark_SolidBackground;
            else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
                return R.style.Theme_Twidere_Dark_Transparent;
            return R.style.Theme_Twidere_Dark;
        }
        return R.style.Theme_Twidere_Light;
    }

    public static int getTitleTextAppearance(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.titleTextStyle},
                android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
        final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo);
        a.recycle();
        return textAppearance;
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

    public static int getUserAccentColor(final Context context) {
        if (context == null) return Color.TRANSPARENT;
        final Resources res = getResources(context);
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final int def = res.getColor(R.color.material_light_blue);
        return pref.getInt(KEY_THEME_COLOR, def);
    }

    public static int getUserAccentColor(final Context context, int themeRes) {
        if (context == null) return Color.TRANSPARENT;
        final int defThemeColor = getThemeColor(context, themeRes);
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getInt(KEY_THEME_COLOR, defThemeColor);
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
        final Drawable d = a.getDrawable(0);
        a.recycle();
        if (isTransparentBackground(context)) {
            applyThemeBackgroundAlphaToDrawable(context, d);
        }
        return d;
    }

    public static Drawable getWindowBackground(final Context context, final int themeRes) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.windowBackground}, 0,
                themeRes);
        final Drawable d = a.getDrawable(0);
        a.recycle();
        if (isTransparentBackground(themeRes)) {
            applyThemeBackgroundAlphaToDrawable(context, d);
        }
        return d;
    }

    public static Drawable getWindowContentOverlay(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowContentOverlay});
        final Drawable d = a.getDrawable(0);
        a.recycle();
        return d;
    }

    public static Drawable getWindowContentOverlayForCompose(final Context context) {
        final int themeRes = getThemeResource(context);
        return getWindowContentOverlay(new ContextThemeWrapper(context, themeRes));
    }

    public static boolean isDarkTheme(final Context context) {
        return isDarkTheme(getThemeResource(context));
    }

    public static boolean isDarkTheme(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Dark:
            case R.style.Theme_Twidere_Dark_SolidBackground:
            case R.style.Theme_Twidere_Dark_Dialog:
            case R.style.Theme_Twidere_Dark_Compose:
            case R.style.Theme_Twidere_Dark_Transparent:
            case R.style.Theme_Twidere_Dark_NoActionBar:
            case R.style.Theme_Twidere_Dark_SolidBackground_NoActionBar:
            case R.style.Theme_Twidere_Dark_Transparent_NoActionBar:
                return true;
        }
        return false;
    }

    public static boolean isFloatingWindow(final Context context) {

        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowIsFloating});
        final boolean b = a.getBoolean(0, false);
        a.recycle();
        return b;
    }

    public static boolean isSolidBackground(final Context context) {
        return VALUE_THEME_BACKGROUND_SOLID.equals(getThemeBackgroundOption(context));
    }

    public static boolean isTransparentBackground(final Context context) {
        return isTransparentBackground(getThemeResource(context));
    }

    public static boolean isTransparentBackground(final int themeRes) {
        switch (themeRes) {
            case R.style.Theme_Twidere_Dark_Transparent:
            case R.style.Theme_Twidere_Light_Transparent:
            case R.style.Theme_Twidere_Dark_Transparent_NoActionBar:
            case R.style.Theme_Twidere_Light_Transparent_NoActionBar:
                return true;
        }
        return false;
    }

    public static void notifyStatusBarColorChanged(final Context context, final int themeResource,
                                                   final int accentColor, final int backgroundAlpha) {
        final Intent intent = new Intent("com.mohammadag.colouredstatusbar.ChangeStatusBarColor");
//        if (isColoredActionBar(themeResource)) {
//            intent.putExtra("status_bar_color", backgroundAlpha << 24 | accentColor);
//        } else {
//            if (isLightActionBar(themeResource)) {
//                intent.putExtra("status_bar_color", backgroundAlpha << 24 | 0xFFDDDDDD);
//            } else {
//                intent.putExtra("status_bar_color", backgroundAlpha << 24 | 0xFF222222);
//            }
//        }
//        if (isLightActionBar(themeResource)) {
//            intent.putExtra("status_bar_icons_color", Color.DKGRAY);
//        } else {
//            intent.putExtra("status_bar_icons_color", Color.WHITE);
//        }
        // Please note that these are not yet implemented!!!
        // You're free to include them in your code so that when they
        // are implemented, your app will work out of the box.
        intent.putExtra("navigation_bar_color", Color.BLACK);
        intent.putExtra("navigation_bar_icon_color", Color.WHITE);
        context.sendOrderedBroadcast(intent, null);
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
        final TypedArray a = activity.obtainStyledAttributes(null, ANIM_CLOSE_STYLE_ATTRS,
                0, android.R.style.Animation_Activity);
        final int activityCloseEnterAnimation = a.getResourceId(0, 0);
        final int activityCloseExitAnimation = a.getResourceId(1, 0);
        a.recycle();
        activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }


    public static boolean shouldApplyColorFilterToActionIcons(final Context context) {
        return false;
    }

    public static boolean shouldApplyColorFilterToActionIcons(final int res) {
        return false;
    }

    private static Drawable applyActionBarDrawable(final Context context, final Drawable d, final boolean applyAlpha) {
        if (d == null) return null;
        d.mutate();
        if (d instanceof LayerDrawable) {
            final Drawable colorLayer = ((LayerDrawable) d).findDrawableByLayerId(R.id.color_layer);
            if (colorLayer != null) {
                final int color = getUserAccentColor(context);
                colorLayer.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
        if (applyAlpha) {
            d.setAlpha(getThemeAlpha(context));
        }
        return d;
    }

    private static View newViewInstance(final String className, final Context context, final AttributeSet attrs)
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        final Class<?> viewCls = Class.forName(className);
        final Constructor<?> constructor = viewCls.getConstructor(Context.class, AttributeSet.class);
        return (View) constructor.newInstance(context, attrs);
    }

    public static int findAttributeResourceValue(AttributeSet attrs, String name, int defaultValue) {
        for (int i = 0, j = attrs.getAttributeCount(); i < j; i++) {
            if (attrs.getAttributeName(i).equals(name))
                return attrs.getAttributeResourceValue(i, defaultValue);
        }
        return defaultValue;
    }

    public static int getThemeColor(Context context, int themeResourceId) {
        final Context appContext = context.getApplicationContext();
        final Resources res = appContext.getResources();
        final TypedArray a = appContext.obtainStyledAttributes(null,
                new int[]{android.R.attr.colorActivatedHighlight}, 0, themeResourceId);
        try {
            return a.getColor(0, res.getColor(R.color.material_light_blue));
        } finally {
            a.recycle();
        }
    }

    public static boolean isColoredActionBar(int themeRes) {
        return !isDarkTheme(themeRes);
    }

    public static void initTextView(TextView view) {
        if (view.isInEditMode()) return;
        final Context context = view.getContext();
//        view.setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
//        view.setHighlightColor(ThemeUtils.getUserHighlightColor(context));
        view.setTypeface(ThemeUtils.getUserTypeface(context, view.getTypeface()));
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

}
