package org.mariotaku.twidere.util.theme

import android.content.Context
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.pnikosis.materialishprogress.ProgressWheel
import com.rengwuxian.materialedittext.MaterialEditText
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.chameleon.view.ChameleonSwitchCompat
import org.mariotaku.chameleon.view.ChameleonTextView
import org.mariotaku.multivalueswitch.library.MultiValueSwitch

object TwidereAppearanceCreator : Chameleon.AppearanceCreator {
    override fun createAppearance(view: View, context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): ChameleonView.Appearance? {
        when (view) {
            is ProgressWheel -> {
                return BasicColorAppearance(theme.colorAccent)
            }
            is MultiValueSwitch -> {
                return ChameleonSwitchCompat.Appearance.create(theme)
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
            is MultiValueSwitch -> {
                appearance as ChameleonSwitchCompat.Appearance
                setMultiValueSwitchTint(view, appearance.accentColor, appearance.isDark)
            }
            is MaterialEditText -> {
                appearance as ChameleonTextView.Appearance
                ChameleonTextView.Appearance.apply(view, appearance)
                view.setPrimaryColor(appearance.tintColor)
            }
        }
    }


    data class BasicColorAppearance(var color: Int) : ChameleonView.Appearance


    fun setMultiValueSwitchTint(switchView: MultiValueSwitch, @ColorInt color: Int, useDarker: Boolean) {
        if (switchView.trackDrawable != null) {
            switchView.trackDrawable = ChameleonSwitchCompat.modifySwitchDrawable(switchView.context,
                    switchView.trackDrawable, color, false, true, useDarker)
        }
        if (switchView.thumbDrawable != null) {
            switchView.thumbDrawable = ChameleonSwitchCompat.modifySwitchDrawable(switchView.context,
                    switchView.thumbDrawable, color, true, true, useDarker)
        }
    }
}