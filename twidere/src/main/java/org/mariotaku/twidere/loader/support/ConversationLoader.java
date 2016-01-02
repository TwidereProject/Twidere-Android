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
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.ArrayList;
import java.util.List;

public class ConversationLoader extends TwitterAPIStatusesLoader {

    private final long mInReplyToStatusId;

    public ConversationLoader(final Context context, final long accountId, final long statusId,
                              final String screenName, final long sinceId, final long maxId,
                              final List<ParcelableStatus> data, final boolean fromUser) {
        super(context, accountId, sinceId, maxId, data, null, -1, fromUser);
        mInReplyToStatusId = statusId;
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
        return twitter.showConversation(mInReplyToStatusId, paging);
    }

    @Override
    protected boolean shouldFilterStatus(SQLiteDatabase database, ParcelableStatus status) {
        return false;
    }

}
