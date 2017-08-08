package org.mariotaku.twidere.extension

import android.app.Dialog
import android.content.DialogInterface.*
import android.content.res.ColorStateList
import android.support.annotation.StringRes
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

fun <T : Dialog> T.applyOnShow(action: T.() -> Unit) {
    setOnShowListener { dialog ->
        @Suppress("UNCHECKED_CAST")
        action(dialog as T)
    }
}

fun AlertDialog.Builder.positive(@StringRes textId: Int, action: (dialog: AlertDialog) -> Unit) {
    setPositiveButton(textId) { dialog, _ ->
        action(dialog as AlertDialog)
    }
}

fun AlertDialog.Builder.negative(@StringRes textId: Int, action: (dialog: AlertDialog) -> Unit) {
    setNegativeButton(textId) { dialog, _ ->
        action(dialog as AlertDialog)
    }
}

fun <T : Dialog> T.onShow(action: (dialog: T) -> Unit) {
    setOnShowListener { dialog ->
        @Suppress("UNCHECKED_CAST")
        action(dialog as T)
    }
}