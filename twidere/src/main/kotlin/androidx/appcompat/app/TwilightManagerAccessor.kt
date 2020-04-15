package androidx.appcompat.app

import android.content.Context

/**
 * Created by mariotaku on 2017/1/7.
 */

object TwilightManagerAccessor {
    fun isNight(context: Context): Boolean {
        return TwilightManager.getInstance(context).isNight
    }

    fun getNightState(context: Context): Int {
        return if (isNight(context)) NIGHT else DAY
    }

    const val UNSPECIFIED = 0
    const val DAY = 1
    const val NIGHT = 2
}
