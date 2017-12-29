package org.mariotaku.twidere.text

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.constant.SharedPreferenceConstants

class TwidereClickableSpan(val highlightStyle: Int, val callback: () -> Unit) : ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
        if (SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE in highlightStyle) {
            ds.isUnderlineText = true
        }
        if (SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT in highlightStyle) {
            ds.color = ds.linkColor
        }
    }

    override fun onClick(widget: View?) {
        callback()
    }
}
