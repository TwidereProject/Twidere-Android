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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TwidereToolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.afollestad.appthemeengine.util.ATEUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.graphic.ActionIconDrawable;
import org.mariotaku.twidere.graphic.iface.DoNotWrapDrawable;
import org.mariotaku.twidere.preference.ThemeBackgroundPreference;
import org.mariotaku.twidere.util.menu.TwidereMenuInfo;
import org.mariotaku.twidere.util.support.ViewSupport;

import java.lang.reflect.Field;

public class ThemeUtils implements Constants {

    public static final int ACCENT_COLOR_THRESHOLD = 192;
    public static final int DARK_COLOR_THRESHOLD = 64;

    public static final int[] ATTRS_TEXT_COLOR_PRIMARY = {android.R.attr.textColorPrimary};
    public static final int[] ATTRS_TEXT_COLOR_PRIMARY_AND_INVERSE = {android.R.attr.textColorPrimary,
            android.R.attr.textColorPrimaryInverse};
    public static final int[] ATTRS_COLOR_FOREGROUND_AND_INVERSE = {android.R.attr.colorForeground,
            android.R.attr.colorForegroundInverse};

    private ThemeUtils() {
        throw new AssertionError("ThemeUtils should never be instantiated");
    }


    public static void applyColorFilterToMenuIcon(final Menu menu, @ColorInt final int color,
                                                  @ColorInt final int popupColor,
                                                  @ColorInt final int highlightColor, final Mode mode,
                                                  final int... excludedGroups) {
        for (int i = 0, j = menu.size(); i < j; i++) {
            final MenuItem item = menu.getItem(i);
            final Drawable icon = item.getIcon();
            final ContextMenuInfo info = item.getMenuInfo();
            if (icon != null && !ArrayUtils.contains(excludedGroups, item.getGroupId())) {
                icon.mutate();
                if (info instanceof TwidereMenuInfo) {
                    final TwidereMenuInfo sInfo = (TwidereMenuInfo) info;
                    final int stateColor = sInfo.isHighlight() ? sInfo.getHighlightColor(highlightColor) : color;
                    if (stateColor != 0) {
                        icon.setColorFilter(stateColor, mode);
                    }
                } else if (color != 0) {
                    icon.setColorFilter(color, mode);
                }
            }
            if (item.hasSubMenu()) {
                // SubMenu item is always in popup
                applyColorFilterToMenuIcon(item.getSubMenu(), popupColor, popupColor, highlightColor, mode, excludedGroups);
            }
        }
    }

    public static void applyWindowBackground(@NonNull Context context, @NonNull Window window, String option, int alpha) {
        if (isWindowFloating(context)) return;
        if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(option)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
            window.setBackgroundDrawable(getWindowBackgroundFromThemeApplyAlpha(context, alpha));
        } else if (VALUE_THEME_BACKGROUND_SOLID.equals(option)) {
            window.setBackgroundDrawable(new ColorDrawable(!isLightTheme(context) ? Color.BLACK : Color.WHITE));
        } else {
            window.setBackgroundDrawable(getWindowBackgroundFromTheme(context));
        }
    }

    public static void applyWindowBackground(Context context, View window, String option, int alpha) {
        if (isWindowFloating(context)) return;
        if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(option)) {
            ViewSupport.setBackground(window, getWindowBackgroundFromThemeApplyAlpha(context, alpha));
        } else if (VALUE_THEME_BACKGROUND_SOLID.equals(option)) {
            ViewSupport.setBackground(window, new ColorDrawable(!isLightTheme(context) ? Color.BLACK : Color.WHITE));
        } else {
            ViewSupport.setBackground(window, getWindowBackgroundFromTheme(context));
        }
    }


    public static int getCardBackgroundColor(final Context context, String backgroundOption, int themeAlpha) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.cardItemBackgroundColor});
        final int color = a.getColor(0, Color.TRANSPARENT);
        a.recycle();
        if (isTransparentBackground(backgroundOption)) {
            return themeAlpha << 24 | (0x00FFFFFF & color);
        } else if (isSolidBackground(backgroundOption)) {
            return TwidereColorUtils.getContrastYIQ(color, Color.WHITE, Color.BLACK);
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
                return contentLayout.getForeground();
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


    public static int getContrastColor(int color, int darkColor, int lightColor) {
        if (TwidereColorUtils.getYIQLuminance(color) <= ACCENT_COLOR_THRESHOLD) {
            //return light text color
            return lightColor;
        }
        //return dark text color
        return darkColor;
    }

    public static int getContrastActionBarItemColor(Context context) {
        return getColorFromAttribute(context, android.R.attr.colorForeground, 0);
    }

    public static Context getDialogThemedContext(final Context context) {
        return new ContextThemeWrapper(context, R.style.Theme_Twidere_Dialog);
    }

    public static Drawable getImageHighlightDrawable(final Context context) {
        final Drawable d = getSelectableItemBackgroundDrawable(context);
        if (d != null) {
            d.setAlpha(0x80);
        }
        return d;
    }

    public static int getOptimalAccentColor(int accentColor, int foregroundColor) {
        final int[] yiq = new int[3];
        TwidereColorUtils.colorToYIQ(foregroundColor, yiq);
        final int foregroundColorY = yiq[0];
        TwidereColorUtils.colorToYIQ(accentColor, yiq);
        if (foregroundColorY < DARK_COLOR_THRESHOLD && yiq[0] <= ACCENT_COLOR_THRESHOLD) {
            return accentColor;
        } else if (foregroundColorY > ACCENT_COLOR_THRESHOLD && yiq[0] > DARK_COLOR_THRESHOLD) {
            return accentColor;
        }
        yiq[0] = yiq[0] + (foregroundColorY - yiq[0]) / 2;
        return TwidereColorUtils.YIQToColor(Color.alpha(accentColor), yiq);
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

    public static float getSupportActionBarElevation(final Context context) {
        @SuppressWarnings("ConstantConditions")
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{R.attr.elevation}, R.attr.actionBarStyle, 0);
        try {
            return a.getDimension(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static int getColorFromAttribute(Context context, int attr, int def) {
        final TypedValue outValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(attr, outValue, true))
            return def;
        if (outValue.type == TypedValue.TYPE_REFERENCE)
            return ContextCompat.getColor(context, attr);
        return outValue.data;
    }


    public static int getTextColorPrimary(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS_TEXT_COLOR_PRIMARY);
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

    public static int getThemeColor(Context context) {
        final Context appContext = context.getApplicationContext();
        final TypedArray a = appContext.obtainStyledAttributes(new int[]{R.attr.colorPrimary});
        try {
            return a.getColor(0, ContextCompat.getColor(context, R.color.material_light_blue));
        } finally {
            a.recycle();
        }
    }

    public static String getThemeFontFamily(@NonNull final SharedPreferences pref) {
        final String fontFamily = pref.getString(KEY_THEME_FONT_FAMILY, VALUE_THEME_FONT_FAMILY_REGULAR);
        if (!TextUtils.isEmpty(fontFamily)) return fontFamily;
        return VALUE_THEME_FONT_FAMILY_REGULAR;
    }

    public static int getThemeForegroundColor(final Context context) {
        return getThemeForegroundColor(context, 0);
    }

    public static int getThemeForegroundColor(final Context context, int themeRes) {
        @SuppressWarnings("ConstantConditions")
        final TypedValue value = new TypedValue();
        final Resources.Theme theme;
        if (themeRes != 0) {
            theme = context.getResources().newTheme();
            theme.applyStyle(themeRes, false);
        } else {
            theme = context.getTheme();
        }
        if (!theme.resolveAttribute(android.R.attr.colorForeground, value, true)) {
            return 0;
        }
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            return value.data;
        }
        return 0;
    }

    public static String getThemeNameOption(final Context context) {
        if (context == null) return VALUE_THEME_NAME_LIGHT;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return pref.getString(KEY_THEME, VALUE_THEME_NAME_LIGHT);
    }

    public static int getTitleTextAppearance(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(null, new int[]{android.R.attr.titleTextStyle},
                android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
        final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo);
        a.recycle();
        return textAppearance;
    }

    public static int getUserAccentColor(@NonNull final Context context) {
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        final int def = ContextCompat.getColor(context, R.color.branding_color);
        return pref.getInt(KEY_THEME_COLOR, def);
    }

    public static int getOptimalAccentColor(final Context context, boolean isActionBarContext) {
        return getOptimalAccentColor(context, getUserAccentColor(context), isActionBarContext);
    }

    public static int getOptimalAccentColor(final Context context, final int accentColor, boolean isActionBarContext) {
        final int backgroundColorApprox;
        final boolean isDarkTheme = !isLightTheme(context);
        if (!isActionBarContext) {
            backgroundColorApprox = isDarkTheme ? Color.BLACK : Color.WHITE;
        } else if (isDarkTheme) {
            // View context is derived from ActionBar but is currently dark theme, so we should show
            // light
            backgroundColorApprox = Color.BLACK;
        } else {
            // View context is derived from ActionBar and it's light theme, so we use contrast color
            backgroundColorApprox = Color.WHITE;
        }
        if (Math.abs(TwidereColorUtils.getYIQContrast(backgroundColorApprox, accentColor)) > DARK_COLOR_THRESHOLD)
            return accentColor;
        return getColorFromAttribute(context, R.attr.colorAccent,
                ContextCompat.getColor(context, R.color.branding_color));
    }

    public static int getUserThemeBackgroundAlpha(final Context context) {
        if (context == null) return DEFAULT_THEME_BACKGROUND_ALPHA;
        final SharedPreferencesWrapper pref = getSharedPreferencesWrapper(context);
        return TwidereMathUtils.clamp(pref.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA),
                ThemeBackgroundPreference.MIN_ALPHA, ThemeBackgroundPreference.MAX_ALPHA);
    }


    public static int getActionBarAlpha(final int alpha) {
        final int normalizedAlpha = TwidereMathUtils.clamp(alpha, 0, 0xFF);
        final int delta = (ThemeBackgroundPreference.MAX_ALPHA - normalizedAlpha);
        return TwidereMathUtils.clamp(ThemeBackgroundPreference.MAX_ALPHA - delta / 2,
                ThemeBackgroundPreference.MIN_ALPHA, ThemeBackgroundPreference.MAX_ALPHA);
    }

    public static Typeface getUserTypeface(final Context context, final String fontFamily, final Typeface defTypeface) {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return Typeface.DEFAULT;
        final int fontStyle = defTypeface != null ? defTypeface.getStyle() : Typeface.NORMAL;
        final Typeface tf = Typeface.create(fontFamily, fontStyle);
        if (tf != null) return tf;
        return Typeface.create(Typeface.DEFAULT, fontStyle);
    }

    public static Drawable getWindowBackgroundFromTheme(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.windowNormalBackground,
                android.R.attr.windowBackground});
        try {
            if (a.hasValue(0)) {
                return a.getDrawable(0);
            } else {
                return a.getDrawable(1);
            }
        } finally {
            a.recycle();
        }
    }

    public static Drawable getWindowBackgroundFromThemeApplyAlpha(final Context context, final int alpha) {
        final Drawable d = getWindowBackgroundFromTheme(context);
        if (d == null) return null;
        d.mutate();
        d.setAlpha(TwidereMathUtils.clamp(alpha, ThemeBackgroundPreference.MIN_ALPHA,
                ThemeBackgroundPreference.MAX_ALPHA));
        return d;
    }

    public static Drawable getWindowContentOverlay(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowContentOverlay});
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }

    public static Drawable getNormalWindowContentOverlay(final Context context) {
        return getWindowContentOverlay(context);
    }

    public static boolean isLightTheme(final Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.isLightTheme});
        try {
            return a.getBoolean(0, false);
        } finally {
            a.recycle();
        }
    }

    public static boolean isSolidBackground(final String option) {
        return VALUE_THEME_BACKGROUND_SOLID.equals(option);
    }

    public static boolean isTransparentBackground(final Context context) {
        return isTransparentBackground(getThemeBackgroundOption(context));
    }

    public static boolean isTransparentBackground(final String option) {
        return VALUE_THEME_BACKGROUND_TRANSPARENT.equals(option);
    }

    public static boolean isWindowFloating(Context context) {
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.windowIsFloating});
        try {
            return a.getBoolean(0, false);
        } finally {
            a.recycle();
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

    public static void setActionBarOverflowColor(Toolbar toolbar, int itemColor) {
        if (toolbar == null) return;
        if (toolbar instanceof TwidereToolbar) {
            ((TwidereToolbar) toolbar).setItemColor(itemColor);
        }
        final Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(itemColor, Mode.SRC_ATOP);
            toolbar.setOverflowIcon(overflowIcon);
        }
    }

    public static void setCompatToolbarOverlay(Activity activity, Drawable overlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        final Window window = activity.getWindow();
        final View view = window.findViewById(android.support.v7.appcompat.R.id.decor_content_parent);
        if (!(view instanceof ActionBarOverlayLayout)) return;
        try {
            final Field field = ActionBarOverlayLayout.class.getDeclaredField("mWindowContentOverlay");
            field.setAccessible(true);
            field.set(view, overlay);
        } catch (Exception ignore) {
        }
    }

    public static void setCompatContentViewOverlay(Window window, Drawable overlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        View contentLayout = window.findViewById(android.support.v7.appcompat.R.id.action_bar_activity_content);
        if (contentLayout == null) {
            contentLayout = window.findViewById(android.R.id.content);
        }
        if (contentLayout instanceof FrameLayout) {
            ViewSupport.setForeground(contentLayout, overlay);
        }
    }

    public static void setWindowOverlayViewOverlay(Activity activity, Drawable overlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        final Window window = activity.getWindow();
        final View windowOverlay = window.findViewById(R.id.window_overlay);
        if (windowOverlay == null) {
            return;
        }
        ViewSupport.setBackground(windowOverlay, overlay);
    }

    public static void setupDrawerBackground(Context context, View view) {
        if (!(context instanceof IThemedActivity)) return;
        final String backgroundOption = ((IThemedActivity) context).getThemeBackgroundOption();
        final int alpha = ((IThemedActivity) context).getCurrentThemeBackgroundAlpha();
        final Drawable d;
        if (isSolidBackground(backgroundOption)) {
            d = new ColorDrawable(!isLightTheme(context) ? Color.BLACK : Color.WHITE);
        } else {
            d = getWindowBackgroundFromTheme(context);
        }
        if (d == null) throw new NullPointerException();
        d.mutate();
        if (isTransparentBackground(backgroundOption)) {
            d.setAlpha(alpha);
        }
        ViewSupport.setBackground(view, d);
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
        final Context context = view.getContext();
        final int colorDark = ContextCompat.getColor(context, R.color.action_icon_dark);
        final int colorLight = ContextCompat.getColor(context, R.color.action_icon_light);
        wrapMenuIcon(view, colorDark, colorLight, excludeGroups);
    }


    public static int getActionIconColor(Context context) {
        final int colorDark = ContextCompat.getColor(context, R.color.action_icon_dark);
        final int colorLight = ContextCompat.getColor(context, R.color.action_icon_light);
        final int itemBackgroundColor = ThemeUtils.getThemeBackgroundColor(context);
        return TwidereColorUtils.getContrastYIQ(itemBackgroundColor, colorDark, colorLight);
    }

    public static int getActionIconColor(Context context, int backgroundColor) {
        final int colorDark = ContextCompat.getColor(context, R.color.action_icon_dark);
        final int colorLight = ContextCompat.getColor(context, R.color.action_icon_light);
        return ATEUtil.isColorLight(backgroundColor) ? colorDark : colorLight;
    }

    public static void setLightStatusBar(@NonNull Window window, boolean lightStatusBar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        final View decorView = window.getDecorView();

        final int systemUiVisibility = decorView.getSystemUiVisibility();
        if (lightStatusBar) {
            decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void wrapMenuIcon(ActionMenuView view, int colorDark, int colorLight, int... excludeGroups) {
        final Context context = view.getContext();
        final int itemBackgroundColor = ThemeUtils.getThemeBackgroundColor(context);
        final int popupItemBackgroundColor = ThemeUtils.getThemeBackgroundColor(context, view.getPopupTheme());
        final int itemColor = TwidereColorUtils.getContrastYIQ(itemBackgroundColor, colorDark, colorLight);
        final int popupItemColor = TwidereColorUtils.getContrastYIQ(popupItemBackgroundColor, colorDark, colorLight);
        final Menu menu = view.getMenu();
        final int childCount = view.getChildCount();
        for (int i = 0, j = menu.size(), k = 0; i < j; i++) {
            final MenuItem item = menu.getItem(i);
            wrapMenuItemIcon(item, itemColor, excludeGroups);
            if (item.hasSubMenu()) {
                wrapMenuIcon(item.getSubMenu(), popupItemColor, popupItemColor, excludeGroups);
            }
            if (item.isVisible()) {
                k++;
            }
        }
    }


    public static void wrapMenuItemIcon(@NonNull MenuItem item, int itemColor, int... excludeGroups) {
        if (ArrayUtils.contains(excludeGroups, item.getGroupId())) return;
        final Drawable icon = item.getIcon();
        if (icon == null || icon instanceof DoNotWrapDrawable) return;
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
                wrapMenuIcon(item.getSubMenu(), popupItemColor, popupItemColor, excludeGroups);
            }
            if (item.isVisible()) {
                k++;
            }
        }
    }

    @NonNull
    private static SharedPreferencesWrapper getSharedPreferencesWrapper(Context context) {
        final Context appContext = context.getApplicationContext();
        return SharedPreferencesWrapper.getInstance(appContext, SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    public static int getActionBarHeight(Context context) {
        final TypedValue tv = new TypedValue();
        final Resources.Theme theme = context.getTheme();
        final int attr = R.attr.actionBarSize;
        if (theme.resolveAttribute(attr, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static Context getActionBarThemedContext(Context base) {
        final TypedValue outValue = new TypedValue();
        final Resources.Theme baseTheme = base.getTheme();
        baseTheme.resolveAttribute(android.support.v7.appcompat.R.attr.actionBarTheme, outValue, true);

        if (outValue.resourceId != 0) {
            final Resources.Theme actionBarTheme = base.getResources().newTheme();
            actionBarTheme.setTo(baseTheme);
            actionBarTheme.applyStyle(outValue.resourceId, true);

            final ActionBarContextThemeWrapper actionBarContext = new ActionBarContextThemeWrapper(base, outValue.resourceId);
            actionBarContext.getTheme().setTo(actionBarTheme);
            return actionBarContext;
        } else {
            return base;
        }
    }

    public static Context getActionBarThemedContext(Context base, int accentColor) {
        final int actionBarThemeId;
        if (!isLightTheme(base) || TwidereColorUtils.getYIQLuminance(accentColor) <= ACCENT_COLOR_THRESHOLD) {
            actionBarThemeId = R.style.Theme_Twidere;
        } else {
            actionBarThemeId = R.style.Theme_Twidere;
        }
        final Resources.Theme baseTheme = base.getTheme();
        final Resources.Theme actionBarTheme = base.getResources().newTheme();
        actionBarTheme.setTo(baseTheme);
        actionBarTheme.applyStyle(actionBarThemeId, true);

        final ActionBarContextThemeWrapper actionBarContext = new ActionBarContextThemeWrapper(base, actionBarThemeId);
        actionBarContext.getTheme().setTo(actionBarTheme);
        return actionBarContext;
    }

    public static void applyToolbarItemColor(Context context, Toolbar toolbar, int toolbarColor) {
        if (toolbar == null) {
            return;
        }
        final int contrastForegroundColor = getColorDependent(toolbarColor);
        toolbar.setTitleTextColor(contrastForegroundColor);
        toolbar.setSubtitleTextColor(contrastForegroundColor);
        int popupItemColor, popupTheme = toolbar.getPopupTheme();
        if (popupTheme != 0) {
            popupItemColor = getThemeForegroundColor(context, popupTheme);
        } else {
            popupItemColor = getThemeForegroundColor(context);
        }
        final Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(contrastForegroundColor, Mode.SRC_ATOP);
            toolbar.setNavigationIcon(navigationIcon);
        }
        getThemeForegroundColor(context);
        setActionBarOverflowColor(toolbar, contrastForegroundColor);
        wrapToolbarMenuIcon(ViewSupport.findViewByType(toolbar, ActionMenuView.class),
                contrastForegroundColor, popupItemColor);
        if (toolbar instanceof TwidereToolbar) {
            ((TwidereToolbar) toolbar).setItemColor(contrastForegroundColor);
        }
    }

    public static int getLocalNightMode(SharedPreferences preferences) {
        switch (Utils.getNonEmptyString(preferences, KEY_THEME, VALUE_THEME_NAME_LIGHT)) {
            case VALUE_THEME_NAME_DARK: {
                return AppCompatDelegate.MODE_NIGHT_YES;
            }
            case VALUE_THEME_NAME_AUTO: {
                return AppCompatDelegate.MODE_NIGHT_AUTO;
            }
        }
        return AppCompatDelegate.MODE_NIGHT_NO;
    }

    public static void applyDayNight(SharedPreferences preferences, AppCompatDelegate delegate) {
        switch (getLocalNightMode(preferences)) {
            case AppCompatDelegate.MODE_NIGHT_AUTO: {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
            }
            case AppCompatDelegate.MODE_NIGHT_YES: {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            }
            default: {
                delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            }
        }
    }

    public static void fixNightMode(Resources resources, Configuration newConfig) {
        int currentNightMode = resources.getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES)
            newConfig.uiMode = (newConfig.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                    | Configuration.UI_MODE_NIGHT_YES;
    }

    public static String getATEKey(Context context) {
        TypedValue value = new TypedValue();
        if (!context.getTheme().resolveAttribute(R.attr.ateThemeKey, value, true)) {
            return "dark";
        }
        if (TextUtils.isEmpty(value.string)) return "dark";
        return String.valueOf(value.string);
    }

    public static int getColorDependent(int color) {
        final boolean isDark = !ATEUtil.isColorLight(color);
        return isDark ? Color.WHITE : Color.BLACK;
    }


    public static final class ActionBarContextThemeWrapper extends android.support.v7.view.ContextThemeWrapper {

        public ActionBarContextThemeWrapper(Context base, int themeres) {
            super(base, themeres);
        }
    }
}
