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
import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;

import java.util.List;

public class UserFriendsLoader extends CursorSupportUsersLoader {

    private final UserKey mUserKey;
    private final String mScreenName;

    public UserFriendsLoader(final Context context, final UserKey accountKey, final UserKey userKey,
                             final String screenName, final List<ParcelableUser> userList,
                             boolean fromUser) {
        super(context, accountKey, userList, fromUser);
        mUserKey = userKey;
        mScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<User> getCursoredUsers(@NonNull final MicroBlog twitter, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging)
            throws MicroBlogException {
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.STATUSNET: {
                if (mUserKey != null) {
                    return twitter.getStatusesFriendsList(mUserKey.getId(), paging);
                } else if (mScreenName != null) {
                    return twitter.getStatusesFriendsListByScreenName(mScreenName, paging);
                }
            }
            case ParcelableAccount.Type.FANFOU: {
                if (mUserKey != null) {
                    return twitter.getUsersFriends(mUserKey.getId(), paging);
                } else if (mScreenName != null) {
                    return twitter.getUsersFriends(mScreenName, paging);
                }
            }
            default: {
                if (mUserKey != null) {
                    return twitter.getFriendsList(mUserKey.getId(), paging);
                } else if (mScreenName != null) {
                    return twitter.getFriendsListByScreenName(mScreenName, paging);
                }
            }
        }
        throw new MicroBlogException("user_id or screen_name required");
    }
}
