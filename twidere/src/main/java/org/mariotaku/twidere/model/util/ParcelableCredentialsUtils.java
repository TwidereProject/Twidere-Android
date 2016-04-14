package org.mariotaku.twidere.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableCredentialsCursorIndices;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.DataStoreUtils;

/**
 * Created by mariotaku on 16/3/4.
 */
public class ParcelableCredentialsUtils {
    private ParcelableCredentialsUtils() {
    }

    public static boolean isOAuth(int authType) {
        switch (authType) {
            case ParcelableCredentials.AuthType.OAUTH:
            case ParcelableCredentials.AuthType.XAUTH: {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(@NonNull final Context context,
                                                       @NonNull final UserKey accountKey) {
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


    @NonNull
    public static ParcelableCredentials[] getCredentialses(@Nullable final Cursor cursor, @Nullable final ParcelableCredentialsCursorIndices indices) {
        if (cursor == null || indices == null) return new ParcelableCredentials[0];
        try {
            cursor.moveToFirst();
            final ParcelableCredentials[] credentialses = new ParcelableCredentials[cursor.getCount()];
            while (!cursor.isAfterLast()) {
                credentialses[cursor.getPosition()] = indices.newObject(cursor);
                cursor.moveToNext();
            }
            return credentialses;
        } finally {
            cursor.close();
        }
    }


    public static ParcelableCredentials[] getCredentialses(@NonNull final Context context) {
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS, null, null, null);
        if (cur == null) return new ParcelableCredentials[0];
        return getCredentialses(cur, new ParcelableCredentialsCursorIndices(cur));
    }
}
