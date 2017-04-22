/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.loader.statuses

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import org.mariotaku.commons.parcel.ParcelUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.SearchQuery
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import java.util.*

class ConversationLoader(
        context: Context,
        status: ParcelableStatus,
        adapterData: List<ParcelableStatus>?,
        fromUser: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, status.account_key, adapterData, null, -1, fromUser, loadingMore) {

    private val status = ParcelUtils.clone(status)
    private var canLoadAllReplies: Boolean = false

    init {
        ParcelableStatusUtils.makeOriginalStatus(this.status)
    }

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> return getMastodonStatuses(account, paging).mapTo(PaginatedArrayList()) {
                it.toParcelable(account)
            }
            else -> return getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
                it.toParcelable(account, profileImageSize)
            }
        }
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): List<MastodonStatus> {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        canLoadAllReplies = true
        val statusContext = mastodon.getStatusContext(status.id)
        return statusContext.ancestors + statusContext.descendants
    }

    @Throws(MicroBlogException::class)
    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        canLoadAllReplies = false
        when (account.type) {
            AccountType.TWITTER -> {
                val isOfficial = account.isOfficial(context)
                canLoadAllReplies = isOfficial
                if (isOfficial) {
                    return microBlog.showConversation(status.id, paging)
                }
                return showConversationCompat(microBlog, account, status, true)
            }
            AccountType.STATUSNET -> {
                canLoadAllReplies = true
                status.extras?.statusnet_conversation_id?.let {
                    return microBlog.getStatusNetConversation(it, paging)
                }
            }
            AccountType.FANFOU -> {
                canLoadAllReplies = true
                return microBlog.getContextTimeline(status.id, paging)
            }
            else -> {
                throw APINotSupportedException(account.type)
            }
        }
        canLoadAllReplies = true
        return showConversationCompat(microBlog, account, status, true)
    }

    @Throws(MicroBlogException::class)
    private fun showConversationCompat(twitter: MicroBlog, details: AccountDetails,
            status: ParcelableStatus, loadReplies: Boolean): List<Status> {
        val statuses = ArrayList<Status>()
        val pagination = this.pagination as? SinceMaxPagination
        val maxId = pagination?.maxId
        val sinceId = pagination?.sinceId
        val maxSortId = pagination?.maxSortId ?: -1
        val sinceSortId = pagination?.sinceSortId ?: -1
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
                if (details.type == AccountType.TWITTER) {
                    query.query("to:${status.user_screen_name}")
                } else {
                    query.query("@${status.user_screen_name}")
                }
                query.sinceId(sinceId ?: status.id)
                try {
                    twitter.search(query).filterTo(statuses) { it.inReplyToStatusId == status.id }
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

