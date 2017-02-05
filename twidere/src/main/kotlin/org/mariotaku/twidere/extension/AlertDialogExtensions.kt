package org.mariotaku.twidere.extension

import android.content.DialogInterface.*
import android.support.v7.app.AlertDialog
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils

/**
 * Created by mariotaku on 2017/2/5.
 */

fun AlertDialog.applyTheme(): AlertDialog {
    val theme = Chameleon.getOverrideTheme(context, ChameleonUtils.getActivity(context))
    getButton(BUTTON_POSITIVE)?.setTextColor(theme.colorAccent)
    getButton(BUTTON_NEGATIVE)?.setTextColor(theme.colorAccent)
    getButton(BUTTON_NEUTRAL)?.setTextColor(theme.colorAccent)
    return this
}
