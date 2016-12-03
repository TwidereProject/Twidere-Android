package org.mariotaku.twidere.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.extension.AccountExtensionsKt;
import org.mariotaku.twidere.model.AccountDetails;
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

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, @NonNull Account[] accounts) {
        AccountDetails[] details = new AccountDetails[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            details[i] = getAccountDetails(am, accounts[i]);
        }
        return details;
    }

    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull Account account) {
        AccountDetails details = new AccountDetails();
        details.key = AccountExtensionsKt.getAccountKey(account, am);
        details.account = account;
        details.color = AccountExtensionsKt.getColor(account, am);
        details.position = AccountExtensionsKt.getPosition(account, am);
        details.credentials = AccountExtensionsKt.getCredentials(account, am);
        details.user = AccountExtensionsKt.getAccountUser(account, am);
        details.activated = AccountExtensionsKt.isAccountActivated(account, am);
        details.type = AccountExtensionsKt.getAccountType(account, am);
        details.credentials_type = AccountExtensionsKt.getCredentialsType(account, am);
        return details;
    }
}
