package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ThemedPreferenceDialogFragmentCompat;
import org.mariotaku.twidere.preference.iface.IDialogPreference;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

/**
 * Created by mariotaku on 16/3/15.
 */
public class KeyboardShortcutPreference extends DialogPreference implements IDialogPreference {

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferencesChangeListener;

    private String mContextTag, mAction;

    @Inject
    KeyboardShortcutsHandler mKeyboardShortcutsHandler;

    public KeyboardShortcutPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public KeyboardShortcutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public KeyboardShortcutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet set) {
        GeneralComponentHelper.build(context).inject(this);
        TypedArray a = context.obtainStyledAttributes(set, R.styleable.KeyboardShortcutPreference);
        mContextTag = a.getString(R.styleable.KeyboardShortcutPreference_android_tag);
        mAction = a.getString(R.styleable.KeyboardShortcutPreference_android_action);
        a.recycle();

        if (TextUtils.isEmpty(mAction)) {
            throw new IllegalArgumentException("android:action required");
        }
        setKey(mAction);

        setDialogLayoutResource(R.layout.dialog_keyboard_shortcut_input);
        setPersistent(false);
        setDialogTitle(KeyboardShortcutsHandler.getActionLabel(context, mAction));
        setTitle(KeyboardShortcutsHandler.getActionLabel(context, mAction));
        mPreferencesChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                updateSummary();
            }
        };
        updateSummary();
    }

    @Override
    protected void onPrepareForRemoval() {
        mKeyboardShortcutsHandler.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
        super.onPrepareForRemoval();
    }

    @Override
    public void onAttached() {
        super.onAttached();
        mKeyboardShortcutsHandler.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
    }

    private void updateSummary() {
        final KeyboardShortcutSpec spec = mKeyboardShortcutsHandler.findKey(mAction);
        setSummary(spec != null ? spec.toKeyString() : null);
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        KeyboardShortcutDialogFragment df = KeyboardShortcutDialogFragment.newInstance(mAction);
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), mAction);
    }

    public String getAction() {
        return mAction;
    }

    private String getContextTag() {
        return mContextTag;
    }

    public KeyboardShortcutsHandler getKeyboardShortcutsHandler() {
        return mKeyboardShortcutsHandler;
    }

    public static class KeyboardShortcutDialogFragment extends ThemedPreferenceDialogFragmentCompat
            implements DialogInterface.OnKeyListener {

        private TextView mKeysLabel;
        private TextView mConflictLabel;

        private KeyboardShortcutSpec mKeySpec;
        private int mModifierStates;

        public static KeyboardShortcutDialogFragment newInstance(String key) {
            final KeyboardShortcutDialogFragment df = new KeyboardShortcutDialogFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_KEY, key);
            df.setArguments(args);
            return df;
        }


        @Override
        public void onDialogClosed(boolean positiveResult) {

        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            builder.setNeutralButton(R.string.clear, this);
            builder.setOnKeyListener(this);
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
            final KeyboardShortcutPreference preference = (KeyboardShortcutPreference) getPreference();
            final String action = preference.getAction();
            final String contextTag = preference.getContextTag();
            final KeyboardShortcutsHandler handler = preference.getKeyboardShortcutsHandler();

            final KeyboardShortcutSpec spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(contextTag,
                    keyCode, event, KeyEvent.normalizeMetaState(mModifierStates | event.getMetaState()));
            if (spec == null || !spec.isValid()) {
                Log.d(LOGTAG, String.format("Invalid key %s", event), new Exception());
                return false;
            }
            mKeySpec = spec;
            mKeysLabel.setText(spec.toKeyString());
            final String oldAction = handler.findAction(spec);
            final Context context = getContext();
            if (action.equals(oldAction) || TextUtils.isEmpty(oldAction)) {
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
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            mKeysLabel = (TextView) view.findViewById(R.id.keys_label);
            mConflictLabel = (TextView) view.findViewById(R.id.conflict_label);
            mConflictLabel.setVisibility(View.GONE);
        }


        @Override
        public void onClick(DialogInterface dialog, int which) {
            final KeyboardShortcutPreference preference = (KeyboardShortcutPreference) getPreference();
            final String action = preference.getAction();
            final KeyboardShortcutsHandler handler = preference.getKeyboardShortcutsHandler();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    if (mKeySpec == null) return;
                    handler.register(mKeySpec, action);
                    break;
                }
                case DialogInterface.BUTTON_NEUTRAL: {
                    handler.unregister(action);
                    break;
                }
            }
        }

    }
}
