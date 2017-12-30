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

package org.mariotaku.twidere.data.user

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.ComputableExceptionLiveData
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.insert
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.model.hasSameHost
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableRelationshipUtils
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils

class RelationshipLiveData(
        private val context: Context,
        private val accountKey: UserKey?
) : ComputableExceptionLiveData<ParcelableRelationship>(false) {

    var user: ParcelableUser? = null

    val relationship: ParcelableRelationship? get() = value?.data

    @Throws(Exception::class)
    override fun compute(): ParcelableRelationship {
        val user = this.user ?: throw RequiredFieldNotFoundException("user")
        if (accountKey == null) throw RequiredFieldNotFoundException("account_key", "user")
        val userKey = user.key
        val isFiltering = DataStoreUtils.isFilteringUser(context, userKey)
        if (accountKey == user.key) {
            return ParcelableRelationship().apply {
                account_key = accountKey
                user_key = userKey
                filtering = isFiltering
            }
        }
        val details = AccountManager.get(context).getDetailsOrThrow(accountKey, true)
        if (details.type == AccountType.TWITTER) {
            if (!accountKey.hasSameHost(user.key)) {
                return ParcelableRelationshipUtils.create(user, isFiltering)
            }
        }
        val data = when (details.type) {
            AccountType.MASTODON -> {
                val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                mastodon.getRelationships(arrayOf(userKey.id))?.firstOrNull()
                        ?.toParcelable(accountKey, userKey, isFiltering)
                        ?: throw MicroBlogException("No relationship")
            }
            else -> {
                val microBlog = details.newMicroBlogInstance(context, MicroBlog::class.java)
                microBlog.showFriendship(user.key.id).toParcelable(accountKey, userKey,
                        isFiltering)
            }
        }

        if (data.blocking || data.blocked_by) {
            Utils.setLastSeen(context, userKey, -1)
        } else {
            Utils.setLastSeen(context, userKey, System.currentTimeMillis())
        }
        val resolver = context.contentResolver
        resolver.insert(TwidereDataStore.CachedRelationships.CONTENT_URI, data,
                ParcelableRelationship::class.java)
        return data
    }
}
