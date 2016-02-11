package org.mariotaku.twidere.task;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.task.twitter.GetActivitiesTask;
import org.mariotaku.twidere.util.ErrorInfoStore;

/**
 * Created by mariotaku on 16/2/11.
 */
public class GetActivitiesByFriendsTask extends GetActivitiesTask {

    public GetActivitiesByFriendsTask(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_ACTIVITIES_BY_FRIENDS;
    }

    @Override
    protected void saveReadPosition(long accountId, Twitter twitter) {

    }

    @Override
    protected ResponseList<Activity> getActivities(@NonNull Twitter twitter, long accountId, Paging paging) throws TwitterException {
        return twitter.getActivitiesByFriends(paging);
    }

    @Override
    protected Uri getContentUri() {
        return TwidereDataStore.Activities.ByFriends.CONTENT_URI;
    }
}
