package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST;

/**
 * Created by mariotaku on 16/3/11.
 */
public class CreateUserMuteTask extends AbsFriendshipOperationTask {
    public CreateUserMuteTask(Context context) {
        super(context, FriendshipTaskEvent.Action.MUTE);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials,
                           @NonNull Arguments args) throws MicroBlogException {
        return twitter.createMute(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter,
                                   @NonNull ParcelableCredentials credentials,
                                   @NonNull Arguments args, @NonNull ParcelableUser user) {
        final ContentResolver resolver = context.getContentResolver();
        Utils.setLastSeen(context, args.userKey, -1);
        for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
            final Expression where = Expression.and(
                    Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.USER_KEY)
            );
            final String[] whereArgs = {args.accountKey.toString(), args.userKey.toString()};
            resolver.delete(uri, where.getSQL(), whereArgs);
        }
        if (!user.is_following) {
            for (final Uri uri : TwidereDataStore.ACTIVITIES_URIS) {
                final Expression where = Expression.and(
                        Expression.equalsArgs(Activities.ACCOUNT_KEY),
                        Expression.equalsArgs(Activities.STATUS_USER_KEY)
                );
                final String[] whereArgs = {args.accountKey.toString(), args.userKey.toString()};
                resolver.delete(uri, where.getSQL(), whereArgs);
            }
        }
        // I bet you don't want to see this user in your auto complete list.
        final ContentValues values = new ContentValues();
        values.put(CachedRelationships.ACCOUNT_KEY, args.accountKey.toString());
        values.put(CachedRelationships.USER_KEY, args.userKey.toString());
        values.put(CachedRelationships.MUTING, true);
        resolver.insert(CachedRelationships.CONTENT_URI, values);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        final String message = context.getString(R.string.muted_user, manager.getDisplayName(user,
                nameFirst));
        Utils.showInfoMessage(context, message, false);

    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(context, R.string.action_muting, exception, true);
    }
}
