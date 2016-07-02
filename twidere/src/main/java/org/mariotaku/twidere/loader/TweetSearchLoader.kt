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

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.SearchQuery
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory

open class TweetSearchLoader(
        context: Context,
        accountKey: UserKey?,
        private val query: String?,
        sinceId: String?,
        maxId: String?,
        page: Int,
        adapterData: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        override val isGapEnabled: Boolean,
        loadingMore: Boolean
) : MicroBlogAPIStatusesLoader(context, accountKey, sinceId, maxId, page, adapterData, savedStatusesArgs,
        tabPosition, fromUser, loadingMore) {

    @Throws(MicroBlogException::class)
    public override fun getStatuses(microBlog: MicroBlog,
                                    credentials: ParcelableCredentials,
                                    paging: Paging): List<Status> {
        if (query == null) throw MicroBlogException("Empty query")
        val processedQuery = processQuery(credentials, query)
        when (ParcelableAccountUtils.getAccountType(credentials)) {
            ParcelableAccount.Type.TWITTER -> {
                val query = SearchQuery(processedQuery)
                query.paging(paging)
                return microBlog.search(query)
            }
            ParcelableAccount.Type.STATUSNET -> {
                return microBlog.searchStatuses(processedQuery, paging)
            }
            ParcelableAccount.Type.FANFOU -> {
                return microBlog.searchPublicTimeline(processedQuery, paging)
            }
        }
        throw MicroBlogException("Not implemented")
    }

    protected open fun processQuery(credentials: ParcelableCredentials, query: String): String {
        if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
            return String.format("%s exclude:retweets", query)
        }
        return query
    }

    @WorkerThread
    override fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean {
        return InternalTwitterContentUtils.isFiltered(database, status, true)
    }

    override fun processPaging(credentials: ParcelableCredentials, loadItemLimit: Int, paging: Paging) {
        if (MicroBlogAPIFactory.isStatusNetCredentials(credentials)) {
            paging.setRpp(loadItemLimit)
            val page = page
            if (page > 0) {
                paging.setPage(page)
            }
        } else {
            super.processPaging(credentials, loadItemLimit, paging)
        }
    }

}
