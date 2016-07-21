package org.mariotaku.twidere.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccountCursorIndices;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.util.List;

/**
 * Created by mariotaku on 16/2/20.
 */
public class ParcelableAccountUtils {

    private ParcelableAccountUtils() {
    }

    public static UserKey[] getAccountKeys(@NonNull ParcelableAccount[] accounts) {
        UserKey[] ids = new UserKey[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = accounts[i].account_key;
        }
        return ids;
    }

    @Nullable
    public static ParcelableAccount getAccount(@NonNull final Context context,
                                               @NonNull final UserKey accountKey) {
        final Cursor c = DataStoreUtils.getAccountCursor(context,
                Accounts.COLUMNS_NO_CREDENTIALS, accountKey);
        if (c == null) return null;
        try {
            final ParcelableAccountCursorIndices i = new ParcelableAccountCursorIndices(c);
            if (c.moveToFirst()) {
                return i.newObject(c);
            }
        } finally {
            c.close();
        }
        return null;
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(final Context context, final boolean activatedOnly,
                                                  final boolean officialKeyOnly) {
        final List<ParcelableAccount> list = DataStoreUtils.getAccountsList(context, activatedOnly, officialKeyOnly);
        return list.toArray(new ParcelableAccount[list.size()]);
    }

    public static ParcelableAccount[] getAccounts(@NonNull final Context context) {
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, null, null, Accounts.SORT_POSITION);
        if (cur == null) return new ParcelableAccount[0];
        return getAccounts(cur, new ParcelableAccountCursorIndices(cur));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@NonNull final Context context, @NonNull final UserKey... accountIds) {
        final String where = Expression.in(new Columns.Column(Accounts.ACCOUNT_KEY),
                new ArgsArray(accountIds.length)).getSQL();
        final String[] whereArgs = TwidereArrayUtils.toStringArray(accountIds);
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, where, whereArgs, null);
        if (cur == null) return new ParcelableAccount[0];
        return getAccounts(cur, new ParcelableAccountCursorIndices(cur));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor) {
        if (cursor == null) return new ParcelableAccount[0];
        return getAccounts(cursor, new ParcelableAccountCursorIndices(cursor));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor, @Nullable final ParcelableAccountCursorIndices indices) {
        if (cursor == null || indices == null) return new ParcelableAccount[0];
        try {
            cursor.moveToFirst();
            final ParcelableAccount[] names = new ParcelableAccount[cursor.getCount()];
            while (!cursor.isAfterLast()) {
                names[cursor.getPosition()] = indices.newObject(cursor);
                cursor.moveToNext();
            }
            return names;
        } finally {
            cursor.close();
        }
    }

    @NonNull
    @ParcelableAccount.Type
    public static String getAccountType(@NonNull ParcelableAccount account) {
        if (account.account_type == null) return ParcelableAccount.Type.TWITTER;
        return account.account_type;
    }

    public static int getAccountTypeIcon(@Nullable String accountType) {
        if (accountType == null) return R.drawable.ic_account_logo_twitter;
        switch (accountType) {
            case ParcelableAccount.Type.TWITTER: {
                return R.drawable.ic_account_logo_twitter;
            }
            case ParcelableAccount.Type.FANFOU: {
                return R.drawable.ic_account_logo_fanfou;
            }
            case ParcelableAccount.Type.STATUSNET: {
                return R.drawable.ic_account_logo_statusnet;
            }

        }
        return R.drawable.ic_account_logo_twitter;
    }
}
