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

import static org.mariotaku.twidere.util.ContentValuesCreator.makeFilterdUserContentValues;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;

import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.SourceAutoCompleteAdapter;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredLinksFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredUsersFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class FiltersActivity extends BaseActivity implements TabListener, OnPageChangeListener {

	private static final String EXTRA_AUTO_COMPLETE_TYPE = "auto_complete_type";
	private static final int AUTO_COMPLETE_TYPE_SOURCES = 2;

	private ViewPager mViewPager;
	private TabsAdapter mAdapter;

	private ActionBar mActionBar;
	private SharedPreferences mPreferences;

	@Override
	public void onContentChanged() {
		mViewPager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		setContentView(R.layout.activity_filters);
		mActionBar = getActionBar();
		mAdapter = new TabsAdapter(this, getFragmentManager(), null);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		addTab(FilteredUsersFragment.class, R.string.users, 0);
		addTab(FilteredKeywordsFragment.class, R.string.keywords, 1);
		addTab(FilteredSourcesFragment.class, R.string.sources, 2);
		addTab(FilteredLinksFragment.class, R.string.links, 3);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_filters, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				navigateUpFromSameTask();
				return true;
			}
			case MENU_ADD: {
				final Fragment f = mAdapter.getItem(mViewPager.getCurrentItem());
				if (!(f instanceof BaseFiltersFragment)) return true;
				final Bundle args = new Bundle();
				if (f instanceof FilteredUsersFragment) {
					final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
					intent.setClass(this, UserListSelectorActivity.class);
					intent.putExtra(EXTRA_ACCOUNT_ID, getDefaultAccountId(this));
					startActivityForResult(intent, REQUEST_SELECT_USER);
					return true;
				}
				if (f instanceof FilteredSourcesFragment) {
					args.putInt(EXTRA_AUTO_COMPLETE_TYPE, AUTO_COMPLETE_TYPE_SOURCES);
				}
				args.putParcelable(EXTRA_URI, ((BaseFiltersFragment) f).getContentUri());
				final AddItemFragment dialog = new AddItemFragment();
				dialog.setArguments(args);
				dialog.show(getFragmentManager(), "add_rule");
				return true;
			}
			case R.id.enable_in_home_timeline: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(KEY_FILTERS_IN_HOME_TIMELINE, !item.isChecked());
				editor.apply();
				break;
			}
			case R.id.enable_in_mentions: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(KEY_FILTERS_IN_MENTIONS, !item.isChecked());
				editor.apply();
				break;
			}
			case R.id.enable_for_rts: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(KEY_FILTERS_FOR_RTS, !item.isChecked());
				editor.apply();
				break;
			}
		}
		return false;
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(final int state) {

	}

	@Override
	public void onPageSelected(final int position) {
		if (mActionBar == null) return;
		mActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean enable_in_home_timeline = mPreferences.getBoolean(KEY_FILTERS_IN_HOME_TIMELINE, true);
		final boolean enable_in_mentions = mPreferences.getBoolean(KEY_FILTERS_IN_MENTIONS, true);
		final boolean enable_for_rts = mPreferences.getBoolean(KEY_FILTERS_FOR_RTS, true);
		menu.findItem(R.id.enable_in_home_timeline).setChecked(enable_in_home_timeline);
		menu.findItem(R.id.enable_in_mentions).setChecked(enable_in_mentions);
		menu.findItem(R.id.enable_for_rts).setChecked(enable_for_rts);
		return true;
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {

	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SELECT_USER: {
				final Fragment filter = mAdapter.getItem(mViewPager.getCurrentItem());
				if (resultCode != RESULT_OK || !(filter instanceof FilteredUsersFragment) || !data.hasExtra(EXTRA_USER))
					return;
				final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
				final ContentValues values = makeFilterdUserContentValues(user);
				final ContentResolver resolver = getContentResolver();
				resolver.delete(Filters.Users.CONTENT_URI, Where.equals(Filters.Users.USER_ID, user.id).getSQL(), null);
				resolver.insert(Filters.Users.CONTENT_URI, values);
				break;
			}
		}
	}

	private void addTab(final Class<? extends Fragment> cls, final int name, final int position) {
		if (mActionBar == null || mAdapter == null) return;
		mActionBar.addTab(mActionBar.newTab().setText(name).setTabListener(this));
		mAdapter.addTab(cls, null, getString(name), null, position);
	}

	public static final class AddItemFragment extends BaseDialogFragment implements OnClickListener {

		private AutoCompleteTextView mEditText;

		private SimpleCursorAdapter mUserAutoCompleteAdapter;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (mEditText.length() <= 0) return;
					final ContentValues values = new ContentValues();
					values.put(Filters.VALUE, getText());
					final Bundle args = getArguments();
					final Uri uri = args.getParcelable(EXTRA_URI);
					getContentResolver().insert(uri, values);
					break;
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
			final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
			buildDialog(builder);
			final View view = LayoutInflater.from(wrapped).inflate(R.layout.auto_complete_textview, null);
			builder.setView(view);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			final Bundle args = getArguments();
			final int auto_complete_type = args != null ? args.getInt(EXTRA_AUTO_COMPLETE_TYPE, 0) : 0;
			if (auto_complete_type != 0) {
				if (auto_complete_type == AUTO_COMPLETE_TYPE_SOURCES) {
					mUserAutoCompleteAdapter = new SourceAutoCompleteAdapter(getActivity());
				} else {
					mUserAutoCompleteAdapter = new UserHashtagAutoCompleteAdapter(getActivity());
				}
				mEditText.setAdapter(mUserAutoCompleteAdapter);
				mEditText.setThreshold(1);
			}
			builder.setTitle(R.string.add_rule);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		protected String getText() {
			return ParseUtils.parseString(mEditText.getText());
		}

		private void buildDialog(final Builder builder) {
			// TODO Auto-generated method stub

		}
	}

}
