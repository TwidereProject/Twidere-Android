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

import org.mariotaku.twidere.model.ParcelableUserList;

import java.util.List;

import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.UserList;

public class UserListsLoader extends BaseUserListsLoader {

    private final long mUserId;
    private final String mScreenName;
    private final boolean mReverse;

    public UserListsLoader(final Context context, final long accountId, final long userId,
                           final String screenName, final boolean reverse, final List<ParcelableUserList> data) {
        super(context, accountId, 0, data);
        mUserId = userId;
        mScreenName = screenName;
        mReverse = reverse;
    }

    @Override
    public ResponseList<UserList> getUserLists(final Twitter twitter) throws TwitterException {
        if (twitter == null) return null;
        if (mUserId > 0)
            return twitter.getUserLists(mUserId, mReverse);
        else if (mScreenName != null) return twitter.getUserLists(mScreenName, mReverse);
        return null;
    }

    @Override
    protected boolean isFollowing(final UserList list) {
        return true;
    }
}
