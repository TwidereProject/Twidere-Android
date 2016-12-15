package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Trends
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends

/**
 * Created by mariotaku on 16/2/24.
 */
class GetLocalTrendsTask(
        context: Context,
        accountKey: UserKey,
        private val woeid: Int
) : GetTrendsTask(context, accountKey) {

    @Throws(MicroBlogException::class)
    override fun getTrends(twitter: MicroBlog): List<Trends> {
        return twitter.getLocationTrends(woeid)
    }

    override val contentUri: Uri = CachedTrends.Local.CONTENT_URI

}
