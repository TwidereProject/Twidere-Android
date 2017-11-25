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
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.twitter.model.SavedSearch
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.promise.accountTask
import org.mariotaku.twidere.extension.promise.toastOnResult
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.SavedSearchDestroyedEvent
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject


class SavedSearchPromises(private val application: Application) {

    @Inject
    lateinit var bus: Bus

    init {
        GeneralComponent.get(application).inject(this)
    }

    fun destroy(accountKey: UserKey, id: Long): Promise<SavedSearch, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask twitter.destroySavedSearch(id)
            }
            else -> throw APINotSupportedException("Destroy saved search", account.type)
        }
    }.toastOnResult(application) { search ->
        return@toastOnResult application.getString(R.string.destroy_saved_search, search.name)
    }.successUi {
        bus.post(SavedSearchDestroyedEvent(accountKey, id))
    }

    fun create(accountKey: UserKey, query: String): Promise<SavedSearch, Exception> = accountTask(application, accountKey) { account ->
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(application, MicroBlog::class.java)
                return@accountTask twitter.createSavedSearch(query)
            }
            else -> throw APINotSupportedException("Create saved search", account.type)
        }
    }.toastOnResult(application) { search ->
        return@toastOnResult application.getString(R.string.message_toast_search_name_saved, search.name)
    }

    companion object : ApplicationContextSingletonHolder<SavedSearchPromises>(::SavedSearchPromises)
}
