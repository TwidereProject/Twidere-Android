package org.mariotaku.twidere.loader

import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserCursorIndices
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

    override fun processUsersData(list: MutableList<ParcelableUser>) {
        if (query.isEmpty() || !fromCache) return
        val queryEscaped = query.replace("_", "^_")
        val nicknameKeys = Utils.getMatchedNicknameKeys(query, userColorNameManager)
        val selection = Expression.or(Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^"),
                Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size))
        val selectionArgs = arrayOf(queryEscaped, queryEscaped, *nicknameKeys)
        val c = context.contentResolver.query(CachedUsers.CONTENT_URI, CachedUsers.BASIC_COLUMNS,
                selection.sql, selectionArgs, null)!!
        val i = ParcelableUserCursorIndices(c)
        c.moveToFirst()
        while (!c.isAfterLast) {
            if (list.none { it.key.toString() == c.getString(i.key) }) {
                list.add(i.newObject(c))
            }
            c.moveToNext()
        }
        c.close()
        val collator = Collator.getInstance()
        list.sortWith(Comparator { l, r ->
            val compare = collator.compare(l.name, r.name)
            if (compare != 0) return@Comparator compare
            return@Comparator l.screen_name.compareTo(r.screen_name)
        })
    }
}