package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.chameleon.view.ChameleonSwitchCompat

/**
 * Created by mariotaku on 2016/12/22.
 */

class ToolbarToggleSwitch @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null
) : ChameleonSwitchCompat(context, attributeSet) {

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): Appearance? {
        val appearance = Appearance()
        appearance.accentColor = if (theme.isToolbarColored) {
            ChameleonUtils.getColorDependent(theme.colorToolbar)
        } else {
            theme.colorAccent
        }
        appearance.isDark = !ChameleonUtils.isColorLight(theme.colorBackground)
        return appearance
    }
}
