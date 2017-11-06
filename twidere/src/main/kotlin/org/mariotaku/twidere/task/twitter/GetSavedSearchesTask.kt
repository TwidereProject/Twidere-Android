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

package org.mariotaku.twidere.task.twitter

import android.content.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.all
import nl.komponents.kovenant.task
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.exception.NoAccountException
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches
import org.mariotaku.twidere.task.PromiseTask
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.content.ContentResolverUtils

class GetSavedSearchesTask(
        private val context: Context
) : PromiseTask<Array<UserKey>, List<Unit>> {

    override fun toPromise(param: Array<UserKey>): Promise<List<Unit>, Exception> = all(param.map { accountKey ->
        return@map task {
            val cr = context.contentResolver
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?:
                    throw NoAccountException()
            val searches = twitter.savedSearches
            val values = ContentValuesCreator.createSavedSearches(searches,
                    accountKey)
            val where = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY)
            val whereArgs = arrayOf(accountKey.toString())
            cr.delete(SavedSearches.CONTENT_URI, where.sql, whereArgs)
            ContentResolverUtils.bulkInsert(cr, SavedSearches.CONTENT_URI, values)
            return@task
        }
    }, cancelOthersOnError = false)

}
