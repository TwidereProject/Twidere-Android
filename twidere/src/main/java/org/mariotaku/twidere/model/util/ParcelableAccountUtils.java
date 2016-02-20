package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableAccount;

/**
 * Created by mariotaku on 16/2/20.
 */
public class ParcelableAccountUtils {

    public static long[] getAccountIds(@NonNull ParcelableAccount[] accounts) {
        long[] ids = new long[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = accounts[i].account_id;
        }
        return ids;
    }

}
