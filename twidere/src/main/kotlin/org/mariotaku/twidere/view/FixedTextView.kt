package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.chameleon.view.ChameleonTextView
import org.mariotaku.twidere.text.util.SafeEditableFactory
import org.mariotaku.twidere.text.util.SafeSpannableFactory

/**
 * Created by mariotaku on 2017/2/3.
 */

open class FixedTextView(context: Context, attrs: AttributeSet? = null) : ChameleonTextView(context, attrs) {

    init {
        setSpannableFactory(SafeSpannableFactory)
        setEditableFactory(SafeEditableFactory)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        try {
            return super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            return true
        }
    }
}
