package org.mariotaku.twidere.task

import android.content.Context
import android.util.Log
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.content.ContentResolverUtils

/**
 * Created by mariotaku on 16/2/13.
 */
class GetSavedSearchesTask(
        private val context: Context
) : AbstractTask<Array<UserKey>, SingleResponse<Unit>, Any?>() {

    public override fun doLongOperation(params: Array<UserKey>): SingleResponse<Unit> {
        val cr = context.contentResolver
        for (accountKey in params) {
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: continue
            try {
                val searches = twitter.savedSearches
                val values = ContentValuesCreator.createSavedSearches(searches,
                        accountKey)
                val where = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY)
                val whereArgs = arrayOf(accountKey.toString())
                cr.delete(SavedSearches.CONTENT_URI, where.sql, whereArgs)
                ContentResolverUtils.bulkInsert(cr, SavedSearches.CONTENT_URI, values)
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
            }
        }
        return SingleResponse.getInstance(Unit)
    }
}
