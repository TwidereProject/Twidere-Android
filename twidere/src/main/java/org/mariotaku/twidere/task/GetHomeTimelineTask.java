package org.mariotaku.twidere.task;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.task.twitter.GetStatusesTask;
import org.mariotaku.twidere.util.ErrorInfoStore;

import edu.tsinghua.hotmobi.model.TimelineType;

/**
 * Created by mariotaku on 16/2/11.
 */
public class GetHomeTimelineTask extends GetStatusesTask {

    public GetHomeTimelineTask(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ResponseList<Status> getStatuses(final MicroBlog twitter, final Paging paging)
            throws MicroBlogException {
        return twitter.getHomeTimeline(paging);
    }

    @NonNull
    @Override
    protected Uri getContentUri() {
        return TwidereDataStore.Statuses.CONTENT_URI;
    }

    @TimelineType
    @Override
    protected String getTimelineType() {
        return TimelineType.HOME;
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_HOME_TIMELINE;
    }

}
