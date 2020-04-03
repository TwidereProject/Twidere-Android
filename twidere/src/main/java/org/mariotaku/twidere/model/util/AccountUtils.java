package org.mariotaku.twidere.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.annotation.AuthTypeInt;
import org.mariotaku.twidere.extension.model.AccountExtensionsKt;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.account.cred.Credentials;
import org.mariotaku.twidere.util.Utils;

import java.util.Arrays;

import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_TYPE;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_ACTIVATED;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_COLOR;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_CREDS_TYPE;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_EXTRAS;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_KEY;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_POSITION;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_TEST;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_TYPE;
import static org.mariotaku.twidere.TwidereConstants.ACCOUNT_USER_DATA_USER;

/**
 * Created by mariotaku on 2016/12/3.
 */

public class AccountUtils {

    public static final String[] ACCOUNT_USER_DATA_KEYS = {
            ACCOUNT_USER_DATA_KEY,
            ACCOUNT_USER_DATA_TYPE,
            ACCOUNT_USER_DATA_CREDS_TYPE,
            ACCOUNT_USER_DATA_ACTIVATED,
            ACCOUNT_USER_DATA_USER,
            ACCOUNT_USER_DATA_EXTRAS,
            ACCOUNT_USER_DATA_COLOR,
            ACCOUNT_USER_DATA_POSITION,
            ACCOUNT_USER_DATA_TEST,
    };

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

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, @NonNull Account[] accounts, boolean getCredentials) {
        AccountDetails[] details = new AccountDetails[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            details[i] = getAccountDetails(am, accounts[i], getCredentials);
        }
        Arrays.sort(details);
        return details;
    }

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, @NonNull UserKey[] accountKeys, boolean getCredentials) {
        AccountDetails[] details = new AccountDetails[accountKeys.length];
        for (int i = 0; i < accountKeys.length; i++) {
            details[i] = getAccountDetails(am, accountKeys[i], getCredentials);
        }
        Arrays.sort(details);
        return details;
    }

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, boolean getCredentials) {
        return getAllAccountDetails(am, getAccounts(am), getCredentials);
    }

    @Nullable
    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull UserKey accountKey, boolean getCredentials) {
        final Account account = findByAccountKey(am, accountKey);
        if (account == null) return null;
        return getAccountDetails(am, account, getCredentials);
    }

    @Nullable
    public static AccountDetails getDefaultAccountDetails(@NonNull Context context, @NonNull AccountManager am, boolean getCredentials) {
        final UserKey accountKey = Utils.INSTANCE.getDefaultAccountKey(context);
        if (accountKey == null) return null;
        final Account account = findByAccountKey(am, accountKey);
        if (account == null) return null;
        return getAccountDetails(am, account, getCredentials);
    }

    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull Account account, boolean getCredentials) {
        AccountDetails details = new AccountDetails();
        details.key = AccountExtensionsKt.getAccountKey(account, am);
        details.account = account;
        details.color = AccountExtensionsKt.getColor(account, am);
        details.position = AccountExtensionsKt.getPosition(account, am);
        details.activated = AccountExtensionsKt.isActivated(account, am);
        details.type = AccountExtensionsKt.getAccountType(account, am);
        details.credentials_type = AccountExtensionsKt.getCredentialsType(account, am);
        details.user = AccountExtensionsKt.getAccountUser(account, am);
        details.user.color = details.color;

        details.extras = AccountExtensionsKt.getAccountExtras(account, am);

        if (getCredentials) {
            details.credentials = AccountExtensionsKt.getCredentials(account, am);
        }
        return details;
    }

    @Nullable
    public static Account findByScreenName(AccountManager am, @NonNull String screenName) {
        for (Account account : getAccounts(am)) {
            if (screenName.equalsIgnoreCase(AccountExtensionsKt.getAccountUser(account, am).screen_name)) {
                return account;
            }
        }
        return null;
    }

    public static boolean isOfficial(@Nullable final Context context, @NonNull final UserKey accountKey) {
        if (context == null) {
            return false;
        }
        AccountManager am = AccountManager.get(context);
        Account account = AccountUtils.findByAccountKey(am, accountKey);
        if (account == null) return false;
        return AccountExtensionsKt.isOfficial(account, am, context);
    }

    public static boolean hasOfficialKeyAccount(Context context) {
        final AccountManager am = AccountManager.get(context);
        for (Account account : getAccounts(am)) {
            if (AccountExtensionsKt.isOfficial(account, am, context)) {
                return true;
            }
        }
        return false;
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
            case AccountType.MASTODON: {
                return R.drawable.ic_account_logo_mastodon;
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


    public static boolean hasAccountPermission(@NonNull AccountManager am) {
        try {
            getAccounts(am);
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }



}
