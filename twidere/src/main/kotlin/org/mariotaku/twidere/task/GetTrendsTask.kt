package org.mariotaku.twidere.task

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Trends
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.TrendsRefreshedEvent
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

/**
 * Created by mariotaku on 16/2/24.
 */
abstract class GetTrendsTask(private val context: Context, private val accountId: UserKey) : AbstractTask<Any, Any, Any>() {

    @Inject
    lateinit var bus: Bus

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(MicroBlogException::class)
    abstract fun getTrends(twitter: MicroBlog): List<Trends>

    public override fun doLongOperation(param: Any): Any? {
        val twitter = MicroBlogAPIFactory.getInstance(context, accountId) ?: return null
        try {
            val trends = getTrends(twitter)
            storeTrends(context.contentResolver, contentUri, trends)
            return null
        } catch (e: MicroBlogException) {
            return null
        }

    }

    override fun afterExecute(handler: Any?, result: Any?) {
        bus.post(TrendsRefreshedEvent())
    }

    protected abstract val contentUri: Uri

    private fun storeTrends(cr: ContentResolver, uri: Uri, trendsList: List<Trends>) {
        val hashtags = ArrayList<String>()
        val hashtagValues = ArrayList<ContentValues>()
        if (trendsList.isNotEmpty()) {
            val valuesArray = ContentValuesCreator.createTrends(trendsList)
            for (values in valuesArray) {
                val hashtag = values.getAsString(TwidereDataStore.CachedTrends.NAME).replaceFirst("#".toRegex(), "")
                if (hashtags.contains(hashtag)) {
                    continue
                }
                hashtags.add(hashtag)
                val hashtagValue = ContentValues()
                hashtagValue.put(TwidereDataStore.CachedHashtags.NAME, hashtag)
                hashtagValues.add(hashtagValue)
            }
            cr.delete(uri, null, null)
            ContentResolverUtils.bulkInsert(cr, uri, valuesArray)
            ContentResolverUtils.bulkDelete(cr, TwidereDataStore.CachedHashtags.CONTENT_URI, TwidereDataStore.CachedHashtags.NAME, hashtags, null)
            ContentResolverUtils.bulkInsert(cr, TwidereDataStore.CachedHashtags.CONTENT_URI,
                    hashtagValues.toTypedArray())
        }
    }
}
