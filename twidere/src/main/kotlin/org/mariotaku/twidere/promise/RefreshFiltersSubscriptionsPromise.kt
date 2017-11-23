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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.model.instantiateComponent
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.FiltersSubscription
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import org.mariotaku.twidere.util.sync.LOGTAG_SYNC
import java.io.IOException
import java.util.*

class RefreshFiltersSubscriptionsPromise(val context: Context) {
    fun promise(): Promise<Unit, Exception> = task {
        val resolver = context.contentResolver
        val sourceIds = ArrayList<Long>()
        resolver.queryAll(Filters.Subscriptions.CONTENT_URI, Filters.Subscriptions.COLUMNS,
                null, null, cls = FiltersSubscription::class.java).forEach { subscription ->
            sourceIds.add(subscription.id)
            val component = subscription.instantiateComponent(context) ?: return@forEach
            try {
                if (component.fetchFilters()) {
                    updateUserItems(resolver, component.users, subscription.id)
                    updateBaseItems(resolver, component.keywords, Filters.Keywords.CONTENT_URI, subscription.id)
                    updateBaseItems(resolver, component.links, Filters.Links.CONTENT_URI, subscription.id)
                    updateBaseItems(resolver, component.sources, Filters.Sources.CONTENT_URI, subscription.id)
                }
            } catch (e: IOException) {
                DebugLog.w(LOGTAG_SYNC, "Unable to refresh filters", e)
            }
        }
        // Delete 'orphaned' filter items with `sourceId` > 0
        val extraWhere = Expression.greaterThan(Filters.SOURCE, 0).sql
        ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI, Filters.Users.SOURCE,
                true, sourceIds, extraWhere, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Keywords.CONTENT_URI, Filters.Keywords.SOURCE,
                true, sourceIds, extraWhere, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Sources.CONTENT_URI, Filters.Sources.SOURCE,
                true, sourceIds, extraWhere, null)
        ContentResolverUtils.bulkDelete(resolver, Filters.Links.CONTENT_URI, Filters.Links.SOURCE,
                true, sourceIds, extraWhere, null)
        Thread.sleep(1000)
    }

    private fun updateUserItems(resolver: ContentResolver, items: List<FiltersData.UserItem>?, sourceId: Long) {
        resolver.delete(Filters.Users.CONTENT_URI, Expression.equalsArgs(Filters.Users.SOURCE).sql,
                arrayOf(sourceId.toString()))
        val creator = ObjectCursor.valuesCreatorFrom(FiltersData.UserItem::class.java)
        items?.map { item ->
            item.source = sourceId
            return@map creator.create(item)
        }?.let {
            ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, it)
        }
    }

    private fun updateBaseItems(resolver: ContentResolver, items: List<FiltersData.BaseItem>?, uri: Uri, sourceId: Long) {
        resolver.delete(uri, Expression.equalsArgs(Filters.SOURCE).sql,
                arrayOf(sourceId.toString()))
        val creator = ObjectCursor.valuesCreatorFrom(FiltersData.BaseItem::class.java)
        items?.map { item ->
            item.source = sourceId
            return@map creator.create(item)
        }?.let {
            ContentResolverUtils.bulkInsert(resolver, uri, it)
        }
    }

    companion object : ApplicationContextSingletonHolder<RefreshFiltersSubscriptionsPromise>(::RefreshFiltersSubscriptionsPromise)
}