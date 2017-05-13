package org.mariotaku.twidere.task.cache

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.util.ArraySet
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.bulkInsert
import org.mariotaku.twidere.extension.model.applyTo
import org.mariotaku.twidere.extension.model.relationship
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.task.BaseAbstractTask

class CacheUserRelationshipTask(
        context: Context,
        val accountKey: UserKey,
        val accountType: String,
        val users: Collection<ParcelableUser>,
        val cacheRelationship: Boolean
) : BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        val cr = context.contentResolver
        cr.bulkInsert(CachedUsers.CONTENT_URI, users, ParcelableUser::class.java)


        if (cacheRelationship) {
            val selectionArgsList = users.mapTo(mutableListOf(accountKey.toString())) {
                it.key.toString()
            }
            @SuppressLint("Recycle")
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
