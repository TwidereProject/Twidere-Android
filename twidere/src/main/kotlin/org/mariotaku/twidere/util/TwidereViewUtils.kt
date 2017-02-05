package org.mariotaku.twidere.util

import android.support.annotation.UiThread
import android.view.View

/**
 * Created by mariotaku on 16/1/23.
 */
object TwidereViewUtils {

    @UiThread
    fun hitView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return x in (location[0] until location[0] + view.width)
                && y in (location[1] until location[1] + view.height)
    }
}
