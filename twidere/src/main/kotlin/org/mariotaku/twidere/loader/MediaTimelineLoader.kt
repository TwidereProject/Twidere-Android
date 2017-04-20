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
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.microblog.library.mastodon.model.TimelineOption as MastodonTimelineOption

class MediaTimelineLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        sinceId: String?,
        maxId: String?,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : AbsRequestStatusesLoader(context, accountKey, sinceId, maxId, -1, data, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    private var user: User? = null

    private val isMyTimeline: Boolean
        get() {
            val accountKey = accountKey ?: return false
            if (userKey != null) {
                return userKey.maybeEquals(accountKey)
            } else {
                val accountScreenName = DataStoreUtils.getAccountScreenName(context, accountKey)
                return accountScreenName != null && accountScreenName.equals(screenName, ignoreCase = true)
            }
        }

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> return getMastodonStatuses(account, paging)
            else -> return getMicroBlogStatuses(account, paging).map {
                it.toParcelable(account.key, account.type, profileImageSize)
            }
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        if (status.media.isNullOrEmpty()) return false
        val retweetUserKey = status.user_key.takeIf { status.is_retweet }
        return !isMyTimeline && InternalTwitterContentUtils.isFiltered(database, retweetUserKey,
                status.text_plain, status.quoted_text_plain, status.spans, status.quoted_spans,
                status.source, status.quoted_source, null, status.quoted_user_key)
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): ResponseList<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    if (userKey != null) {
                        return microBlog.getMediaTimeline(userKey.id, paging)
                    }
                    if (screenName != null) {
                        return microBlog.getMediaTimelineByScreenName(screenName, paging)
                    }
                } else {
                    val screenName = this.screenName ?: run {
                        return@run this.user ?: run fetchUser@ {
                            if (userKey == null) throw MicroBlogException("Invalid parameters")
                            val user = microBlog.tryShowUser(userKey.id, null, account.type)
                            this.user = user
                            return@fetchUser user
                        }.screenName
                    }
                    val query = SearchQuery("from:$screenName filter:media exclude:retweets")
                    query.paging(paging)
                    val result = ResponseList<Status>()
                    microBlog.search(query).filterTo(result) { status ->
                        val user = status.user
                        return@filterTo user.id == userKey?.id
                                || user.screenName.equals(this.screenName, ignoreCase = true)
                    }
                    return result
                }
                throw MicroBlogException("Wrong user")
            }
            AccountType.FANFOU -> {
                if (userKey != null) {
                    return microBlog.getPhotosUserTimeline(userKey.id, paging)
                }
                if (screenName != null) {
                    return microBlog.getPhotosUserTimeline(screenName, paging)
                }
                throw MicroBlogException("Wrong user")
            }
        }
        throw MicroBlogException("Not implemented")
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus> {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        val option = MastodonTimelineOption()
        option.onlyMedia(true)
        return UserTimelineLoader.getMastodonStatuses(mastodon, userKey, screenName, paging,
                option).map { it.toParcelable(account.key) }
    }
}
