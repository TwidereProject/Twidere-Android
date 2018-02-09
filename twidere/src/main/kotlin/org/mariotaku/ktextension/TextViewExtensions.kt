package org.mariotaku.ktextension

import android.graphics.Typeface
import android.view.View
import android.widget.TextView

inline val TextView.empty: Boolean
    get() = length() <= 0

inline var TextView.string: String?
    get() = text?.toString()
    set(value) {
        text = value
    }

inline var TextView.spannable: CharSequence?
    get() = text
    set(value) {
        setText(value, TextView.BufferType.SPANNABLE)
    }

inline var TextView.charSequence: CharSequence?
    get() = text
    set(value) {
        text = value
    }

inline val TextView.textIfVisible: CharSequence?
    get() = if (visibility == View.VISIBLE) text else null

fun TextView.applyFontFamily(lightFont: Boolean) {
    if (lightFont) {
        typeface = Typeface.create("sans-serif-light", typeface?.style ?: Typeface.NORMAL)
    }
}

fun TextView.hideIfEmpty(hideVisibility: Int = View.GONE) {
    visibility = if (empty) {
        hideVisibility
    } else {
        View.VISIBLE
    }
}