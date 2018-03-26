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
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import nl.komponents.kovenant.toSuccessVoid
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.microblog.library.model.microblog.SavedSearch
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.blockBulkInsert
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.promise.toastOnResult
import org.mariotaku.twidere.extension.promise.twitterTask
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.SavedSearchDestroyedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches
import org.mariotaku.twidere.singleton.BusSingleton
import org.mariotaku.twidere.util.ContentValuesCreator.createSavedSearch
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder


class SavedSearchPromises(private val application: Application) {

    fun destroy(accountKey: UserKey, id: Long): Promise<SavedSearch, Exception> = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.destroySavedSearch(id)
    }.toastOnResult(application) { search ->
        return@toastOnResult application.getString(R.string.destroy_saved_search, search.name)
    }.successUi {
        BusSingleton.post(SavedSearchDestroyedEvent(accountKey, id))
    }

    fun create(accountKey: UserKey, query: String): Promise<SavedSearch, Exception> = twitterTask(application, accountKey) { account, twitter ->
        return@twitterTask twitter.createSavedSearch(query)
    }.toastOnResult(application) { search ->
        return@toastOnResult application.getString(R.string.message_toast_search_name_saved, search.name)
    }

    fun refresh(accountKeys: Array<UserKey>): Promise<Unit, Exception> = all(accountKeys.map { accountKey ->
        twitterTask(application, accountKey) { account, twitter ->
            val cr = application.contentResolver
            val searches = twitter.savedSearches
            val values = searches.map { createSavedSearch(it, accountKey) }
            val where = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY)
            val whereArgs = arrayOf(accountKey.toString())
            cr.delete(SavedSearches.CONTENT_URI, where.sql, whereArgs)
            cr.blockBulkInsert(SavedSearches.CONTENT_URI, values)
        }
    }).toSuccessVoid()

    companion object : ApplicationContextSingletonHolder<SavedSearchPromises>(::SavedSearchPromises)
}
