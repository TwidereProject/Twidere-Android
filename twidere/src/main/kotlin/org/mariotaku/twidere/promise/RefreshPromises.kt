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

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import nl.komponents.kovenant.task
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toNulls
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.twidere.constant.homeRefreshDirectMessagesKey
import org.mariotaku.twidere.constant.homeRefreshMentionsKey
import org.mariotaku.twidere.constant.homeRefreshSavedSearchesKey
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.promise
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetHomeTimelineTask
import org.mariotaku.twidere.task.twitter.GetActivitiesAboutMeTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject

class RefreshPromises private constructor(val application: Context) {
    @Inject
    lateinit var preferences: SharedPreferences

    init {
        GeneralComponent.get(application).inject(this)
    }

    fun refreshAll(): Promise<List<*>, Exception> {
        return refreshAll(lazy { DataStoreUtils.getActivatedAccountKeys(application) })
    }

    fun refreshAll(accountKeys: Array<UserKey>): Promise<List<*>, Exception> {
        return refreshAll(lazy { accountKeys })
    }

    fun refreshAll(lazyAccountKeys: Lazy<Array<UserKey>>): Promise<List<*>, Exception> {
        val promises = mutableListOf<Promise<*, Exception>>()
        val homeTask = GetHomeTimelineTask(application)
        homeTask.params = object : ContentRefreshParam {

            override val accountKeys by lazyAccountKeys

            override val pagination by lazy {
                return@lazy DataStoreUtils.getNewestStatusIds(application, Statuses.HomeTimeline.CONTENT_URI,
                        this.accountKeys.toNulls()).mapToArray {
                    return@mapToArray SinceMaxPagination.sinceId(it, -1)
                }
            }
        }
        promises += homeTask.promise()
        if (preferences[homeRefreshMentionsKey]) {
            val task = GetActivitiesAboutMeTask(application)
            task.params = object : ContentRefreshParam {

                override val accountKeys by lazyAccountKeys

                override val pagination by lazy {
                    return@lazy DataStoreUtils.getRefreshNewestActivityMaxPositions(application,
                            Activities.AboutMe.CONTENT_URI, this.accountKeys.toNulls()).mapToArray {
                        return@mapToArray SinceMaxPagination.sinceId(it, -1)
                    }
                }
            }
            promises += task.promise()
        }
        if (preferences[homeRefreshDirectMessagesKey]) {
            val task = GetMessagesTask(application)
            task.params = object : GetMessagesTask.RefreshMessagesParam(application) {
                override val accountKeys by lazyAccountKeys
            }
            promises += task.promise()
        }
        if (preferences[homeRefreshSavedSearchesKey]) {
            promises += SavedSearchPromises.get(application).refresh(lazyAccountKeys.value)
        }
        return all(promises)
    }

    fun setActivitiesAboutMeUnreadAsync(accountKeys: Array<UserKey>, cursor: Long) = task {
        for (accountKey in accountKeys) {
            val microBlog = AccountManager.get(application).getDetailsOrThrow(accountKey, true)
                    .newMicroBlogInstance(application, MicroBlog::class.java)
            if (!AccountUtils.isOfficial(application, accountKey)) continue
            microBlog.setActivitiesAboutMeUnread(cursor)
        }
    }

    companion object : ApplicationContextSingletonHolder<RefreshPromises>(::RefreshPromises)

}
