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

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ThemedPreferenceDialogFragmentCompat;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

public class NotificationTypePreference extends DialogPreference implements
        Constants, IDialogPreference {

    private final int mDefaultValue;

    public NotificationTypePreference(final Context context) {
        this(context, null);
    }

    public NotificationTypePreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public NotificationTypePreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NotificationTypePreference);
        mDefaultValue = a.getInteger(R.styleable.NotificationTypePreference_notificationType, 0);
        a.recycle();
    }

    private boolean[] getCheckedItems(final int value) {
        final int[] flags = getFlags();
        final boolean[] checkedItems = new boolean[flags.length];
        for (int i = 0, j = flags.length; i < j; i++) {
            checkedItems[i] = (value & flags[i]) != 0;
        }
        return checkedItems;
    }

    private String[] getEntries() {
        final Context context = getContext();
        final String[] entries = new String[3];
        entries[0] = context.getString(R.string.notification_method_ringtone);
        entries[1] = context.getString(R.string.notification_method_vibration);
        entries[2] = context.getString(R.string.notification_method_light);
        return entries;
    }

    @Override
    public CharSequence getSummary() {
        final StringBuilder sb = new StringBuilder();
        String[] entries = getEntries();
        boolean[] states = getCheckedItems(getPersistedInt(mDefaultValue));
        for (int i = 0, j = entries.length; i < j; i++) {
            if (states[i]) {
                if (sb.length() != 0) {
                    sb.append(", ");
                }
                sb.append(entries[i]);
            }
        }
        return sb;
    }

    private int[] getFlags() {
        return new int[]{VALUE_NOTIFICATION_FLAG_RINGTONE, VALUE_NOTIFICATION_FLAG_VIBRATION,
                VALUE_NOTIFICATION_FLAG_LIGHT};
    }

    @Override
    public void displayDialog(@NonNull PreferenceFragmentCompat fragment) {
        final NotificationTypeDialogFragment df = NotificationTypeDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getParentFragmentManager(), getKey());
    }

    public int getDefaultValue() {
        return mDefaultValue;
    }

    public final static class NotificationTypeDialogFragment extends ThemedPreferenceDialogFragmentCompat
            implements DialogInterface.OnMultiChoiceClickListener {

        private boolean[] mCheckedItems;

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            NotificationTypePreference preference = (NotificationTypePreference) getPreference();
            final int value = preference.getPersistedInt(preference.getDefaultValue());
            mCheckedItems = preference.getCheckedItems(value);
            builder.setMultiChoiceItems(preference.getEntries(), mCheckedItems, this);
        }

        @Override
        public void onDialogClosed(boolean positive) {
            if (!positive || mCheckedItems == null) return;
            NotificationTypePreference preference = (NotificationTypePreference) getPreference();
            int value = 0;
            final int[] flags = preference.getFlags();
            for (int i = 0, j = flags.length; i < j; i++) {
                if (mCheckedItems[i]) {
                    value |= flags[i];
                }
            }
            preference.persistInt(value);
            preference.callChangeListener(value);
            preference.notifyChanged();
        }

        public static NotificationTypeDialogFragment newInstance(String key) {
            final NotificationTypeDialogFragment df = new NotificationTypeDialogFragment();
            final Bundle args = new Bundle();
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
            df.setArguments(args);
            return df;
        }

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            mCheckedItems[which] = isChecked;
        }

    }
}
