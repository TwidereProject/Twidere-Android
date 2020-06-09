package org.mariotaku.twidere.util.theme

import android.content.Context
import android.content.res.TypedArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_THEME_NAME_AUTO
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_THEME_NAME_DARK
import org.mariotaku.twidere.util.ThemeUtils

/**
 * Created by mariotaku on 2017/1/7.
 */

fun getCurrentThemeResource(context: Context, theme: String, fromThemeResource: Int = 0): Int {
    val a: TypedArray = if (fromThemeResource == 0) {
        context.obtainStyledAttributes(R.styleable.TwidereTheme)
    } else {
        context.obtainStyledAttributes(fromThemeResource, R.styleable.TwidereTheme)
    }
    try {
        val lightTheme = a.getResourceId(R.styleable.TwidereTheme_lightThemeResource, 0)
        val darkTheme = a.getResourceId(R.styleable.TwidereTheme_darkThemeResource, 0)
        if (lightTheme == 0 || darkTheme == 0) return 0
        return when (theme) {
            VALUE_THEME_NAME_AUTO -> ThemeUtils.getCurrentThemeResource(context, lightTheme, darkTheme)
            VALUE_THEME_NAME_DARK -> darkTheme
            else -> lightTheme
        }
    } finally {
        a.recycle()
    }
}