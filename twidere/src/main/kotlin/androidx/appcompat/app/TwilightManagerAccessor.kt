package androidx.appcompat.app

import android.content.Context
import android.content.res.Configuration

/**
 * Created by mariotaku on 2017/1/7.
 * Edited by 0x416c6578 on 2021/8/3
 */

object TwilightManagerAccessor {
    fun isNight(context: Context): Boolean {
        //From https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#kotlin
        val currentNightMode: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> { return false }
            Configuration.UI_MODE_NIGHT_YES -> { return true }
        }
        return false;
    }

    fun getNightState(context: Context): Int {
        return if (isNight(context)) NIGHT else DAY
    }

    const val UNSPECIFIED = 0
    const val DAY = 1
    const val NIGHT = 2
}
