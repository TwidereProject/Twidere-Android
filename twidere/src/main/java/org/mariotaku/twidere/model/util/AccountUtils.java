package org.mariotaku.twidere.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.annotation.AuthTypeInt;
import org.mariotaku.twidere.extension.AccountExtensionsKt;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.account.cred.Credentials;

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

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, @NonNull UserKey[] accountKeys) {
        AccountDetails[] details = new AccountDetails[accountKeys.length];
        for (int i = 0; i < accountKeys.length; i++) {
            details[i] = getAccountDetails(am, accountKeys[i]);
        }
        return details;
    }

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am) {
        Account[] accounts = getAccounts(am);
        AccountDetails[] details = new AccountDetails[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            details[i] = getAccountDetails(am, accounts[i]);
        }
        return details;
    }

    @Nullable
    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull UserKey accountKey) {
        final Account account = findByAccountKey(am, accountKey);
        if (account == null) return null;
        return getAccountDetails(am, account);
    }

    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull Account account) {
        AccountDetails details = new AccountDetails();
        details.key = AccountExtensionsKt.getAccountKey(account, am);
        details.account = account;
        details.color = AccountExtensionsKt.getColor(account, am);
        details.position = AccountExtensionsKt.getPosition(account, am);
        details.credentials = AccountExtensionsKt.getCredentials(account, am);
        details.user = AccountExtensionsKt.getAccountUser(account, am);
        details.activated = AccountExtensionsKt.isActivated(account, am);
        details.type = AccountExtensionsKt.getAccountType(account, am);
        details.credentials_type = AccountExtensionsKt.getCredentialsType(account, am);
        return details;
    }

    public static Account findByScreenName(AccountManager am, String screenName) {
        for (Account account : getAccounts(am)) {
            if (StringUtils.equalsIgnoreCase(UserKey.valueOf(account.name).getId(), screenName)) {
                return account;
            }
        }
        return null;
    }

    public static int getAccountTypeIcon(@Nullable String accountType) {
        if (accountType == null) return R.drawable.ic_account_logo_twitter;
        switch (accountType) {
            case AccountType.TWITTER: {
                return R.drawable.ic_account_logo_twitter;
            }
            case AccountType.FANFOU: {
                return R.drawable.ic_account_logo_fanfou;
            }
            case AccountType.STATUSNET: {
                return R.drawable.ic_account_logo_statusnet;
            }

        }
        return R.drawable.ic_account_logo_twitter;
    }

    public static String getCredentialsType(@AuthTypeInt int authType) {
        switch (authType) {
            case AuthTypeInt.OAUTH:
                return Credentials.Type.OAUTH;
            case AuthTypeInt.BASIC:
                return Credentials.Type.BASIC;
            case AuthTypeInt.TWIP_O_MODE:
                return Credentials.Type.EMPTY;
            case AuthTypeInt.XAUTH:
                return Credentials.Type.XAUTH;
            case AuthTypeInt.OAUTH2:
                return Credentials.Type.OAUTH2;
        }
        throw new UnsupportedOperationException();
    }

}
