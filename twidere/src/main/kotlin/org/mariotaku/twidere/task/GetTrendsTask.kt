package org.mariotaku.twidere.task

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Trends
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.TrendsRefreshedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.MicroBlogAPIFactory
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
        val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return
        try {
            val trends = twitter.getLocationTrends(woeId)
            storeTrends(context.contentResolver, CachedTrends.Local.CONTENT_URI, trends)
            return
        } catch (e: MicroBlogException) {
            return
        }

    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: ContentResolver, uri: Uri, trendsList: List<Trends>) {
        val hashtags = ArrayList<String>()
        val hashtagValues = ArrayList<ContentValues>()
        val deleteWhere = Expression.and(Expression.equalsArgs(CachedTrends.ACCOUNT_KEY),
                Expression.equalsArgs(CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)
        trendsList.forEach {

        }
        if (trendsList.isNotEmpty()) {
            val valuesArray = ContentValuesCreator.createTrends(trendsList)
            for (values in valuesArray) {
                val hashtag = values.getAsString(CachedTrends.NAME).replaceFirst("#", "")
                if (hashtags.contains(hashtag)) {
                    continue
                }
                hashtags.add(hashtag)
                val hashtagValue = ContentValues()
                hashtagValue.put(CachedHashtags.NAME, hashtag)
                hashtagValues.add(hashtagValue)
            }
            cr.delete(uri, null, null)
            ContentResolverUtils.bulkInsert(cr, uri, valuesArray)
            ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false, hashtags, null)
            ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtagValues.toTypedArray())
        }
    }
}
