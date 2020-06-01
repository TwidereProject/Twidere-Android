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

package org.mariotaku.twidere.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnShowListener;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.extension.DialogExtensionsKt;
import org.mariotaku.twidere.fragment.iface.ISupportDialogFragmentCallback;
import org.mariotaku.twidere.util.DataImportExportUtils;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_FLAGS;
import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_PATH;
import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_TITLE;

public final class DataExportImportTypeSelectorDialogFragment extends BaseDialogFragment implements
        OnMultiChoiceClickListener, OnClickListener, OnShowListener, OnItemClickListener {

    private ListView mListView;

    @Override
    public void onCancel(final DialogInterface dialog) {
        super.onCancel(dialog);
        final FragmentActivity a = getActivity();
        if (a instanceof Callback) {
            ((Callback) a).onCancelled(this);
        }
    }

    @Override
    public final void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final int flags = getCheckedFlags();
            onPositiveButtonClicked(flags);
        }
    }

    @Override
    public final void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
        updatePositiveButton(dialog);
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getActivity();
        final int flags = getEnabledFlags();
        mListView = new ListView(context);
        final TypeAdapter adapter = new TypeAdapter(context, flags);
        adapter.add(new Type(R.string.action_settings, DataImportExportUtils.FLAG_PREFERENCES));
        adapter.add(new Type(R.string.title_nicknames, DataImportExportUtils.FLAG_NICKNAMES));
        adapter.add(new Type(R.string.title_user_colors, DataImportExportUtils.FLAG_USER_COLORS));
        adapter.add(new Type(R.string.custom_host_mapping, DataImportExportUtils.FLAG_HOST_MAPPING));
        adapter.add(new Type(R.string.keyboard_shortcuts, DataImportExportUtils.FLAG_KEYBOARD_SHORTCUTS));
        adapter.add(new Type(R.string.title_filters, DataImportExportUtils.FLAG_FILTERS));
        adapter.add(new Type(R.string.tabs, DataImportExportUtils.FLAG_TABS));
        mListView.setAdapter(adapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(this);
        for (int i = 0, j = adapter.getCount(); i < j; i++) {
            mListView.setItemChecked(i, adapter.isEnabled(i));
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getTitle());
        builder.setView(mListView);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this);
        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final FragmentActivity a = getActivity();
        if (a instanceof Callback) {
            ((Callback) a).onDismissed(this);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
        updatePositiveButton(getDialog());
    }

    @Override
    public final void onShow(final DialogInterface dialog) {
        DialogExtensionsKt.applyTheme((AlertDialog) dialog);
        updatePositiveButton(dialog);
    }

    private int getCheckedFlags() {
        final SparseBooleanArray checked = mListView.getCheckedItemPositions();
        int flags = 0;
        for (int i = 0, j = checked.size(); i < j; i++) {
            final Type type = (Type) mListView.getItemAtPosition(i);
            if (checked.valueAt(i)) {
                flags |= type.flag;
            }
        }
        return flags;
    }

    private int getEnabledFlags() {
        final Bundle args = getArguments();
        if (args == null) return DataImportExportUtils.FLAG_ALL;
        return args.getInt(EXTRA_FLAGS, DataImportExportUtils.FLAG_ALL);
    }

    private CharSequence getTitle() {
        final Bundle args = getArguments();
        if (args == null) return null;
        return args.getCharSequence(EXTRA_TITLE);
    }

    private void onPositiveButtonClicked(final int flags) {
        final FragmentActivity a = getActivity();
        final Bundle args = getArguments();
        if (args == null) return;
        final Uri path = args.getParcelable(EXTRA_PATH);
        if (a instanceof Callback) {
            ((Callback) a).onPositiveButtonClicked(path, flags);
        }
    }

    private void updatePositiveButton(final DialogInterface dialog) {
        if (!(dialog instanceof AlertDialog)) return;
        final AlertDialog alertDialog = (AlertDialog) dialog;
        final Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setEnabled(getCheckedFlags() != 0);
    }

    public interface Callback extends ISupportDialogFragmentCallback {
        void onPositiveButtonClicked(Uri path, int flags);
    }

    private static class Type {
        private final int title, flag;

        Type(final int title, final int flag) {
            this.title = title;
            this.flag = flag;
        }
    }

    private static class TypeAdapter extends ArrayAdapter<Type> {

        private final int mEnabledFlags;

        public TypeAdapter(final Context context, final int enabledFlags) {
            super(context, android.R.layout.simple_list_item_multiple_choice);
            mEnabledFlags = enabledFlags;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(final int position, @Nullable final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final TextView text1 = view.findViewById(android.R.id.text1);
            text1.setText(getItem(position).title);
            view.setEnabled(isEnabled(position));
            return view;
        }

        @Override
        public boolean isEnabled(final int position) {
            return (mEnabledFlags & getItem(position).flag) != 0;
        }

    }

}
