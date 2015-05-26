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

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.loader.support.iface.ICursorSupportLoader;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.api.twitter.model.CursorSupport;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.UserList;

import static org.mariotaku.twidere.util.TwitterAPIFactory.getTwitterInstance;

public abstract class BaseUserListsLoader extends AsyncTaskLoader<List<ParcelableUserList>>
        implements ICursorSupportLoader {

    protected final NoDuplicatesArrayList<ParcelableUserList> mData = new NoDuplicatesArrayList<>();
    protected final long mAccountId;
    private final long mCursor;

    private long mNextCursor, mPrevCursor;

    public BaseUserListsLoader(final Context context, final long accountId, final long cursor,
                               final List<ParcelableUserList> data) {
        super(context);
        if (data != null) {
            mData.addAll(data);
        }
        mCursor = cursor;
        mAccountId = accountId;
    }

    @Override
    public long getCursor() {
        return mCursor;
    }

    @Override
    public long getNextCursor() {
        return mNextCursor;
    }

    @Override
    public long getPrevCursor() {
        return mPrevCursor;
    }

    public abstract List<UserList> getUserLists(final Twitter twitter) throws TwitterException;

    @Override
    public List<ParcelableUserList> loadInBackground() {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, true);
        List<UserList> listLoaded = null;
        try {
            listLoaded = getUserLists(twitter);
        } catch (final TwitterException e) {
            e.printStackTrace();
        }
        if (listLoaded != null) {
            final int listSize = listLoaded.size();
            if (listLoaded instanceof PageableResponseList) {
                mNextCursor = ((CursorSupport) listLoaded).getNextCursor();
                mPrevCursor = ((CursorSupport) listLoaded).getPreviousCursor();
                final int dataSize = mData.size();
                for (int i = 0; i < listSize; i++) {
                    final UserList list = listLoaded.get(i);
                    mData.add(new ParcelableUserList(list, mAccountId, dataSize + i, isFollowing(list)));
                }
            } else {
                for (int i = 0; i < listSize; i++) {
                    final UserList list = listLoaded.get(i);
                    mData.add(new ParcelableUserList(listLoaded.get(i), mAccountId, i, isFollowing(list)));
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
