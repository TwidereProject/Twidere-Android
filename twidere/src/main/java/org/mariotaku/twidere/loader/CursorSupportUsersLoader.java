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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.api.MicroBlog;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.CursorSupport;
import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.loader.iface.ICursorSupportLoader;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

public abstract class CursorSupportUsersLoader extends TwitterAPIUsersLoader
        implements ICursorSupportLoader {

    private int mPage = -1;
    private long mCursor;
    private final int mLoadItemLimit;

    private long mNextCursor, mPrevCursor;
    private int mNextPage;

    public CursorSupportUsersLoader(final Context context, final UserKey accountKey,
                                    final List<ParcelableUser> data, boolean fromUser) {
        super(context, accountKey, data, fromUser);
        final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        mLoadItemLimit = Math.min(100, loadItemLimit);
    }

    public final int getCount() {
        return mLoadItemLimit;
    }

    public final void setCursor(long cursor) {
        mCursor = cursor;
    }

    public final int getPage() {
        return mPage;
    }

    public final void setPage(int page) {
        mPage = page;
    }

    @Override
    public final long getCursor() {
        return mCursor;
    }

    @Override
    public final long getNextCursor() {
        return mNextCursor;
    }

    @Override
    public final long getPrevCursor() {
        return mPrevCursor;
    }

    protected final void setCursors(final CursorSupport cursor) {
        if (cursor == null) return;
        mNextCursor = cursor.getNextCursor();
        mPrevCursor = cursor.getPreviousCursor();
    }

    protected final void incrementPage(List<User> users) {
        if (users.isEmpty()) return;
        if (mPage == -1) {
            mPage = 1;
        }
        mNextPage = mPage + 1;
    }

    public int getNextPage() {
        return mNextPage;
    }


    @NonNull
    protected List<User> getCursoredUsers(@NonNull final MicroBlog twitter,
                                          @NonNull final ParcelableCredentials credentials,
                                          @NonNull final Paging paging)
            throws TwitterException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    protected IDs getIDs(@NonNull final MicroBlog twitter, @NonNull final ParcelableCredentials credentials,
                         @NonNull final Paging paging) throws TwitterException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    protected final List<User> getUsers(@NonNull final MicroBlog twitter, @NonNull ParcelableCredentials credentials) throws TwitterException {
        final Paging paging = new Paging();
        paging.count(getCount());
        if (getCursor() > 0) {
            paging.setCursor(getCursor());
        } else if (getPage() > 1) {
            paging.setPage(getPage());
        }
        final List<User> users;
        if (useIDs(credentials)) {
            final IDs ids = getIDs(twitter, credentials, paging);
            setCursors(ids);
            users = twitter.lookupUsers(ids.getIDs());
        } else {
            users = getCursoredUsers(twitter, credentials, paging);
            if (users instanceof CursorSupport) {
                setCursors((CursorSupport) users);
            }
        }
        incrementPage(users);
        return users;
    }

    protected boolean useIDs(@NonNull ParcelableCredentials credentials) {
        return false;
    }
}
