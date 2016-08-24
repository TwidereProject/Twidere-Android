package org.mariotaku.twidere.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.UriUtils;

import java.util.ArrayList;

import static org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM;
import static org.mariotaku.twidere.TwidereConstants.USER_TYPE_TWITTER_COM;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UserKeyUtils {

    private UserKeyUtils() {
    }

    @Nullable
    public static UserKey findById(Context context, String id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) return UserKey.valueOf(cur.getString(0));
        } finally {
            cur.close();
        }
        return null;
    }

    @NonNull
    public static UserKey[] findByIds(Context context, String... id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return new UserKey[0];
        try {
            final ArrayList<UserKey> accountKeys = new ArrayList<>();
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                accountKeys.add(UserKey.valueOf(cur.getString(0)));
                cur.moveToNext();
            }
            return accountKeys.toArray(new UserKey[accountKeys.size()]);
        } finally {
            cur.close();
        }
    }

    public static UserKey fromUser(User user) {
        return new UserKey(user.getId(), getUserHost(user));
    }

    public static String getUserHost(User user) {
        if (isFanfouUser(user)) return USER_TYPE_FANFOU_COM;
        return getUserHost(user.getStatusnetProfileUrl(), USER_TYPE_TWITTER_COM);
    }

    public static String getUserHost(ParcelableUser user) {
        if (isFanfouUser(user)) return USER_TYPE_FANFOU_COM;
        if (user.extras == null) return USER_TYPE_TWITTER_COM;

        return getUserHost(user.extras.statusnet_profile_url, USER_TYPE_TWITTER_COM);
    }

    public static boolean isFanfouUser(User user) {
        return user.getUniqueId() != null && user.getProfileImageUrlLarge() != null;
    }

    public static boolean isFanfouUser(ParcelableUser user) {
        return USER_TYPE_FANFOU_COM.equals(user.key.getHost());
    }


    @NonNull
    public static String getUserHost(@Nullable String uri, @Nullable String def) {
        if (def == null) {
            def = USER_TYPE_TWITTER_COM;
        }
        if (uri == null) return def;
        final String authority = UriUtils.getAuthority(uri);
        if (authority == null) return def;
        return authority.replaceAll("[^\\w\\d\\.]", "-");
    }

    public static boolean isSameHost(UserKey accountKey, UserKey userKey) {
        return isSameHost(accountKey.getHost(), userKey.getHost());
    }

    public static boolean isSameHost(String a, String b) {
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) return true;
        return TextUtils.equals(a, b);
    }
}
