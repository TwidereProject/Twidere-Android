package org.mariotaku.twidere.extension

import android.graphics.BitmapFactory
import kotlin.math.max
import kotlin.math.roundToInt

fun BitmapFactory.Options.calculateInSampleSize(preferredWidth: Int, preferredHeight: Int): Int {
    if (preferredHeight > outHeight && preferredWidth > outWidth) {
        return 1
    }
    if (preferredHeight <= 0 && preferredWidth <= 0) {
        return 1
    }
    val result = (max(outWidth, outHeight) / max(preferredWidth, preferredHeight)
        .toFloat()).roundToInt()
    return max(1, result)
}