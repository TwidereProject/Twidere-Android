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

import org.mariotaku.twidere.model.ParcelableUser;

import java.util.List;

import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;

public class StatusRetweetersLoader extends IDsUsersLoader {

    private final long mStatusId;

    public StatusRetweetersLoader(final Context context, final long accountId, final long statusId,
                                  final long cursor, final List<ParcelableUser> data, boolean fromUser) {
        super(context, accountId, cursor, data, fromUser);
        mStatusId = statusId;
    }

    @Override
    protected IDs getIDs(final Twitter twitter, final Paging paging) throws TwitterException {
        if (twitter == null) return null;
        if (mStatusId > 0) return twitter.getRetweetersIDs(mStatusId, paging);
        return null;
    }

}
