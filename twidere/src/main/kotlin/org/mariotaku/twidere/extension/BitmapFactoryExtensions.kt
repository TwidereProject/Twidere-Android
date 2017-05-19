package org.mariotaku.twidere.extension

import android.graphics.BitmapFactory

fun BitmapFactory.Options.calculateInSampleSize(preferredWidth: Int, preferredHeight: Int): Int {
    if (preferredHeight > outHeight && preferredWidth > outWidth) {
        return 1
    }
    val result = Math.round(Math.max(outWidth, outHeight) / Math.max(preferredWidth, preferredHeight).toFloat())
    return Math.max(1, result)
}