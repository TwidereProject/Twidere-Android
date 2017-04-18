package org.mariotaku.ktextension

import android.graphics.Typeface
import android.widget.TextView

val TextView.empty: Boolean
    get() = length() <= 0

val TextView.string: String?
    get() = text?.toString()

fun TextView.applyFontFamily(lightFont: Boolean) {
    if (lightFont) {
        typeface = Typeface.create("sans-serif-light", typeface?.style ?: Typeface.NORMAL)
    }
}