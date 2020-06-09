package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

/**
 * Created by mariotaku on 16/3/15.
 */
abstract class ThemedPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    @Inject
    lateinit var kPreferences: KPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponent.get(context).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context
        val preference = preference
        onClick(null, DialogInterface.BUTTON_NEGATIVE)
        val builder = AlertDialog.Builder(requireContext())
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
