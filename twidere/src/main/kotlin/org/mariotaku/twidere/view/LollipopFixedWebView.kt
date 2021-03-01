package org.mariotaku.twidere.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

private fun Context.fixForLollipop(): Context {
    return if (Build.VERSION.SDK_INT in 21..22) {
        applicationContext
    } else this
}

class LollipopFixWebView: WebView {

    constructor(context: Context) : super(context.fixForLollipop())
    constructor(context: Context, attrs: AttributeSet?) : super(context.fixForLollipop(), attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context.fixForLollipop(),
            attrs,
            defStyleAttr
    )

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }
}
