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
import org.mariotaku.commons.parcel.ParcelUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.SearchQuery
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.Nullables
import org.mariotaku.twidere.util.Utils
import java.util.*

class ConversationLoader(
        context: Context,
        status: ParcelableStatus,
        sinceId: String?,
        maxId: String?,
        val sinceSortId: Long,
        val maxSortId: Long,
        adapterData: List<ParcelableStatus>?,
        fromUser: Boolean,
        loadingMore: Boolean
) : MicroBlogAPIStatusesLoader(context, status.account_key, sinceId, maxId, -1, adapterData, null, -1,
        fromUser, loadingMore) {

    private val status: ParcelableStatus
    private var canLoadAllReplies: Boolean = false

    init {
        this.status = Nullables.assertNonNull(ParcelUtils.clone(status))
        ParcelableStatusUtils.makeOriginalStatus(this.status)
    }

    @Throws(MicroBlogException::class)
    public override fun getStatuses(microBlog: MicroBlog,
                                    credentials: ParcelableCredentials,
                                    paging: Paging): List<Status> {
        canLoadAllReplies = false
        when (ParcelableAccountUtils.getAccountType(credentials)) {
            ParcelableAccount.Type.TWITTER -> {
                val isOfficial = Utils.isOfficialCredentials(context, credentials)
                canLoadAllReplies = isOfficial
                if (isOfficial) {
                    return microBlog.showConversation(status.id, paging)
                } else {
                    return showConversationCompat(microBlog, credentials, status, true)
                }
            }
            ParcelableAccount.Type.STATUSNET -> {
                canLoadAllReplies = true
                if (status.extras != null && status.extras.statusnet_conversation_id != null) {
                    return microBlog.getStatusNetConversation(status.extras.statusnet_conversation_id, paging)
                }
                return microBlog.showConversation(status.id, paging)
            }
            ParcelableAccount.Type.FANFOU -> {
                canLoadAllReplies = true
                return microBlog.getContextTimeline(status.id, paging)
            }
        }
        // Set to true because there's no conversation support on this platform
        canLoadAllReplies = true
        return showConversationCompat(microBlog, credentials, status, false)
    }

    @Throws(MicroBlogException::class)
    protected fun showConversationCompat(twitter: MicroBlog,
                                         credentials: ParcelableCredentials,
                                         status: ParcelableStatus,
                                         loadReplies: Boolean): List<Status> {
        val statuses = ArrayList<Status>()
        val noSinceMaxId = maxId == null && sinceId == null
        // Load conversations
        if (maxId != null && maxSortId < status.sort_id || noSinceMaxId) {
            var inReplyToId: String? = maxId ?: status.in_reply_to_status_id
            var count = 0
            while (inReplyToId != null && count < 10) {
                val item = twitter.showStatus(inReplyToId)
                inReplyToId = item.inReplyToStatusId
                statuses.add(item)
                count++
            }
        }
        if (loadReplies) {
            // Load replies
            if (sinceId != null && sinceSortId > status.sort_id || noSinceMaxId) {
                val query = SearchQuery()
                if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
                    query.query("to:${status.user_screen_name}")
                } else {
                    query.query("@${status.user_screen_name}")
                }
                query.sinceId(sinceId ?: status.id)
                try {
                    for (item in twitter.search(query)) {
                        if (TextUtils.equals(item.inReplyToStatusId, status.id)) {
                            statuses.add(item)
                        }
                    }
                } catch (e: MicroBlogException) {
                    // Ignore for now
                }

            }
        }
        return statuses
    }

    fun canLoadAllReplies(): Boolean {
        return canLoadAllReplies
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, false)
    }

}
