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

package org.mariotaku.twidere.task

import android.content.ContentValues
import android.content.Context
import com.twitter.Extractor
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.TwitterWrapper.TwitterListResponse
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*

class CacheUsersStatusesTask(private val context: Context) : AbstractTask<TwitterListResponse<Status>, Any, Any>() {

    public override fun doLongOperation(params: TwitterListResponse<Status>): Any? {
        val resolver = context.contentResolver
        val extractor = Extractor()
        val list = params.data ?: return null
        var bulkIdx = 0
        val totalSize = list.size
        while (bulkIdx < totalSize) {
            var idx = bulkIdx
            val end = Math.min(totalSize, bulkIdx + ContentResolverUtils.MAX_BULK_COUNT)
            while (idx < end) {
                val status = list.get(idx)

                val usersValues = HashSet<ContentValues>()
                val statusesValues = HashSet<ContentValues>()
                val hashTagValues = HashSet<ContentValues>()

                val accountKey = params.accountKey
                statusesValues.add(ContentValuesCreator.createStatus(status, accountKey))
                val text = InternalTwitterContentUtils.unescapeTwitterStatusText(status.getExtendedText())
                for (hashtag in extractor.extractHashtags(text)) {
                    val values = ContentValues()
                    values.put(CachedHashtags.NAME, hashtag)
                    hashTagValues.add(values)
                }
                val cachedUser = ContentValuesCreator.createCachedUser(status.getUser())
                cachedUser.put(CachedUsers.LAST_SEEN, System.currentTimeMillis())
                usersValues.add(cachedUser)
                if (status.isRetweet()) {
                    val cachedRetweetedUser = ContentValuesCreator.createCachedUser(status.getRetweetedStatus().getUser())
                    cachedRetweetedUser.put(CachedUsers.LAST_SEEN, System.currentTimeMillis())
                    usersValues.add(cachedRetweetedUser)
                }

                ContentResolverUtils.bulkInsert(resolver, CachedStatuses.CONTENT_URI, statusesValues)
                ContentResolverUtils.bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashTagValues)
                ContentResolverUtils.bulkInsert(resolver, CachedUsers.CONTENT_URI, usersValues)
                idx++
            }
            bulkIdx += 100
        }
        return null
    }

}
