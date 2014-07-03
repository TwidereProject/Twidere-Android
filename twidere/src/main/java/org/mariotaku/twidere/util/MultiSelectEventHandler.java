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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.ContentValuesCreator.makeFilterdUserContentValues;
import static org.mariotaku.twidere.util.Utils.getAccountScreenNames;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkDelete;
import static org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.twitter.Extractor;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.BaseSupportActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.menu.AccountActionProvider;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SuppressLint("Registered")
public class MultiSelectEventHandler implements Constants, ActionMode.Callback, MultiSelectManager.Callback {

	private TwidereApplication mApplication;

	private AsyncTwitterWrapper mTwitterWrapper;

	private MultiSelectManager mMultiSelectManager;

	private ActionMode mActionMode;

	private final BaseSupportActivity mActivity;

	private AccountActionProvider mAccountActionProvider;

	public static final int MENU_GROUP = 201;

	public MultiSelectEventHandler(final BaseSupportActivity activity) {
		mActivity = activity;
	}

	/**
	 * Call before super.onCreate
	 */
	public void dispatchOnCreate() {
		mApplication = mActivity.getTwidereApplication();
		mTwitterWrapper = mApplication.getTwitterWrapper();
		mMultiSelectManager = mApplication.getMultiSelectManager();
	}

	/**
	 * Call after super.onStart
	 */
	public void dispatchOnStart() {
		mMultiSelectManager.registerCallback(this);
		updateMultiSelectState();
	}

	/**
	 * Call before super.onStop
	 */
	public void dispatchOnStop() {
		mMultiSelectManager.unregisterCallback(this);
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		final List<Object> selectedItems = mMultiSelectManager.getSelectedItems();
		if (selectedItems.isEmpty()) return false;
		switch (item.getItemId()) {
			case MENU_REPLY: {
				final Extractor extractor = new Extractor();
				final Intent intent = new Intent(INTENT_ACTION_REPLY_MULTIPLE);
				final Bundle bundle = new Bundle();
				final String[] accountScreenNames = getAccountScreenNames(mActivity);
				final Collection<String> allMentions = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
				for (final Object object : selectedItems) {
					if (object instanceof ParcelableStatus) {
						final ParcelableStatus status = (ParcelableStatus) object;
						allMentions.add(status.user_screen_name);
						allMentions.addAll(extractor.extractMentionedScreennames(status.text_plain));
					} else if (object instanceof ParcelableUser) {
						final ParcelableUser user = (ParcelableUser) object;
						allMentions.add(user.screen_name);
					}
				}
				allMentions.removeAll(Arrays.asList(accountScreenNames));
				final Object firstObj = selectedItems.get(0);
				if (firstObj instanceof ParcelableStatus) {
					final ParcelableStatus first_status = (ParcelableStatus) firstObj;
					bundle.putLong(EXTRA_IN_REPLY_TO_ID, first_status.id);
				}
				bundle.putLong(EXTRA_ACCOUNT_ID, mMultiSelectManager.getAccountId());
				bundle.putStringArray(EXTRA_SCREEN_NAMES, allMentions.toArray(new String[allMentions.size()]));
				intent.putExtras(bundle);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(intent);
				mode.finish();
				break;
			}
			case MENU_MUTE_USER: {
				final ContentResolver resolver = mActivity.getContentResolver();
				final ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
				final Set<Long> userIds = new HashSet<Long>();
				for (final Object object : selectedItems) {
					if (object instanceof ParcelableStatus) {
						final ParcelableStatus status = (ParcelableStatus) object;
						userIds.add(status.user_id);
						valuesList.add(makeFilterdUserContentValues(status));
					} else if (object instanceof ParcelableUser) {
						final ParcelableUser user = (ParcelableUser) object;
						userIds.add(user.id);
						valuesList.add(makeFilterdUserContentValues(user));
					} else {
						continue;
					}
				}
				bulkDelete(resolver, Filters.Users.CONTENT_URI, Filters.Users.USER_ID, userIds, null, false);
				bulkInsert(resolver, Filters.Users.CONTENT_URI, valuesList);
				Crouton.showText(mActivity, R.string.message_users_muted, CroutonStyle.INFO);
				mode.finish();
				mActivity.sendBroadcast(new Intent(BROADCAST_MULTI_MUTESTATE_CHANGED));
				break;
			}
			case MENU_BLOCK: {
				final long accountId = mMultiSelectManager.getAccountId();
				final long[] userIds = MultiSelectManager.getSelectedUserIds(selectedItems);
				if (accountId > 0 && userIds != null) {
					mTwitterWrapper.createMultiBlockAsync(accountId, userIds);
				}
				mode.finish();
				break;
			}
			case MENU_REPORT_SPAM: {
				final long accountId = mMultiSelectManager.getAccountId();
				final long[] userIds = MultiSelectManager.getSelectedUserIds(selectedItems);
				if (accountId > 0 && userIds != null) {
					mTwitterWrapper.reportMultiSpam(accountId, userIds);
				}
				mode.finish();
				break;
			}
		}
		if (item.getGroupId() == AccountActionProvider.MENU_GROUP) {
			final Intent intent = item.getIntent();
			if (intent == null || !intent.hasExtra(EXTRA_ACCOUNT)) return false;
			final Account account = intent.getParcelableExtra(EXTRA_ACCOUNT);
			mMultiSelectManager.setAccountId(account.account_id);
			if (mAccountActionProvider != null) {
				mAccountActionProvider.setAccountId(account.account_id);
			}
			mode.invalidate();
		}
		return true;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		new MenuInflater(mActivity).inflate(R.menu.action_multi_select_contents, menu);
		mAccountActionProvider = (AccountActionProvider) menu.findItem(MENU_SELECT_ACCOUNT).getActionProvider();
		mAccountActionProvider.setAccountId(mMultiSelectManager.getFirstSelectAccountId());
		return true;
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {
		if (mMultiSelectManager.getCount() != 0) {
			mMultiSelectManager.clearSelectedItems();
		}
		mAccountActionProvider = null;
		mActionMode = null;
	}

	@Override
	public void onItemsCleared() {
		updateMultiSelectState();
	}

	@Override
	public void onItemSelected(final Object item) {
		updateMultiSelectState();
	}

	@Override
	public void onItemUnselected(final Object item) {
		updateMultiSelectState();
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		updateSelectedCount(mode);
		return true;
	}

	private void updateMultiSelectState() {
		if (mMultiSelectManager.isActive()) {
			if (mActionMode == null) {
				mActionMode = mActivity.startActionMode(this);
			}
			updateSelectedCount(mActionMode);
		} else {
			if (mActionMode != null) {
				mActionMode.finish();
				mActionMode = null;
			}
		}
	}

	private void updateSelectedCount(final ActionMode mode) {
		if (mode == null || mActivity == null || mMultiSelectManager == null) return;
		final int count = mMultiSelectManager.getCount();
		mode.setTitle(mActivity.getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
	}

}
