package org.mariotaku.twidere.task.cache

import android.content.Context
import androidx.collection.ArraySet
import org.mariotaku.ktextension.ContentValues
import org.mariotaku.ktextension.set
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.bulkInsert
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.extension.model.relationship
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.task.GetTimelineResult
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.content.ContentResolverUtils

class CacheTimelineResultTask(
        context: Context,
        val result: GetTimelineResult<*>,
        val cacheRelationship: Boolean
) : BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        val cr = context.contentResolver
        val account = result.account
        val users = result.users
        val hashtags = result.hashtags

        cr.bulkInsert(CachedUsers.CONTENT_URI, users, ParcelableUser::class.java)
        ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            ContentValues { this[CachedHashtags.NAME] = it.substringAfter("#") }
        })

        if (cacheRelationship) {
            val selectionArgsList = users.mapTo(mutableListOf(account.key.toString())) {
                it.key.toString()
            }
            val localRelationships = cr.queryAll(CachedRelationships.CONTENT_URI, CachedRelationships.COLUMNS,
                    Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                            Expression.inArgs(CachedRelationships.USER_KEY, users.size)).sql,
                    selectionArgsList.toTypedArray(), null, ParcelableRelationship::class.java)
            val relationships = users.mapTo(ArraySet<ParcelableRelationship>()) { user ->
                val userKey = user.key
                return@mapTo localRelationships.find {
                    it.user_key == userKey
                }?.apply { user.applyTo(this) } ?: user.relationship
            }
            cr.bulkInsert(CachedRelationships.CONTENT_URI, relationships, ParcelableRelationship::class.java)
        }
    }

}
