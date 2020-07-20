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

package org.mariotaku.twidere.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.KeyboardShortcutPreferenceCompatActivity;
import org.mariotaku.twidere.constant.KeyboardShortcutConstants;
import org.mariotaku.twidere.extension.DialogExtensionsKt;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec;

/**
 * Created by mariotaku on 15/4/10.
 */
public class KeyboardShortcutsFragment extends BasePreferenceFragment implements KeyboardShortcutConstants {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_keyboard_shortcuts);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_keyboard_shortcuts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset) {
            final DialogFragment f = new ResetKeyboardShortcutConfirmDialogFragment();
            f.show(getParentFragmentManager(), "reset_keyboard_shortcut_confirm");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class KeyboardShortcutPreferenceCompat extends Preference {

        private final KeyboardShortcutsHandler mKeyboardShortcutHandler;
        private final String mContextTag, mAction;
        private final OnSharedPreferenceChangeListener mPreferencesChangeListener;

        public KeyboardShortcutPreferenceCompat(final Context context, final KeyboardShortcutsHandler handler,
                                                @Nullable final String contextTag, @NonNull final String action) {
            super(context);
            mKeyboardShortcutHandler = handler;
            mContextTag = contextTag;
            mAction = action;
            setPersistent(false);
            setTitle(KeyboardShortcutsHandler.getActionLabel(context, action));
            mPreferencesChangeListener = (preferences, key) -> updateSummary();
            updateSummary();
        }

        @Override
        protected void onClick() {
            final Context context = getContext();
            final Intent intent = new Intent(context, KeyboardShortcutPreferenceCompatActivity.class);
            intent.putExtra(KeyboardShortcutPreferenceCompatActivity.EXTRA_CONTEXT_TAG, mContextTag);
            intent.putExtra(KeyboardShortcutPreferenceCompatActivity.EXTRA_KEY_ACTION, mAction);
            context.startActivity(intent);
        }

        private void updateSummary() {
            final KeyboardShortcutSpec spec = mKeyboardShortcutHandler.findKey(mAction);
            setSummary(spec != null ? spec.toKeyString() : null);
        }

        @Override
        public void onAttached() {
            super.onAttached();
            mKeyboardShortcutHandler.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
        }

        @Override
        protected void onPrepareForRemoval() {
            mKeyboardShortcutHandler.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
            super.onPrepareForRemoval();
        }


    }

    public static class ResetKeyboardShortcutConfirmDialogFragment extends BaseDialogFragment
            implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                keyboardShortcutsHandler.reset();
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.reset_keyboard_shortcuts_confirm);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> DialogExtensionsKt.applyTheme((AlertDialog) dialog1));
            return dialog;
        }
    }
}
