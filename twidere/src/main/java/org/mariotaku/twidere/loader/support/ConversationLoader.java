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

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.SearchQuery;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusUtils;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.util.Nullables;
import org.mariotaku.twidere.util.ParcelUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ConversationLoader extends TwitterAPIStatusesLoader {

    private final boolean mTwitterOptimizedSearches;

    @NonNull
    private final ParcelableStatus mStatus;

    public ConversationLoader(final Context context, @NonNull final ParcelableStatus status,
                              final long sinceId, final long maxId, final List<ParcelableStatus> data,
                              final boolean fromUser, final boolean twitterOptimizedSearches) {
        super(context, status.account_id, sinceId, maxId, data, null, -1, fromUser);
        mStatus = Nullables.assertNonNull(ParcelUtils.clone(status));
        ParcelableStatusUtils.makeOriginalStatus(mStatus);
        mTwitterOptimizedSearches = twitterOptimizedSearches;
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
        final ParcelableStatus status = mStatus;
        if (Utils.shouldForceUsingPrivateAPIs(getContext()) || TwitterAPIFactory.isOfficialTwitterInstance(getContext(), twitter)) {
            return twitter.showConversation(status.id, paging);
        }
        final List<Status> statuses = new ArrayList<>();
        final long maxId = getMaxId(), sinceId = getSinceId();
        boolean getConversations = maxId < status.id || (maxId <= 0 && sinceId <= 0);
        boolean getReplies = sinceId > status.id || (maxId <= 0 && sinceId <= 0);
        if (getConversations) {
            long inReplyToId = status.in_reply_to_status_id;
            int count = 0;
            while (inReplyToId > 0 && count < 10) {
                final Status item = twitter.showStatus(inReplyToId);
                inReplyToId = item.getInReplyToStatusId();
                statuses.add(item);
                count++;
            }
        }
        if ((sinceId <= 0 && maxId <= 0) || (sinceId > 0 && sinceId < status.id) || (maxId > 0 && maxId >= status.id)) {
//            statuses.add(StatusUtils.fromParcelableStatus(status));
        }
        if (getReplies) {
            SearchQuery query = new SearchQuery();
            if (mTwitterOptimizedSearches) {
                query.query("to:" + status.user_screen_name);
            } else {
                query.query("@" + status.user_screen_name);
            }
            query.sinceId(status.id);
            try {
                for (Status item : twitter.search(query)) {
                    if (item.getInReplyToStatusId() == status.id) {
                        statuses.add(item);
                    }
                }
            } catch (TwitterException e) {
                // Ignore for now
            }
        }
        return statuses;
    }

    @Override
    protected boolean shouldFilterStatus(SQLiteDatabase database, ParcelableStatus status) {
        return false;
    }

}
