package org.mariotaku.twidere.task;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.annotation.ReadPositionTag;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.CursorTimestampResponse;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.task.twitter.GetActivitiesTask;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 16/2/11.
 */
public class GetActivitiesAboutMeTask extends GetActivitiesTask {

    public GetActivitiesAboutMeTask(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_INTERACTIONS;
    }

    @Override
    protected void saveReadPosition(long accountId, Twitter twitter) {
        try {
            CursorTimestampResponse response = twitter.getActivitiesAboutMeUnread(true);
            final String tag = Utils.getReadPositionTagWithAccounts(ReadPositionTag.ACTIVITIES_ABOUT_ME, accountId);
            readStateManager.setPosition(tag, response.getCursor(), false);
        } catch (TwitterException e) {
            // Ignore
        }
    }

    @Override
    protected ResponseList<Activity> getActivities(@NonNull final Twitter twitter, final long accountId, final Paging paging) throws TwitterException {
        if (Utils.isOfficialCredentials(context, accountId)) {
            return twitter.getActivitiesAboutMe(paging);
        }
        final ResponseList<Activity> activities = new ResponseList<>();
        for (Status status : twitter.getMentionsTimeline(paging)) {
            activities.add(Activity.fromMention(accountId, status));
        }
        return activities;
    }

    @Override
    protected Uri getContentUri() {
        return Activities.AboutMe.CONTENT_URI;
    }
}
