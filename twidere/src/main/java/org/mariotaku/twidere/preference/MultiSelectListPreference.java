/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.extension.DialogExtensionsKt;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

abstract class MultiSelectListPreference extends DialogPreference implements IDialogPreference {

    protected MultiSelectListPreference(final Context context) {
        this(context, null);
    }

    protected MultiSelectListPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    protected MultiSelectListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void displayDialog(@NonNull PreferenceFragmentCompat fragment) {
        final MultiSelectListDialogFragment df = MultiSelectListDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getParentFragmentManager(), getKey());
    }

    protected abstract boolean[] getDefaults();

    @NonNull
    protected SharedPreferences getDefaultSharedPreferences() {
        return getSharedPreferences();
    }

    protected abstract String[] getKeys();

    protected abstract String[] getNames();

    public final static class MultiSelectListDialogFragment extends PreferenceDialogFragmentCompat implements
            OnMultiChoiceClickListener, OnClickListener {

        private boolean[] mValues;
        private boolean[] mDefaultValues;
        private SharedPreferences mPreferences;
        private String[] mNames, mKeys;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MultiSelectListPreference preference = (MultiSelectListPreference) getPreference();
            mNames = preference.getNames();
            mKeys = preference.getKeys();
            mDefaultValues = preference.getDefaults();

            final int length = mKeys.length;
            if (length != mNames.length || length != mDefaultValues.length)
                throw new IllegalArgumentException();
            mValues = new boolean[length];
            mPreferences = preference.getDefaultSharedPreferences();
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            for (int i = 0; i < length; i++) {
                mValues[i] = mPreferences.getBoolean(mKeys[i], mDefaultValues[i]);
            }
            builder.setTitle(preference.getDialogTitle());
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setMultiChoiceItems(mNames, mValues, this);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> DialogExtensionsKt.applyTheme((AlertDialog) dialog1));
            return dialog;
        }

        @Override
        public void onDialogClosed(boolean positive) {
            if (mPreferences == null || !positive) return;
            final SharedPreferences.Editor editor = mPreferences.edit();
            final int length = mKeys.length;
            for (int i = 0; i < length; i++) {
                editor.putBoolean(mKeys[i], mValues[i]);
            }
            editor.apply();
        }

        @Override
        public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
            mValues[which] = isChecked;
        }

        public static MultiSelectListDialogFragment newInstance(String key) {
            final MultiSelectListDialogFragment df = new MultiSelectListDialogFragment();
            final Bundle args = new Bundle();
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
            df.setArguments(args);
            return df;
        }
    }

}
