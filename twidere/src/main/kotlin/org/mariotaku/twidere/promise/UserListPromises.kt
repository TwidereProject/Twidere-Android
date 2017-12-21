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
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.model.microblog.UserListUpdate
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.promise.toastOnResult
import org.mariotaku.twidere.extension.promise.twitterTask
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.*
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject

class UserListPromises private constructor(private val application: Application) {

    private val profileImageSize: String = application.getString(R.string.profile_image_size)

    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponent.get(application).inject(this)
    }

    fun create(accountKey: UserKey, update: UserListUpdate): Promise<ParcelableUserList, Exception>
            = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.createUserList(update).toParcelable(accountKey,
                profileImageSize = profileImageSize)
    }.toastOnResult(application) { list ->
        return@toastOnResult application.getString(R.string.created_list, list.name)
    }.successUi { list ->
        bus.post(UserListCreatedEvent(list))
    }

    fun update(accountKey: UserKey, id: String, update: UserListUpdate): Promise<ParcelableUserList, Exception>
            = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.updateUserList(id, update).toParcelable(accountKey,
                profileImageSize = profileImageSize)
    }.toastOnResult(application) { list ->
        return@toastOnResult application.getString(R.string.updated_list_details, list.name)
    }.successUi { list ->
        bus.post(UserListUpdatedEvent(list))
    }

    fun destroy(accountKey: UserKey, id: String): Promise<ParcelableUserList, Exception>
            = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.destroyUserList(id).toParcelable(accountKey,
                profileImageSize = profileImageSize)
    }.toastOnResult(application) { list ->
        return@toastOnResult application.getString(R.string.deleted_list, list.name)
    }.successUi { list ->
        bus.post(UserListDestroyedEvent(list))
    }

    fun subscribe(accountKey: UserKey, id: String): Promise<ParcelableUserList, Exception>
            = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.createUserListSubscription(id).toParcelable(accountKey,
                profileImageSize = profileImageSize)
    }.toastOnResult(application) { list ->
        return@toastOnResult application.getString(R.string.subscribed_to_list, list.name)
    }.successUi { list ->
        bus.post(UserListSubscriptionEvent(UserListSubscriptionEvent.Action.SUBSCRIBE, list))
    }

    fun unsubscribe(accountKey: UserKey, id: String): Promise<ParcelableUserList, Exception>
            = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.destroyUserListSubscription(id).toParcelable(accountKey,
                profileImageSize = profileImageSize)
    }.toastOnResult(application) { list ->
        return@toastOnResult application.getString(R.string.unsubscribed_from_list, list.name)
    }.successUi { list ->
        bus.post(UserListSubscriptionEvent(UserListSubscriptionEvent.Action.UNSUBSCRIBE, list))
    }

    fun addMembers(accountKey: UserKey, id: String, vararg users: ParcelableUser):
            Promise<ParcelableUserList, Exception> = twitterTask(application, accountKey) { account, twitter ->
        val userIds = users.mapToArray { it.key.id }
        val result = twitter.addUserListMembers(id, userIds)
        return@twitterTask result.toParcelable(account.key)
    }.toastOnResult(application) { list ->
        if (users.size == 1) {
            val user = users.first()
            val nameFirst = preferences[nameFirstKey]
            val displayName = userColorNameManager.getDisplayName(user.key, user.name,
                    user.screen_name)
            return@toastOnResult application.getString(R.string.message_toast_added_user_to_list,
                    displayName, list.name)
        } else {
            val res = application.resources
            return@toastOnResult res.getQuantityString(R.plurals.added_N_users_to_list, users.size,
                    users.size, list.name)
        }
    }.successUi { list ->
        bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.ADDED, list, users))
    }

    fun deleteMembers(accountKey: UserKey, id: String, vararg users: ParcelableUser):
            Promise<ParcelableUserList, Exception> = twitterTask(application, accountKey) { account, twitter ->
        val userIds = users.mapToArray { it.key.id }
        val result = twitter.deleteUserListMembers(id, userIds)
        return@twitterTask result.toParcelable(account.key)
    }.toastOnResult(application) { list ->
        if (users.size == 1) {
            val user = users.first()
            val nameFirst = preferences[nameFirstKey]
            val displayName = userColorNameManager.getDisplayName(user.key, user.name,
                    user.screen_name)
            return@toastOnResult application.getString(R.string.deleted_user_from_list,
                    displayName, list.name)
        } else {
            val res = application.resources
            return@toastOnResult res.getQuantityString(R.plurals.deleted_N_users_from_list, users.size,
                    users.size, list.name)
        }
    }.successUi { list ->
        bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.ADDED, list, users))
    }

    companion object : ApplicationContextSingletonHolder<UserListPromises>(::UserListPromises)
}