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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

public class UserFavoritesLoader extends TwitterAPIStatusesLoader {

    private final String mUserId;
    private final String mUserScreenName;

    public UserFavoritesLoader(final Context context, final UserKey accountKey, final String userId,
                               final String screenName, final String sinceId, final String maxId,
                               final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, boolean fromUser) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
        mUserId = userId;
        mUserScreenName = screenName;
    }

    @NonNull
    @Override
    public ResponseList<Status> getStatuses(@NonNull final Twitter twitter, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging) throws TwitterException {
        if (mUserId != null) {
            return twitter.getFavorites(mUserId, paging);
        } else if (mUserScreenName != null) {
            return twitter.getFavoritesByScreenName(mUserScreenName, paging);
        }
        throw new TwitterException("Null user");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return false;
    }
}
