package org.mariotaku.twidere.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST;

/**
 * Created by mariotaku on 16/3/11.
 */
public class DenyFriendshipTask extends AbsFriendshipOperationTask {

    public DenyFriendshipTask(final Context context) {
        super(context, FriendshipTaskEvent.Action.DENY);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args) throws MicroBlogException {
        switch (details.type) {
            case AccountType.FANFOU: {
                return twitter.denyFanfouFriendship(args.getUserKey().getId());
            }
        }
        return twitter.denyFriendship(args.getUserKey().getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args, @NonNull ParcelableUser user) {
        Utils.setLastSeen(getContext(), user.key, -1);
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(getContext(), R.string.action_denying_follow_request, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = getPreferences().getBoolean(KEY_NAME_FIRST);
        final String message = getContext().getString(R.string.denied_users_follow_request,
                getManager().getDisplayName(user, nameFirst));
        Utils.showOkMessage(getContext(), message, false);
    }

}
