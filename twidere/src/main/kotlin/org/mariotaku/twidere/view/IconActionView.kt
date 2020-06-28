package org.mariotaku.twidere.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.IconActionButton.Companion.activatedState
import org.mariotaku.twidere.view.IconActionButton.Companion.disabledState
import org.mariotaku.twidere.view.IconActionButton.Companion.updateColorFilter
import org.mariotaku.twidere.view.iface.IIconActionButton

/**
 * Created by mariotaku on 14/11/5.
 */
open class IconActionView(
        context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), IIconActionButton {

    final override var defaultColor: Int = 0
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

    final override var activatedColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColorStateList?.getColorForState(activatedState, defaultColor) ?: defaultColor
        }
        set(@ColorInt activatedColor) {
            field = activatedColor
            updateColorFilter()
        }

    final override var disabledColor: Int = 0
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
        defaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0)
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

    override fun isPostApplyTheme(): Boolean {
        return false
    }

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): IIconActionButton.Appearance? {
        return IIconActionButton.Appearance.create(context, attributeSet, theme)
    }

    override fun applyAppearance(appearance: ChameleonView.Appearance) {
        IIconActionButton.Appearance.apply(this, appearance as IIconActionButton.Appearance)
    }

}
