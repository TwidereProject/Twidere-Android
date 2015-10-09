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
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.KeyboardShortcutPreferenceCompatActivity;
import org.mariotaku.twidere.constant.KeyboardShortcutConstants;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec;

/**
 * Created by mariotaku on 15/4/10.
 */
public class KeyboardShortcutsFragment extends BasePreferenceFragment implements KeyboardShortcutConstants {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Activity activity = getActivity();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset: {
                final DialogFragment f = new ResetKeyboardShortcutConfirmDialogFragment();
                f.show(getFragmentManager().beginTransaction(), "reset_keyboard_shortcut_confirm");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPreferences() {
        final PreferenceCategory general = makeAndAddCategory(getString(R.string.general));
        general.addPreference(makePreferences(null, "compose"));
        general.addPreference(makePreferences(null, "search"));
        general.addPreference(makePreferences(null, "message"));
        final PreferenceCategory home = makeAndAddCategory(getString(R.string.home));
        home.addPreference(makePreferences("home", ACTION_HOME_ACCOUNTS_DASHBOARD));
        final PreferenceCategory navigation = makeAndAddCategory(getString(R.string.navigation));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_PREVIOUS));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_NEXT));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_PAGE_DOWN));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_PAGE_UP));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_PREVIOUS_TAB));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_NEXT_TAB));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_REFRESH));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_BACK));
        navigation.addPreference(makePreferences(CONTEXT_TAG_NAVIGATION, ACTION_NAVIGATION_TOP));
        final PreferenceCategory statuses = makeAndAddCategory(getString(R.string.statuses));
        statuses.addPreference(makePreferences(CONTEXT_TAG_STATUS, ACTION_STATUS_REPLY));
        statuses.addPreference(makePreferences(CONTEXT_TAG_STATUS, ACTION_STATUS_RETWEET));
        statuses.addPreference(makePreferences(CONTEXT_TAG_STATUS, ACTION_STATUS_FAVORITE));
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

    private Preference makePreferences(String contextTag, String action) {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        final Context context = preferenceScreen.getContext();
        final KeyboardShortcutsHandler shortcutHandler = mKeyboardShortcutHandler;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new KeyboardShortcutPreferenceCompat(context, shortcutHandler, contextTag, action);
        }
        return new KeyboardShortcutPreference(context, shortcutHandler, contextTag, action);
    }

    private static class KeyboardShortcutPreference extends DialogPreference implements OnKeyListener {

        private final KeyboardShortcutsHandler mKeyboardShortcutHandler;
        private final String mContextTag, mAction;
        private final OnSharedPreferenceChangeListener mPreferencesChangeListener;
        private TextView mKeysLabel, mConflictLabel;
        private KeyboardShortcutSpec mKeySpec;
        private int mModifierStates;

        public KeyboardShortcutPreference(final Context context, final KeyboardShortcutsHandler handler,
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
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (KeyEvent.isModifierKey(keyCode)) {
                    mModifierStates |= KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
                }
            } else if (event.getAction() != KeyEvent.ACTION_UP) {
                return false;
            }
            if (KeyEvent.isModifierKey(keyCode)) {
                mModifierStates &= ~KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            }
            final KeyboardShortcutSpec spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(mContextTag,
                    keyCode, event, KeyEvent.normalizeMetaState(mModifierStates | event.getMetaState()));
            if (spec == null || !spec.isValid()) {
                Log.d(LOGTAG, String.format("Invalid key %s", event), new Exception());
                return false;
            }
            mKeySpec = spec;
            mKeysLabel.setText(spec.toKeyString());
            final String oldAction = mKeyboardShortcutHandler.findAction(spec);
            final Context context = getContext();
            if (mAction.equals(oldAction) || TextUtils.isEmpty(oldAction)) {
                mConflictLabel.setVisibility(View.GONE);
                if (dialog instanceof AlertDialog) {
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setText((android.R.string.ok));
                }
            } else {
                mConflictLabel.setVisibility(View.VISIBLE);
                final String label = KeyboardShortcutsHandler.getActionLabel(context, oldAction);
                mConflictLabel.setText(context.getString(R.string.conflicts_with_name, label));
                if (dialog instanceof AlertDialog) {
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setText((R.string.overwrite));
                }
            }
            return true;
        }

        @Override
        protected void onPrepareDialogBuilder(Builder builder) {
            builder.setPositiveButton(getContext().getString(android.R.string.ok), this);
            builder.setNegativeButton(getContext().getString(android.R.string.cancel), this);
            builder.setNeutralButton(getContext().getString(R.string.clear), this);
            builder.setOnKeyListener(this);
        }

        @Override
        protected void onAttachedToActivity() {
            super.onAttachedToActivity();
            mKeyboardShortcutHandler.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
        }

        @Override
        protected View onCreateDialogView() {
            final View view = super.onCreateDialogView();
            mKeysLabel = (TextView) view.findViewById(R.id.keys_label);
            mConflictLabel = (TextView) view.findViewById(R.id.conflict_label);
            mConflictLabel.setVisibility(View.GONE);
            return view;
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
        protected void onPrepareForRemoval() {
            mKeyboardShortcutHandler.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
            super.onPrepareForRemoval();
        }

        private void updateSummary() {
            final KeyboardShortcutSpec spec = mKeyboardShortcutHandler.findKey(mAction);
            setSummary(spec != null ? spec.toKeyString() : null);
        }


    }

    private static class KeyboardShortcutPreferenceCompat extends Preference {

        private final KeyboardShortcutsHandler mKeyboardShortcutHandler;
        private final String mContextTag, mAction;
        private final OnSharedPreferenceChangeListener mPreferencesChangeListener;

        public KeyboardShortcutPreferenceCompat(final Context context, final KeyboardShortcutsHandler handler,
                                                @Nullable final String contextTag, @NonNull final String action) {
            //noinspection ConstantConditions
            super(context, null);
            mKeyboardShortcutHandler = handler;
            mContextTag = contextTag;
            mAction = action;
            setPersistent(false);
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
        protected void onAttachedToActivity() {
            super.onAttachedToActivity();
            mKeyboardShortcutHandler.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
        }

        @Override
        protected void onPrepareForRemoval() {
            mKeyboardShortcutHandler.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
            super.onPrepareForRemoval();
        }


    }

    public static class ResetKeyboardShortcutConfirmDialogFragment extends BaseDialogFragment implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    mKeyboardShortcutsHandler.reset();
                    break;
                }
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.reset_keyboard_shortcuts_confirm);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            return builder.create();
        }
    }
}
