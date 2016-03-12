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

package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.Menu;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.BaseAppCompatActivity;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;

public class SettingsActivity extends BaseAppCompatActivity {

    private static final int RESULT_SETTINGS_CHANGED = 10;

    private boolean mShouldNotifyChange;

    public static void setShouldNotifyChange(Activity activity) {
        if (!(activity instanceof SettingsActivity)) return;
        ((SettingsActivity) activity).setShouldNotifyChange(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsDetailsFragment fragment = new SettingsDetailsFragment();
        final Bundle args = new Bundle();
        args.putInt(EXTRA_RESID, R.xml.preferences_network);
        fragment.setArguments(args);
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.content, fragment);
        ft.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_SETTINGS_CHANGED && data != null && data.getBooleanExtra(EXTRA_CHANGED, false)) {
            setShouldNotifyChange(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (!isTopSettings()) return false;
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    private boolean isTopSettings() {
        return Boolean.parseBoolean("true");
    }


    @Override
    public void finish() {
        if (shouldNotifyChange()) {
            final Intent data = new Intent();
            data.putExtra(EXTRA_CHANGED, true);
            setResult(isTopSettings() ? RESULT_OK : RESULT_SETTINGS_CHANGED, data);
        }
        super.finish();
    }

    private void finishNoRestart() {
        super.finish();
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            onBackPressed();
            return true;
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        return ACTION_NAVIGATION_BACK.equals(action);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }


    private void setShouldNotifyChange(boolean notify) {
        mShouldNotifyChange = notify;
    }

    private boolean shouldNotifyChange() {
        return mShouldNotifyChange;
    }

    @Override
    public void onBackPressed() {
        if (isTopSettings() && shouldNotifyChange()) {
            final RestartConfirmDialogFragment df = new RestartConfirmDialogFragment();
            df.show(getSupportFragmentManager(), "restart_confirm");
            return;
        }
        super.onBackPressed();
    }

    public static class RestartConfirmDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.app_restart_confirm);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(R.string.dont_restart, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final SettingsActivity activity = (SettingsActivity) getActivity();
            if (activity == null) return;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    activity.finish();
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    activity.finishNoRestart();
                    break;
                }
            }
        }
    }


}
