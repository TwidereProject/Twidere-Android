package org.mariotaku.twidere.text

import android.text.TextPaint
import android.text.style.ClickableSpan
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.constant.SharedPreferenceConstants

/**
 * Created by Mariotaku on 2017/5/21.
 */

abstract class TwidereClickableSpan(val highlightStyle: Int): ClickableSpan() {

    override fun updateDrawState(ds: TextPaint) {
        if (SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE in highlightStyle) {
            ds.isUnderlineText = true
        }
        if (SharedPreferenceConstants.VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT in highlightStyle) {
            ds.color = ds.linkColor
        }
    }
}
