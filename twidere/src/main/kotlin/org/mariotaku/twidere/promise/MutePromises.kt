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

package org.mariotaku.twidere.promise

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.promise.notifyCreatePromise
import org.mariotaku.twidere.extension.promise.notifyOnResult
import org.mariotaku.twidere.extension.promise.thenGetAccount
import org.mariotaku.twidere.extension.promise.toastOnResult
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject

class MutePromises(private val application: Application) {

    private val profileImageSize: String = application.getString(R.string.profile_image_size)

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var manager: UserColorNameManager
    @Inject
    lateinit var bus: Bus

    init {
        GeneralComponent.get(application).inject(this)
    }

    fun mute(accountKey: UserKey, userKey: UserKey, filterEverywhere: Boolean): Promise<ParcelableUser, Exception>
            = notifyCreatePromise(bus, FriendshipTaskEvent.Action.MUTE, accountKey, userKey)
            .thenGetAccount(application, accountKey).then { account ->
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@then twitter.createMute(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(application, Mastodon::class.java)
                mastodon.muteUser(userKey.id)
                return@then mastodon.getAccount(userKey.id).toParcelable(account)
            }
            else -> throw APINotSupportedException("Muting", account.type)
        }
    }.then { user ->
        val resolver = application.contentResolver
        Utils.setLastSeen(application, userKey, -1)
        for (uri in DataStoreUtils.STATUSES_URIS) {
            val where = Expression.and(
                    Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.USER_KEY)
            )
            val whereArgs = arrayOf(accountKey.toString(), userKey.toString())
            resolver.delete(uri, where.sql, whereArgs)
        }
        if (!user.is_following) {
            for (uri in DataStoreUtils.ACTIVITIES_URIS) {
                val where = Expression.and(
                        Expression.equalsArgs(Activities.ACCOUNT_KEY),
                        Expression.equalsArgs(Activities.USER_KEY)
                )
                val whereArgs = arrayOf(accountKey.toString(), userKey.toString())
                resolver.delete(uri, where.sql, whereArgs)
            }
        }
        // I bet you don't want to see this user in your auto complete list.
        val values = ContentValues()
        values.put(CachedRelationships.ACCOUNT_KEY, accountKey.toString())
        values.put(CachedRelationships.USER_KEY, userKey.toString())
        values.put(CachedRelationships.MUTING, true)
        resolver.insert(CachedRelationships.CONTENT_URI, values)
        if (filterEverywhere) {
            DataStoreUtils.addToFilter(application, listOf(user), true)
        }
        return@then user
    }.toastOnResult(application) { user ->
        return@toastOnResult application.getString(R.string.muted_user, manager.getDisplayName(user,
                preferences[nameFirstKey]))
    }.notifyOnResult(bus, FriendshipTaskEvent.Action.MUTE, accountKey, userKey)

    fun unmute(accountKey: UserKey, userKey: UserKey): Promise<ParcelableUser, Exception>
            = notifyCreatePromise(bus, FriendshipTaskEvent.Action.UNMUTE, accountKey, userKey)
            .thenGetAccount(application, accountKey).then { account ->
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@then twitter.destroyMute(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(application, Mastodon::class.java)
                mastodon.unmuteUser(userKey.id)
                return@then mastodon.getAccount(userKey.id).toParcelable(account)
            }
            else -> throw APINotSupportedException("API", account.type)
        }
    }.then { user ->
        val resolver = application.contentResolver
        // I bet you don't want to see this user in your auto complete list.
        val values = ContentValues()
        values.put(CachedRelationships.ACCOUNT_KEY, accountKey.toString())
        values.put(CachedRelationships.USER_KEY, userKey.toString())
        values.put(CachedRelationships.MUTING, false)
        resolver.insert(CachedRelationships.CONTENT_URI, values)
        return@then user
    }.toastOnResult(application) { user ->
        return@toastOnResult application.getString(R.string.unmuted_user,
                manager.getDisplayName(user, preferences[nameFirstKey]))
    }.notifyOnResult(bus, FriendshipTaskEvent.Action.UNMUTE, accountKey, userKey)

    companion object : ApplicationContextSingletonHolder<MutePromises>(::MutePromises) {
        fun muteUsers(context: Context, account: AccountDetails, userKeys: Array<UserKey>) {
            when (account.type) {
                AccountType.TWITTER -> {
                    val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
                    userKeys.forEach { userKey ->
                        twitter.createMute(userKey.id).toParcelable(account)
                    }
                }
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                    userKeys.forEach { userKey ->
                        mastodon.muteUser(userKey.id)
                    }
                }
                else -> throw APINotSupportedException("API", account.type)
            }
        }
    }
}

