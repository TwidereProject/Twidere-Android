package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.UriUtils;

import static org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM;
import static org.mariotaku.twidere.TwidereConstants.USER_TYPE_TWITTER_COM;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UserKeyUtils {

    private UserKeyUtils() {
    }

    public static UserKey fromUser(@NonNull User user) {
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

    public static boolean isSameHost(@Nullable UserKey accountKey, @Nullable UserKey userKey) {
        if (accountKey == null || userKey == null) return false;
        return isSameHost(accountKey.getHost(), userKey.getHost());
    }

    public static boolean isSameHost(String a, String b) {
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) return true;
        return TextUtils.equals(a, b);
    }
}
