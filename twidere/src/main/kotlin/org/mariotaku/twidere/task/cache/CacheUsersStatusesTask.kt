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

package org.mariotaku.twidere.task.cache

import android.content.ContentValues
import android.content.Context
import com.twitter.Extractor
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.ktextension.addTo
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*

class CacheUsersStatusesTask(
        private val context: Context,
        private val accountKey: UserKey,
        private val accountType: String,
        private val statuses: List<Status>
) : AbstractTask<Any?, Unit?, Unit?>() {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun doLongOperation(params: Any?) {
        val resolver = context.contentResolver
        val extractor = Extractor()
        var bulkIdx = 0
        val totalSize = statuses.size
        while (bulkIdx < totalSize) {
            var idx = bulkIdx
            val end = Math.min(totalSize, bulkIdx + ContentResolverUtils.MAX_BULK_COUNT)
            while (idx < end) {
                val status = statuses[idx]

                val users = HashSet<User>()
                val statusesValues = HashSet<ContentValues>()
                val hashTagValues = HashSet<ContentValues>()

                statusesValues.add(ContentValuesCreator.createStatus(status, accountKey, accountType,
                        profileImageSize))
                val text = InternalTwitterContentUtils.unescapeTwitterStatusText(status.extendedText)
                for (hashtag in extractor.extractHashtags(text)) {
                    val values = ContentValues()
                    values.put(CachedHashtags.NAME, hashtag)
                    hashTagValues.add(values)
                }

                status.user?.addTo(users)
                status.retweetedStatus?.user?.addTo(users)
                status.quotedStatus?.user?.addTo(users)

                ContentResolverUtils.bulkInsert(resolver, CachedStatuses.CONTENT_URI, statusesValues)
                ContentResolverUtils.bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashTagValues)
                CacheUserRelationshipTask.cacheUserRelationships(resolver, accountKey, accountType,
                        users)
                idx++
            }
            bulkIdx += 100
        }
    }

}
