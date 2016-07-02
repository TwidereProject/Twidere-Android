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
import org.apache.commons.lang3.StringUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.util.*


class MediaTimelineLoader(
        context: Context,
        accountKey: UserKey,
        private val userKey: UserKey?,
        private val screenName: String?,
        sinceId: String?,
        maxId: String?,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean
) : MicroBlogAPIStatusesLoader(context, accountKey, sinceId, maxId, -1, data, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    private var user: User? = null

    @Throws(MicroBlogException::class)
    override fun getStatuses(microBlog: MicroBlog,
                             credentials: ParcelableCredentials,
                             paging: Paging): ResponseList<Status> {
        val context = context
        when (ParcelableAccountUtils.getAccountType(credentials)) {
            ParcelableAccount.Type.TWITTER -> {
                if (Utils.isOfficialCredentials(context, credentials)) {
                    if (userKey != null) {
                        return microBlog.getMediaTimeline(userKey.id, paging)
                    }
                    if (screenName != null) {
                        return microBlog.getMediaTimelineByScreenName(screenName, paging)
                    }
                } else {
                    val screenName: String
                    if (this.screenName != null) {
                        screenName = this.screenName
                    } else if (userKey != null) {
                        if (user == null) {
                            user = TwitterWrapper.tryShowUser(microBlog, userKey.id, null,
                                    credentials.account_type)
                        }
                        screenName = user!!.screenName
                    } else {
                        throw MicroBlogException("Invalid parameters")
                    }
                    val query: SearchQuery
                    if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
                        query = SearchQuery("from:$screenName filter:media exclude:retweets")
                    } else {
                        query = SearchQuery("@$screenName pic.twitter.com -RT")
                    }
                    query.paging(paging)
                    val result = ResponseList<Status>()
                    for (status in microBlog.search(query)) {
                        val user = status.user
                        if (userKey != null && TextUtils.equals(user.id, userKey.id) || StringUtils.endsWithIgnoreCase(user.screenName, this.screenName)) {
                            result.add(status)
                        }
                    }
                    return result
                }
                throw MicroBlogException("Wrong user")
            }
            ParcelableAccount.Type.FANFOU -> {
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

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        val retweetUserId = if (status.is_retweet) status.user_key else null
        return !isMyTimeline && InternalTwitterContentUtils.isFiltered(database, retweetUserId,
                status.text_plain, status.quoted_text_plain, status.spans, status.quoted_spans,
                status.source, status.quoted_source, null, status.quoted_user_key)
    }

    private val isMyTimeline: Boolean
        get() {
            val accountKey = accountKey ?: return false
            if (userKey != null) {
                return userKey.maybeEquals(accountKey)
            } else {
                val accountScreenName = DataStoreUtils.getAccountScreenName(context, accountKey)
                return accountScreenName != null && accountScreenName.equals(screenName!!, ignoreCase = true)
            }
        }
}
