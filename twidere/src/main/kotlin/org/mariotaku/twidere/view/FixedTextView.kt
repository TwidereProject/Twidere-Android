package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.chameleon.view.ChameleonTextView
import org.mariotaku.twidere.extension.setupEmojiFactory

/**
 * Created by mariotaku on 2017/2/3.
 */

open class FixedTextView(context: Context, attrs: AttributeSet? = null) : ChameleonTextView(context, attrs) {

    init {
        setupEmojiFactory()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            true
        }
    }

}
