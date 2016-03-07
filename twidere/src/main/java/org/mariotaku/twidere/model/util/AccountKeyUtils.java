package org.mariotaku.twidere.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.DataStoreUtils;

import java.util.ArrayList;

/**
 * Created by mariotaku on 16/3/7.
 */
public class AccountKeyUtils {

    @Nullable
    public static AccountKey findById(Context context, long id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) return AccountKey.valueOf(cur.getString(0));
        } finally {
            cur.close();
        }
        return null;
    }

    @NonNull
    public static AccountKey[] findByIds(Context context, long... id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return new AccountKey[0];
        try {
            final ArrayList<AccountKey> accountKeys = new ArrayList<>();
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                accountKeys.add(AccountKey.valueOf(cur.getString(0)));
                cur.moveToNext();
            }
            return accountKeys.toArray(new AccountKey[accountKeys.size()]);
        } finally {
            cur.close();
        }
    }
}
