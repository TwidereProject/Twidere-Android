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

package org.mariotaku.twidere.loader

import android.content.Context
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.MicroBlogAPIFactory

class UserMentionsLoader(
        context: Context,
        accountId: UserKey,
        screenName: String,
        maxId: String?,
        sinceId: String?,
        page: Int,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        makeGap: Boolean,
        loadingMore: Boolean
) : TweetSearchLoader(context, accountId, screenName, sinceId, maxId, page, data, savedStatusesArgs,
        tabPosition, fromUser, makeGap, loadingMore) {

    override fun processQuery(credentials: ParcelableCredentials, query: String): String {
        val accountKey = accountKey ?: return query
        val screenName = if (query.startsWith("@")) query.substring(1) else query
        if (MicroBlogAPIFactory.isTwitterCredentials(context, accountKey)) {
            return "to:$screenName exclude:retweets"
        }
        return "@$screenName -RT"
    }

}
