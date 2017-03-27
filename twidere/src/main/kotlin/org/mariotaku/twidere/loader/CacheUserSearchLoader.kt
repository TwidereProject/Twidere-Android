package org.mariotaku.twidere.loader

import android.annotation.SuppressLint
import android.content.Context
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
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
) : UserSearchLoader(context, accountKey, query, 0, null, fromUser) {
    @Inject
    internal lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun getUsers(twitter: MicroBlog, details: AccountDetails): List<User> {
        if (query.isEmpty() || !fromNetwork) return emptyList()
        return super.getUsers(twitter, details)
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
        @SuppressLint("Recycle")
        val c = context.contentResolver.query(CachedUsers.CONTENT_URI, CachedUsers.BASIC_COLUMNS,
                selection.sql, selectionArgs, null)!!
        val i = ObjectCursor.indicesFrom(c, ParcelableUser::class.java)
        c.moveToFirst()
        while (!c.isAfterLast) {
            if (list.none { it.key.toString() == c.getString(i[CachedUsers.USER_KEY]) }) {
                list.add(i.newObject(c))
            }
            c.moveToNext()
        }
        c.close()
        val collator = Collator.getInstance()
        list.sortWith(Comparator { l, r ->
            val compare = collator.compare(r.name, l.name)
            if (compare != 0) return@Comparator compare
            return@Comparator r.screen_name.compareTo(l.screen_name)
        })
    }
}