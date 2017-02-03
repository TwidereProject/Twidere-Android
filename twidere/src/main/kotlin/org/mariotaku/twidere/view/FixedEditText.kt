package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.chameleon.view.ChameleonEditText

/**
 * Created by mariotaku on 2017/2/3.
 */

class FixedEditText(context: Context, attrs: AttributeSet? = null) : ChameleonEditText(context, attrs) {

    override fun onTextContextMenuItem(id: Int): Boolean {
        try {
            return super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            return true
        }
    }
}
