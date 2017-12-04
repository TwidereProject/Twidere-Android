package org.mariotaku.twidere.text

import android.text.SpannableStringBuilder
import org.mariotaku.ktextension.fixSHY

class SafeSpannableStringBuilder(source: CharSequence) : SpannableStringBuilder(source) {

    init {
        fixSHY()
    }

    override fun setSpan(what: Any, start: Int, end: Int, flags: Int) {
        val validRange = 0..length
        if (end < start || start !in validRange || end !in validRange) {
            // Silently ignore
            return
        }
        super.setSpan(what, start, end, flags)
    }


}
