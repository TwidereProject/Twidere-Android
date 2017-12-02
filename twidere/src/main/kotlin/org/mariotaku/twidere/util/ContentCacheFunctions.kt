package org.mariotaku.twidere.util

import android.content.ContentResolver
import android.support.v4.util.ArraySet
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.ktextension.ContentValues
import org.mariotaku.ktextension.set
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.blockBulkInsert
import org.mariotaku.twidere.extension.bulkInsert
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.extension.model.relationship
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.task.GetTimelineResult
import org.mariotaku.twidere.provider.TwidereDataStore.*

fun ContentResolver.cacheTimelineResult(result: GetTimelineResult<*>, cacheRelationship: Boolean): Promise<Unit, Exception> = task {
    val account = result.account
    val users = result.users
    val hashtags = result.hashtags

    bulkInsert(CachedUsers.CONTENT_URI, users, ParcelableUser::class.java)
    blockBulkInsert(CachedHashtags.CONTENT_URI, hashtags.map {
        ContentValues { this[CachedHashtags.NAME] = it.substringAfter("#") }
    })

    if (!cacheRelationship) {
        return@task
    }
    val selectionArgsList = users.mapTo(mutableListOf(account.key.toString())) {
        it.key.toString()
    }
    val localRelationships = queryAll(CachedRelationships.CONTENT_URI, CachedRelationships.COLUMNS,
            Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                    Expression.inArgs(CachedRelationships.USER_KEY, users.size)).sql,
            selectionArgsList.toTypedArray(), cls = ParcelableRelationship::class.java)
    val relationships = users.mapTo(ArraySet<ParcelableRelationship>()) { user ->
        val userKey = user.key
        return@mapTo localRelationships?.find {
            it.user_key == userKey
        }?.apply { user.applyTo(this) } ?: user.relationship
    }
    bulkInsert(CachedRelationships.CONTENT_URI, relationships, ParcelableRelationship::class.java)
}