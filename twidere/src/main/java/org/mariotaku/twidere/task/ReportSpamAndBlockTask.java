package org.mariotaku.twidere.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
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
    protected User perform(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials,
                           @NonNull Arguments args) throws MicroBlogException {
        return twitter.reportSpam(args.userKey.getId());
    }
}
