package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST;

/**
 * Created by mariotaku on 16/3/11.
 */
public class DestroyFriendshipTask extends AbsFriendshipOperationTask {

    public DestroyFriendshipTask(final Context context) {
        super(context, FriendshipTaskEvent.Action.UNFOLLOW);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args) throws MicroBlogException {
        switch (details.type) {
            case AccountType.FANFOU: {
                return twitter.destroyFanfouFriendship(args.getUserKey().getId());
            }
        }
        return twitter.destroyFriendship(args.getUserKey().getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args, @NonNull ParcelableUser user) {
        user.is_following = false;
        Utils.setLastSeen(getContext(), user.key, -1);
        final Expression where = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.or(Expression.equalsArgs(Statuses.USER_KEY),
                        Expression.equalsArgs(Statuses.RETWEETED_BY_USER_KEY)));
        final String[] whereArgs = {args.getUserKey().toString(), args.getUserKey().toString(),
                args.getUserKey().toString()};
        final ContentResolver resolver = getContext().getContentResolver();
        resolver.delete(Statuses.CONTENT_URI, where.getSQL(), whereArgs);
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(getContext(), R.string.action_unfollowing, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = getPreferences().getBoolean(KEY_NAME_FIRST);
        final String message = getContext().getString(R.string.unfollowed_user,
                getManager().getDisplayName(user, nameFirst));
        Utils.showInfoMessage(getContext(), message, false);
    }

}
