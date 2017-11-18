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
import android.widget.Toast
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.StatusDestroyedEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.task.AbsAccountRequestTask
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.dagger.PromisesComponent
import org.mariotaku.twidere.util.deleteActivityStatus
import org.mariotaku.twidere.util.deleteStatus
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject

class StatusPromises private constructor(private val application: Application) {
    @Inject
    lateinit var bus: Bus

    init {
        PromisesComponent.get(application).inject(this)
    }

    fun destroy(accountKey: UserKey, id: String): Promise<ParcelableStatus, Exception> = (promiseOnUi {
        DestroyTasks.addTaskId(accountKey, id)
        bus.post(StatusListChangedEvent())
    } and accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(application, cls = Mastodon::class.java)
                val result = mastodon.favouriteStatus(id)
                mastodon.deleteStatus(id)
                return@accountTask result.toParcelable(account)
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(application, cls = MicroBlog::class.java)
                return@accountTask microBlog.destroyStatus(id).toParcelable(account)
            }
        }
    }).then { (_, status) ->
        return@then status
    }.success { status ->
        application.contentResolver.deleteStatus(accountKey, id, status)
        application.contentResolver.deleteActivityStatus(accountKey, id, status)
    }.fail { ex ->
        if (ex is MicroBlogException && ex.errorCode == ErrorInfo.STATUS_NOT_FOUND) {
            application.contentResolver.deleteStatus(accountKey, id, null)
            application.contentResolver.deleteActivityStatus(accountKey, id, null)
        }
    }.alwaysUi {
        DestroyTasks.removeTaskId(accountKey, id)
    }.successUi { status ->
        if (status.retweet_id != null) {
            Toast.makeText(application, R.string.message_toast_retweet_cancelled, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(application, R.string.message_toast_status_deleted, Toast.LENGTH_SHORT).show()
        }
        bus.post(StatusDestroyedEvent(status))
    }.toastOnFail(application)

    fun cancelRetweet(accountKey: UserKey, statusId: String?, myRetweetId: String?): Promise<ParcelableStatus, Exception> = when {
        myRetweetId != null -> destroy(accountKey, myRetweetId)
        statusId != null -> destroy(accountKey, statusId)
        else -> Promise.ofFail(IllegalArgumentException())
    }

    companion object : ApplicationContextSingletonHolder<StatusPromises>(::StatusPromises)

    private object DestroyTasks : AbsAccountRequestTask.ObjectIdTaskCompanion()
}
