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
    public static ParcelableAccount getAccount(@NonNull final Context context,
                                               @NonNull final AccountKey accountKey) {
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
