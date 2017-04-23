package org.mariotaku.ktextension

import android.graphics.Typeface
import android.view.View
import android.widget.TextView

val TextView.empty: Boolean
    get() = length() <= 0

var TextView.string: String?
    get() = text?.toString()
    set(value) {
        text = value
    }

var TextView.charSequence: CharSequence?
    get() = text
    set(value) {
        text = value
    }

fun TextView.applyFontFamily(lightFont: Boolean) {
    if (lightFont) {
        typeface = Typeface.create("sans-serif-light", typeface?.style ?: Typeface.NORMAL)
    }
}

fun TextView.hideIfEmpty() {
    visibility = if (empty) {
        View.GONE
    } else {
        View.VISIBLE
    }
}