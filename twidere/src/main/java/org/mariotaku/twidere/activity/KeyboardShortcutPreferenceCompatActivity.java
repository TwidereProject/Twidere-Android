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

package org.mariotaku.twidere.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec;
import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 15/4/20.
 */
public class KeyboardShortcutPreferenceCompatActivity extends BaseThemedActivity implements Constants, OnClickListener {

    public static final String EXTRA_CONTEXT_TAG = "context_tag";
    public static final String EXTRA_KEY_ACTION = "key_action";

    private TextView mKeysLabel, mConflictLabel;

    private KeyboardShortcutSpec mKeySpec;
    private Button mButtonPositive, mButtonNegative, mButtonNeutral;
    private int mMetaState;

    @Override
    public String getThemeBackgroundOption() {
        return VALUE_THEME_BACKGROUND_DEFAULT;
    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getThemeColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getDialogThemeResource(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_shortcut_input);
        setTitle(KeyboardShortcutsHandler.getActionLabel(this, getKeyAction()));

        mButtonPositive.setOnClickListener(this);
        mButtonNegative.setOnClickListener(this);
        mButtonNeutral.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_positive: {
                if (mKeySpec == null) return;
                mKeyboardShortcutHandler.register(mKeySpec, getKeyAction());
                finish();
                break;
            }
            case R.id.button_neutral: {
                mKeyboardShortcutHandler.unregister(getKeyAction());
                finish();
                break;
            }
            case R.id.button_negative: {
                finish();
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.isModifierKey(keyCode)) {
            mMetaState |= KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (KeyEvent.isModifierKey(keyCode)) {
            mMetaState &= ~KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
        }
        final String keyAction = getKeyAction();
        if (keyAction == null) return false;
        final KeyboardShortcutSpec spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(getContextTag(),
                keyCode, event, KeyEvent.normalizeMetaState(mMetaState | event.getMetaState()));
        if (spec == null || !spec.isValid()) {
            return super.onKeyUp(keyCode, event);
        }
        mKeySpec = spec;
        mKeysLabel.setText(spec.toKeyString());
        final String oldAction = mKeyboardShortcutHandler.findAction(spec);
        final KeyboardShortcutSpec copyOfSpec = spec.copy();
        copyOfSpec.setContextTag(null);
        final String oldGeneralAction = mKeyboardShortcutHandler.findAction(copyOfSpec);
        if (!TextUtils.isEmpty(oldAction) && !keyAction.equals(oldAction)) {
            // Conflicts with keys in same context tag
            mConflictLabel.setVisibility(View.VISIBLE);
            final String label = KeyboardShortcutsHandler.getActionLabel(this, oldAction);
            mConflictLabel.setText(getString(R.string.conflicts_with_name, label));
            mButtonPositive.setText((R.string.overwrite));
        } else if (!TextUtils.isEmpty(oldGeneralAction) && !keyAction.equals(oldGeneralAction)) {
            // Conflicts with keys in root context
            mConflictLabel.setVisibility(View.VISIBLE);
            final String label = KeyboardShortcutsHandler.getActionLabel(this, oldGeneralAction);
            mConflictLabel.setText(getString(R.string.conflicts_with_name, label));
            mButtonPositive.setText((R.string.overwrite));
        } else {
            mConflictLabel.setVisibility(View.GONE);
            mButtonPositive.setText((android.R.string.ok));
        }
        return true;
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mKeysLabel = (TextView) findViewById(R.id.keys_label);
        mConflictLabel = (TextView) findViewById(R.id.conflict_label);
        mButtonPositive = (Button) findViewById(R.id.button_positive);
        mButtonNegative = (Button) findViewById(R.id.button_negative);
        mButtonNeutral = (Button) findViewById(R.id.button_neutral);
    }

    private String getContextTag() {
        return getIntent().getStringExtra(EXTRA_CONTEXT_TAG);
    }

    private String getKeyAction() {
        return getIntent().getStringExtra(EXTRA_KEY_ACTION);
    }
}
