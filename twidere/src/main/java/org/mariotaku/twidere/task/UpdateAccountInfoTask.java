package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Pair;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 16/3/8.
 */
public class UpdateAccountInfoTask extends AbstractTask<Pair<UserKey, ParcelableUser>, Object, Object> {
    private final Context context;

    public UpdateAccountInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doLongOperation(Pair<UserKey, ParcelableUser> params) {
        final ContentResolver resolver = context.getContentResolver();
        final UserKey accountKey = params.first;
        final ParcelableUser user = params.second;
        if (!Utils.isMyAccount(context, user.key)) {
            return null;
        }

        final String accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).getSQL();
        final String[] accountWhereArgs = {accountKey.toString()};

        final ContentValues accountValues = new ContentValues();
        accountValues.put(Accounts.NAME, user.name);
        accountValues.put(Accounts.SCREEN_NAME, user.screen_name);
        accountValues.put(Accounts.PROFILE_IMAGE_URL, user.profile_image_url);
        accountValues.put(Accounts.PROFILE_BANNER_URL, user.profile_banner_url);
        accountValues.put(Accounts.ACCOUNT_USER, JsonSerializer.serialize(user,
                ParcelableUser.class));
        accountValues.put(Accounts.ACCOUNT_KEY, String.valueOf(user.key));

        resolver.update(Accounts.CONTENT_URI, accountValues, accountWhere, accountWhereArgs);

        final ContentValues accountKeyValues = new ContentValues();
        accountKeyValues.put(AccountSupportColumns.ACCOUNT_KEY, String.valueOf(user.key));

        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);

        return null;
    }
}
