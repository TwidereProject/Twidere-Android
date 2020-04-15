package org.mariotaku.twidere.view.iface

import android.content.Context
import androidx.annotation.ColorInt
import android.util.AttributeSet

import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.chameleon.internal.ChameleonTypedArray
import org.mariotaku.chameleon.view.ChameleonTextView
import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 16/3/19.
 */
interface IIconActionButton : ChameleonView {

    var defaultColor: Int
        @ColorInt get @ColorInt set
    var activatedColor: Int
        @ColorInt get @ColorInt set
    var disabledColor: Int
        @ColorInt get @ColorInt set

    class Appearance : ChameleonTextView.Appearance() {
        var defaultColor: Int = 0
            @ColorInt get @ColorInt set
        var activatedColor: Int = 0
            @ColorInt get @ColorInt set
        var disabledColor: Int = 0
            @ColorInt get @ColorInt set

        companion object {

            fun create(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): Appearance {
                val appearance = Appearance()
                val a = ChameleonTypedArray.obtain(context, attributeSet, R.styleable.IconActionButton, theme)
                appearance.defaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0, false)
                appearance.activatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0, false)
                appearance.disabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0, false)
                a.recycle()
                return appearance
            }

            fun apply(view: IIconActionButton, appearance: Appearance) {
                val defaultColor = appearance.defaultColor
                if (defaultColor != 0) {
                    view.defaultColor = defaultColor
                }
                val activatedColor = appearance.activatedColor
                if (activatedColor != 0) {
                    view.activatedColor = activatedColor
                }
                val disabledColor = appearance.disabledColor
                if (disabledColor != 0) {
                    view.disabledColor = disabledColor
                }
            }
        }
    }
}
