package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.task.twitter.GetStatusesTask
import org.mariotaku.twidere.util.ErrorInfoStore

/**
 * Created by mariotaku on 16/2/11.
 */
class GetHomeTimelineTask(context: Context) : GetStatusesTask(context) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(twitter: MicroBlog, paging: Paging): ResponseList<Status> {
        return twitter.getHomeTimeline(paging)
    }

    override val contentUri: Uri
        get() = TwidereDataStore.Statuses.CONTENT_URI

    @TimelineType
    override val timelineType: String
        get() = TimelineType.HOME

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_HOME_TIMELINE

}
