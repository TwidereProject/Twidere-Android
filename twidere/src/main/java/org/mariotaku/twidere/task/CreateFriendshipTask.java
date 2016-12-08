package org.mariotaku.twidere.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 16/3/11.
 */
public class CreateFriendshipTask extends AbsFriendshipOperationTask implements Constants {

    public CreateFriendshipTask(final Context context) {
        super(context, FriendshipTaskEvent.Action.FOLLOW);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args) throws MicroBlogException {
        switch (details.type) {
            case AccountType.FANFOU: {
                return twitter.createFanfouFriendship(args.getUserKey().getId());
            }
        }
        return twitter.createFriendship(args.getUserKey().getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args, @NonNull ParcelableUser user) {
        user.is_following = true;
        Utils.setLastSeen(getContext(), user.key, System.currentTimeMillis());
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        if (USER_TYPE_FANFOU_COM.equals(params.getAccountKey().getHost())) {
            // Fanfou returns 403 for follow request
            if (exception instanceof MicroBlogException) {
                MicroBlogException te = (MicroBlogException) exception;
                if (te.getStatusCode() == 403 && !TextUtils.isEmpty(te.getErrorMessage())) {
                    Utils.showErrorMessage(getContext(), te.getErrorMessage(), false);
                    return;
                }
            }
        }
        Utils.showErrorMessage(getContext(), R.string.action_following, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final String message;
        final boolean nameFirst = getPreferences().getBoolean(KEY_NAME_FIRST);
        if (user.is_protected) {
            message = getContext().getString(R.string.sent_follow_request_to_user,
                    getManager().getDisplayName(user, nameFirst));
        } else {
            message = getContext().getString(R.string.followed_user,
                    getManager().getDisplayName(user, nameFirst));
        }
        Utils.showOkMessage(getContext(), message, false);
    }

}
