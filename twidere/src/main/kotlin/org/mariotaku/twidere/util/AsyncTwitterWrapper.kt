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

package org.mariotaku.twidere.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.apache.commons.collections.primitives.ArrayIntList
import org.apache.commons.collections.primitives.ArrayLongList
import org.apache.commons.collections.primitives.IntList
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toNulls
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.event.*
import org.mariotaku.twidere.model.util.ParcelableRelationshipUtils
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.task.*
import org.mariotaku.twidere.task.twitter.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.twitter.GetHomeTimelineTask
import org.mariotaku.twidere.task.twitter.GetSavedSearchesTask
import org.mariotaku.twidere.task.twitter.GetTrendsTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.util.collection.CompactHashSet
import java.util.*

class AsyncTwitterWrapper(
        val context: Context,
        private val bus: Bus,
        private val preferences: SharedPreferencesWrapper,
        private val asyncTaskManager: AsyncTaskManager,
        private val notificationManager: NotificationManagerWrapper
) {
    private val resolver = context.contentResolver


    var destroyingStatusIds: IntList = ArrayIntList()
    private val updatingRelationshipIds = ArrayIntList()

    private val sendingDraftIds = ArrayLongList()

    private val getMessageTasks = CompactHashSet<Uri>()
    private val getStatusTasks = CompactHashSet<Uri>()

    init {
        bus.register(object : Any() {
            @Subscribe
            fun onGetDirectMessagesTaskEvent(event: GetMessagesTaskEvent) {
                if (event.running) {
                    getMessageTasks.add(event.uri)
                } else {
                    getMessageTasks.remove(event.uri)
                }
            }

            @Subscribe
            fun onGetStatusesTaskEvent(event: GetStatusesTaskEvent) {
                if (event.running) {
                    getStatusTasks.add(event.uri)
                } else {
                    getStatusTasks.remove(event.uri)
                }
            }
        })
    }

    fun acceptFriendshipAsync(accountKey: UserKey, userKey: UserKey) {
        val task = AcceptFriendshipTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun addSendingDraftId(id: Long) {
        synchronized(sendingDraftIds) {
            sendingDraftIds.add(id)
            resolver.notifyChange(Drafts.CONTENT_URI_UNSENT, null)
        }
    }

    fun addUserListMembersAsync(accountKey: UserKey, listId: String, vararg users: ParcelableUser) {
        val task = AddUserListMembersTask(context, accountKey, listId, users)
        TaskStarter.execute(task)
    }

    fun cancelRetweetAsync(accountKey: UserKey, statusId: String?, myRetweetId: String?) {
        if (myRetweetId != null) {
            destroyStatusAsync(accountKey, myRetweetId)
        } else if (statusId != null) {
            destroyStatusAsync(accountKey, statusId)
        }
    }

    fun clearNotificationAsync(notificationType: Int) {
        clearNotificationAsync(notificationType, null)
    }

    fun clearNotificationAsync(notificationId: Int, accountKey: UserKey?) {
        notificationManager.cancelById(Utils.getNotificationId(notificationId, accountKey))
    }

    fun createBlockAsync(accountKey: UserKey, userKey: UserKey, filterEverywhere: Boolean) {
        val task = CreateUserBlockTask(context, filterEverywhere)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun createFavoriteAsync(accountKey: UserKey, status: ParcelableStatus) {
        val task = CreateFavoriteTask(context, accountKey, status)
        TaskStarter.execute(task)
    }

    fun createFriendshipAsync(accountKey: UserKey, userKey: UserKey) {
        val task = CreateFriendshipTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun createMultiBlockAsync(accountKey: UserKey, userIds: Array<String>): Int {
        val task = CreateMultiBlockTask(context, accountKey, userIds)
        return asyncTaskManager.add(task, true)
    }

    fun createMuteAsync(accountKey: UserKey, userKey: UserKey, filterEverywhere: Boolean) {
        val task = CreateUserMuteTask(context, filterEverywhere)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun createSavedSearchAsync(accountKey: UserKey, query: String): Int {
        val task = CreateSavedSearchTask(accountKey, query)
        return asyncTaskManager.add(task, true)
    }

    fun createUserListAsync(accountKey: UserKey, listName: String, isPublic: Boolean,
            description: String): Int {
        val task = CreateUserListTask(context, accountKey, listName, isPublic,
                description)
        return asyncTaskManager.add(task, true)
    }

    fun createUserListSubscriptionAsync(accountKey: UserKey, listId: String): Int {
        val task = CreateUserListSubscriptionTask(accountKey, listId)
        return asyncTaskManager.add(task, true)
    }

    fun deleteUserListMembersAsync(accountKey: UserKey, listId: String, vararg users: ParcelableUser): Int {
        val task = DeleteUserListMembersTask(accountKey, listId, users)
        return asyncTaskManager.add(task, true)
    }

    fun denyFriendshipAsync(accountKey: UserKey, userKey: UserKey) {
        val task = DenyFriendshipTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun destroyBlockAsync(accountKey: UserKey, userKey: UserKey) {
        val task = DestroyUserBlockTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun destroyFavoriteAsync(accountKey: UserKey, statusId: String) {
        val task = DestroyFavoriteTask(context, accountKey, statusId)
        TaskStarter.execute(task)
    }

    fun destroyFriendshipAsync(accountKey: UserKey, userKey: UserKey) {
        val task = DestroyFriendshipTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun destroyMuteAsync(accountKey: UserKey, userKey: UserKey) {
        val task = DestroyUserMuteTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun destroySavedSearchAsync(accountKey: UserKey, searchId: Long): Int {
        val task = DestroySavedSearchTask(context, accountKey, searchId)
        return asyncTaskManager.add(task, true)
    }

    fun destroyStatusAsync(accountKey: UserKey, statusId: String) {
        val task = DestroyStatusTask(context, accountKey, statusId)
        TaskStarter.execute(task)
    }

    fun destroyUserListAsync(accountKey: UserKey, listId: String) {
        val task = DestroyUserListTask(context, accountKey, listId)
        TaskStarter.execute(task)
    }

    fun destroyUserListSubscriptionAsync(accountKey: UserKey, listId: String): Int {
        val task = DestroyUserListSubscriptionTask(context, accountKey, listId)
        return asyncTaskManager.add(task, true)
    }

    fun getHomeTimelineAsync(param: RefreshTaskParam): Boolean {
        val task = GetHomeTimelineTask(context)
        task.params = param
        TaskStarter.execute(task)
        return true
    }

    fun getLocalTrendsAsync(accountId: UserKey, woeId: Int) {
        val task = GetTrendsTask(context, accountId, woeId)
        TaskStarter.execute<Any, Unit, Any>(task)
    }

    fun getMessagesAsync(param: GetMessagesTask.RefreshMessagesTaskParam) {
        val task = GetMessagesTask(context)
        task.params = param
        TaskStarter.execute(task)
    }

    fun getSavedSearchesAsync(accountKeys: Array<UserKey>) {
        val task = GetSavedSearchesTask(context)
        task.params = accountKeys
        TaskStarter.execute<Array<UserKey>, SingleResponse<Unit>, Any>(task)
    }

    fun getSendingDraftIds(): LongArray {
        return sendingDraftIds.toArray()
    }

    fun isDestroyingStatus(accountId: UserKey?, statusId: String?): Boolean {
        return destroyingStatusIds.contains(calculateHashCode(accountId, statusId))
    }

    fun isStatusTimelineRefreshing(uri: Uri): Boolean {
        return getStatusTasks.contains(uri)
    }

    fun refreshAll() {
        refreshAll { DataStoreUtils.getActivatedAccountKeys(context) }
    }

    fun refreshAll(accountKeys: Array<UserKey>): Boolean {
        return refreshAll { accountKeys }
    }

    fun refreshAll(action: () -> Array<UserKey>): Boolean {
        getHomeTimelineAsync(object : SimpleRefreshTaskParam() {

            override val accountKeys: Array<UserKey> by lazy { action() }

            override val sinceIds: Array<String?>? by lazy {
                DataStoreUtils.getNewestStatusIds(context, Statuses.CONTENT_URI,
                        accountKeys.toNulls())
            }
        })
        if (preferences.getBoolean(SharedPreferenceConstants.KEY_HOME_REFRESH_MENTIONS)) {
            getActivitiesAboutMeAsync(object : SimpleRefreshTaskParam() {
                override val accountKeys: Array<UserKey> by lazy { action() }

                override val sinceIds: Array<String?>? by lazy {
                    DataStoreUtils.getRefreshNewestActivityMaxPositions(context,
                            Activities.AboutMe.CONTENT_URI, accountKeys.toNulls())
                }
            })
        }
        if (preferences.getBoolean(SharedPreferenceConstants.KEY_HOME_REFRESH_DIRECT_MESSAGES)) {
            getMessagesAsync(object : GetMessagesTask.RefreshMessagesTaskParam(context) {
                override val accountKeys: Array<UserKey> by lazy { action() }
            })
        }
        if (preferences.getBoolean(SharedPreferenceConstants.KEY_HOME_REFRESH_SAVED_SEARCHES)) {
            getSavedSearchesAsync(action())
        }
        return true
    }

    fun removeSendingDraftId(id: Long) {
        synchronized(sendingDraftIds) {
            sendingDraftIds.removeElement(id)
            resolver.notifyChange(Drafts.CONTENT_URI_UNSENT, null)
        }
    }

    fun reportMultiSpam(accountKey: UserKey, userIds: Array<String>) {
        // TODO implementation
    }

    fun reportSpamAsync(accountKey: UserKey, userKey: UserKey) {
        val task = ReportSpamAndBlockTask(context)
        task.setup(accountKey, userKey)
        TaskStarter.execute(task)
    }

    fun retweetStatusAsync(accountKey: UserKey, status: ParcelableStatus) {
        val task = RetweetStatusTask(context, accountKey, status)
        TaskStarter.execute<Any, SingleResponse<ParcelableStatus>, Any>(task)
    }

    fun updateUserListDetails(accountKey: UserKey, listId: String, update: UserListUpdate) {
        val task = UpdateUserListDetailsTask(context, accountKey, listId, update)
        TaskStarter.execute(task)
    }

    fun updateFriendship(accountKey: UserKey, userKey: UserKey, update: FriendshipUpdate) {
        TaskStarter.execute(object : ExceptionHandlingAbstractTask<Any?, Relationship, Exception, Any>(context) {
            override val exceptionClass = Exception::class.java

            override fun onExecute(params: Any?): Relationship {
                val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey)
                        ?: throw MicroBlogException("No account")
                val relationship = microBlog.updateFriendship(userKey.id, update)
                val cr = context.contentResolver
                if (!relationship.isSourceWantRetweetsFromTarget) {
                    // TODO remove cached retweets
                    val where = Expression.and(
                            Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                            Expression.equalsArgs(Statuses.RETWEETED_BY_USER_KEY)
                    )
                    val selectionArgs = arrayOf(accountKey.toString(), userKey.toString())
                    cr.delete(Statuses.CONTENT_URI, where.sql, selectionArgs)
                }

                ParcelableRelationshipUtils.insert(cr, listOf(ParcelableRelationshipUtils
                        .create(accountKey, userKey, relationship)))
                return relationship
            }

            override fun onSucceed(callback: Any?, result: Relationship) {
                bus.post(FriendshipUpdatedEvent(accountKey, userKey, result))
            }

            override fun onException(callback: Any?, exception: Exception) {
                if (exception !is MicroBlogException) {
                    Analyzer.logException(exception)
                    return
                }
                DebugLog.w(TwidereConstants.LOGTAG, "Unable to update friendship", exception)
            }

        })
    }

    fun getActivitiesAboutMeAsync(param: RefreshTaskParam) {
        val task = GetActivitiesAboutMeTask(context)
        task.params = param
        TaskStarter.execute(task)
    }

    fun setActivitiesAboutMeUnreadAsync(accountKeys: Array<UserKey>, cursor: Long) {
        val task = object : ExceptionHandlingAbstractTask<Any?, Unit, MicroBlogException, Any?>(context) {
            override val exceptionClass = MicroBlogException::class.java

            override fun onExecute(params: Any?) {
                for (accountId in accountKeys) {
                    val microBlog = MicroBlogAPIFactory.getInstance(context, accountId) ?: continue
                    if (!Utils.isOfficialCredentials(context, accountId)) continue
                    microBlog.setActivitiesAboutMeUnread(cursor)
                }
            }
        }
        TaskStarter.execute(task)
    }

    fun addUpdatingRelationshipId(accountKey: UserKey, userId: UserKey) {
        updatingRelationshipIds.add(ParcelableUser.calculateHashCode(accountKey, userId))
    }

    fun removeUpdatingRelationshipId(accountKey: UserKey, userId: UserKey) {
        updatingRelationshipIds.removeElement(ParcelableUser.calculateHashCode(accountKey, userId))
    }

    fun isUpdatingRelationship(accountId: UserKey, userId: UserKey): Boolean {
        return updatingRelationshipIds.contains(ParcelableUser.calculateHashCode(accountId, userId))
    }

    internal inner class CreateMultiBlockTask(
            context: Context,
            private val accountKey: UserKey,
            private val userIds: Array<String>
    ) : ManagedAsyncTask<Any, Any, ListResponse<String>>(context) {

        private fun deleteCaches(list: List<String>) {
            // I bet you don't want to see these users in your auto complete list.
            //TODO insert to blocked users data
            val values = ContentValues()
            values.put(CachedRelationships.BLOCKING, true)
            values.put(CachedRelationships.FOLLOWING, false)
            values.put(CachedRelationships.FOLLOWED_BY, false)
            val where = Expression.inArgs(CachedRelationships.USER_KEY, list.size).sql
            val selectionArgs = list.toTypedArray()
            resolver.update(CachedRelationships.CONTENT_URI, values, where, selectionArgs)
        }

        override fun doInBackground(vararg params: Any): ListResponse<String> {
            val blockedUsers = ArrayList<String>()
            val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey)
            if (microBlog != null) {
                for (userId in userIds) {
                    try {
                        val user = microBlog.createBlock(userId)
                        blockedUsers.add(user.id)
                    } catch (e: MicroBlogException) {
                        deleteCaches(blockedUsers)
                        return ListResponse.getListInstance<String>(e)
                    }

                }
            }
            deleteCaches(blockedUsers)
            return ListResponse.getListInstance(blockedUsers)
        }

        override fun onPostExecute(result: ListResponse<String>) {
            if (result.hasData()) {
                Utils.showInfoMessage(context, R.string.users_blocked, false)
            } else {
                Utils.showErrorMessage(context, R.string.action_blocking, result.exception, true)
            }
            bus.post(UsersBlockedEvent(accountKey, userIds))
            super.onPostExecute(result)
        }


    }

    internal inner class CreateSavedSearchTask(private val mAccountKey: UserKey, private val mQuery: String) : ManagedAsyncTask<Any, Any, SingleResponse<SavedSearch>>(context) {

        override fun doInBackground(vararg params: Any): SingleResponse<SavedSearch>? {
            val microBlog = MicroBlogAPIFactory.getInstance(context, mAccountKey) ?: return null
            try {
                return SingleResponse.getInstance(microBlog.createSavedSearch(mQuery))
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<SavedSearch>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<SavedSearch>) {
            if (result.hasData()) {
                val message = context.getString(R.string.message_toast_search_name_saved, result.data!!.query)
                Utils.showOkMessage(context, message, false)
            } else if (result.hasException()) {
                val exception = result.exception
                // https://github.com/TwidereProject/Twidere-Android/issues/244
                if (exception is MicroBlogException && exception.statusCode == 403) {
                    val desc = context.getString(R.string.saved_searches_already_saved_hint)
                    Utils.showErrorMessage(context, R.string.action_saving_search, desc, false)
                } else {
                    Utils.showErrorMessage(context, R.string.action_saving_search, exception, false)
                }
            }
            super.onPostExecute(result)
        }

    }

    internal inner class CreateUserListSubscriptionTask(private val mAccountKey: UserKey, private val mListId: String) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableUserList>>(context) {

        override fun doInBackground(vararg params: Any): SingleResponse<ParcelableUserList> {
            val microBlog = MicroBlogAPIFactory.getInstance(context, mAccountKey) ?: return SingleResponse.getInstance<ParcelableUserList>()
            try {
                val userList = microBlog.createUserListSubscription(mListId)
                val list = ParcelableUserListUtils.from(userList, mAccountKey)
                return SingleResponse.getInstance(list)
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<ParcelableUserList>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<ParcelableUserList>) {
            val succeed = result.hasData()
            if (succeed) {
                val message = context.getString(R.string.subscribed_to_list, result.data!!.name)
                Utils.showOkMessage(context, message, false)
                bus.post(UserListSubscriptionEvent(UserListSubscriptionEvent.Action.SUBSCRIBE,
                        result.data))
            } else {
                Utils.showErrorMessage(context, R.string.action_subscribing_to_list, result.exception, true)
            }
            super.onPostExecute(result)
        }

    }

    internal class CreateUserListTask(context: Context, private val mAccountKey: UserKey, private val mListName: String?,
            private val mIsPublic: Boolean, private val mDescription: String) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableUserList>>(context) {

        override fun doInBackground(vararg params: Any): SingleResponse<ParcelableUserList> {
            val microBlog = MicroBlogAPIFactory.getInstance(context, mAccountKey
            )
            if (microBlog == null || mListName == null)
                return SingleResponse.getInstance<ParcelableUserList>()
            try {
                val userListUpdate = UserListUpdate()
                userListUpdate.setName(mListName)
                userListUpdate.setMode(if (mIsPublic) UserList.Mode.PUBLIC else UserList.Mode.PRIVATE)
                userListUpdate.setDescription(mDescription)
                val list = microBlog.createUserList(userListUpdate)
                return SingleResponse.getInstance(ParcelableUserListUtils.from(list, mAccountKey))
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<ParcelableUserList>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<ParcelableUserList>) {
            val context = context
            if (result.hasData()) {
                val userList = result.data
                val message = context.getString(R.string.created_list, userList!!.name)
                Utils.showOkMessage(context, message, false)
                bus.post(UserListCreatedEvent(userList))
            } else {
                Utils.showErrorMessage(context, R.string.action_creating_list, result.exception, true)
            }
            super.onPostExecute(result)
        }

    }

    internal inner class DeleteUserListMembersTask(
            private val accountKey: UserKey,
            private val userListId: String,
            private val users: Array<out ParcelableUser>
    ) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableUserList>>(context) {

        override fun doInBackground(vararg params: Any): SingleResponse<ParcelableUserList> {
            val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return SingleResponse.getInstance<ParcelableUserList>()
            try {
                val userIds = users.map { it.key }.toTypedArray()
                val userList = microBlog.deleteUserListMembers(userListId, UserKey.getIds(userIds))
                val list = ParcelableUserListUtils.from(userList, accountKey)
                return SingleResponse.getInstance(list)
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<ParcelableUserList>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<ParcelableUserList>) {
            val succeed = result.hasData()
            val message: String
            if (succeed) {
                if (users.size == 1) {
                    val user = users[0]
                    val nameFirst = preferences[nameFirstKey]
                    val displayName = userColorNameManager.getDisplayName(user.key,
                            user.name, user.screen_name, nameFirst)
                    message = context.getString(R.string.deleted_user_from_list, displayName,
                            result.data!!.name)
                } else {
                    val res = context.resources
                    message = res.getQuantityString(R.plurals.deleted_N_users_from_list, users.size, users.size,
                            result.data!!.name)
                }
                bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.REMOVED,
                        result.data, users))
                Utils.showInfoMessage(context, message, false)
            } else {
                Utils.showErrorMessage(context, R.string.action_deleting, result.exception, true)
            }
            super.onPostExecute(result)
        }

    }


    internal inner class DestroySavedSearchTask(
            context: Context,
            private val accountKey: UserKey,
            private val searchId: Long
    ) : ManagedAsyncTask<Any, Any, SingleResponse<SavedSearch>>(context) {

        override fun doInBackground(vararg params: Any): SingleResponse<SavedSearch> {
            val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return SingleResponse.getInstance<SavedSearch>()
            try {
                return SingleResponse.getInstance(microBlog.destroySavedSearch(searchId))
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<SavedSearch>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<SavedSearch>) {
            if (result.hasData()) {
                val message = context.getString(R.string.message_toast_search_name_deleted, result.data!!.query)
                Utils.showOkMessage(context, message, false)
                bus.post(SavedSearchDestroyedEvent(accountKey, searchId))
            } else {
                Utils.showErrorMessage(context, R.string.action_deleting_search, result.exception, false)
            }
            super.onPostExecute(result)
        }

    }

    internal inner class DestroyUserListSubscriptionTask(
            context: Context,
            private val accountKey: UserKey,
            private val listId: String
    ) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableUserList>>(context) {

        override fun doInBackground(vararg params: Any): SingleResponse<ParcelableUserList> {

            val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return SingleResponse.getInstance<ParcelableUserList>()
            try {
                val userList = microBlog.destroyUserListSubscription(listId)
                val list = ParcelableUserListUtils.from(userList, accountKey)
                return SingleResponse.getInstance(list)
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<ParcelableUserList>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<ParcelableUserList>) {
            val succeed = result.hasData()
            if (succeed) {
                val message = context.getString(R.string.unsubscribed_from_list, result.data!!.name)
                Utils.showOkMessage(context, message, false)
                bus.post(UserListSubscriptionEvent(UserListSubscriptionEvent.Action.UNSUBSCRIBE,
                        result.data))
            } else {
                Utils.showErrorMessage(context, R.string.action_unsubscribing_from_list, result.exception, true)
            }
            super.onPostExecute(result)
        }

    }


    companion object {

        fun calculateHashCode(accountId: UserKey?, statusId: String?): Int {
            return (accountId?.hashCode() ?: 0) xor (statusId?.hashCode() ?: 0)
        }

        fun <T : Response<*>> getException(responses: List<T>): Exception? {
            for (response in responses) {
                if (response.hasException()) return response.exception
            }
            return null
        }
    }
}
