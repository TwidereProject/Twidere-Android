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
import android.arch.paging.KeyedDataSource
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.getDetails
import org.mariotaku.twidere.extension.model.api.mastodon.getLinkPagination
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.timeline.TimelineFilter

class StatusesLivePagedListProvider(
        private val context: Context,
        private val fetcher: StatusesFetcher,
        private val accountKey: UserKey,
        private val timelineFilter: TimelineFilter?,
        private val errorHandler: (Exception) -> Unit
) : ExtendedPagedListProvider<Pagination, ParcelableStatus>() {

    override fun onCreateDataSource(): DataSource<Pagination, ParcelableStatus> {
        return StatusesDataSource(context, fetcher, accountKey, timelineFilter, errorHandler)
    }

    private class StatusesDataSource(
            private val context: Context,
            private val fetcher: StatusesFetcher,
            private val accountKey: UserKey,
            private val timelineFilter: TimelineFilter?,
            val errorHandler: (Exception) -> Unit
    ) : KeyedDataSource<Pagination, ParcelableStatus>() {

        private val profileImageSize = context.getString(R.string.profile_image_size)

        private var lastEndKey: String? = null

        override fun getKey(item: ParcelableStatus): Pagination {
            val prevKey = item.extras?.prev_key ?: item.id
            val nextKey = item.extras?.next_key ?: item.id
            return SinceMaxPagination().apply {
                sinceId = nextKey
                maxId = prevKey
            }
        }

        override fun loadInitial(pageSize: Int): List<ParcelableStatus>? {
            val loaded = try {
                load(Paging().count(pageSize))
            } catch (e: MicroBlogException) {
                errorHandler(e)
                return emptyList()
            }
            return loaded.filter {
                timelineFilter?.check(it) != false
            }
        }

        override fun loadBefore(currentBeginKey: Pagination, pageSize: Int): List<ParcelableStatus>? {
            val sinceId = (currentBeginKey as? SinceMaxPagination)?.sinceId ?: return null
            return loadOrNull(Paging().count(pageSize).sinceId(sinceId))?.filter {
                it.id != sinceId && timelineFilter?.check(it) != false
            }
        }

        override fun loadAfter(currentEndKey: Pagination, pageSize: Int): List<ParcelableStatus>? {
            val maxId = (currentEndKey as? SinceMaxPagination)?.maxId ?: return null
            if (lastEndKey == maxId) {
                return null
            }
            val loadResult = loadOrNull(Paging().count(pageSize).maxId(maxId)) ?: return null
            lastEndKey = loadResult.singleOrNull()?.id
            return loadResult.filter {
                it.id != maxId && timelineFilter?.check(it) != false
            }
        }

        private fun load(paging: Paging): List<ParcelableStatus> {
            val am = AccountManager.get(context)
            val account = am.getDetails(accountKey, true) ?: return emptyList()
            when (account.type) {
                AccountType.TWITTER -> {
                    val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
                    val timeline = fetcher.forTwitter(account, twitter, paging, timelineFilter)
                    return timeline.map {
                        it.toParcelable(account, profileImageSize)
                    }
                }
                AccountType.STATUSNET -> {
                    val statusnet = account.newMicroBlogInstance(context, MicroBlog::class.java)
                    val timeline = fetcher.forStatusNet(account, statusnet, paging, timelineFilter)
                    return timeline.map {
                        it.toParcelable(account, profileImageSize)
                    }
                }
                AccountType.FANFOU -> {
                    val fanfou = account.newMicroBlogInstance(context, MicroBlog::class.java)
                    val timeline = fetcher.forFanfou(account, fanfou, paging, timelineFilter)
                    return timeline.map {
                        it.toParcelable(account, profileImageSize)
                    }
                }
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                    val timeline = fetcher.forMastodon(account, mastodon, paging, timelineFilter)
                    val prevPagination = timeline.getLinkPagination("prev") as? SinceMaxPagination
                    val nextPagination = timeline.getLinkPagination("next") as? SinceMaxPagination
                    return timeline.map {
                        val status = it.toParcelable(account)
                        status.extras?.prev_key = prevPagination?.sinceId
                        status.extras?.next_key = nextPagination?.maxId
                        return@map status
                    }
                }
                else -> throw APINotSupportedException(platform = account.type)
            }
        }

        private fun loadOrNull(paging: Paging): List<ParcelableStatus>? {
            return try {
                load(paging)
            } catch (e: MicroBlogException) {
                null
            }
        }
    }

}
