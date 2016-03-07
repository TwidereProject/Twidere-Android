package org.mariotaku.twidere.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccountCursorIndices;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.DataStoreUtils;

import java.util.List;

/**
 * Created by mariotaku on 16/2/20.
 */
public class ParcelableAccountUtils {

    public static AccountKey[] getAccountKeys(@NonNull ParcelableAccount[] accounts) {
        AccountKey[] ids = new AccountKey[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = accounts[i].account_key;
        }
        return ids;
    }

    @Nullable
    public static ParcelableAccount getAccount(final Context context, final long accountId,
                                               final String accountHost) {
        if (context == null || accountId < 0) return null;
        final Expression where = Expression.equals(Accounts.ACCOUNT_KEY, accountId);
        Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, where.getSQL(), null, null);
        if (cur == null) return null;
        try {
            ParcelableAccountCursorIndices i = new ParcelableAccountCursorIndices(cur);
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                final AccountKey accountKey = AccountKey.valueOf(cur.getString(i.account_key));
                if (accountKey == null) continue;
                if (accountKey.isAccount(accountId, accountHost)) {
                    return i.newObject(cur);
                }
                cur.moveToNext();
            }
            if (cur.moveToFirst()) {
                return i.newObject(cur);
            }
        } finally {
            cur.close();
        }
        return null;
    }

    public static ParcelableAccount getAccount(final Context context, final AccountKey accountKey) {
        return getAccount(context, accountKey.getId(), accountKey.getHost());
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(final Context context, final boolean activatedOnly,
                                                  final boolean officialKeyOnly) {
        final List<ParcelableAccount> list = DataStoreUtils.getAccountsList(context, activatedOnly, officialKeyOnly);
        return list.toArray(new ParcelableAccount[list.size()]);
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Context context, @Nullable final AccountKey... accountIds) {
        if (context == null) return new ParcelableAccount[0];
        final String where = accountIds != null ? Expression.in(new Columns.Column(Accounts.ACCOUNT_KEY),
                new RawItemArray(AccountKey.getIds(accountIds))).getSQL() : null;
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS_NO_CREDENTIALS, where, null, null);
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

}
