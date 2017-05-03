package org.mariotaku.twidere.task.cache

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.support.v4.util.ArraySet
import org.mariotaku.ktextension.map
import org.mariotaku.ktextension.useCursor
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.model.api.key
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.model.ParcelableRelationship
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableRelationshipUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.content.ContentResolverUtils

class CacheUserRelationshipTask(
        context: Context,
        val accountKey: UserKey,
        val accountType: String,
        val users: Collection<User>
) : BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        cacheUserRelationships(context.contentResolver, accountKey, accountType, users)
    }

    companion object {
        fun cacheUserRelationships(cr: ContentResolver, accountKey: UserKey, accountType: String,
                users: Collection<User>) {

            val parcelableUsers = users.map { it.toParcelable(accountKey, accountType) }

            val userValuesCreator = ObjectCursor.valuesCreatorFrom(ParcelableUser::class.java)
            ContentResolverUtils.bulkInsert(cr, CachedUsers.CONTENT_URI, parcelableUsers.map(userValuesCreator::create))

            val selectionArgsList = parcelableUsers.mapTo(mutableListOf(accountKey.toString())) {
                it.key.toString()
            }
            @SuppressLint("Recycle")
            val localRelationships = cr.query(CachedRelationships.CONTENT_URI, CachedRelationships.COLUMNS,
                    Expression.and(Expression.equalsArgs(CachedRelationships.ACCOUNT_KEY),
                            Expression.inArgs(CachedRelationships.USER_KEY, users.size)).sql,
                    selectionArgsList.toTypedArray(), null).useCursor { cur ->
                return@useCursor cur.map(ObjectCursor.indicesFrom(cur, ParcelableRelationship::class.java))
            }
            val relationships = users.mapTo(ArraySet<ParcelableRelationship>()) { user ->
                val userKey = user.key
                return@mapTo localRelationships.find {
                    it.user_key == userKey
                }?.apply {
                    user.isFollowing?.let { this.following = it }
                    user.isFollowedBy?.let { this.followed_by = it }
                    user.isBlocking?.let { this.blocking = it }
                    user.isBlockedBy?.let { this.blocked_by = it }
                    user.isMuting?.let { this.muting = it }
                    user.isNotificationsEnabled?.let { this.notifications_enabled = it }
                } ?: ParcelableRelationshipUtils.create(accountKey, userKey, user)
            }
            ParcelableRelationshipUtils.insert(cr, relationships)
        }
    }

}
