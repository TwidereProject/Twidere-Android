package org.mariotaku.twidere.singleton

import android.support.v4.text.BidiFormatter

object BidiFormatterSingleton {
    private var instance = BidiFormatter.getInstance()

    fun update() {
        instance = BidiFormatter.getInstance()
    }

    fun get(): BidiFormatter {
        return instance
    }

}
