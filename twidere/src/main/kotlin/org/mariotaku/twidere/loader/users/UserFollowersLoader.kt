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

package org.mariotaku.twidere.loader.users

import android.content.Context
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.model.microblog.Paging
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.mastodon.mapToPaginated
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.microblog.mapToPaginated
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedList

class UserFollowersLoader(
        context: Context,
        accountKey: UserKey?,
        userKey: UserKey?,
        screenName: String?,
        data: List<ParcelableUser>?,
        fromUser: Boolean
) : UserRelatedUsersLoader(context, accountKey, userKey, screenName, data, fromUser) {

    @Throws(MicroBlogException::class)
    override fun getUsersByKey(details: AccountDetails, paging: Paging, userKey: UserKey): PaginatedList<ParcelableUser> {
        when (details.type) {
            AccountType.MASTODON -> {
                val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                return mastodon.getFollowers(userKey.id, paging).mapToPaginated {
                    it.toParcelable(details)
                }
            }
            AccountType.STATUSNET -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getStatusesFollowersList(userKey.id, paging).mapToPaginated {
                    it.toParcelable(details, profileImageSize = profileImageSize)
                }
            }
            AccountType.FANFOU -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getUsersFollowers(userKey.id, paging).mapToPaginated(pagination) {
                    it.toParcelable(details, profileImageSize = profileImageSize)
                }
            }
            else -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getFollowersList(userKey.id, paging).mapToPaginated {
                    it.toParcelable(details, profileImageSize = profileImageSize)
                }
            }
        }
    }

    @Throws(MicroBlogException::class)
    override fun getUsersByScreenName(details: AccountDetails, paging: Paging, screenName: String): PaginatedList<ParcelableUser> {
        when (details.type) {
            AccountType.MASTODON -> {
                throw MicroBlogException("Only ID supported")
            }
            AccountType.STATUSNET -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getStatusesFollowersListByScreenName(screenName, paging).mapToPaginated {
                    it.toParcelable(details, profileImageSize = profileImageSize)
                }
            }
            AccountType.FANFOU -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getUsersFollowers(screenName, paging).mapToPaginated(pagination) {
                    it.toParcelable(details, profileImageSize = profileImageSize)
                }
            }
            else -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getFollowersListByScreenName(screenName, paging).mapToPaginated {
                    it.toParcelable(details, profileImageSize = profileImageSize)
                }
            }
        }
    }
}
