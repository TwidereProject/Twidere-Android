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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import org.mariotaku.refreshnow.widget.RefreshNowConfig;
import org.mariotaku.refreshnow.widget.RefreshNowProgressIndicator.IndicatorConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.mariotaku.twidere.content.TwidereContextWrapper;
import org.mariotaku.twidere.content.iface.ITwidereContextWrapper;
import org.mariotaku.twidere.content.res.TwidereAccentResources;
import org.mariotaku.twidere.content.res.TwidereResources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ThemeUtils implements Constants {

	private static final int[] ANIM_OPEN_STYLE_ATTRS = { android.R.attr.activityOpenEnterAnimation,
			android.R.attr.activityOpenExitAnimation };
	private static final int[] ANIM_CLOSE_STYLE_ATTRS = { android.R.attr.activityCloseEnterAnimation,
			android.R.attr.activityCloseExitAnimation };

	private static final String[] sClassPrefixList = { "android.widget.", "android.webkit." };

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

	public static void applyBackground(final View view) {
		if (view == null) return;
		applyBackground(view, getUserThemeColor(view.getContext()));
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

	public static void applyThemeAlphaToDrawable(final Context context, final Drawable d) {
		if (context == null || d == null) return;
		d.setAlpha(getThemeAlpha(getThemeResource(context)));
	}

	public static void applyThemeBackgroundAlphaToDrawable(final Context context, final Drawable d) {
		if (context == null || d == null) return;
		d.setAlpha(getUserThemeBackgroundAlpha(context));
	}

	public static IndicatorConfig buildRefreshIndicatorConfig(final Context context) {
		final IndicatorConfig.Builder builder = new IndicatorConfig.Builder(context);
		final Resources res = context.getResources();
		final float width = 3 * res.getDisplayMetrics().density;
		final int themeColor = getUserThemeColor(context);
		builder.progressColor(themeColor);
		builder.indeterminateColor(themeColor);
		builder.progressStrokeWidth(width);
		builder.indeterminateStrokeWidth(width);
		return builder.build();
	}

	public static RefreshNowConfig buildRefreshNowConfig(final Context context) {
		final RefreshNowConfig.Builder builder = new RefreshNowConfig.Builder(context);
		return builder.build();
	}

	public static View createView(final String name, final Context context, final AttributeSet attrs) {
		try {
			return newViewInstance(name, context, attrs);
		} catch (final Exception e) {
			// In this case we want to let the base class take a crack
			// at it.
		}
		for (final String prefix : sClassPrefixList) {
			try {
				return newViewInstance(prefix + name, context, attrs);
			} catch (final Exception e) {
				// In this case we want to let the base class take a crack
				// at it.
			}
		}
		return null;
	}

	public static Resources getAccentResourcesForActionIcons(final Context baseContext, final int themeRes,
			final int accentColor) {
		return new TwidereAccentResources(baseContext, baseContext.getResources(), themeRes, accentColor);
	}

	@Deprecated
	public static Drawable getActionBarBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.background },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, applyAlpha);
	}

	public static Drawable getActionBarBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.background },
				android.R.attr.actionBarStyle, themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
	}

	public static Context getActionBarContext(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.actionBarWidgetTheme });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) return new TwidereContextWrapper(context);
		return new TwidereContextThemeWrapper(context, resId, getUserThemeColor(context));
	}

	@Deprecated
	public static Drawable getActionBarSplitBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, applyAlpha);
	}

	public static Drawable getActionBarSplitBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
				android.R.attr.actionBarStyle, themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
	}

	@Deprecated
	public static Drawable getActionBarStackedBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundStacked },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, applyAlpha);
	}

	public static Drawable getActionBarStackedBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundStacked },
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
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_ActionBar_Colored_Light:
			case R.style.Theme_Twidere_Settings_Light:
			case R.style.Theme_Twidere_Light_DarkActionBar_DarkIcon:
			case R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground_DarkIcon:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent_DarkIcon:
			case R.style.Theme_Twidere_Light_DarkActionBar_Compose_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose_DarkIcon:
			case R.style.Theme_Twidere_Settings_Light_DarkActionBar_DarkIcon:
				return 0x99333333;
		}
		return 0xCCFFFFFF;
	}

	public static Drawable getCardItemBackground(final Context context) {

		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.cardItemBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return d;
	}

	public static Drawable getCardItemMenuOverflowButtonDrawable(final Context context) {
		final Resources res = getResources(context);
		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.cardOverflowIcon });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null) return res.getDrawable(R.drawable.ic_menu_moreoverflow_card_light);
		return d;
	}

	public static int getColorBackgroundCacheHint(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.colorBackgroundCacheHint });
		final int color = a.getColor(0, Color.TRANSPARENT);
		a.recycle();
		return color;
	}

	public static int getComposeThemeResource(final Context context) {
		return getComposeThemeResource(getThemeNameOption(context), getDarkActionBarOption(context));
	}

	public static int getComposeThemeResource(final String name, final boolean darkActionBar) {
		if (VALUE_THEME_NAME_TWIDERE.equals(name))
			return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar_Compose
					: R.style.Theme_Twidere_Colored_Compose;
		else if (VALUE_THEME_NAME_LIGHT.equals(name))
			return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar_Compose
					: R.style.Theme_Twidere_Light_Compose;
		else if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Compose;
		return R.style.Theme_Twidere_Colored_Compose;
	}

	public static boolean getDarkActionBarOption(final Context context) {
		if (context == null) return true;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref == null || pref.getBoolean(KEY_THEME_DARK_ACTIONBAR, true);
	}

	public static Context getDialogThemedContext(final Context context) {
		return new TwidereContextThemeWrapper(context, getDialogThemeResource(context), getThemeColor(context));
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
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_Transparent:
				return R.style.Theme_Twidere_Drawer_Dark_Transparent;
		}
		return R.style.Theme_Twidere_Drawer_Dark;
	}

	public static Drawable getImageHighlightDrawable(final Context context) {
		final Drawable d = getSelectableItemBackgroundDrawable(context);
		if (d != null) {
			d.setAlpha(0x80);
		}
		return d;
	}

	public static Drawable getListMenuOverflowButtonDrawable(final Context context) {
		final Resources res = getResources(context);
		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.listMenuOverflowButton });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null) return res.getDrawable(R.drawable.ic_list_menu_moreoverflow_normal_holo_light);
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
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_ActionBar_Colored_Light:
			case R.style.Theme_Twidere_Settings_Light:
			case R.style.Theme_Twidere_Light_DarkActionBar_DarkIcon:
			case R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground_DarkIcon:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent_DarkIcon:
			case R.style.Theme_Twidere_Light_DarkActionBar_Compose_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent_DarkIcon:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose_DarkIcon:
			case R.style.Theme_Twidere_Settings_Light_DarkActionBar_DarkIcon:
				return 0x99333333;
		}
		return 0xCCFFFFFF;
	}

	public static int getNoDisplayThemeResource(final Context context) {
		if (context == null) return R.style.Theme_Twidere_Dark_NoDisplay;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String theme = pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE);
		if (VALUE_THEME_NAME_DARK.equals(theme)) return R.style.Theme_Twidere_Dark_NoDisplay;
		return R.style.Theme_Twidere_Light_NoDisplay;
	}

	public static Resources getResources(final Context context) {
		if (context instanceof IThemedActivity) {
			final Resources defRes = ((IThemedActivity) context).getDefaultResources();
			return defRes;
		}
		return context.getResources();
	}

	public static Resources getResourcesForActionIcons(final Context baseContext, final int themeRes) {
		return new TwidereResources(baseContext, baseContext.getResources(), themeRes);
	}

	public static Drawable getSelectableItemBackgroundDrawable(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground });
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
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textAppearanceLarge });
		final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo_Large);
		a.recycle();
		return textAppearance;
	}

	public static int getTextColorPrimary(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textColorPrimary });
		try {
			return a.getColor(0, Color.TRANSPARENT);
		} finally {
			a.recycle();
		}
	}

	public static int getTextColorSecondary(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
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
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
				return 0xa0;
		}
		return 0xff;
	}

	public static String getThemeBackgroundOption(final Context context) {
		if (context == null) return VALUE_THEME_BACKGROUND_DEFAULT;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (pref == null) return VALUE_THEME_BACKGROUND_DEFAULT;
		return pref.getString(KEY_THEME_BACKGROUND, VALUE_THEME_BACKGROUND_DEFAULT);
	}

	public static int getThemeColor(final Context context) {
		final Resources res = getResources(context);
		final Context wrapped = getThemedContext(context, res);
		final TypedArray a = wrapped.obtainStyledAttributes(new int[] { android.R.attr.colorActivatedHighlight });
		try {
			return a.getColor(0, res.getColor(android.R.color.holo_blue_light));
		} finally {
			a.recycle();
		}
	}

	public static Context getThemedContext(final Context context) {
		return new TwidereContextWrapper(context, getResources(context));
	}

	public static Context getThemedContext(final Context context, final Resources res) {
		return new TwidereContextWrapper(context, res);
	}

	public static Context getThemedContextForActionIcons(final Context context) {
		final int themeRes, accentColor;
		if (context instanceof IThemedActivity) {
			themeRes = ((IThemedActivity) context).getThemeResourceId();
			accentColor = ((IThemedActivity) context).getThemeColor();
		} else {
			themeRes = getSettingsThemeResource(context);
			accentColor = getUserThemeColor(context);
		}
		return new TwidereContextThemeWrapper(context, getThemeResActionIcons(themeRes), accentColor);
	}

	public static Context getThemedContextForActionIcons(final Context baseContext, final int baseThemeRes) {
		return new TwidereContextWrapper(baseContext, getThemeResActionIcons(baseThemeRes));
	}

	public static Context getThemedContextForActionIcons(final Context baseContext, final int baseThemeRes,
			final int accentColor) {
		return new TwidereContextThemeWrapper(baseContext, getThemeResActionIcons(baseThemeRes), accentColor);
	}

	public static LayoutInflater getThemedLayoutInflaterForActionIcons(final Context context) {
		final int themeRes, accentColor;
		if (context instanceof IThemedActivity) {
			themeRes = ((IThemedActivity) context).getThemeResourceId();
			accentColor = ((IThemedActivity) context).getThemeColor();
		} else {
			themeRes = getSettingsThemeResource(context);
			accentColor = getUserThemeColor(context);
		}
		final Context theme = getThemedContextForActionIcons(context, themeRes, accentColor);
		return LayoutInflater.from(theme);
	}

	public static String getThemeFontFamily(final Context context) {
		if (context == null) return VALUE_THEME_FONT_FAMILY_REGULAR;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String fontFamily = pref.getString(KEY_THEME_FONT_FAMILY, VALUE_THEME_FONT_FAMILY_REGULAR);
		if (!TextUtils.isEmpty(fontFamily)) return fontFamily;
		return VALUE_THEME_FONT_FAMILY_REGULAR;
	}

	public static int getThemeForegroundColor(final Context context) {
		final Resources res = getResources(context);
		final Context wrapped = getThemedContext(context, res);
		final TypedArray a = wrapped.obtainStyledAttributes(new int[] { android.R.attr.colorForeground });
		try {
			return a.getColor(0, Color.GRAY);
		} finally {
			a.recycle();
		}
	}

	public static String getThemeNameOption(final Context context) {
		if (context == null) return VALUE_THEME_NAME_TWIDERE;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref != null ? pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE) : VALUE_THEME_NAME_TWIDERE;
	}

	public static int getThemeResActionIcons(final int baseThemeRes) {
		switch (baseThemeRes) {
			case R.style.Theme_Twidere_Light_DarkActionBar: {
				return R.style.Theme_Twidere_Light_DarkActionBar_DarkIcon;
			}
			case R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground: {
				return R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground_DarkIcon;
			}
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent: {
				return R.style.Theme_Twidere_Light_DarkActionBar_Transparent_DarkIcon;
			}
			case R.style.Theme_Twidere_Light_DarkActionBar_Compose: {
				return R.style.Theme_Twidere_Light_DarkActionBar_Compose_DarkIcon;
			}
			case R.style.Theme_Twidere_Colored_DarkActionBar: {
				return R.style.Theme_Twidere_Colored_DarkActionBar_DarkIcon;
			}
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground: {
				return R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground_DarkIcon;
			}
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent: {
				return R.style.Theme_Twidere_Colored_DarkActionBar_Transparent_DarkIcon;
			}
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose: {
				return R.style.Theme_Twidere_Colored_DarkActionBar_Compose_DarkIcon;
			}
			case R.style.Theme_Twidere_Settings_Light_DarkActionBar: {
				return R.style.Theme_Twidere_Settings_Light_DarkActionBar_DarkIcon;
			}
		}
		return baseThemeRes;
	}

	public static int getThemeResource(final Context context) {
		return getThemeResource(getThemeNameOption(context), getThemeBackgroundOption(context),
				getDarkActionBarOption(context));
	}

	public static int getThemeResource(final String name, final String background, final boolean darkActionBar) {
		if (VALUE_THEME_NAME_TWIDERE.equals(name)) {
			if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground
						: R.style.Theme_Twidere_Colored_SolidBackground;
			else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar_Transparent
						: R.style.Theme_Twidere_Colored_Transparent;
			return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar : R.style.Theme_Twidere_Colored;
		} else if (VALUE_THEME_NAME_LIGHT.equals(name)) {
			if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground
						: R.style.Theme_Twidere_Light_SolidBackground;
			else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar_Transparent
						: R.style.Theme_Twidere_Light_Transparent;
			return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar : R.style.Theme_Twidere_Light;

		} else if (VALUE_THEME_NAME_DARK.equals(name)) {
			if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
				return R.style.Theme_Twidere_Dark_SolidBackground;
			else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
				return R.style.Theme_Twidere_Dark_Transparent;
			return R.style.Theme_Twidere_Dark;
		}
		return R.style.Theme_Twidere_Colored_DarkActionBar;
	}

	public static int getTitleTextAppearance(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.titleTextStyle },
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
		final int themeColor = getUserThemeColor(context);
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
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA);
	}

	public static int getUserThemeColor(final Context context) {
		if (context == null) return Color.TRANSPARENT;
		final Resources res = getResources(context);
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int def = res.getColor(android.R.color.holo_blue_light);
		return pref != null ? pref.getInt(KEY_THEME_COLOR, def) : def;
	}

	public static Typeface getUserTypeface(final Context context, final Typeface defTypeface) {
		if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return Typeface.DEFAULT;
		final int fontStyle = defTypeface != null ? defTypeface.getStyle() : Typeface.NORMAL;
		final String fontFamily = getThemeFontFamily(context);
		final Typeface tf = Typeface.create(fontFamily, fontStyle);
		if (tf != null) return tf;
		return Typeface.create(Typeface.DEFAULT, fontStyle);
	}

	public static int getViewerThemeResource(final Context context) {
		final boolean isTransparent = VALUE_THEME_BACKGROUND_TRANSPARENT.equals(getThemeBackgroundOption(context));
		return isTransparent ? R.style.Theme_Twidere_Viewer_Transparent : R.style.Theme_Twidere_Viewer;
	}

	public static Drawable getWindowBackground(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (isTransparentBackground(context)) {
			applyThemeBackgroundAlphaToDrawable(context, d);
		}
		return d;
	}

	public static Drawable getWindowBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.windowBackground }, 0,
				themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (isTransparentBackground(themeRes)) {
			applyThemeBackgroundAlphaToDrawable(context, d);
		}
		return d;
	}

	public static Drawable getWindowContentOverlay(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowContentOverlay });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return d;
	}

	public static Drawable getWindowContentOverlayForCompose(final Context context) {
		final int themeRes = getThemeResource(context);
		return getWindowContentOverlay(new ContextThemeWrapper(context, themeRes));
	}

	public static boolean isColoredActionBar(final Context context) {
		return isColoredActionBar(getThemeResource(context));
	}

	public static boolean isColoredActionBar(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar:
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose:
				return true;
		}
		return false;
	}

	public static boolean isDarkDrawerEnabled(final Context context) {
		final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		if (prefs == null) return false;
		return prefs.getBoolean(KEY_DARK_DRAWER, true);
	}

	public static boolean isDarkTheme(final Context context) {
		if (context instanceof ITwidereContextWrapper)
			return isDarkTheme(((ITwidereContextWrapper) context).getThemeResourceId());
		return isDarkTheme(getThemeResource(context));
	}

	public static boolean isDarkTheme(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Dark:
			case R.style.Theme_Twidere_Dark_SolidBackground:
			case R.style.Theme_Twidere_Dark_Dialog:
			case R.style.Theme_Twidere_Dark_Compose:
			case R.style.Theme_Twidere_Dark_Transparent:
				return true;
		}
		return false;
	}

	public static boolean isFloatingWindow(final Context context) {

		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowIsFloating });
		final boolean b = a.getBoolean(0, false);
		a.recycle();
		return b;
	}

	public static boolean isLightActionBar(final Context context) {
		return isLightActionBar(getThemeResource(context));
	}

	public static boolean isLightActionBar(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Light:
			case R.style.Theme_Twidere_Light_SolidBackground:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_Compose:
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_Settings_Light:
				return true;
		}
		return false;
	}

	public static boolean isSolidBackground(final Context context) {
		return VALUE_THEME_BACKGROUND_SOLID.equals(getThemeBackgroundOption(context));
	}

	public static boolean isTransparentBackground(final Context context) {
		return isTransparentBackground(getThemeResource(context));
	}

	public static boolean isTransparentBackground(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent_DarkIcon:
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent_DarkIcon:
			case R.style.Theme_Twidere_Viewer_Transparent:
				return true;
		}
		return false;
	}

	public static void notifyStatusBarColorChanged(final Context context, final int themeResource,
			final int accentColor, final int backgroundAlpha) {
		final Intent intent = new Intent("com.mohammadag.colouredstatusbar.ChangeStatusBarColor");
		if (isColoredActionBar(themeResource)) {
			intent.putExtra("status_bar_color", backgroundAlpha << 24 | accentColor);
		} else {
			if (isLightActionBar(themeResource)) {
				intent.putExtra("status_bar_color", backgroundAlpha << 24 | 0xFFDDDDDD);
			} else {
				intent.putExtra("status_bar_color", backgroundAlpha << 24 | 0xFF222222);
			}
		}
		if (isLightActionBar(themeResource)) {
			intent.putExtra("status_bar_icons_color", Color.DKGRAY);
		} else {
			intent.putExtra("status_bar_icons_color", Color.WHITE);
		}
		// Please note that these are not yet implemented!!!
		// You're free to include them in your code so that when they
		// are implemented, your app will work out of the box.
		intent.putExtra("navigation_bar_color", Color.BLACK);
		intent.putExtra("navigation_bar_icon_color", Color.WHITE);
		context.sendOrderedBroadcast(intent, null);
	}

	public static void overrideActivityCloseAnimation(final Activity activity) {
		TypedArray a = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
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

		TypedArray a = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
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

		final TypedArray a = activity
				.obtainStyledAttributes(android.R.style.Animation_Activity, ANIM_CLOSE_STYLE_ATTRS);
		final int activityCloseEnterAnimation = a.getResourceId(0, 0);
		final int activityCloseExitAnimation = a.getResourceId(1, 0);
		a.recycle();
		activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}

	public static boolean shouldApplyColorFilter(final Context context) {
		return shouldApplyColorFilter(getThemeResource(context));
	}

	public static boolean shouldApplyColorFilter(final int res) {
		switch (res) {
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar:
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
				return false;
		}
		return true;
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
				final int color = getUserThemeColor(context);
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

}
