/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.DataExportActivity;
import org.mariotaku.twidere.activity.DataImportActivity;
import org.mariotaku.twidere.extension.DialogExtensionsKt;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

/**
 * Created by mariotaku on 15/3/19.
 */
public class SettingsImportExportPreference extends DialogPreference implements IDialogPreference {
    public SettingsImportExportPreference(Context context) {
        this(context, null);
    }

    public SettingsImportExportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void displayDialog(@NonNull PreferenceFragmentCompat fragment) {
        ImportExportDialogFragment df = ImportExportDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getParentFragmentManager(), getKey());
    }

    public static class ImportExportDialogFragment extends PreferenceDialogFragmentCompat {

        public static ImportExportDialogFragment newInstance(String key) {
            final ImportExportDialogFragment df = new ImportExportDialogFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_KEY, key);
            df.setArguments(args);
            return df;
        }


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final Context context = getContext();
            final String[] entries = new String[2];
            final Intent[] values = new Intent[2];
            entries[0] = context.getString(R.string.import_settings);
            entries[1] = context.getString(R.string.export_settings);
            values[0] = new Intent(context, DataImportActivity.class);
            values[1] = new Intent(context, DataExportActivity.class);
            builder.setItems(entries, (dialog, which) -> startActivity(values[which]));
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> DialogExtensionsKt.applyTheme((AlertDialog) dialog1));
            return dialog;
        }

        @Override
        public void onDialogClosed(boolean positive) {

        }
    }

}
