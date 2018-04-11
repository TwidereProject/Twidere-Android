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

package org.mariotaku.twidere.data


import android.accounts.AccountManager
import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import android.content.Context
import org.mariotaku.microblog.library.Fanfou
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.StatusNet
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.generateDisplayInfo
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.timeline.TimelineFilter

class StatusesDataSourceFactory(
        private val context: Context,
        private val fetcher: StatusesFetcher,
        private val accountKey: UserKey,
        private val timelineFilter: TimelineFilter?,
        private val errorHandler: (Exception) -> Unit
) : DataSource.Factory<Pagination, ParcelableStatus>() {

    override fun create(): DataSource<Pagination, ParcelableStatus> {
        return StatusesDataSource(context, fetcher, accountKey, timelineFilter, errorHandler)
    }

    private class StatusesDataSource(
            private val context: Context,
            private val fetcher: StatusesFetcher,
            private val accountKey: UserKey,
            private val timelineFilter: TimelineFilter?,
            val errorHandler: (Exception) -> Unit
    ) : PageKeyedDataSource<Pagination, ParcelableStatus>() {

        private val profileImageSize = context.getString(R.string.profile_image_size)

        override fun loadInitial(params: LoadInitialParams<Pagination>, callback: LoadInitialCallback<Pagination, ParcelableStatus>) {
            val paging = Paging().count(params.requestedLoadSize)
            try {
                val loaded = load(paging)
                val filtered = filtered(loaded)
                val processed = processed(filtered)
                callback.onResult(processed, null, loaded.nextPage)
            } catch (e: Exception) {
                errorHandler(e)
                invalidate()
            }
        }

        override fun loadBefore(params: LoadParams<Pagination>, callback: LoadCallback<Pagination, ParcelableStatus>) {
            val paging = Paging().count(params.requestedLoadSize)
            params.key.applyTo(paging)
            try {
                val loaded = load(paging)
                val filtered = filtered(loaded).filterNot { params.key.isFromStatus(it) }
                val processed = processed(filtered)
                callback.onResult(processed, null)
            } catch (e: Exception) {
                errorHandler(e)
                callback.onResult(emptyList(), null)
            }
        }

        override fun loadAfter(params: LoadParams<Pagination>, callback: LoadCallback<Pagination, ParcelableStatus>) {
            val paging = Paging().count(params.requestedLoadSize)
            params.key.applyTo(paging)
            try {
                val loaded = load(paging)
                val filtered = filtered(loaded).filterNot { params.key.isFromStatus(it) }
                val processed = processed(filtered)
                callback.onResult(processed, loaded.nextPage)
            } catch (e: Exception) {
                errorHandler(e)
                callback.onResult(emptyList(), null)
            }
        }

        private fun load(paging: Paging): PaginatedList<ParcelableStatus> {
            val am = AccountManager.get(context)
            val account = am.getDetailsOrThrow(accountKey, true)
            when (account.type) {
                AccountType.TWITTER -> {
                    val twitter = account.newMicroBlogInstance(context, Twitter::class.java)
                    val timeline = fetcher.forTwitter(account, twitter, paging, timelineFilter)
                    return timeline.mapToPaginated { it.toParcelable(account, profileImageSize) }
                }
                AccountType.STATUSNET -> {
                    val statusnet = account.newMicroBlogInstance(context, StatusNet::class.java)
                    val timeline = fetcher.forStatusNet(account, statusnet, paging, timelineFilter)
                    return timeline.mapToPaginated { it.toParcelable(account, profileImageSize) }
                }
                AccountType.FANFOU -> {
                    val fanfou = account.newMicroBlogInstance(context, Fanfou::class.java)
                    val timeline = fetcher.forFanfou(account, fanfou, paging, timelineFilter)
                    return timeline.mapToPaginated { it.toParcelable(account, profileImageSize) }
                }
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                    val timeline = fetcher.forMastodon(account, mastodon, paging, timelineFilter)
                    return timeline.mapToPaginated { it.toParcelable(account) }
                }
                else -> throw APINotSupportedException(platform = account.type)
            }
        }

        private fun filtered(list: List<ParcelableStatus>): List<ParcelableStatus> {
            val filter = timelineFilter ?: return list
            return list.filter(filter::check)
        }

        private fun processed(list: List<ParcelableStatus>): List<ParcelableStatus> {
            list.forEach {
                it.display = it.generateDisplayInfo(context)
            }
            return list
        }

        private fun <T> List<T>.mapToPaginated(transform: (T) -> ParcelableStatus) = mapTo(PaginatedArrayList(), transform).apply {
            val first = firstOrNull()
            if (first != null) {
                previousPage = SinceMaxPagination.sinceId(first.id, first.sort_id)
            }
            val last = lastOrNull()
            if (last != null) {
                nextPage = SinceMaxPagination.maxId(last.id, last.sort_id)
            }
        }

        private fun Pagination.isFromStatus(status: ParcelableStatus) = when (this) {
            is SinceMaxPagination -> sinceId == status.id || maxId == status.id
            else -> false
        }

    }

}
