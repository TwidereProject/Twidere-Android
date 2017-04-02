package org.mariotaku.twidere.extension

import android.content.DialogInterface.*
import android.content.res.ColorStateList
import android.support.v7.app.AlertDialog
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.twidere.util.ThemeUtils

/**
 * Created by mariotaku on 2017/2/5.
 */

fun AlertDialog.applyTheme(): AlertDialog {
    val theme = Chameleon.getOverrideTheme(context, ChameleonUtils.getActivity(context))
    val optimalAccent = ThemeUtils.getOptimalAccentColor(theme.colorAccent, theme.colorForeground)
    val buttonColor = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(0)),
            intArrayOf(theme.textColorSecondary, optimalAccent))
    getButton(BUTTON_POSITIVE)?.setTextColor(buttonColor)
    getButton(BUTTON_NEGATIVE)?.setTextColor(buttonColor)
    getButton(BUTTON_NEUTRAL)?.setTextColor(buttonColor)
    return this
}
