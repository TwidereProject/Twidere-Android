package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet

import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.twidere.view.iface.IIconActionButton

/**
 * Created by mariotaku on 2016/12/23.
 */
class PreferencesItemIconView(context: Context, attrs: AttributeSet? = null) : IconActionView(context, attrs) {

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): IIconActionButton.Appearance? {
        val appearance = IIconActionButton.Appearance()
        appearance.activatedColor = ChameleonUtils.getColorDependent(theme.colorControlActivated)
        appearance.defaultColor = theme.colorForeground
        return appearance
    }
}
