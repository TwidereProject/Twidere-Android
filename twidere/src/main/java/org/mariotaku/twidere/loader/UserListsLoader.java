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

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

public class UserListsLoader extends BaseUserListsLoader {

    private final UserKey mUserKey;
    private final String mScreenName;
    private final boolean mReverse;

    public UserListsLoader(final Context context, final UserKey accountKey, final UserKey userKey,
                           final String screenName, final boolean reverse, final List<ParcelableUserList> data) {
        super(context, accountKey, 0, data);
        mUserKey = userKey;
        mScreenName = screenName;
        mReverse = reverse;
    }

    @Override
    public ResponseList<UserList> getUserLists(final MicroBlog twitter) throws MicroBlogException {
        if (twitter == null) return null;
        if (mUserKey != null) {
            return twitter.getUserLists(mUserKey.getId(), mReverse);
        } else if (mScreenName != null) {
            return twitter.getUserListsByScreenName(mScreenName, mReverse);
        }
        return null;
    }

    @Override
    protected boolean isFollowing(final UserList list) {
        return true;
    }
}
