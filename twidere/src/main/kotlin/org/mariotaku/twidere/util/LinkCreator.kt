/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util

import android.net.Uri
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.originalId
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

/**
 * Creates links for sharing
 *
 * Created by mariotaku on 15/3/14.
 */
object LinkCreator {

    private val AUTHORITY_TWITTER = "twitter.com"
    private val AUTHORITY_FANFOU = "fanfou.com"

    fun getTwidereStatusLink(accountKey: UserKey?, statusId: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_STATUS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, statusId)
        return builder.build()
    }

    fun getTwidereUserLink(accountKey: UserKey?, userKey: UserKey?, screenName: String?): Uri {
        return getTwidereUserRelatedLink(AUTHORITY_USER, accountKey, userKey, screenName)
    }

    fun getTwidereUserRelatedLink(authority: String, accountKey: UserKey?, userKey: UserKey?,
            screenName: String?): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(authority)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        }
        return builder.build()
    }

    fun getTwidereUserListRelatedLink(authority: String, accountKey: UserKey?, listId: String?,
            userKey: UserKey?, screenName: String?, listName: String?): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(authority)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        if (listId != null) {
            builder.appendQueryParameter(QUERY_PARAM_LIST_ID, listId)
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        }
        if (listName != null) {
            builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, listName)
        }
        return builder.build()
    }

    fun getTwitterUserListLink(userScreenName: String, listName: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_HTTPS)
        builder.authority(AUTHORITY_TWITTER)
        builder.appendPath(userScreenName)
        builder.appendPath(listName)
        return builder.build()
    }

    fun getStatusWebLink(status: ParcelableStatus): Uri {
        status.extras?.external_url?.takeIf(String::isNotEmpty)?.let {
            return Uri.parse(it)
        }
        if (USER_TYPE_FANFOU_COM == status.account_key.host) {
            return getFanfouStatusLink(status.id)
        }
        return getTwitterStatusLink(status.user_screen_name, status.originalId)
    }

    fun getQuotedStatusWebLink(status: ParcelableStatus): Uri {
        val extras = status.extras
        if (extras != null) {
            extras.quoted_external_url?.takeIf(String::isNotEmpty)?.let {
                return Uri.parse(it)
            }
            extras.external_url?.takeIf(String::isNotEmpty)?.let {
                return Uri.parse(it)
            }
        }
        if (USER_TYPE_FANFOU_COM == status.account_key.host) {
            return getFanfouStatusLink(status.quoted_id)
        }
        return getTwitterStatusLink(status.quoted_user_screen_name, status.quoted_id)
    }

    fun getUserWebLink(user: ParcelableUser): Uri {
        if (user.extras != null && user.extras?.statusnet_profile_url != null) {
            return Uri.parse(user.extras?.statusnet_profile_url)
        }
        when (user.user_type) {
            AccountType.FANFOU -> return getFanfouUserLink(user.key.id)
            AccountType.MASTODON -> {
                val host = (user.key.host ?: user.account_key?.host)!! // Let it crash
                return getMastodonUserLink(host, user.screen_name)
            }
        }
        return getTwitterUserLink(user.screen_name)
    }

    internal fun getTwitterStatusLink(screenName: String, statusId: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_HTTPS)
        builder.authority(AUTHORITY_TWITTER)
        builder.appendPath(screenName)
        builder.appendPath("status")
        builder.appendPath(statusId)
        return builder.build()
    }

    internal fun getTwitterUserLink(screenName: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_HTTPS)
        builder.authority(AUTHORITY_TWITTER)
        builder.appendPath(screenName)
        return builder.build()
    }

    internal fun getFanfouStatusLink(id: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_HTTP)
        builder.authority(AUTHORITY_FANFOU)
        builder.appendPath("statuses")
        builder.appendPath(id)
        return builder.build()
    }

    internal fun getFanfouUserLink(id: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_HTTP)
        builder.authority(AUTHORITY_FANFOU)
        builder.appendPath(id)
        return builder.build()
    }

    internal fun getMastodonUserLink(host: String, username: String): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_HTTPS)
        builder.authority(host)
        builder.appendEncodedPath(Uri.encode("@$username", "@"))
        return builder.build()
    }
}
