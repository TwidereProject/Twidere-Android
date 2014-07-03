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

package org.mariotaku.twidere.activity.support;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ArrayUtils;

public class AccountSelectorActivity extends BaseSupportDialogActivity implements LoaderCallbacks<Cursor>,
		OnClickListener, OnItemClickListener {

	private final ContentObserver mContentObserver = new ContentObserver(null) {

		@Override
		public void onChange(final boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(final boolean selfChange, final Uri uri) {
			// Handle change.
			if (!isFinishing()) {
				getLoaderManager().restartLoader(0, null, AccountSelectorActivity.this);
			}
		}
	};

	private SharedPreferences mPreferences;

	private ListView mListView;
	private AccountsAdapter mAdapter;

	private boolean mFirstCreated;

	private View mSelectAccountDivider;

	private View mSelectAccountButtons;

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.save: {
				final long[] checkedIds = mListView.getCheckedItemIds();
				if (checkedIds == null || checkedIds.length == 0 && !isSelectNoneAllowed()) {
					Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show();
					return;
				}
				final Intent data = new Intent();
				data.putExtra(EXTRA_IDS, checkedIds);
				setResult(RESULT_OK, data);
				finish();
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mListView = (ListView) findViewById(android.R.id.list);
		mSelectAccountDivider = findViewById(R.id.select_account_divider);
		mSelectAccountButtons = findViewById(R.id.select_account_buttons);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final String where = isOAuthOnly() ? Accounts.AUTH_TYPE + " = " + Accounts.AUTH_TYPE_OAUTH : null;
		return new CursorLoader(this, Accounts.CONTENT_URI, Accounts.COLUMNS, where, null, null);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Intent data = new Intent();
		data.putExtra(EXTRA_ID, id);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.swapCursor(cursor);
		if (cursor != null && mFirstCreated) {
			final long[] activatedIds = getIntentExtraIds();
			for (int i = 0, j = mAdapter.getCount(); i < j; i++) {
				mListView.setItemChecked(i, ArrayUtils.contains(activatedIds, mAdapter.getItemId(i)));
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFirstCreated = savedInstanceState == null;
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		setContentView(R.layout.activity_account_selector);
		mAdapter = new AccountsAdapter(this);
		final boolean isSingleSelection = isSingleSelection();
		mListView.setChoiceMode(isSingleSelection ? ListView.CHOICE_MODE_NONE : ListView.CHOICE_MODE_MULTIPLE);
		mAdapter.setChoiceMode(mListView.getChoiceMode());
		if (isSingleSelection) {
			mListView.setOnItemClickListener(this);
		}
		mSelectAccountDivider.setVisibility(isSingleSelection ? View.GONE : View.VISIBLE);
		mSelectAccountButtons.setVisibility(isSingleSelection ? View.GONE : View.VISIBLE);
		mListView.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
	}

	@Override
	protected void onStart() {
		super.onStart();
		getContentResolver().registerContentObserver(Accounts.CONTENT_URI, true, mContentObserver);
	}

	@Override
	protected void onStop() {
		getContentResolver().unregisterContentObserver(mContentObserver);
		super.onStop();
	}

	private long[] getIntentExtraIds() {
		final Intent intent = getIntent();
		return intent.getLongArrayExtra(EXTRA_IDS);
	}

	private boolean isOAuthOnly() {
		final Intent intent = getIntent();
		return intent.getBooleanExtra(EXTRA_OAUTH_ONLY, false);
	}

	private boolean isSelectNoneAllowed() {
		final Intent intent = getIntent();
		return intent.getBooleanExtra(EXTRA_ALLOW_SELECT_NONE, false);
	}

	private boolean isSingleSelection() {
		final Intent intent = getIntent();
		return intent.getBooleanExtra(EXTRA_SINGLE_SELECTION, false);
	}

}
