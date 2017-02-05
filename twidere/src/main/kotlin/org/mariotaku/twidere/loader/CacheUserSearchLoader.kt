package org.mariotaku.twidere.loader

import android.content.Context
import android.text.TextUtils
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserCursorIndices
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

class CacheUserSearchLoader(
        context: Context,
        accountKey: UserKey,
        query: String,
        private val fromCache: Boolean,
        fromUser: Boolean
) : UserSearchLoader(context, accountKey, query, 0, null, fromUser) {
    @Inject
    internal lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun loadInBackground(): List<ParcelableUser> {
        if (TextUtils.isEmpty(query)) return emptyList()
        if (fromCache) {
            val cachedList = ArrayList<ParcelableUser>()
            val queryEscaped = query.replace("_", "^_")
            val nicknameKeys = Utils.getMatchedNicknameKeys(query, userColorNameManager)
            val selection = Expression.or(Expression.likeRaw(Columns.Column(TwidereDataStore.CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                    Expression.likeRaw(Columns.Column(TwidereDataStore.CachedUsers.NAME), "?||'%'", "^"),
                    Expression.inArgs(Columns.Column(TwidereDataStore.CachedUsers.USER_KEY), nicknameKeys.size))
            val selectionArgs = arrayOf(queryEscaped, queryEscaped, *nicknameKeys)
            val order = arrayOf(TwidereDataStore.CachedUsers.LAST_SEEN, TwidereDataStore.CachedUsers.SCREEN_NAME, TwidereDataStore.CachedUsers.NAME)
            val ascending = booleanArrayOf(false, true, true)
            val orderBy = OrderBy(order, ascending)
            val c = context.contentResolver.query(TwidereDataStore.CachedUsers.CONTENT_URI,
                    TwidereDataStore.CachedUsers.BASIC_COLUMNS, selection?.sql,
                    selectionArgs, orderBy.sql)!!
            val i = ParcelableUserCursorIndices(c)
            c.moveToFirst()
            while (!c.isAfterLast) {
                cachedList.add(i.newObject(c))
                c.moveToNext()
            }
            c.close()
            return cachedList
        }
        return super.loadInBackground()
    }
}