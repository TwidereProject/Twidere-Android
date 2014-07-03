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

import static org.mariotaku.twidere.util.Utils.getDisplayName;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Filters;

public abstract class BaseFiltersFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
		MultiChoiceModeListener {

	private ListView mListView;

	private SimpleCursorAdapter mAdapter;

	private ContentResolver mResolver;

	private ActionMode mActionMode;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_FILTERS_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, BaseFiltersFragment.this);
			}
		}

	};

	public abstract Uri getContentUri();

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE: {
				final Where where = Where.in(new Column(Filters._ID), new RawItemArray(mListView.getCheckedItemIds()));
				mResolver.delete(getContentUri(), where.getSQL(), null);
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
	public void onActivityCreated(final Bundle savedInstanceState) {
		mResolver = getContentResolver();
		super.onActivityCreated(savedInstanceState);
		mAdapter = createListAdapter();
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(this);
		setEmptyText(getString(R.string.no_rule));
		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		mActionMode = mode;
		getActivity().getMenuInflater().inflate(R.menu.action_multi_select_items, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), getContentUri(), getContentColumns(), null, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final View lv = view.findViewById(android.R.id.list);
		final Resources res = getResources();
		final float density = res.getDisplayMetrics().density;
		final int padding = (int) density * 16;
		lv.setId(android.R.id.list);
		lv.setPadding(padding, 0, padding, 0);
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
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mAdapter.swapCursor(data);
		setListShown(true);
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		updateTitle(mode);
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_FILTERS_UPDATED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	@Override
	public void setUserVisibleHint(final boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (!isVisibleToUser && mActionMode != null) {
			mActionMode.finish();
		}
	}

	protected SimpleCursorAdapter createListAdapter() {
		return new FilterListAdapter(getActivity());
	}

	protected abstract String[] getContentColumns();

	private void updateTitle(final ActionMode mode) {
		if (mListView == null || mode == null || getActivity() == null) return;
		final int count = mListView.getCheckedItemCount();
		mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
	}

	public static final class FilteredKeywordsFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Keywords.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Keywords.CONTENT_URI;
		}

	}

	public static final class FilteredLinksFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Links.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Links.CONTENT_URI;
		}

	}

	public static final class FilteredSourcesFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Sources.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Sources.CONTENT_URI;
		}

	}

	public static final class FilteredUsersFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Users.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Users.CONTENT_URI;
		}

		@Override
		protected SimpleCursorAdapter createListAdapter() {
			return new FilterUsersListAdapter(getActivity());
		}

		private static final class FilterUsersListAdapter extends SimpleCursorAdapter {

			private int mUserIdIdx, mNameIdx, mScreenNameIdx;

			private final boolean mNameFirst, mNicknameOnly;

			public FilterUsersListAdapter(final Context context) {
				super(context, android.R.layout.simple_list_item_activated_1, null, new String[0], new int[0], 0);
				final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
						Context.MODE_PRIVATE);
				mNameFirst = prefs.getBoolean(KEY_NAME_FIRST, true);
				mNicknameOnly = prefs.getBoolean(KEY_NICKNAME_ONLY, false);
			}

			@Override
			public void bindView(final View view, final Context context, final Cursor cursor) {
				super.bindView(view, context, cursor);
				final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				final long user_id = cursor.getLong(mUserIdIdx);
				final String name = cursor.getString(mNameIdx);
				final String screen_name = cursor.getString(mScreenNameIdx);
				final String display_name = getDisplayName(context, user_id, name, screen_name, mNameFirst,
						mNicknameOnly);
				text1.setText(display_name);
			}

			@Override
			public Cursor swapCursor(final Cursor c) {
				final Cursor old = super.swapCursor(c);
				if (c != null) {
					mUserIdIdx = c.getColumnIndex(Filters.Users.USER_ID);
					mNameIdx = c.getColumnIndex(Filters.Users.NAME);
					mScreenNameIdx = c.getColumnIndex(Filters.Users.SCREEN_NAME);
				}
				return old;
			}

		}
	}

	private static final class FilterListAdapter extends SimpleCursorAdapter {

		private static final String[] from = new String[] { Filters.VALUE };

		private static final int[] to = new int[] { android.R.id.text1 };

		public FilterListAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_activated_1, null, from, to, 0);
		}

	}
}
