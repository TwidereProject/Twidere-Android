package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.Tab;
import org.mariotaku.twidere.model.TabCursorIndices;
import org.mariotaku.twidere.model.TabValuesCreator;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.tab.argument.TabArguments;
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.JsonSerializer;

import kotlin.Pair;

/**
 * Created by mariotaku on 16/3/8.
 */
public class UpdateAccountInfoTask extends AbstractTask<Pair<ParcelableAccount, ParcelableUser>, Object, Object> {
    private final Context context;

    public UpdateAccountInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doLongOperation(Pair<ParcelableAccount, ParcelableUser> params) {
        final ContentResolver resolver = context.getContentResolver();
        final ParcelableAccount account = params.getFirst();
        final ParcelableUser user = params.getSecond();
        if (account == null || user == null) return null;
        if (user.is_cache) {
            return null;
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return null;
        }

        final String accountWhere = Expression.equalsArgs(Accounts._ID).getSQL();
        final String[] accountWhereArgs = {String.valueOf(account.id)};

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
        final String accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).getSQL();
        final String[] accountKeyWhereArgs = {account.account_key.toString()};


        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);

        updateTabs(context, resolver, user.key);


        return null;
    }

    private void updateTabs(@NonNull Context context, @NonNull ContentResolver resolver, UserKey accountKey) {
        Cursor tabsCursor = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, null);
        if (tabsCursor == null) return;
        try {
            TabCursorIndices indices = new TabCursorIndices(tabsCursor);
            tabsCursor.moveToFirst();
            LongSparseArray<ContentValues> values = new LongSparseArray<>();
            while (!tabsCursor.isAfterLast()) {
                Tab tab = indices.newObject(tabsCursor);
                TabArguments arguments = tab.getArguments();
                if (arguments != null) {
                    final String accountId = arguments.getAccountId();
                    final UserKey[] keys = arguments.getAccountKeys();
                    if (TextUtils.equals(accountKey.getId(), accountId) && keys == null) {
                        arguments.setAccountKeys(new UserKey[]{accountKey});
                        values.put(tab.getId(), TabValuesCreator.create(tab));
                    }
                }
                tabsCursor.moveToNext();
            }
            final String where = Expression.equalsArgs(Tabs._ID).getSQL();
            for (int i = 0, j = values.size(); i < j; i++) {
                final String[] whereArgs = {String.valueOf(values.keyAt(i))};
                resolver.update(Tabs.CONTENT_URI, values.valueAt(i), where, whereArgs);
            }
        } finally {
            tabsCursor.close();
        }
    }
}
