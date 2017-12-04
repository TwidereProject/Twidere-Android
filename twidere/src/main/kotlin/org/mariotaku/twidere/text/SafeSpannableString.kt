package org.mariotaku.twidere.text

import android.text.SpannableString
import org.mariotaku.ktextension.fixSHY

class SafeSpannableString(source: CharSequence) : SpannableString(source) {

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
