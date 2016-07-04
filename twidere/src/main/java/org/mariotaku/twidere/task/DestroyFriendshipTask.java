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
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.provider.TwidereDataStore;
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
    protected User perform(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials, @NonNull Arguments args) throws MicroBlogException {
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.FANFOU: {
                return twitter.destroyFanfouFriendship(args.userKey.getId());
            }
        }
        return twitter.destroyFriendship(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials, @NonNull Arguments args, @NonNull ParcelableUser user) {
        user.is_following = false;
        Utils.setLastSeen(context, user.key, -1);
        final Expression where = Expression.and(Expression.equalsArgs(TwidereDataStore.Statuses.ACCOUNT_KEY),
                Expression.or(Expression.equalsArgs(TwidereDataStore.Statuses.USER_KEY),
                        Expression.equalsArgs(TwidereDataStore.Statuses.RETWEETED_BY_USER_KEY)));
        final String[] whereArgs = {args.userKey.toString(), args.userKey.toString(),
                args.userKey.toString()};
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(TwidereDataStore.Statuses.CONTENT_URI, where.getSQL(), whereArgs);
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(context, R.string.action_unfollowing, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        final String message = context.getString(R.string.unfollowed_user,
                manager.getDisplayName(user, nameFirst));
        Utils.showInfoMessage(context, message, false);
    }

}
