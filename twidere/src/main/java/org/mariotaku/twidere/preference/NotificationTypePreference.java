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
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

public class NotificationTypePreference extends DialogPreference implements Constants {

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

//    @Override
//    public void onClick(final DialogInterface dialog, final int which) {
//        final Dialog showingDialog = getDialog();
//        if (!(showingDialog instanceof AlertDialog)) return;
//        final AlertDialog alertDialog = (AlertDialog) showingDialog;
//        final ListView listView = alertDialog.getListView();
//        if (listView == null) return;
//        int value = 0;
//        final int[] flags = getFlags();
//        for (int i = 0, j = flags.length; i < j; i++) {
//            if (listView.isItemChecked(i)) {
//                value |= flags[i];
//            }
//        }
//        persistInt(value);
//        notifyChanged();
//    }
//
//    @Override
//    public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
//        super.onPrepareDialogBuilder(builder);
//        builder.setPositiveButton(android.R.string.ok, this);
//        builder.setNegativeButton(android.R.string.cancel, null);
//        final int value = getPersistedInt(mDefaultValue);
//        builder.setMultiChoiceItems(getEntries(), getCheckedItems(value), null);
//    }

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
        entries[0] = context.getString(R.string.ringtone);
        entries[1] = context.getString(R.string.vibration);
        entries[2] = context.getString(R.string.light);
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

}
