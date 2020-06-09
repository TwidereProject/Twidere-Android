package org.mariotaku.twidere.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.chameleon.view.ChameleonTextView

/**
 * Created by mariotaku on 2016/12/23.
 */

class PreferencesItemTextView(context: Context, attrs: AttributeSet? = null) : FixedTextView(context, attrs) {

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): Appearance? {
        val appearance = Appearance()
        val activatedColor = ChameleonUtils.getColorDependent(theme.colorControlActivated)
        val defaultColor = theme.textColorPrimary
        appearance.textColor = ColorStateList(arrayOf(ACTIVATED_STATE_SET, EMPTY_STATE_SET), intArrayOf(activatedColor, defaultColor))
        return appearance
    }

    companion object {

        private val ACTIVATED_STATE_SET = intArrayOf(android.R.attr.state_activated)
        private val EMPTY_STATE_SET = intArrayOf(0)
    }
}
