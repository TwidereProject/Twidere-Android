package org.mariotaku.twidere.util.theme

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.pnikosis.materialishprogress.ProgressWheel
import com.rengwuxian.materialedittext.MaterialEditText
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.chameleon.view.ChameleonTextView

object TwidereAppearanceCreator : Chameleon.AppearanceCreator {
    override fun createAppearance(view: View, context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): ChameleonView.Appearance? {
        when (view) {
            is ProgressWheel -> {
                return BasicColorAppearance(theme.colorAccent)
            }
            is MaterialEditText -> {
                return ChameleonTextView.Appearance.create(view, context, attributeSet, theme)
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
            is MaterialEditText -> {
                appearance as ChameleonTextView.Appearance
                ChameleonTextView.Appearance.apply(view, appearance)
                view.setPrimaryColor(appearance.tintColor)
            }
        }
    }


    data class BasicColorAppearance(var color: Int) : ChameleonView.Appearance

}