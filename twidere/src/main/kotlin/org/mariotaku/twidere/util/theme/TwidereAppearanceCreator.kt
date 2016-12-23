package org.mariotaku.twidere.util.theme

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.pnikosis.materialishprogress.ProgressWheel
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView

object TwidereAppearanceCreator : Chameleon.AppearanceCreator {
    override fun createAppearance(view: View, context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): ChameleonView.Appearance? {
        when (view) {
            is ProgressWheel -> {
                return BasicColorAppearance(theme.colorAccent)
            }
        }
        return null
    }

    override fun applyAppearance(view: View, appearance: ChameleonView.Appearance) {
        when (view) {
            is ProgressWheel -> {
                appearance as BasicColorAppearance
                view.barColor = appearance.color
            }
        }
    }


    data class BasicColorAppearance(var color: Int) : ChameleonView.Appearance

}