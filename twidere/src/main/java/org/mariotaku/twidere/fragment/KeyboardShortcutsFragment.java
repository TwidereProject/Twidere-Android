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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec;

/**
 * Created by mariotaku on 15/4/10.
 */
public class KeyboardShortcutsFragment extends BasePreferenceFragment {

    private KeyboardShortcutsHandler mKeyboardShortcutHandler;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity activity = getActivity();
        mKeyboardShortcutHandler = TwidereApplication.getInstance(activity).getKeyboardShortcutsHandler();
        final PreferenceScreen defaultScreen = getPreferenceScreen();
        final PreferenceScreen preferenceScreen;
        if (defaultScreen != null) {
            defaultScreen.removeAll();
            preferenceScreen = defaultScreen;
        } else {
            preferenceScreen = getPreferenceManager().createPreferenceScreen(activity);
        }
        setPreferenceScreen(preferenceScreen);
        addPreferences();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_keyboard_shortcuts, menu);
    }

    private void addPreferences() {
        final PreferenceCategory general = makeAndAddCategory(getString(R.string.general));
        general.addPreference(makePreferences(null, "compose"));
        general.addPreference(makePreferences(null, "search"));
        general.addPreference(makePreferences(null, "message"));
        final PreferenceCategory statuses = makeAndAddCategory(getString(R.string.statuses));
        statuses.addPreference(makePreferences("status", "status.reply"));
        statuses.addPreference(makePreferences("status", "status.retweet"));
        statuses.addPreference(makePreferences("status", "status.favorite"));
    }

    private PreferenceCategory makeAndAddCategory(String title) {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        final Context context = preferenceScreen.getContext();
        //noinspection ConstantConditions
        final PreferenceCategory category = new PreferenceCategory(context, null);
        category.setTitle(title);
        preferenceScreen.addPreference(category);
        return category;
    }

    private KeyboardShortcutPreferences makePreferences(String contextTag, String action) {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        final Context context = preferenceScreen.getContext();
        return new KeyboardShortcutPreferences(context, mKeyboardShortcutHandler, contextTag, action);
    }

    private static class KeyboardShortcutPreferences extends DialogPreference implements OnKeyListener {

        private final KeyboardShortcutsHandler mKeyboardShortcutHandler;
        private final String mContextTag, mAction;
        private final OnSharedPreferenceChangeListener mPreferencesChangeListener;
        private TextView mKeysLabel, mConflictLabel;
        private KeyboardShortcutSpec mKeySpec;

        public KeyboardShortcutPreferences(final Context context, final KeyboardShortcutsHandler handler,
                                           @Nullable final String contextTag, @NonNull final String action) {
            //noinspection ConstantConditions
            super(context, null);
            mKeyboardShortcutHandler = handler;
            mContextTag = contextTag;
            mAction = action;
            setDialogLayoutResource(R.layout.dialog_keyboard_shortcut_input);
            setPersistent(false);
            setDialogTitle(KeyboardShortcutsHandler.getActionLabel(context, action));
            setTitle(KeyboardShortcutsHandler.getActionLabel(context, action));
            mPreferencesChangeListener = new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    updateSummary();
                }
            };
            updateSummary();
        }

        @Override
        protected void onAttachedToActivity() {
            super.onAttachedToActivity();
            mKeyboardShortcutHandler.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
        }

        @Override
        protected void onPrepareForRemoval() {
            mKeyboardShortcutHandler.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
            super.onPrepareForRemoval();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    if (mKeySpec == null) return;
                    mKeyboardShortcutHandler.register(mKeySpec, mAction);
                    break;
                }
                case DialogInterface.BUTTON_NEUTRAL: {
                    mKeyboardShortcutHandler.unregister(mAction);
                    break;
                }
            }
        }

        @Override
        protected void onPrepareDialogBuilder(Builder builder) {
            builder.setPositiveButton(getContext().getString(android.R.string.ok), this);
            builder.setNegativeButton(getContext().getString(android.R.string.cancel), this);
            builder.setNeutralButton(getContext().getString(R.string.clear), this);
        }

        @Override
        protected void showDialog(Bundle state) {
            super.showDialog(state);
            final Dialog dialog = getDialog();
            dialog.setOnKeyListener(this);
            mKeysLabel = (TextView) dialog.findViewById(R.id.keys_label);
            mConflictLabel = (TextView) dialog.findViewById(R.id.conflict_label);

            mConflictLabel.setVisibility(View.GONE);
        }

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_UP) return false;
            if (event.hasNoModifiers() && keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
                mKeyboardShortcutHandler.unregister(mAction);
                return true;
            }
            final KeyboardShortcutSpec spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(mContextTag, keyCode, event);
            if (spec == null || !spec.isValid()) return false;
            mKeySpec = spec;
            mKeysLabel.setText(spec.toKeyString());
            final String oldAction = mKeyboardShortcutHandler.findAction(spec);
            final Context context = getContext();
            if (mAction.equals(oldAction) || TextUtils.isEmpty(oldAction)) {
                mConflictLabel.setVisibility(View.GONE);
                if (dialog instanceof AlertDialog) {
                    ((AlertDialog) dialog).setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
                }
            } else {
                mConflictLabel.setVisibility(View.VISIBLE);
                final String label = KeyboardShortcutsHandler.getActionLabel(context, oldAction);
                mConflictLabel.setText(context.getString(R.string.conflicts_with_name, label));
                if (dialog instanceof AlertDialog) {
                    ((AlertDialog) dialog).setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.overwrite), this);
                }
            }
            return true;
        }

        private void updateSummary() {
            final KeyboardShortcutSpec spec = mKeyboardShortcutHandler.findKey(mAction);
            setSummary(spec != null ? spec.toKeyString() : null);
        }
    }
}
