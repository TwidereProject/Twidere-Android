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

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.model.KeyboardShortcutSpec;
import org.mariotaku.twidere.util.ParseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by mariotaku on 15/4/10.
 */
public class KeyboardShortcutsFragment extends BaseListFragment implements LoaderCallbacks<List<KeyboardShortcutSpec>>, MultiChoiceModeListener {

    private Adapter mAdapter;
    private ListView mListView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mAdapter = new Adapter(getActivity());
        setListAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_multi_select_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        updateTitle(mode);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE: {
                final SharedPreferences.Editor editor = getSharedPreferences(KEYBOARD_SHORTCUTS_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
                final SparseBooleanArray array = mListView.getCheckedItemPositions();
                if (array == null) return false;
                for (int i = 0, size = array.size(); i < size; i++) {
                    if (array.valueAt(i)) {
                        editor.remove(mAdapter.getItem(i).getRawKey());
                    }
                }
                editor.apply();
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
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public Loader<List<KeyboardShortcutSpec>> onCreateLoader(int id, Bundle args) {
        return new KeyboardShortcutSpecsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<KeyboardShortcutSpec>> loader, List<KeyboardShortcutSpec> data) {
        mAdapter.clear();
        mAdapter.addAll(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<List<KeyboardShortcutSpec>> loader) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_keyboard_shortcuts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD: {
                final SharedPreferences preferences = getSharedPreferences(KEYBOARD_SHORTCUTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
                final Editor editor = preferences.edit();
                editor.putString("ctrl+m", "compose");
                editor.apply();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = getListView();
    }

    private void updateTitle(final ActionMode mode) {
        if (mListView == null || mode == null || getActivity() == null) return;
        final int count = mListView.getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
    }

    private static class Adapter extends ArrayAdapter<KeyboardShortcutSpec> {

        public Adapter(Context context) {
            super(context, android.R.layout.simple_list_item_activated_2);
        }


        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            final KeyboardShortcutSpec spec = getItem(position);
            text1.setText(spec.getValueName(getContext()));
            text2.setText(spec.toKeyString());
            return view;
        }
    }


    private static class KeyboardShortcutSpecsLoader extends AsyncTaskLoader<List<KeyboardShortcutSpec>> {
        private final SharedPreferences preferences;
        private final OnSharedPreferenceChangeListener changeListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                forceLoad();
            }
        };

        public KeyboardShortcutSpecsLoader(Context context) {
            super(context);
            preferences = context.getSharedPreferences(KEYBOARD_SHORTCUTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
            preferences.registerOnSharedPreferenceChangeListener(changeListener);
        }

        @Override
        public List<KeyboardShortcutSpec> loadInBackground() {
            final ArrayList<KeyboardShortcutSpec> list = new ArrayList<>();
            for (Entry<String, ?> entry : preferences.getAll().entrySet()) {
                final KeyboardShortcutSpec spec = new KeyboardShortcutSpec(entry.getKey(), ParseUtils.parseString(entry.getValue()));
                if (spec.isValid()) {
                    list.add(spec);
                }
            }
            return list;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onReset() {
            if (isAbandoned()) {
                preferences.unregisterOnSharedPreferenceChangeListener(changeListener);
            }
            super.onReset();
        }
    }
}
