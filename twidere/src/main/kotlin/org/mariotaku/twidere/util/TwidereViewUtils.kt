package org.mariotaku.twidere.util

import android.graphics.RectF
import androidx.annotation.UiThread
import android.view.MotionEvent
import android.view.View

/**
 * Created by mariotaku on 16/1/23.
 */
object TwidereViewUtils {

    private val location = IntArray(2)
    private val rect = RectF()

    @UiThread
    fun hitView(event: MotionEvent, view: View): Boolean {
        view.getLocationOnScreen(location)
        rect.set(location[0].toFloat(), location[1].toFloat(), location[0].toFloat() + view.width,
                location[1].toFloat() + view.height)
        return rect.contains(event.rawX, event.rawY)
    }
}
