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

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.activity.support.CustomTabEditorActivity;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.CustomTabConfiguration.CustomTabConfigurationComparator;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static org.mariotaku.twidere.util.CustomTabUtils.getConfigurationMap;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabIconDrawable;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabIconObject;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabTypeName;
import static org.mariotaku.twidere.util.CustomTabUtils.isTabAdded;
import static org.mariotaku.twidere.util.CustomTabUtils.isTabTypeValid;
import static org.mariotaku.twidere.util.Utils.getAccountIds;

public class CustomTabsFragment extends BaseFragment implements LoaderCallbacks<Cursor>,
        MultiChoiceModeListener, OnItemClickListener {

    private ContentResolver mResolver;

    private CustomTabsAdapter mAdapter;

    private DragSortListView mListView;
    private View mEmptyView;
    private View mListContainer, mProgressContainer;
    private TextView mEmptyText;
    private ImageView mEmptyIcon;


    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete: {
                final long[] itemIds = mListView.getCheckedItemIds();
                final Expression where = Expression.in(new Column(Tabs._ID), new RawItemArray(itemIds));
                mResolver.delete(Tabs.CONTENT_URI, where.getSQL(), null);
                break;
            }
        }
        mode.finish();
        return true;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mResolver = getContentResolver();
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        mAdapter = new CustomTabsAdapter(context);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setDropListener(new DropListener() {
            @Override
            public void drop(final int from, final int to) {
                mAdapter.drop(from, to);
                if (mListView.getChoiceMode() != AbsListView.CHOICE_MODE_NONE) {
                    mListView.moveCheckState(from, to);
                }
                saveTabPositions();
            }
        });
        mEmptyText.setText(R.string.no_tab);
        mEmptyIcon.setImageResource(R.drawable.ic_info_tab);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor c = mAdapter.getCursor();
        c.moveToPosition(mAdapter.getCursorPosition(position));
        final Intent intent = new Intent(INTENT_ACTION_EDIT_TAB);
        intent.setClass(getActivity(), CustomTabEditorActivity.class);
        intent.putExtra(EXTRA_ID, c.getLong(c.getColumnIndex(Tabs._ID)));
        intent.putExtra(EXTRA_TYPE, c.getString(c.getColumnIndex(Tabs.TYPE)));
        intent.putExtra(EXTRA_NAME, c.getString(c.getColumnIndex(Tabs.NAME)));
        intent.putExtra(EXTRA_ICON, c.getString(c.getColumnIndex(Tabs.ICON)));
        intent.putExtra(EXTRA_EXTRAS, c.getString(c.getColumnIndex(Tabs.EXTRAS)));
        startActivityForResult(intent, REQUEST_EDIT_TAB);
    }

    private void setListShown(boolean shown) {
        mListContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (DragSortListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        mEmptyIcon = (ImageView) view.findViewById(R.id.empty_icon);
        mEmptyText = (TextView) view.findViewById(R.id.empty_text);
        mListContainer = view.findViewById(R.id.list_container);
        mProgressContainer = view.findViewById(R.id.progress_container);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_TAB: {
                if (resultCode == Activity.RESULT_OK) {
                    final ContentValues values = new ContentValues();
                    values.put(Tabs.NAME, data.getStringExtra(EXTRA_NAME));
                    values.put(Tabs.ICON, data.getStringExtra(EXTRA_ICON));
                    values.put(Tabs.TYPE, data.getStringExtra(EXTRA_TYPE));
                    values.put(Tabs.ARGUMENTS, data.getStringExtra(EXTRA_ARGUMENTS));
                    values.put(Tabs.EXTRAS, data.getStringExtra(EXTRA_EXTRAS));
                    values.put(Tabs.POSITION, mAdapter.getCount());
                    mResolver.insert(Tabs.CONTENT_URI, values);
                    SettingsActivity.setShouldNotifyChange(getActivity());
                }
                break;
            }
            case REQUEST_EDIT_TAB: {
                if (resultCode == Activity.RESULT_OK && data.hasExtra(EXTRA_ID)) {
                    final ContentValues values = new ContentValues();
                    values.put(Tabs.NAME, data.getStringExtra(EXTRA_NAME));
                    values.put(Tabs.ICON, data.getStringExtra(EXTRA_ICON));
                    values.put(Tabs.EXTRAS, data.getStringExtra(EXTRA_EXTRAS));
                    final String where = Expression.equals(Tabs._ID, data.getLongExtra(EXTRA_ID, -1)).getSQL();
                    mResolver.update(Tabs.CONTENT_URI, values, where, null);
                    SettingsActivity.setShouldNotifyChange(getActivity());
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_multi_select_items, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new CursorLoader(getActivity(), Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_custom_tabs, menu);
        final Resources res = getResources();
        final boolean hasOfficialKeyAccounts = Utils.hasAccountSignedWithOfficialKeys(getActivity());
        final boolean forcePrivateAPI = mPreferences.getBoolean(KEY_FORCE_USING_PRIVATE_APIS, false);
        final long[] accountIds = getAccountIds(getActivity());
        final MenuItem itemAdd = menu.findItem(R.id.add_submenu);
        if (itemAdd != null && itemAdd.hasSubMenu()) {
            final SubMenu subMenu = itemAdd.getSubMenu();
            subMenu.clear();
            final HashMap<String, CustomTabConfiguration> map = getConfigurationMap();
            final List<Entry<String, CustomTabConfiguration>> tabs = new ArrayList<>(
                    map.entrySet());
            Collections.sort(tabs, CustomTabConfigurationComparator.SINGLETON);
            for (final Entry<String, CustomTabConfiguration> entry : tabs) {
                final String type = entry.getKey();
                final CustomTabConfiguration conf = entry.getValue();

                final boolean isOfficialKeyAccountRequired = TAB_TYPE_ACTIVITIES_ABOUT_ME.equals(type)
                        || TAB_TYPE_ACTIVITIES_BY_FRIENDS.equals(type);
                final boolean accountIdRequired = conf.getAccountRequirement() == CustomTabConfiguration.ACCOUNT_REQUIRED;

                final Intent intent = new Intent(INTENT_ACTION_ADD_TAB);
                intent.setClass(getActivity(), CustomTabEditorActivity.class);
                intent.putExtra(EXTRA_TYPE, type);
                intent.putExtra(EXTRA_OFFICIAL_KEY_ONLY, isOfficialKeyAccountRequired);

                final MenuItem subItem = subMenu.add(conf.getDefaultTitle());
                final boolean disabledByNoAccount = accountIdRequired && accountIds.length == 0;
                final boolean disabledByNoOfficialKey = !forcePrivateAPI && isOfficialKeyAccountRequired && !hasOfficialKeyAccounts;
                final boolean disabledByDuplicateTab = conf.isSingleTab() && isTabAdded(getActivity(), type);
                final boolean shouldDisable = disabledByDuplicateTab || disabledByNoOfficialKey || disabledByNoAccount;
                subItem.setVisible(!shouldDisable);
                subItem.setEnabled(!shouldDisable);
                final Drawable icon = ResourcesCompat.getDrawable(res, conf.getDefaultIcon(), null);
                subItem.setIcon(icon);
                subItem.setIntent(intent);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_draggable_list_with_empty_view, container, false);
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {

    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                                          final boolean checked) {
        updateTitle(mode);
    }


    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        mAdapter.changeCursor(cursor);
        setListShown(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            default: {
                final Intent intent = item.getIntent();
                if (intent == null) return false;
                startActivityForResult(intent, REQUEST_ADD_TAB);
                return true;
            }
        }
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        updateTitle(mode);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void saveTabPositions() {
        final ArrayList<Integer> positions = mAdapter.getCursorPositions();
        final Cursor c = mAdapter.getCursor();
        if (positions != null && c != null && !c.isClosed()) {
            final int idIdx = c.getColumnIndex(Tabs._ID);
            for (int i = 0, j = positions.size(); i < j; i++) {
                c.moveToPosition(positions.get(i));
                final long id = c.getLong(idIdx);
                final ContentValues values = new ContentValues();
                values.put(Tabs.POSITION, i);
                final String where = Expression.equals(Tabs._ID, id).getSQL();
                mResolver.update(Tabs.CONTENT_URI, values, where, null);
            }
        }
        SettingsActivity.setShouldNotifyChange(getActivity());
    }

    private void updateTitle(final ActionMode mode) {
        if (mListView == null || mode == null || getActivity() == null) return;
        final int count = mListView.getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
    }

    public static class CustomTabsAdapter extends SimpleDragSortCursorAdapter {

        private final int mIconColor;
        private CursorIndices mIndices;

        public CustomTabsAdapter(final Context context) {
            super(context, R.layout.list_item_custom_tab, null, new String[0], new int[0], 0);
            mIconColor = ThemeUtils.getThemeForegroundColor(context);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);
            final TwoLineWithIconViewHolder holder = (TwoLineWithIconViewHolder) view.getTag();
            final String type = cursor.getString(mIndices.type);
            final String name = cursor.getString(mIndices.name);
            final String iconKey = cursor.getString(mIndices.icon);
            if (isTabTypeValid(type)) {
                final String typeName = getTabTypeName(context, type);
                holder.text1.setText(TextUtils.isEmpty(name) ? typeName : name);
                holder.text1.setPaintFlags(holder.text1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(typeName);
            } else {
                holder.text1.setText(name);
                holder.text1.setPaintFlags(holder.text1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.text2.setText(R.string.invalid_tab);
            }
            final Drawable icon = getTabIconDrawable(context, getTabIconObject(iconKey));
            holder.icon.setVisibility(View.VISIBLE);
            if (icon != null) {
                holder.icon.setImageDrawable(icon);
            } else {
                holder.icon.setImageResource(R.drawable.ic_action_list);
            }
            holder.icon.setColorFilter(mIconColor, Mode.SRC_ATOP);
        }

        @Override
        public void changeCursor(final Cursor cursor) {
            if (cursor != null) {
                mIndices = new CursorIndices(cursor);
            }
            super.changeCursor(cursor);
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            final View view = super.newView(context, cursor, parent);
            final Object tag = view.getTag();
            if (!(tag instanceof TwoLineWithIconViewHolder)) {
                final TwoLineWithIconViewHolder holder = new TwoLineWithIconViewHolder(view);
                view.setTag(holder);
            }
            return view;
        }

        static class CursorIndices {
            final int _id, name, icon, type, arguments;

            CursorIndices(final Cursor mCursor) {
                _id = mCursor.getColumnIndex(Tabs._ID);
                icon = mCursor.getColumnIndex(Tabs.ICON);
                name = mCursor.getColumnIndex(Tabs.NAME);
                type = mCursor.getColumnIndex(Tabs.TYPE);
                arguments = mCursor.getColumnIndex(Tabs.ARGUMENTS);
            }
        }

    }

}
