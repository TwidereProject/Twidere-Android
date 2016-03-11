package org.mariotaku.twidere.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableCredentials;

/**
 * Created by mariotaku on 16/3/11.
 */
public class ReportSpamAndBlockTask extends CreateUserBlockTask {
    public ReportSpamAndBlockTask(Context context) {
        super(context);
    }


    @NonNull
    @Override
    protected User perform(@NonNull Twitter twitter, @NonNull ParcelableCredentials credentials,
                           @NonNull Arguments args) throws TwitterException {
        return twitter.reportSpam(args.userKey.getId());
    }
}
