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

package org.mariotaku.twidere.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.statusnet.model.Group;
import org.mariotaku.microblog.library.twitter.model.CursorSupport;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.loader.iface.ICursorSupportLoader;
import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableGroupUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.Collections;
import java.util.List;


public abstract class BaseGroupsLoader extends AsyncTaskLoader<List<ParcelableGroup>>
        implements TwidereConstants, ICursorSupportLoader {

    protected final NoDuplicatesArrayList<ParcelableGroup> mData = new NoDuplicatesArrayList<>();
    protected final UserKey mAccountId;
    private final long mCursor;

    private long mNextCursor, mPrevCursor;

    public BaseGroupsLoader(final Context context, final UserKey accountKey, final long cursor,
                            final List<ParcelableGroup> data) {
        super(context);
        if (data != null) {
            mData.addAll(data);
        }
        mCursor = cursor;
        mAccountId = accountKey;
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

    public abstract List<Group> getGroups(final MicroBlog twitter) throws MicroBlogException;

    @Override
    public List<ParcelableGroup> loadInBackground() {
        final MicroBlog twitter = MicroBlogAPIFactory.getInstance(getContext(), mAccountId, true);
        List<Group> listLoaded = null;
        try {
            listLoaded = getGroups(twitter);
        } catch (final MicroBlogException e) {
            Log.w(LOGTAG, e);
        }
        if (listLoaded != null) {
            final int listSize = listLoaded.size();
            if (listLoaded instanceof PageableResponseList) {
                mNextCursor = ((CursorSupport) listLoaded).getNextCursor();
                mPrevCursor = ((CursorSupport) listLoaded).getPreviousCursor();
                final int dataSize = mData.size();
                for (int i = 0; i < listSize; i++) {
                    final Group group = listLoaded.get(i);
                    mData.add(ParcelableGroupUtils.from(group, mAccountId, dataSize + i, isMember(group)));
                }
            } else {
                for (int i = 0; i < listSize; i++) {
                    final Group list = listLoaded.get(i);
                    mData.add(ParcelableGroupUtils.from(listLoaded.get(i), mAccountId, i, isMember(list)));
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

    protected boolean isMember(final Group list) {
        return list.isMember();
    }
}
