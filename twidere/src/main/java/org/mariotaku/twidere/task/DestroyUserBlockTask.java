package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST;

/**
 * Created by mariotaku on 16/3/11.
 */
public class DestroyUserBlockTask extends AbsFriendshipOperationTask {
    public DestroyUserBlockTask(Context context) {
        super(context, FriendshipTaskEvent.Action.UNBLOCK);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull ParcelableCredentials credentials,
                           @NonNull Arguments args) throws MicroBlogException {
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.FANFOU: {
                return twitter.destroyFanfouBlock(args.userKey.getId());
            }
        }
        return twitter.destroyBlock(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter,
                                   @NonNull ParcelableCredentials credentials,
                                   @NonNull Arguments args, @NonNull ParcelableUser user) {
        final ContentResolver resolver = context.getContentResolver();
        // I bet you don't want to see this user in your auto complete list.
        final ContentValues values = new ContentValues();
        values.put(CachedRelationships.ACCOUNT_KEY, args.accountKey.toString());
        values.put(CachedRelationships.USER_KEY, args.userKey.toString());
        values.put(CachedRelationships.BLOCKING, false);
        values.put(CachedRelationships.FOLLOWING, false);
        values.put(CachedRelationships.FOLLOWED_BY, false);
        resolver.insert(CachedRelationships.CONTENT_URI, values);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        final String message = context.getString(R.string.unblocked_user, manager.getDisplayName(user,
                nameFirst));
        Utils.showInfoMessage(context, message, false);

    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(context, R.string.action_unblocking, exception, true);
    }
}
