/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.mariotaku.twidere.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;

public class ThemedListPreferenceDialogFragmentCompat extends ThemedPreferenceDialogFragmentCompat {
    private int mClickedDialogEntryIndex;

    public static ThemedListPreferenceDialogFragmentCompat newInstance(String key) {
        final ThemedListPreferenceDialogFragmentCompat fragment =
                new ThemedListPreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    private ListPreference getListPreference() {
        return (ListPreference) getPreference();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        final ListPreference preference = getListPreference();
        final CharSequence[] entries = preference.getEntries();
        if (entries == null || preference.getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }
        mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
        builder.setSingleChoiceItems(entries, mClickedDialogEntryIndex,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedDialogEntryIndex = which;
                        /*
                         * Clicking on an item simulates the positive button
                         * click, and dismisses the dialog.
                         */
                        ThemedListPreferenceDialogFragmentCompat.this.onClick(dialog,
                                DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                });
        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        //noinspection ConstantConditions
        builder.setPositiveButton(null, null);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final ListPreference preference = getListPreference();
        if (positiveResult && mClickedDialogEntryIndex >= 0 &&
                preference.getEntryValues() != null) {
            String value = preference.getEntryValues()[mClickedDialogEntryIndex].toString();
            if (preference.callChangeListener(value)) {
                preference.setValue(value);
            }
        }
    }
}