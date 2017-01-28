package org.mariotaku.ktextension

import android.graphics.Typeface
import android.widget.TextView

val TextView.empty: Boolean
    get() = length() <= 0

fun TextView.applyFontFamily(lightFont: Boolean) {
    if (lightFont) {
        typeface = Typeface.create("sans-serif-light", typeface.style)
    }
}