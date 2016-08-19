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
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import android.text.TextUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import java.util.concurrent.atomic.AtomicReference

class UserTimelineLoader(
        context: Context,
        accountId: UserKey?,
        private val userId: UserKey?,
        private val screenName: String?,
        sinceId: String?,
        maxId: String?,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean,
        val pinnedStatusIds: Array<String>?
) : MicroBlogAPIStatusesLoader(context, accountId, sinceId, maxId, -1, data, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    private val pinnedStatusesRef = AtomicReference<List<ParcelableStatus>>()

    var pinnedStatuses: List<ParcelableStatus>?
        get() = pinnedStatusesRef.get()
        private set(value) {
            pinnedStatusesRef.set(value)
        }

    @Throws(MicroBlogException::class)
    override fun getStatuses(microBlog: MicroBlog,
                             credentials: ParcelableCredentials,
                             paging: Paging): ResponseList<Status> {
        if (pinnedStatusIds != null) {
            try {
                pinnedStatuses = microBlog.lookupStatuses(pinnedStatusIds).mapIndexed { idx, status ->
                    val created = ParcelableStatusUtils.fromStatus(status, credentials.account_key, false)
                    created.sort_id = idx.toLong()
                    return@mapIndexed created
                }
            } catch (e: MicroBlogException) {
                // Ignore
            }
        }
        if (userId != null) {
            return microBlog.getUserTimeline(userId.id, paging)
        } else if (screenName != null) {
            return microBlog.getUserTimelineByScreenName(screenName, paging)
        } else {
            throw MicroBlogException("Invalid user")
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        val accountId = accountKey
        if (accountId != null && userId != null && TextUtils.equals(accountId.id, userId.id))
            return false
        val retweetUserId = if (status.is_retweet) status.user_key else null
        return InternalTwitterContentUtils.isFiltered(database, retweetUserId, status.text_plain,
                status.quoted_text_plain, status.spans, status.quoted_spans, status.source,
                status.quoted_source, null, status.quoted_user_key)
    }
}
