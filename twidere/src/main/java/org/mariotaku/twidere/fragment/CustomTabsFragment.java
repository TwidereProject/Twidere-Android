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

import static org.mariotaku.twidere.util.CustomTabUtils.getConfiguraionMap;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabIconDrawable;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabIconObject;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabTypeName;
import static org.mariotaku.twidere.util.CustomTabUtils.isTabAdded;
import static org.mariotaku.twidere.util.CustomTabUtils.isTabTypeValid;
import static org.mariotaku.twidere.util.Utils.getAccountIds;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.CustomTabEditorActivity;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.CustomTabConfiguration.CustomTabConfigurationComparator;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.Tabs;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CustomTabsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, Panes.Right,
		MultiChoiceModeListener, DropListener {

	private ContentResolver mResolver;

	private DragSortListView mListView;

	private PopupMenu mPopupMenu;

	private CustomTabsAdapter mAdapter;

	@Override
	public void drop(final int from, final int to) {
		mAdapter.drop(from, to);
		mListView.moveCheckState(from, to);
		saveTabPositions();
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE: {
				final Where where = Where.in(new Column(Tabs._ID), new RawItemArray(mListView.getCheckedItemIds()));
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
		final Activity activity = getActivity();
		final int themeRes;
		if (activity instanceof IThemedActivity) {
			themeRes = ((IThemedActivity) activity).getThemeResourceId();
		} else {
			themeRes = ThemeUtils.getSettingsThemeResource(activity);
		}
		mAdapter = new CustomTabsAdapter(ThemeUtils.getThemedContextForActionIcons(activity, themeRes));
		setListAdapter(mAdapter);
		setEmptyText(getString(R.string.no_tab_hint));
		mListView = (DragSortListView) getListView();
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(this);
		mListView.setDropListener(this);
		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
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
				}
				break;
			}
			case REQUEST_EDIT_TAB: {
				if (resultCode == Activity.RESULT_OK && data.hasExtra(EXTRA_ID)) {
					final ContentValues values = new ContentValues();
					values.put(Tabs.NAME, data.getStringExtra(EXTRA_NAME));
					values.put(Tabs.ICON, data.getStringExtra(EXTRA_ICON));
					values.put(Tabs.EXTRAS, data.getStringExtra(EXTRA_EXTRAS));
					final String where = Where.equals(Tabs._ID, data.getLongExtra(EXTRA_ID, -1)).getSQL();
					mResolver.update(Tabs.CONTENT_URI, values, where, null);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		new MenuInflater(getActivity()).inflate(R.menu.action_multi_select_items, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_custom_tabs, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(android.R.layout.list_content, null, false);
		final ListView originalList = (ListView) view.findViewById(android.R.id.list);
		final ViewGroup listContainer = (ViewGroup) originalList.getParent();
		listContainer.removeView(originalList);
		inflater.inflate(R.layout.fragment_custom_tabs, listContainer, true);
		return view;
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
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
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
	public void onPrepareOptionsMenu(final Menu menu) {
		final Resources res = getResources();
		final boolean hasOfficialKeyAccounts = Utils.hasAccountSignedWithOfficialKeys(getActivity());
		final long[] account_ids = getAccountIds(getActivity());
		final MenuItem itemAdd = menu.findItem(R.id.add_submenu);
		if (itemAdd != null && itemAdd.hasSubMenu()) {
			final SubMenu subMenu = itemAdd.getSubMenu();
			subMenu.clear();
			final HashMap<String, CustomTabConfiguration> map = getConfiguraionMap();
			final List<Entry<String, CustomTabConfiguration>> tabs = new ArrayList<Entry<String, CustomTabConfiguration>>(
					map.entrySet());
			Collections.sort(tabs, CustomTabConfigurationComparator.SINGLETON);
			for (final Entry<String, CustomTabConfiguration> entry : tabs) {
				final String type = entry.getKey();
				final CustomTabConfiguration conf = entry.getValue();

				final boolean isOfficiakKeyAccountRequired = TAB_TYPE_ACTIVITIES_ABOUT_ME.equals(type)
						|| TAB_TYPE_ACTIVITIES_BY_FRIENDS.equals(type);
				final boolean accountIdRequired = conf.getAccountRequirement() == CustomTabConfiguration.ACCOUNT_REQUIRED;

				final Intent intent = new Intent(INTENT_ACTION_ADD_TAB);
				intent.setClass(getActivity(), CustomTabEditorActivity.class);
				intent.putExtra(EXTRA_TYPE, type);
				intent.putExtra(EXTRA_OFFICIAL_KEY_ONLY, isOfficiakKeyAccountRequired);

				final MenuItem subItem = subMenu.add(conf.getDefaultTitle());
				final boolean shouldDisable = conf.isSingleTab() && isTabAdded(getActivity(), type)
						|| isOfficiakKeyAccountRequired && !hasOfficialKeyAccounts || accountIdRequired
						&& account_ids.length == 0;
				subItem.setVisible(!shouldDisable);
				subItem.setEnabled(!shouldDisable);
				final Drawable icon = res.getDrawable(conf.getDefaultIcon());
				subItem.setIcon(icon);
				subItem.setIntent(intent);
			}
		}
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
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
				final String where = Tabs._ID + " = " + id;
				mResolver.update(Tabs.CONTENT_URI, values, where, null);
			}
		}
	}

	private void updateTitle(final ActionMode mode) {
		if (mListView == null || mode == null || getActivity() == null) return;
		final int count = mListView.getCheckedItemCount();
		mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
	}

	public static class CustomTabsAdapter extends SimpleDragSortCursorAdapter implements OnClickListener {

		private CursorIndices mIndices;

		public CustomTabsAdapter(final Context context) {
			super(context, R.layout.list_item_custom_tab, null, new String[0], new int[0], 0);
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
				ViewAccessor.setBackground(holder.icon, icon);
			} else {
				holder.icon.setBackgroundResource(R.drawable.ic_iconic_action_list);
			}
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

		@Override
		public void onClick(final View view) {

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
