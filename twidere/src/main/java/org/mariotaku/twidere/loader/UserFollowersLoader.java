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
import android.support.annotation.Nullable;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;

import java.util.List;

public class UserFollowersLoader extends CursorSupportUsersLoader {

    @Nullable
    private final UserKey mUserKey;
    @Nullable
    private final String mScreenName;

    public UserFollowersLoader(final Context context, final UserKey accountId,
                               @Nullable final UserKey userKey, @Nullable final String screenName,
                               final List<ParcelableUser> data, final boolean fromUser) {
        super(context, accountId, data, fromUser);
        mUserKey = userKey;
        mScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<User> getCursoredUsers(@NonNull final Twitter twitter, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging)
            throws TwitterException {
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.STATUSNET: {
                if (mUserKey != null) {
                    return twitter.getStatusesFollowersList(mUserKey.getId(), paging);
                } else if (mScreenName != null) {
                    return twitter.getStatusesFollowersListByScreenName(mScreenName, paging);
                }
            }
            case ParcelableAccount.Type.FANFOU: {
                if (mUserKey != null) {
                    return twitter.getUsersFollowers(mUserKey.getId(), paging);
                } else if (mScreenName != null) {
                    return twitter.getUsersFollowers(mScreenName, paging);
                }
            }
            default: {
                if (mUserKey != null) {
                    return twitter.getFollowersList(mUserKey.getId(), paging);
                } else if (mScreenName != null) {
                    return twitter.getFollowersListByScreenName(mScreenName, paging);
                }
            }
        }
        throw new TwitterException("user_id or screen_name required");
    }

}
