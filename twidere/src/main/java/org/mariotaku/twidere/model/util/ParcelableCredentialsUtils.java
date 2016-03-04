package org.mariotaku.twidere.model.util;

import org.mariotaku.twidere.model.ParcelableCredentials;

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
}
