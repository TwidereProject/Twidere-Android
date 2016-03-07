package org.mariotaku.twidere.task;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Trends;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;

import java.util.List;

/**
 * Created by mariotaku on 16/2/24.
 */
public class GetLocalTrendsTask extends GetTrendsTask {

    private final int woeid;

    public GetLocalTrendsTask(final Context context, final UserKey accountKey, final int woeid) {
        super(context, accountKey);
        this.woeid = woeid;
    }

    @Override
    public List<Trends> getTrends(@NonNull final Twitter twitter) throws TwitterException {
        return twitter.getLocationTrends(woeid);
    }

    @Override
    protected Uri getContentUri() {
        return CachedTrends.Local.CONTENT_URI;
    }

}
