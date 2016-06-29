package org.mariotaku.twidere.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.view.Window;

/**
 * Created by mariotaku on 16/3/15.
 */
public abstract class ThemedPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        final DialogPreference preference = getPreference();
        onClick(null, DialogInterface.BUTTON_NEGATIVE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(preference.getDialogTitle())
                .setIcon(preference.getDialogIcon())
                .setPositiveButton(preference.getPositiveButtonText(), this)
                .setNegativeButton(preference.getNegativeButtonText(), this);
        View contentView = onCreateDialogView(context);
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(preference.getDialogMessage());
        }
        onPrepareDialogBuilder(builder);
        // Create the dialog
        final Dialog dialog = builder.create();
        if (needInputMethod()) {
            supportRequestInputMethod(dialog);
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {

    }

    private void supportRequestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setSoftInputMode(5);
    }

}
