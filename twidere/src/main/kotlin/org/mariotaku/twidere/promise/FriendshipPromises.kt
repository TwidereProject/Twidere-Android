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
import android.content.SharedPreferences
import android.widget.Toast
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.PromisesComponent
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject

class FriendshipPromises private constructor(val application: Application) {

    private val profileImageSize: String = application.getString(R.string.profile_image_size)

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var manager: UserColorNameManager

    init {
        PromisesComponent.get(application).inject(this)
    }

    fun accept(accountKey: UserKey, userKey: UserKey): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { details ->
        when (details.type) {
            AccountType.FANFOU -> {
                val fanfou = details.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask fanfou.acceptFanfouFriendship(userKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = details.newMicroBlogInstance(application, Mastodon::class.java)
                mastodon.authorizeFollowRequest(userKey.id)
                return@accountTask mastodon.getAccount(userKey.id).toParcelable(details)
            }
            else -> {
                val twitter = details.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask twitter.acceptFriendship(userKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
        }
    }.success {
        Utils.setLastSeen(application, userKey, System.currentTimeMillis())
    }.successUi { user ->
        val nameFirst = preferences[nameFirstKey]
        Toast.makeText(application, application.getString(R.string.message_toast_accepted_users_follow_request,
                manager.getDisplayName(user, nameFirst)), Toast.LENGTH_SHORT).show()
    }.toastOnFail(application)

    fun deny(accountKey: UserKey, userKey: UserKey): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.FANFOU -> {
                val fanfou = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask fanfou.denyFanfouFriendship(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(application, Mastodon::class.java)
                mastodon.rejectFollowRequest(userKey.id)
                return@accountTask mastodon.getAccount(userKey.id).toParcelable(account)
            }
            else -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask twitter.denyFriendship(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
        }
    }.success {
        Utils.setLastSeen(application, userKey, -1)
    }.successUi { user ->
        val nameFirst = preferences[nameFirstKey]
        Toast.makeText(application, application.getString(R.string.denied_users_follow_request,
                manager.getDisplayName(user, nameFirst)), Toast.LENGTH_SHORT).show()
    }.toastOnFail(application)

    fun create(accountKey: UserKey, userKey: UserKey, screenName: String?): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.FANFOU -> {
                val fanfou = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask fanfou.createFanfouFriendship(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(application, Mastodon::class.java)
                if (account.key.host != userKey.host) {
                    if (screenName == null)
                        throw MicroBlogException("Screen name required to follow remote user")
                    return@accountTask mastodon.followRemoteUser("$screenName@${userKey.host}")
                            .toParcelable(account)
                }
                mastodon.followUser(userKey.id)
                return@accountTask mastodon.getAccount(userKey.id).toParcelable(account)
            }
            else -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask twitter.createFriendship(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
        }
    }.then { user ->
        user.is_following = true
        Utils.setLastSeen(application, user.key, System.currentTimeMillis())
        return@then user
    }.successUi { user ->
        val nameFirst = preferences[nameFirstKey]
        val message = if (user.is_protected) {
            application.getString(R.string.sent_follow_request_to_user,
                    manager.getDisplayName(user, nameFirst))
        } else {
            application.getString(R.string.followed_user,
                    manager.getDisplayName(user, nameFirst))
        }
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }.toastOnFail(application)

    fun destroy(accountKey: UserKey, userKey: UserKey): Promise<ParcelableUser, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.FANFOU -> {
                val fanfou = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask fanfou.destroyFanfouFriendship(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(application, Mastodon::class.java)
                mastodon.unfollowUser(userKey.id)
                return@accountTask mastodon.getAccount(userKey.id).toParcelable(account)
            }
            else -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask twitter.destroyFriendship(userKey.id).toParcelable(account,
                        profileImageSize = profileImageSize)
            }
        }
    }.then { user ->
        user.is_following = false
        Utils.setLastSeen(application, user.key, -1)
        val where = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.or(Expression.equalsArgs(Statuses.USER_KEY),
                        Expression.equalsArgs(Statuses.RETWEETED_BY_USER_KEY)))
        val whereArgs = arrayOf(accountKey.toString(), userKey.toString(), userKey.toString())
        val resolver = application.contentResolver
        resolver.delete(Statuses.HomeTimeline.CONTENT_URI, where.sql, whereArgs)
        return@then user
    }.successUi { user ->
        val nameFirst = preferences[nameFirstKey]
        val message = application.getString(R.string.unfollowed_user,
                manager.getDisplayName(user, nameFirst))
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }.toastOnFail(application)

    companion object : ApplicationContextSingletonHolder<FriendshipPromises>(::FriendshipPromises)
}
