package org.mariotaku.twidere.fragment.preference

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceDialogFragmentCompat
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.onShow

abstract class ThemedPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponent.get(context).inject(this)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context!!
        val preference = preference
        onClick(null, DialogInterface.BUTTON_NEGATIVE)
        val builder = AlertDialog.Builder(context)
                .setTitle(preference.dialogTitle)
                .setIcon(preference.dialogIcon)
                .setPositiveButton(preference.positiveButtonText, this)
                .setNegativeButton(preference.negativeButtonText, this)
        val contentView = onCreateDialogView(context)
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(preference.dialogMessage)
        }
        onPrepareDialogBuilder(builder)
        // Create the dialog
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        if (needInputMethod()) {
            supportRequestInputMethod(dialog)
        }
        return dialog
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {

    }

    private fun supportRequestInputMethod(dialog: Dialog) {
        val window = dialog.window
        window?.setSoftInputMode(5)
    }

}
