package org.mariotaku.twidere.view

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.iface.IIconActionButton

/**
 * Created by mariotaku on 14/11/5.
 */
open class IconActionView(
        context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), IIconActionButton {

    override final var defaultColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            val color = ViewCompat.getBackgroundTintList(this)
            if (color != null) {
                val currentColor = color.getColorForState(drawableState, 0)
                return ThemeUtils.getContrastColor(currentColor, Color.BLACK, Color.WHITE)
            }
            return field
        }
        set(@ColorInt defaultColor) {
            field = defaultColor
            updateColorFilter()
        }

    override final var activatedColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColor
        }
        set(@ColorInt value) {
            field = value
            updateColorFilter()
        }

    override final var disabledColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColor
        }
        set(@ColorInt value) {
            field = value
            updateColorFilter()
        }

    init {
        if (!isInEditMode) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton,
                    R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton)
            defaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0)
            activatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0)
            disabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0)
            a.recycle()
        }
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

    private fun updateColorFilter() {
        if (isActivated) {
            setColorFilter(activatedColor)
        } else if (isEnabled) {
            setColorFilter(defaultColor)
        } else {
            setColorFilter(disabledColor)
        }
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
