package org.mariotaku.twidere.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.annotation.AuthTypeInt;
import org.mariotaku.twidere.model.ParcelableCredentialsExtensionsKt;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.account.cred.Credentials;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 16/3/4.
 */
public class ParcelableCredentialsUtils {
    private ParcelableCredentialsUtils() {
    }

    public static boolean isOAuth(int authType) {
        switch (authType) {
            case AuthTypeInt.OAUTH:
            case AuthTypeInt.XAUTH: {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(@NonNull final Context context,
                                                       @NonNull final UserKey accountKey) {
        final AccountManager am = AccountManager.get(context);
        final Account account = AccountUtils.findByAccountKey(am, accountKey);
        if (account == null) return null;
        return ParcelableCredentialsExtensionsKt.toParcelableCredentials(account, am);
    }

    @NonNull
    public static List<ParcelableCredentials> getCredentialses(final Context context, final boolean activatedOnly,
                                                               final boolean officialKeyOnly) {
        ArrayList<ParcelableCredentials> credentialses = new ArrayList<>();
        for (ParcelableCredentials credentials : getCredentialses(context)) {
            boolean activated = credentials.is_activated;
            if (!activated && activatedOnly) continue;
            boolean isOfficialKey = TwitterContentUtils.isOfficialKey(context,
                    credentials.consumer_key, credentials.consumer_secret);
            if (!isOfficialKey && officialKeyOnly) continue;
            credentialses.add(credentials);
        }
        return credentialses;
    }


    public static ParcelableCredentials[] getCredentialses(@NonNull final Context context) {
        final AccountManager am = AccountManager.get(context);
        final Account[] accounts = AccountUtils.getAccounts(am);
        final ParcelableCredentials[] credentialses = new ParcelableCredentials[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            credentialses[i] = ParcelableCredentialsExtensionsKt.toParcelableCredentials(accounts[i], am);
        }
        return credentialses;
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
