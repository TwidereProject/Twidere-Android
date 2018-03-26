package org.mariotaku.twidere.loader

import android.content.Context
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.data.fetcher.users.UserSearchFetcher
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.UserColorNameManager
import javax.inject.Inject

class CacheUserSearchLoader(
        context: Context,
        accountKey: UserKey,
        query: String,
        private val fromNetwork: Boolean,
        private val fromCache: Boolean,
        fromUser: Boolean
) : UserSearchFetcher(query) {
    @Inject
    @Deprecated(message = "Deprecated", replaceWith = ReplaceWith("UserColorNameManager.get(context!!)", imports = ["org.mariotaku.twidere.util.UserColorNameManager"]))
    internal lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponent.get(context).inject(this)
    }

//    override fun getUsers(details: AccountDetails, paging: Paging): PaginatedList<ParcelableUser> {
//        if (query.isEmpty() || !fromNetwork) return PaginatedArrayList()
//        return super.getUsers(details, paging)
//    }
//
//    override fun processUsersData(details: AccountDetails, list: MutableList<ParcelableUser>) {
//        if (query.isEmpty() || !fromCache) return
//        val queryEscaped = query.replace("_", "^_")
//        val nicknameKeys = Utils.getMatchedNicknameKeys(query, userColorNameManager)
//        val selection = Expression.and(Expression.equalsArgs(Columns.Column(CachedUsers.USER_TYPE)),
//                Expression.or(Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
//                        Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^"),
//                        Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size)))
//        val selectionArgs = arrayOf(details.type, queryEscaped, queryEscaped, *nicknameKeys)
//        context.contentResolver.queryReference(CachedUsers.CONTENT_URI, CachedUsers.BASIC_COLUMNS,
//                selection.sql, selectionArgs, null)?.use { (c) ->
//            val i = ObjectCursor.indicesFrom(c, ParcelableUser::class.java)
//            c.moveToFirst()
//            while (!c.isAfterLast) {
//                if (list.none { it.key.toString() == c.getString(i[CachedUsers.USER_KEY]) }) {
//                    list.add(i.newObject(c))
//                }
//                c.moveToNext()
//            }
//        }
//        val collator = Collator.getInstance()
//        list.sortWith(Comparator { l, r ->
//            val compare = collator.compare(r.name, l.name)
//            if (compare != 0) return@Comparator compare
//            return@Comparator r.screen_name.compareTo(l.screen_name)
//        })
//    }
}