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

import android.content.Context
import android.content.SharedPreferences
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toNulls
import org.mariotaku.twidere.constant.homeRefreshDirectMessagesKey
import org.mariotaku.twidere.constant.homeRefreshMentionsKey
import org.mariotaku.twidere.constant.homeRefreshSavedSearchesKey
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.CreateFavoriteTask
import org.mariotaku.twidere.task.DestroyFavoriteTask
import org.mariotaku.twidere.task.RetweetStatusTask
import org.mariotaku.twidere.task.statuses.GetHomeTimelineTask
import org.mariotaku.twidere.task.twitter.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.twitter.GetSavedSearchesTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask

@Deprecated("Not used anymore")
class AsyncTwitterWrapper(
        val context: Context,
        private val preferences: SharedPreferences
) {


    fun createFavoriteAsync(accountKey: UserKey, status: ParcelableStatus) {
        val task = CreateFavoriteTask(context, accountKey, status)
        TaskStarter.execute(task)
    }

    fun destroyFavoriteAsync(accountKey: UserKey, statusId: String) {
        val task = DestroyFavoriteTask(context, accountKey, statusId)
        TaskStarter.execute(task)
    }

    fun getHomeTimelineAsync(param: ContentRefreshParam): Boolean {
        val task = GetHomeTimelineTask(context)
        task.params = param
        TaskStarter.execute(task)
        return true
    }

    fun getMessagesAsync(param: GetMessagesTask.RefreshMessagesParam) {
        val task = GetMessagesTask(context)
        task.params = param
        TaskStarter.execute(task)
    }

    fun getSavedSearchesAsync(accountKeys: Array<UserKey>): Promise<List<Unit>, Exception> {
        val task = GetSavedSearchesTask(context)
        return task.toPromise(accountKeys)
    }

    fun refreshAll() {
        refreshAll { DataStoreUtils.getActivatedAccountKeys(context) }
    }

    fun refreshAll(accountKeys: Array<UserKey>): Boolean {
        return refreshAll { accountKeys }
    }

    fun refreshAll(action: () -> Array<UserKey>): Boolean {
        getHomeTimelineAsync(object : ContentRefreshParam {

            override val accountKeys by lazy { action() }

            override val pagination by lazy {
                return@lazy DataStoreUtils.getNewestStatusIds(context, Statuses.HomeTimeline.CONTENT_URI,
                        accountKeys.toNulls()).mapToArray {
                    return@mapToArray SinceMaxPagination.sinceId(it, -1)
                }
            }
        })
        if (preferences[homeRefreshMentionsKey]) {
            getActivitiesAboutMeAsync(object : ContentRefreshParam {

                override val accountKeys by lazy { action() }

                override val pagination by lazy {
                    return@lazy DataStoreUtils.getRefreshNewestActivityMaxPositions(context,
                            Activities.AboutMe.CONTENT_URI, accountKeys.toNulls()).mapToArray {
                        return@mapToArray SinceMaxPagination.sinceId(it, -1)
                    }
                }
            })
        }
        if (preferences[homeRefreshDirectMessagesKey]) {
            getMessagesAsync(object : GetMessagesTask.RefreshMessagesParam(context) {
                override val accountKeys by lazy { action() }
            })
        }
        if (preferences[homeRefreshSavedSearchesKey]) {
            getSavedSearchesAsync(action())
        }
        return true
    }

    fun retweetStatusAsync(accountKey: UserKey, status: ParcelableStatus) {
        val task = RetweetStatusTask(context, accountKey, status)
        TaskStarter.execute<Any, SingleResponse<ParcelableStatus>, Any>(task)
    }

    fun getActivitiesAboutMeAsync(param: ContentRefreshParam) {
        val task = GetActivitiesAboutMeTask(context)
        task.params = param
        TaskStarter.execute(task)
    }

    fun setActivitiesAboutMeUnreadAsync(accountKeys: Array<UserKey>, cursor: Long) = task {
        for (accountKey in accountKeys) {
            val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?: continue
            if (!AccountUtils.isOfficial(context, accountKey)) continue
            microBlog.setActivitiesAboutMeUnread(cursor)
        }
    }


}
