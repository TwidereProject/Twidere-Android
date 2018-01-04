/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import org.mariotaku.microblog.library.model.microblog.CursorSupport
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.model.api.mastodon.mapToPaginated
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.*

class UsersDataSourceFactory(
        private val context: Context,
        private val fetcher: UsersFetcher,
        private val accountKey: UserKey,
        private val errorHandler: (Exception) -> Unit
) : DataSource.Factory<Pagination, ParcelableUser> {

    override fun create(): DataSource<Pagination, ParcelableUser> {
        return UsersDataSource(context, fetcher, accountKey, errorHandler)
    }

    private class UsersDataSource(
            private val context: Context,
            private val fetcher: UsersFetcher,
            private val accountKey: UserKey,
            val errorHandler: (Exception) -> Unit
    ) : PageKeyedDataSource<Pagination, ParcelableUser>() {

        private val profileImageSize = context.getString(R.string.profile_image_size)

        override fun loadInitial(params: LoadInitialParams<Pagination>, callback: LoadInitialCallback<Pagination, ParcelableUser>) {
            val paging = Paging().count(params.requestedLoadSize)
            try {
                val loaded = load(paging)
                callback.onResult(loaded, null, loaded.nextPage)
            } catch (e: Exception) {
                errorHandler(e)
                invalidate()
            }
        }

        override fun loadBefore(params: LoadParams<Pagination>, callback: LoadCallback<Pagination, ParcelableUser>) {
            val paging = Paging().count(params.requestedLoadSize)
            params.key.applyTo(paging)
            try {
                val loaded = load(paging)
                val filtered = loaded.filterNot { params.key == it.key }
                callback.onResult(filtered, null)
            } catch (e: Exception) {
                errorHandler(e)
                callback.onResult(emptyList(), null)
            }
        }

        override fun loadAfter(params: LoadParams<Pagination>, callback: LoadCallback<Pagination, ParcelableUser>) {
            val paging = Paging().count(params.requestedLoadSize)
            params.key.applyTo(paging)
            try {
                val loaded = load(paging)
                val filtered = loaded.filterNot { params.key == it.key }
                callback.onResult(filtered, loaded.nextPage)
            } catch (e: Exception) {
                errorHandler(e)
                callback.onResult(emptyList(), null)
            }
        }

        private fun load(paging: Paging): PaginatedList<ParcelableUser> {
            val am = AccountManager.get(context)
            val account = am.getDetailsOrThrow(accountKey, true)
            when (account.type) {
                AccountType.TWITTER -> {
                    val twitter = account.newMicroBlogInstance(context, Twitter::class.java)
                    val timeline = fetcher.forTwitter(account, twitter, paging)
                    return timeline.map1 { it.toParcelable(account, profileImageSize = profileImageSize) }
                }
                AccountType.STATUSNET -> {
                    val statusnet = account.newMicroBlogInstance(context, StatusNet::class.java)
                    val timeline = fetcher.forStatusNet(account, statusnet, paging)
                    return timeline.map1 { it.toParcelable(account, profileImageSize = profileImageSize) }
                }
                AccountType.FANFOU -> {
                    val fanfou = account.newMicroBlogInstance(context, Fanfou::class.java)
                    val timeline = fetcher.forFanfou(account, fanfou, paging)
                    return timeline.map1 { it.toParcelable(account, profileImageSize = profileImageSize) }
                }
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                    val timeline = fetcher.forMastodon(account, mastodon, paging)
                    return timeline.mapToPaginated { it.toParcelable(account) }
                }
                else -> throw APINotSupportedException(platform = account.type)
            }
        }

        private fun <T> List<T>.map1(transform: (T) -> ParcelableUser): PaginatedArrayList<ParcelableUser> {
            val mapped = mapTo(PaginatedArrayList(), transform)
            when (this) {
                is CursorSupport -> {
                    mapped.nextPage = CursorPagination.valueOf(nextCursor)
                    mapped.previousPage = CursorPagination.valueOf(previousCursor)
                }
                else -> {
                    val first = mapped.firstOrNull()
                    if (first != null) {
                        mapped.previousPage = SinceMaxPagination.sinceId(first.key.id, -1)
                    }
                    val last = mapped.lastOrNull()
                    if (last != null) {
                        mapped.nextPage = SinceMaxPagination.maxId(last.key.id, -1)
                    }
                }
            }
            return mapped
        }

    }

}
