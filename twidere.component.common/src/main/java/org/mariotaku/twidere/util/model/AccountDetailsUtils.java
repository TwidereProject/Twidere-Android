package org.mariotaku.twidere.util.model;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.account.AccountExtras;
import org.mariotaku.twidere.model.account.StatusNetAccountExtras;
import org.mariotaku.twidere.model.account.TwitterAccountExtras;
import org.mariotaku.twidere.model.account.cred.BasicCredentials;
import org.mariotaku.twidere.model.account.cred.Credentials;
import org.mariotaku.twidere.model.account.cred.EmptyCredentials;
import org.mariotaku.twidere.model.account.cred.OAuth2Credentials;
import org.mariotaku.twidere.model.account.cred.OAuthCredentials;

import java.io.IOException;

/**
 * Created by mariotaku on 2016/12/7.
 */

public class AccountDetailsUtils {
    public static Credentials parseCredentials(String json, @Credentials.Type String type) {
        try {
            switch (type) {
                case Credentials.Type.OAUTH:
                case Credentials.Type.XAUTH: {
                    return LoganSquare.parse(json, OAuthCredentials.class);
                }
                case Credentials.Type.BASIC: {
                    return LoganSquare.parse(json, BasicCredentials.class);
                }
                case Credentials.Type.EMPTY: {
                    return LoganSquare.parse(json, EmptyCredentials.class);
                }
                case Credentials.Type.OAUTH2: {
                    return LoganSquare.parse(json, OAuth2Credentials.class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new UnsupportedOperationException(type);
    }

    public static AccountExtras parseAccountExtras(String json, @AccountType String type) {
        if (json == null) return null;
        try {
            switch (type) {
                case AccountType.TWITTER: {
                    return LoganSquare.parse(json, TwitterAccountExtras.class);
                }
                case AccountType.STATUSNET: {
                    return LoganSquare.parse(json, StatusNetAccountExtras.class);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
