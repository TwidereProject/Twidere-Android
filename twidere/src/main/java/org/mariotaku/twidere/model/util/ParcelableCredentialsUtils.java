package org.mariotaku.twidere.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableCredentialsCursorIndices;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.DataStoreUtils;

/**
 * Created by mariotaku on 16/3/4.
 */
public class ParcelableCredentialsUtils {
    public static boolean isOAuth(int authType) {
        switch (authType) {
            case ParcelableCredentials.AUTH_TYPE_OAUTH:
            case ParcelableCredentials.AUTH_TYPE_XAUTH: {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(@NonNull final Context context,
                                                       @NonNull final AccountKey accountKey) {
        final Cursor c = DataStoreUtils.getAccountCursor(context, Accounts.COLUMNS, accountKey);
        if (c == null) return null;
        try {
            final ParcelableCredentialsCursorIndices i = new ParcelableCredentialsCursorIndices(c);
            if (c.moveToFirst()) {
                return i.newObject(c);
            }
        } finally {
            c.close();
        }
        return null;
    }
}
