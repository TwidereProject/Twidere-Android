package org.mariotaku.twidere.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.extension.AccountExtensionsKt;
import org.mariotaku.twidere.model.UserKey;

import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE;

/**
 * Created by mariotaku on 2016/12/3.
 */

public class AccountUtils {

    @Nullable
    public static Account findByAccountKey(@NonNull AccountManager am, @NonNull UserKey userKey) {
        for (Account account : getAccounts(am)) {
            if (userKey.equals(AccountExtensionsKt.getAccountKey(account, am))) {
                return account;
            }
        }
        return null;
    }

    public static Account[] getAccounts(@NonNull AccountManager am) {
        //noinspection MissingPermission
        return am.getAccountsByType(ACCOUNT_TYPE);
    }
}
