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
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
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
    protected void saveReadPosition(@NonNull UserKey accountId, ParcelableCredentials credentials, @NonNull Twitter twitter) {
        if (ParcelableAccount.Type.TWITTER.equals(ParcelableAccountUtils.getAccountType(credentials))) {
            if (Utils.isOfficialCredentials(context, credentials)) {
                try {
                    CursorTimestampResponse response = twitter.getActivitiesAboutMeUnread(true);
                    final String tag = Utils.getReadPositionTagWithAccounts(ReadPositionTag.ACTIVITIES_ABOUT_ME,
                            accountId);
                    readStateManager.setPosition(tag, response.getCursor(), false);
                } catch (TwitterException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    protected ResponseList<Activity> getActivities(@NonNull final Twitter twitter,
                                                   @NonNull final ParcelableCredentials credentials,
                                                   @NonNull final Paging paging) throws TwitterException {
        if (Utils.isOfficialCredentials(context, credentials)) {
            return twitter.getActivitiesAboutMe(paging);
        }
        final ResponseList<Activity> activities = new ResponseList<>();
        final ResponseList<Status> statuses;
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.FANFOU: {
                statuses = twitter.getMentions(paging);
                break;
            }
            default: {
                statuses = twitter.getMentionsTimeline(paging);
                break;
            }
        }
        for (Status status : statuses) {
            activities.add(Activity.fromMention(credentials.account_key.getId(), status));
        }
        return activities;
    }

    @Override
    protected Uri getContentUri() {
        return Activities.AboutMe.CONTENT_URI;
    }
}
