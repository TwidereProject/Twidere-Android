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
import android.content.SharedPreferences;

import org.mariotaku.twidere.loader.support.iface.ICursorSupportLoader;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.List;

import org.mariotaku.twidere.api.twitter.model.CursorSupport;

public abstract class BaseCursorSupportUsersLoader extends TwitterAPIUsersLoader
        implements ICursorSupportLoader {

    private final long mCursor;
    private final SharedPreferences mPreferences;
    private final int mLoadItemLimit;

    private long mNextCursor, mPrevCursor;

    public BaseCursorSupportUsersLoader(final Context context, final long accountId, final long cursor,
                                        final List<ParcelableUser> data, boolean fromUser) {
        super(context, accountId, data, fromUser);
        mCursor = cursor;
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int loadItemLimit = mPreferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        mLoadItemLimit = Math.min(100, loadItemLimit);
    }

    public final int getCount() {
        return mLoadItemLimit;
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

    protected final void setCursorIds(final CursorSupport cursor) {
        if (cursor == null) return;
        mNextCursor = cursor.getNextCursor();
        mPrevCursor = cursor.getPreviousCursor();
    }

}
