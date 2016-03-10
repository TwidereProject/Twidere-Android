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
import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.TwitterAPIFactory;

import java.util.List;
import java.util.Locale;

public class UserMentionsLoader extends TweetSearchLoader {

    public UserMentionsLoader(final Context context, final UserKey accountId, final String screenName,
                              final String maxId, final String sinceId, int page, final List<ParcelableStatus> data,
                              final String[] savedStatusesArgs, final int tabPosition, boolean fromUser,
                              boolean makeGap, boolean loadingMore) {
        super(context, accountId, screenName, sinceId, maxId, page, data, savedStatusesArgs, tabPosition,
                fromUser, makeGap, loadingMore);
    }

    @NonNull
    @Override
    protected String processQuery(ParcelableCredentials credentials, @NonNull final String query) {
        final String screenName = query.startsWith("@") ? query.substring(1) : query;
        if (TwitterAPIFactory.isTwitterCredentials(getContext(), getAccountKey())) {
            return String.format(Locale.ROOT, "to:%s exclude:retweets", screenName);
        }
        return String.format(Locale.ROOT, "@%s -RT", screenName);
    }

}
