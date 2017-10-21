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

package org.mariotaku.twidere.data.status

import android.accounts.AccountManager
import android.arch.paging.KeyedDataSource
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.util.AccountUtils


class StatusesDataSource(
        private val context: Context,
        private val fetcher: StatusesFetcher,
        private val accountKey: UserKey,
        private val timelineFilter: TimelineFilter?
) : KeyedDataSource<String, ParcelableStatus>() {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun getKey(item: ParcelableStatus) = item.id

    override fun loadInitial(pageSize: Int): List<ParcelableStatus>? {
        return load(Paging().count(pageSize))?.filterNot {
            timelineFilter?.shouldFilter(it) == true
        }
    }

    override fun loadBefore(currentBeginKey: String, pageSize: Int): List<ParcelableStatus>? {
        return load(Paging().count(pageSize).sinceId(currentBeginKey))?.filterNot {
            it.id == currentBeginKey && timelineFilter?.shouldFilter(it) == true
        }
    }

    override fun loadAfter(currentEndKey: String, pageSize: Int): List<ParcelableStatus>? {
        return load(Paging().count(pageSize).maxId(currentEndKey))?.filterNot {
            it.id == currentEndKey && timelineFilter?.shouldFilter(it) == true
        }
    }

    private fun load(paging: Paging): List<ParcelableStatus>? {
        val am = AccountManager.get(context)
        val account = AccountUtils.getAccountDetails(am, accountKey, true) ?:
                return emptyList()
        try {
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
                    return timeline.map {
                        it.toParcelable(account)
                    }
                }
                else -> throw UnsupportedOperationException()
            }
        } catch (e: MicroBlogException) {
            return null
        }
    }

}
