package android.support.v7.app

import android.content.Context

/**
 * Created by mariotaku on 2017/1/7.
 */

object TwilightManagerAccessor {
    fun isNight(context: Context): Boolean {
        return TwilightManager.getInstance(context).isNight
    }
}
