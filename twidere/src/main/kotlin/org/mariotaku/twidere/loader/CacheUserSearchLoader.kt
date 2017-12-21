package org.mariotaku.twidere.loader

import android.content.Context
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.model.microblog.Paging
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.loader.users.UserSearchLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import java.text.Collator
import java.util.*
import javax.inject.Inject

class CacheUserSearchLoader(
        context: Context,
        accountKey: UserKey,
        query: String,
        private val fromNetwork: Boolean,
        private val fromCache: Boolean,
        fromUser: Boolean
) : UserSearchLoader(context, accountKey, query, null, fromUser) {
    @Inject
    internal lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponent.get(context).inject(this)
    }

    override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
        if (query.isEmpty() || !fromNetwork) return PaginatedArrayList()
        return super.getUsers(details, paging)
    }

    override fun processUsersData(details: AccountDetails, list: MutableList<ParcelableUser>) {
        if (query.isEmpty() || !fromCache) return
        val queryEscaped = query.replace("_", "^_")
        val nicknameKeys = Utils.getMatchedNicknameKeys(query, userColorNameManager)
        val selection = Expression.and(Expression.equalsArgs(Columns.Column(CachedUsers.USER_TYPE)),
                Expression.or(Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                        Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^"),
                        Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size)))
        val selectionArgs = arrayOf(details.type, queryEscaped, queryEscaped, *nicknameKeys)
        context.contentResolver.queryReference(CachedUsers.CONTENT_URI, CachedUsers.BASIC_COLUMNS,
                selection.sql, selectionArgs, null)?.use { (c) ->
            val i = ObjectCursor.indicesFrom(c, ParcelableUser::class.java)
            c.moveToFirst()
            while (!c.isAfterLast) {
                if (list.none { it.key.toString() == c.getString(i[CachedUsers.USER_KEY]) }) {
                    list.add(i.newObject(c))
                }
                c.moveToNext()
            }
        }
        val collator = Collator.getInstance()
        list.sortWith(Comparator { l, r ->
            val compare = collator.compare(r.name, l.name)
            if (compare != 0) return@Comparator compare
            return@Comparator r.screen_name.compareTo(l.screen_name)
        })
    }
}