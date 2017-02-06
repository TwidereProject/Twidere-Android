package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArraySet
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Trends
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableTrend
import org.mariotaku.twidere.model.ParcelableTrendValuesCreator
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.TrendsRefreshedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

/**
 * Created by mariotaku on 16/2/24.
 */
class GetTrendsTask(
        private val context: Context,
        private val accountKey: UserKey,
        private val woeId: Int
) : AbstractTask<Any?, Unit, Any?>() {

    @Inject
    lateinit var bus: Bus

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun doLongOperation(param: Any?) {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?: return
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val trends = when {
                details.type == AccountType.FANFOU -> twitter.fanfouTrends
                else -> twitter.getLocationTrends(woeId).firstOrNull()
            } ?: return
            storeTrends(context.contentResolver, CachedTrends.Local.CONTENT_URI, trends)
        } catch (e: MicroBlogException) {
            DebugLog.w(LOGTAG, tr = e)
        }
    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: ContentResolver, uri: Uri, trends: Trends) {
        val hashtags = ArraySet<String>()
        val deleteWhere = Expression.and(Expression.equalsArgs(CachedTrends.ACCOUNT_KEY),
                Expression.equalsArgs(CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)

        val allTrends = ArrayList<ParcelableTrend>()

        trends.trends.forEachIndexed { idx, trend ->
            val hashtag = trend.name.replaceFirst("#", "")
            hashtags.add(hashtag)
            allTrends.add(ParcelableTrend().apply {
                this.account_key = accountKey
                this.woe_id = woeId
                this.name = trend.name
                this.timestamp = System.currentTimeMillis()
                this.trend_order = idx
            })
        }
        ContentResolverUtils.bulkInsert(cr, uri, allTrends.map(ParcelableTrendValuesCreator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null)
        ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
    }
}
