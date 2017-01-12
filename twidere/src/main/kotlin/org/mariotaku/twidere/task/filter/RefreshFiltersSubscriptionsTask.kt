package org.mariotaku.twidere.task.filter

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.ktextension.useCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.model.instantiateComponent
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.FiltersSubscriptionCursorIndices
import org.mariotaku.twidere.model.`FiltersData$BaseItemValuesCreator`
import org.mariotaku.twidere.model.`FiltersData$UserItemValuesCreator`
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.io.IOException

class RefreshFiltersSubscriptionsTask(val context: Context) : AbstractTask<Unit?, Boolean, (Boolean) -> Unit>() {

    override fun doLongOperation(param: Unit?): Boolean {
        val resolver = context.contentResolver
        return resolver.query(Filters.Subscriptions.CONTENT_URI, Filters.Subscriptions.COLUMNS, null, null, null)?.useCursor { cursor ->
            val indices = FiltersSubscriptionCursorIndices(cursor)
            cursor.moveToPosition(-1)
            while (cursor.moveToNext()) {
                val subscription = indices.newObject(cursor)
                val component = subscription.instantiateComponent(context) ?: continue
                try {
                    if (component.fetchFilters()) {
                        updateUserItems(resolver, component.users, subscription.id)
                        updateBaseItems(resolver, component.keywords, Filters.Keywords.CONTENT_URI, subscription.id)
                        updateBaseItems(resolver, component.links, Filters.Links.CONTENT_URI, subscription.id)
                        updateBaseItems(resolver, component.sources, Filters.Sources.CONTENT_URI, subscription.id)
                    }
                } catch (e: IOException) {
                    // Ignore
                }
            }
            return@useCursor true
        } ?: false
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean) {
        callback?.invoke(result)
    }

    private fun updateUserItems(resolver: ContentResolver, items: List<FiltersData.UserItem>?, sourceId: Long) {
        resolver.delete(Filters.Users.CONTENT_URI, Expression.equalsArgs(Filters.Users.SOURCE).sql,
                arrayOf(sourceId.toString()))
        items?.map { item ->
            item.source = sourceId
            return@map `FiltersData$UserItemValuesCreator`.create(item)
        }?.let { items ->
            ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, items)
        }
    }

    private fun updateBaseItems(resolver: ContentResolver, items: List<FiltersData.BaseItem>?, uri: Uri, sourceId: Long) {
        resolver.delete(uri, Expression.equalsArgs(Filters.SOURCE).sql,
                arrayOf(sourceId.toString()))
        items?.map { item ->
            item.source = sourceId
            return@map `FiltersData$BaseItemValuesCreator`.create(item)
        }?.let { items ->
            ContentResolverUtils.bulkInsert(resolver, uri, items)
        }
    }

}