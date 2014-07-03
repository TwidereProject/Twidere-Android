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

package org.mariotaku.twidere.loader.support;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import twitter4j.CursorSupport;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import java.util.Collections;
import java.util.List;

public abstract class BaseUserListsLoader extends AsyncTaskLoader<List<ParcelableUserList>> {

	protected final NoDuplicatesArrayList<ParcelableUserList> mData = new NoDuplicatesArrayList<ParcelableUserList>();
	protected final long mAccountId;
	private final long mCursor;

	private long mNextCursor, mPrevCursor;

	public BaseUserListsLoader(final Context context, final long account_id, final long cursor,
			final List<ParcelableUserList> data) {
		super(context);
		if (data != null) {
			mData.addAll(data);
		}
		mCursor = cursor;
		mAccountId = account_id;
	}

	public long getCursor() {
		return mCursor;
	}

	public long getNextCursor() {
		return mNextCursor;
	}

	public long getPrevCursor() {
		return mPrevCursor;
	}

	public abstract List<UserList> getUserLists(final Twitter twitter) throws TwitterException;;

	@Override
	public List<ParcelableUserList> loadInBackground() {
		final Twitter twitter = getTwitterInstance(getContext(), mAccountId, true);
		List<UserList> list_loaded = null;
		try {
			list_loaded = getUserLists(twitter);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (list_loaded != null) {
			final int list_size = list_loaded.size();
			if (list_loaded instanceof PagableResponseList) {
				mNextCursor = ((CursorSupport) list_loaded).getNextCursor();
				mPrevCursor = ((CursorSupport) list_loaded).getPreviousCursor();
				for (int i = 0; i < list_size; i++) {
					final UserList list = list_loaded.get(i);
					mData.add(new ParcelableUserList(list, mAccountId, (mCursor + 1) * 20 + i, isFollowing(list)));
				}
			} else {
				for (int i = 0; i < list_size; i++) {
					final UserList list = list_loaded.get(i);
					mData.add(new ParcelableUserList(list_loaded.get(i), mAccountId, i, isFollowing(list)));
				}
			}
		}
		Collections.sort(mData);
		return mData;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	protected boolean isFollowing(final UserList list) {
		return list.isFollowing();
	}
}
