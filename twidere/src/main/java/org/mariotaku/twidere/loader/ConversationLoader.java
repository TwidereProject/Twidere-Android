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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.mariotaku.commons.parcel.ParcelUtils;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.SearchQuery;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.Nullables;
import org.mariotaku.twidere.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ConversationLoader extends MicroBlogAPIStatusesLoader {

    @NonNull
    private final ParcelableStatus mStatus;
    private final long mSinceSortId, mMaxSortId;
    private boolean mCanLoadAllReplies;

    public ConversationLoader(final Context context, @NonNull final ParcelableStatus status,
                              final String sinceId, final String maxId,
                              final long sinceSortId, final long maxSortId,
                              final List<ParcelableStatus> data, final boolean fromUser,
                              final boolean loadingMore) {
        super(context, status.account_key, sinceId, maxId, data, null, -1, fromUser, loadingMore);
        mStatus = Nullables.assertNonNull(ParcelUtils.clone(status));
        mSinceSortId = sinceSortId;
        mMaxSortId = maxSortId;
        ParcelableStatusUtils.makeOriginalStatus(mStatus);
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final MicroBlog microBlog,
                                    @NonNull final ParcelableCredentials credentials,
                                    @NonNull final Paging paging) throws MicroBlogException {
        mCanLoadAllReplies = false;
        final ParcelableStatus status = mStatus;
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.TWITTER: {
                final boolean isOfficial = Utils.isOfficialCredentials(getContext(), credentials);
                mCanLoadAllReplies = isOfficial;
                if (isOfficial) {
                    return microBlog.showConversation(status.id, paging);
                } else {
                    return showConversationCompat(microBlog, credentials, status, true);
                }
            }
            case ParcelableAccount.Type.STATUSNET: {
                mCanLoadAllReplies = true;
                if (status.extras != null && status.extras.statusnet_conversation_id != null) {
                    return microBlog.getStatusNetConversation(status.extras.statusnet_conversation_id,
                            paging);
                }
                return microBlog.showConversation(status.id, paging);
            }
            case ParcelableAccount.Type.FANFOU: {
                mCanLoadAllReplies = true;
                return microBlog.getContextTimeline(status.id, paging);
            }
        }
        // Set to true because there's no conversation support on this platform
        mCanLoadAllReplies = true;
        return showConversationCompat(microBlog, credentials, status, false);
    }

    protected List<Status> showConversationCompat(@NonNull final MicroBlog twitter,
                                                  @NonNull final ParcelableCredentials credentials,
                                                  @NonNull final ParcelableStatus status,
                                                  final boolean loadReplies) throws MicroBlogException {
        final List<Status> statuses = new ArrayList<>();
        final String maxId = getMaxId(), sinceId = getSinceId();
        final long maxSortId = getMaxSortId(), sinceSortId = getSinceSortId();
        final boolean noSinceMaxId = maxId == null && sinceId == null;
        // Load conversations
        if ((maxId != null && maxSortId < status.sort_id) || noSinceMaxId) {
            String inReplyToId = maxId != null ? maxId : status.in_reply_to_status_id;
            int count = 0;
            while (inReplyToId != null && count < 10) {
                final Status item = twitter.showStatus(inReplyToId);
                inReplyToId = item.getInReplyToStatusId();
                statuses.add(item);
                count++;
            }
        }
        if (loadReplies) {
            // Load replies
            if ((sinceId != null && sinceSortId > status.sort_id) || noSinceMaxId) {
                SearchQuery query = new SearchQuery();
                if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
                    query.query("to:" + status.user_screen_name);
                } else {
                    query.query("@" + status.user_screen_name);
                }
                query.sinceId(sinceId != null ? sinceId : status.id);
                try {
                    for (Status item : twitter.search(query)) {
                        if (TextUtils.equals(item.getInReplyToStatusId(), status.id)) {
                            statuses.add(item);
                        }
                    }
                } catch (MicroBlogException e) {
                    // Ignore for now
                }
            }
        }
        return statuses;
    }

    public boolean canLoadAllReplies() {
        return mCanLoadAllReplies;
    }

    public long getSinceSortId() {
        return mSinceSortId;
    }

    public long getMaxSortId() {
        return mMaxSortId;
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(SQLiteDatabase database, ParcelableStatus status) {
        return InternalTwitterContentUtils.isFiltered(database, status, false);
    }

}
