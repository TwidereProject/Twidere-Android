/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.appcompat.app.TwilightManagerAccessor
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.TintTypedArray
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TwidereToolbar
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_THEME_BACKGROUND_SOLID
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_THEME_BACKGROUND_TRANSPARENT
import org.mariotaku.twidere.constant.themeBackgroundAlphaKey
import org.mariotaku.twidere.constant.themeBackgroundOptionKey
import org.mariotaku.twidere.constant.themeColorKey
import org.mariotaku.twidere.graphic.ActionIconDrawable
import org.mariotaku.twidere.graphic.WindowBackgroundDrawable
import org.mariotaku.twidere.graphic.iface.DoNotWrapDrawable
import org.mariotaku.twidere.preference.ThemeBackgroundPreference.MAX_ALPHA
import org.mariotaku.twidere.preference.ThemeBackgroundPreference.MIN_ALPHA
import org.mariotaku.twidere.util.menu.TwidereMenuInfo
import org.mariotaku.twidere.util.support.ViewSupport

object ThemeUtils {

    const val ACCENT_COLOR_THRESHOLD = 192
    const val DARK_COLOR_THRESHOLD = 128

    fun getUserTheme(context: Context, preferences: SharedPreferences): Chameleon.Theme {
        val theme = Chameleon.Theme.from(context)
        val userColor = getUserAccentColor(context, preferences)
        theme.colorAccent = userColor
        theme.colorPrimary = userColor
        val backgroundOption = preferences[themeBackgroundOptionKey]
        if (theme.isToolbarColored) {
            theme.colorToolbar = theme.colorPrimary
        } else if (backgroundOption == VALUE_THEME_BACKGROUND_SOLID) {
            theme.colorToolbar = if (isLightTheme(context)) {
                Color.WHITE
            } else {
                Color.BLACK
            }
        }

        if (isTransparentBackground(backgroundOption)) {
            theme.colorToolbar = ColorUtils.setAlphaComponent(theme.colorToolbar,
                    getActionBarAlpha(preferences[themeBackgroundAlphaKey]))
        }
        theme.statusBarColor = ChameleonUtils.darkenColor(theme.colorToolbar)
        theme.lightStatusBarMode = Chameleon.Theme.LightStatusBarMode.AUTO
        theme.textColorLink = getOptimalAccentColor(theme.colorAccent, theme.colorForeground)

        return theme
    }

    @StyleRes
    fun getCurrentThemeResource(context: Context, @StyleRes lightTheme: Int, @StyleRes darkTheme: Int): Int {
        if (TwilightManagerAccessor.isNight(context)) return darkTheme
        return lightTheme
    }

    fun getTextColorPrimary(context: Context): Int {
        return getColorFromAttribute(context, android.R.attr.textColorPrimary)
    }

    fun getTextColorSecondary(context: Context): Int {
        return getColorFromAttribute(context, android.R.attr.textColorSecondary)
    }

    fun getColorBackground(context: Context, styleRes: Int = 0): Int {
        return getColorFromAttribute(context, android.R.attr.colorBackground, styleRes)
    }

    fun getColorForeground(context: Context, styleRes: Int = 0): Int {
        return getColorFromAttribute(context, android.R.attr.colorForeground, styleRes)
    }


    fun getCardBackgroundColor(context: Context, backgroundOption: String, themeAlpha: Int): Int {
        val color = getColorFromAttribute(context, R.attr.cardItemBackgroundColor)
        return when {
            isTransparentBackground(backgroundOption) -> {
                ColorUtils.setAlphaComponent(color, themeAlpha)
            }
            isSolidBackground(backgroundOption) -> {
                TwidereColorUtils.getContrastYIQ(color, Color.WHITE, Color.BLACK)
            }
            else -> {
                color
            }
        }
    }

    fun isLightColor(color: Int): Boolean {
        return ChameleonUtils.isColorLight(color)
    }

    fun getColorDependent(color: Int): Int {
        return ChameleonUtils.getColorDependent(color)
    }

    fun isSolidBackground(option: String): Boolean {
        return VALUE_THEME_BACKGROUND_SOLID == option
    }

    fun isWindowFloating(context: Context): Boolean {
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.windowIsFloating))
        try {
            return a.getBoolean(0, false)
        } finally {
            a.recycle()
        }
    }

    fun isTransparentBackground(option: String): Boolean {
        return VALUE_THEME_BACKGROUND_TRANSPARENT == option
    }

    fun getColorBackground(context: Context, backgroundOption: String, alpha: Int): Int {
        return if (isWindowFloating(context)) {
            getColorBackground(context)
        } else if (backgroundOption == VALUE_THEME_BACKGROUND_TRANSPARENT) {
            ColorUtils.setAlphaComponent(getColorBackground(context), alpha)
        } else if (backgroundOption == VALUE_THEME_BACKGROUND_SOLID) {
            if (isLightTheme(context)) Color.WHITE else Color.BLACK
        } else {
            getColorBackground(context)
        }
    }

    fun applyWindowBackground(context: Context, window: Window, backgroundOption: String, alpha: Int) {
        when {
            isWindowFloating(context) -> {
                window.setBackgroundDrawable(getWindowBackground(context))
            }
            VALUE_THEME_BACKGROUND_TRANSPARENT == backgroundOption -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                window.setBackgroundDrawable(getWindowBackgroundFromThemeApplyAlpha(context, alpha))
            }
            VALUE_THEME_BACKGROUND_SOLID == backgroundOption -> {
                window.setBackgroundDrawable(ColorDrawable(if (isLightTheme(context)) Color.WHITE else Color.BLACK))
            }
            else -> {
                window.setBackgroundDrawable(getWindowBackground(context))
            }
        }
    }


    fun wrapMenuIcon(menu: Menu, itemColor: Int, subItemColor: Int, vararg excludeGroups: Int) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            wrapMenuItemIcon(item, itemColor, *excludeGroups)
            if (item.hasSubMenu()) {
                wrapMenuIcon(item.subMenu, subItemColor, subItemColor, *excludeGroups)
            }
        }
    }

    fun wrapMenuIcon(view: ActionMenuView,
            colorDark: Int = ContextCompat.getColor(view.context, R.color.action_icon_dark),
            colorLight: Int = ContextCompat.getColor(view.context, R.color.action_icon_light),
            vararg excludeGroups: Int) {
        val context = view.context
        val itemBackgroundColor = getColorBackground(context)
        val popupItemBackgroundColor = getColorBackground(context, view.popupTheme)
        val itemColor = TwidereColorUtils.getContrastYIQ(itemBackgroundColor, colorDark, colorLight)
        val popupItemColor = TwidereColorUtils.getContrastYIQ(popupItemBackgroundColor, colorDark, colorLight)
        val menu = view.menu
        var k = 0
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            wrapMenuItemIcon(item, itemColor, *excludeGroups)
            if (item.hasSubMenu()) {
                wrapMenuIcon(item.subMenu, popupItemColor, popupItemColor, *excludeGroups)
            }
            if (item.isVisible) {
                k++
            }
        }
    }


    fun wrapToolbarMenuIcon(view: ActionMenuView, itemColor: Int, popupItemColor: Int, vararg excludeGroups: Int) {
        val menu = view.menu
        var k = 0
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            wrapMenuItemIcon(item, itemColor, *excludeGroups)
            if (item.hasSubMenu()) {
                wrapMenuIcon(item.subMenu, popupItemColor, popupItemColor, *excludeGroups)
            }
            if (item.isVisible) {
                k++
            }
        }
    }

    fun wrapMenuItemIcon(item: MenuItem, itemColor: Int, vararg excludeGroups: Int) {
        if (item.groupId in excludeGroups) return
        val icon = item.icon?.takeUnless { it is DoNotWrapDrawable } ?: return
        if (icon is ActionIconDrawable) {
            icon.defaultColor = itemColor
            item.icon = icon
            return
        }
        icon.mutate()
        val callback = icon.callback
        val newIcon = ActionIconDrawable(icon, itemColor)
        newIcon.callback = callback
        item.icon = newIcon
    }

    fun getActionIconColor(context: Context): Int {
        val itemBackgroundColor = getColorBackground(context)
        return getActionIconColor(context, itemBackgroundColor)
    }

    fun getActionIconColor(context: Context, backgroundColor: Int): Int {
        val colorDark = ContextCompat.getColor(context, R.color.action_icon_dark)
        val colorLight = ContextCompat.getColor(context, R.color.action_icon_light)
        return if (isLightColor(backgroundColor)) colorDark else colorLight
    }

    fun getSelectableItemBackgroundDrawable(context: Context): Drawable? {
        return getDrawableFromThemeAttribute(context, android.R.attr.selectableItemBackground)
    }

    fun getImageHighlightDrawable(context: Context): Drawable? {
        return getSelectableItemBackgroundDrawable(context)?.apply {
            alpha = 0x80
        }
    }

    fun isLightTheme(context: Context): Boolean {
        val a = context.obtainStyledAttributes(intArrayOf(R.attr.isLightTheme))
        try {
            return a.getBoolean(0, false)
        } finally {
            a.recycle()
        }
    }

    fun getWindowBackgroundFromThemeApplyAlpha(context: Context, alpha: Int): Drawable {
        var backgroundColor: Int
        val d = getWindowBackground(context)
        backgroundColor = if (d is ColorDrawable) {
            d.color
        } else {
            getColorBackground(context)
        }
        backgroundColor = ColorUtils.setAlphaComponent(backgroundColor,
                alpha.coerceIn(MIN_ALPHA..MAX_ALPHA))
        return WindowBackgroundDrawable(backgroundColor)
    }

    fun getWindowBackground(context: Context): Drawable? {
        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        try {
            return a.getDrawable(0)
        } finally {
            a.recycle()
        }
    }

    fun getThemeForegroundColor(context: Context): Int {
        return getThemeForegroundColor(context, 0)
    }

    fun getThemeForegroundColor(context: Context, themeRes: Int): Int {
        val value = TypedValue()
        val theme: Resources.Theme
        if (themeRes != 0) {
            theme = context.resources.newTheme()
            theme.applyStyle(themeRes, false)
        } else {
            theme = context.theme
        }
        if (!theme.resolveAttribute(android.R.attr.colorForeground, value, true)) {
            return 0
        }
        if (value.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            return value.data
        }
        return 0
    }


    fun getActionBarAlpha(alpha: Int): Int {
        val normalizedAlpha = alpha.coerceIn(0, 0xFF)
        val delta = MAX_ALPHA - normalizedAlpha
        return (MAX_ALPHA - delta / 2).coerceIn(MIN_ALPHA, MAX_ALPHA)
    }

    fun getActionBarHeight(context: Context): Int {
        val tv = TypedValue()
        val theme = context.theme
        val attr = R.attr.actionBarSize
        if (theme.resolveAttribute(attr, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return 0
    }

    fun getContrastColor(color: Int, darkColor: Int, lightColor: Int): Int {
        if (TwidereColorUtils.getYIQLuminance(color) <= ACCENT_COLOR_THRESHOLD) {
            //return light text color
            return lightColor
        }
        //return dark text color
        return darkColor
    }

    fun resetCheatSheet(menuView: ActionMenuView) {
        val listener = View.OnLongClickListener { v ->
            if ((v as ActionMenuItemView).hasText()) return@OnLongClickListener false
            val menuItem = v.itemData
            Utils.showMenuItemToast(v, menuItem.title, true)
            return@OnLongClickListener true
        }
        (0 until menuView.childCount).forEach { i ->
            val child = menuView.getChildAt(i) as? ActionMenuItemView ?: return@forEach
            if (child.itemData.hasSubMenu()) return@forEach
            child.setOnLongClickListener(listener)
        }
    }

    fun getOptimalAccentColor(accentColor: Int, foregroundColor: Int): Int {
        val yiq = IntArray(3)
        TwidereColorUtils.colorToYIQ(foregroundColor, yiq)
        val foregroundColorY = yiq[0]
        TwidereColorUtils.colorToYIQ(accentColor, yiq)
        if (foregroundColorY < DARK_COLOR_THRESHOLD && yiq[0] <= ACCENT_COLOR_THRESHOLD) {
            return accentColor
        } else if (foregroundColorY > ACCENT_COLOR_THRESHOLD && yiq[0] > DARK_COLOR_THRESHOLD) {
            return accentColor
        }
        yiq[0] = yiq[0] + (foregroundColorY - yiq[0]) / 2
        return TwidereColorUtils.YIQToColor(Color.alpha(accentColor), yiq)
    }

    fun setCompatContentViewOverlay(window: Window, overlay: Drawable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return
        val contentLayout = window.findViewById<FrameLayout>(com.google.android.material.R.id.action_bar_activity_content)
                ?: window.findViewById<FrameLayout>(android.R.id.content) ?: return
        ViewSupport.setForeground(contentLayout, overlay)
    }

    fun getSupportActionBarElevation(context: Context): Float {
        val a = context.obtainStyledAttributes(null, intArrayOf(R.attr.elevation), R.attr.actionBarStyle, 0)
        try {
            return a.getDimension(0, 0f)
        } finally {
            a.recycle()
        }
    }

    fun setActionBarOverflowColor(toolbar: Toolbar, itemColor: Int) {
        if (toolbar is TwidereToolbar) {
            toolbar.setItemColor(itemColor)
        }
        val overflowIcon = toolbar.overflowIcon
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(itemColor, PorterDuff.Mode.SRC_ATOP)
            toolbar.overflowIcon = overflowIcon
        }
    }

    fun applyColorFilterToMenuIcon(menu: Menu, @ColorInt color: Int,
            @ColorInt popupColor: Int, @ColorInt highlightColor: Int, mode: PorterDuff.Mode,
            vararg excludedGroups: Int) {
        var i = 0
        val j = menu.size()
        while (i < j) {
            val item = menu.getItem(i)
            val icon = item.icon
            val info = item.menuInfo
            if (icon != null && item.groupId !in excludedGroups) {
                icon.mutate()
                if (info is TwidereMenuInfo) {
                    val stateColor = if (info.isHighlight) info.getHighlightColor(highlightColor) else color
                    if (stateColor != 0) {
                        icon.setColorFilter(stateColor, mode)
                    }
                } else if (color != 0) {
                    icon.setColorFilter(color, mode)
                }
            }
            if (item.hasSubMenu()) {
                // SubMenu item is always in popup
                applyColorFilterToMenuIcon(item.subMenu, popupColor, popupColor, highlightColor, mode, *excludedGroups)
            }
            i++
        }
    }

    fun applyToolbarItemColor(context: Context, toolbar: Toolbar, toolbarColor: Int) {
        val contrastForegroundColor = getColorDependent(toolbarColor)
        toolbar.setTitleTextColor(contrastForegroundColor)
        toolbar.setSubtitleTextColor(contrastForegroundColor)
        val popupItemColor: Int
        val popupTheme = toolbar.popupTheme
        popupItemColor = if (popupTheme != 0) {
            getThemeForegroundColor(context, popupTheme)
        } else {
            getThemeForegroundColor(context)
        }
        val navigationIcon = toolbar.navigationIcon
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(contrastForegroundColor, PorterDuff.Mode.SRC_ATOP)
            toolbar.navigationIcon = navigationIcon
        }
        getThemeForegroundColor(context)
        setActionBarOverflowColor(toolbar, contrastForegroundColor)
        wrapToolbarMenuIcon(ViewSupport.findViewByType(toolbar, ActionMenuView::class.java),
                contrastForegroundColor, popupItemColor)
        if (toolbar is TwidereToolbar) {
            toolbar.setItemColor(contrastForegroundColor)
        }
    }

    fun getColorFromAttribute(context: Context, @AttrRes attr: Int, styleRes: Int = 0, def: Int = 0): Int {
        val a = context.obtainStyledAttributes(null, intArrayOf(attr), 0, styleRes)
        try {
            return a.getColor(0, def)
        } finally {
            a.recycle()
        }
    }

    fun getColorStateListFromAttribute(context: Context, @AttrRes attr: Int, styleRes: Int = 0): ColorStateList? {
        val a = context.obtainStyledAttributes(null, intArrayOf(attr), 0, styleRes)
        try {
            return a.getColorStateList(0)
        } finally {
            a.recycle()
        }
    }

    fun getBooleanFromAttribute(context: Context, @AttrRes attr: Int, styleRes: Int = 0, def: Boolean = false): Boolean {
        val a = context.obtainStyledAttributes(null, intArrayOf(attr), 0, styleRes)
        try {
            return a.getBoolean(0, def)
        } finally {
            a.recycle()
        }
    }

    fun getDrawableFromThemeAttribute(context: Context, @AttrRes attr: Int): Drawable {
        val a = TintTypedArray.obtainStyledAttributes(context, null, intArrayOf(attr))
        try {
            return a.getDrawable(0)
        } finally {
            a.recycle()
        }
    }

    private fun getUserAccentColor(context: Context, preferences: SharedPreferences): Int {
        val color = preferences[themeColorKey]
        if (color == 0) return ContextCompat.getColor(context, R.color.branding_color)
        return color
    }

}
