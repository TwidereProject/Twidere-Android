package org.mariotaku.twidere.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.appcompat.widget.AppCompatImageButton
import android.util.AttributeSet
import android.widget.ImageView
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.iface.IIconActionButton

/**
 * Created by mariotaku on 14/11/5.
 */
class IconActionButton(
        context: Context, attrs: AttributeSet? = null
) : AppCompatImageButton(context, attrs, R.attr.imageButtonStyle), IIconActionButton {

    override var defaultColor: Int = 0
        @ColorInt
        get() {
            if (field == 0) {
                val color = ViewCompat.getBackgroundTintList(this)
                if (color != null) {
                    val currentColor = color.getColorForState(drawableState, 0)
                    return ThemeUtils.getContrastColor(currentColor, Color.BLACK, Color.WHITE)
                }
            }
            return field
        }
        set(@ColorInt defaultColor) {
            field = defaultColor
            updateColorFilter()
        }

    override var activatedColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColorStateList?.getColorForState(activatedState, defaultColor) ?: defaultColor
        }
        set(@ColorInt activatedColor) {
            field = activatedColor
            updateColorFilter()
        }

    override var disabledColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColorStateList?.getColorForState(disabledState, defaultColor) ?: defaultColor
        }
        set(@ColorInt disabledColor) {
            field = disabledColor
            updateColorFilter()
        }

    private val defaultColorStateList: ColorStateList?

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton,
                R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton)
        defaultColorStateList = a.getColorStateList(R.styleable.IconActionButton_iabColor)
        defaultColor = defaultColorStateList?.defaultColor ?: 0
        activatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0)
        disabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0)
        a.recycle()
        updateColorFilter()
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        updateColorFilter()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateColorFilter()
    }

    override fun setBackgroundTintList(tint: ColorStateList?) {
        super.setBackgroundTintList(tint)
        updateColorFilter()
    }

    override fun setSupportBackgroundTintList(tint: ColorStateList?) {
        super.setSupportBackgroundTintList(tint)
        updateColorFilter()
    }

    override fun isPostApplyTheme(): Boolean {
        return false
    }

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): IIconActionButton.Appearance? {
        return IIconActionButton.Appearance.create(context, attributeSet, theme)
    }

    override fun applyAppearance(appearance: ChameleonView.Appearance) {
        IIconActionButton.Appearance.apply(this, appearance as IIconActionButton.Appearance)
    }

    internal companion object {
        val activatedState = intArrayOf(android.R.attr.state_activated)
        val disabledState = intArrayOf(-android.R.attr.state_enabled)

        fun IIconActionButton.updateColorFilter() {
            this as ImageView
            when {
                isActivated -> {
                    setColorFilter(activatedColor)
                }
                isEnabled -> {
                    setColorFilter(defaultColor)
                }
                else -> {
                    setColorFilter(disabledColor)
                }
            }
        }
    }
}
