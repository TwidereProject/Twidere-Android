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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class HostMappingsListFragment extends BaseListFragment implements MultiChoiceModeListener,
        OnSharedPreferenceChangeListener {

    private ListView mListView;
    private HostMappingAdapter mAdapter;
    private SharedPreferences mPreferences;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mPreferences = getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        mAdapter = new HostMappingAdapter(getActivity());
        setListAdapter(mAdapter);
        mListView = getListView();
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        reloadHostMappings();
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_multi_select_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        updateTitle(mode);
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete: {
                final SharedPreferences.Editor editor = mPreferences.edit();
                final SparseBooleanArray array = mListView.getCheckedItemPositions();
                if (array == null) return false;
                for (int i = 0, size = array.size(); i < size; i++) {
                    if (array.valueAt(i)) {
                        editor.remove(mAdapter.getItem(i));
                    }
                }
                editor.apply();
                reloadHostMappings();
                break;
            }
            default: {
                return false;
            }
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_host_mapping, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                final DialogFragment df = new AddMappingDialogFragment();
                df.show(getFragmentManager(), "add_mapping");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                                          final boolean checked) {
        updateTitle(mode);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        reloadHostMappings();
    }

    public void reloadHostMappings() {
        if (mAdapter == null) return;
        mAdapter.reload();
    }

    private void updateTitle(final ActionMode mode) {
        if (mListView == null || mode == null || getActivity() == null) return;
        final int count = mListView.getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
    }

    public static class AddMappingDialogFragment extends BaseDialogFragment implements OnClickListener,
            OnShowListener, TextWatcher, OnCheckedChangeListener {

        private static final String EXTRA_HOST = "host";
        private static final String EXTRA_ADDRESS = "address";
        private static final String EXTRA_EXCLUDED = "excluded";

        private EditText mEditHost, mEditAddress;
        private CheckBox mCheckExclude;

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            updateButton();
        }

        @Override
        public void afterTextChanged(final Editable s) {

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateAddressField();
            updateButton();
        }

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final String host = ParseUtils.parseString(mEditHost.getText());
                    final String address = mCheckExclude.isChecked() ? host : ParseUtils.parseString(mEditAddress.getText());
                    if (isEmpty(host) || isEmpty(address)) return;
                    final SharedPreferences prefs = getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME,
                            Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(host, address);
                    editor.apply();
                    break;
                }
            }

        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            @SuppressLint("InflateParams")
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_host_mapping, null);
            builder.setView(view);
            mEditHost = (EditText) view.findViewById(R.id.host);
            mEditAddress = (EditText) view.findViewById(R.id.address);
            mCheckExclude = (CheckBox) view.findViewById(R.id.exclude);
            mEditHost.addTextChangedListener(this);
            mEditAddress.addTextChangedListener(this);
            mCheckExclude.setOnCheckedChangeListener(this);
            final Bundle args = getArguments();
            if (savedInstanceState == null && args != null) {
                mEditHost.setText(args.getCharSequence(EXTRA_HOST));
                mEditAddress.setText(args.getCharSequence(EXTRA_ADDRESS));
                mCheckExclude.setChecked(args.getBoolean(EXTRA_EXCLUDED));
            }
            builder.setTitle(R.string.add_host_mapping);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, null);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(this);
            return dialog;
        }

        @Override
        public void onSaveInstanceState(@NonNull final Bundle outState) {
            outState.putCharSequence(EXTRA_HOST, mEditHost.getText());
            outState.putCharSequence(EXTRA_ADDRESS, mEditAddress.getText());
            outState.putCharSequence(EXTRA_EXCLUDED, mEditAddress.getText());
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onShow(final DialogInterface dialog) {
            updateButton();
        }

        private void updateAddressField() {
            mEditAddress.setVisibility(mCheckExclude.isChecked() ? View.GONE : View.VISIBLE);
        }

        private void updateButton() {
            final AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog == null) return;
            final boolean hostValid = !isEmpty(mEditHost.getText());
            final boolean addressValid = !isEmpty(mEditAddress.getText()) || mCheckExclude.isChecked();
            final Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setEnabled(hostValid && addressValid);
        }
    }

    static class HostMappingAdapter extends ArrayAdapter<String> {

        private final SharedPreferences mPreferences;

        public HostMappingAdapter(final Context context) {
            super(context, android.R.layout.simple_list_item_activated_2);
            mPreferences = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            final String key = getItem(position);
            text1.setText(key);
            final String value = mPreferences.getString(key, null);
            if (key.equals(value)) {
                text2.setText(R.string.excluded);
            } else {
                text2.setText(value);
            }
            return view;
        }

        public void reload() {
            clear();
            final Map<String, ?> all = mPreferences.getAll();
            addAll(all.keySet());
        }

    }

}
