package org.mariotaku.twidere.model.util;

import android.text.TextUtils;

import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.TwitterContentUtils;

/**
 * Created by mariotaku on 16/3/5.
 */
public class ParcelableUserListUtils {
    private ParcelableUserListUtils() {
    }

    public static ParcelableUserList from(UserList list, UserKey accountKey) {
        return from(list, accountKey, 0, false);
    }

    public static ParcelableUserList from(UserList list, UserKey accountKey, long position, boolean isFollowing) {
        ParcelableUserList obj = new ParcelableUserList();
        final User user = list.getUser();
        obj.position = position;
        obj.account_key = accountKey;
        obj.id = list.getId();
        obj.is_public = UserList.Mode.PUBLIC.equals(list.getMode());
        obj.is_following = isFollowing;
        obj.name = list.getName();
        obj.description = list.getDescription();
        obj.user_key = UserKeyUtils.fromUser(user);
        obj.user_name = user.getName();
        obj.user_screen_name = user.getScreenName();
        obj.user_profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        obj.members_count = list.getMemberCount();
        obj.subscribers_count = list.getSubscriberCount();
        return obj;
    }

    public static ParcelableUserList[] fromUserLists(UserList[] userLists, UserKey accountKey) {
        if (userLists == null) return null;
        int size = userLists.length;
        final ParcelableUserList[] result = new ParcelableUserList[size];
        for (int i = 0; i < size; i++) {
            result[i] = from(userLists[i], accountKey);
        }
        return result;
    }

    public static boolean check(ParcelableUserList userList, UserKey accountKey, String listId,
                                UserKey userKey, String screenName, String listName) {
        if (!userList.account_key.equals(accountKey)) return false;
        if (listId != null) {
            return TextUtils.equals(listId, userList.id);
        } else if (listName != null) {
            if (!TextUtils.equals(listName, userList.name)) return false;
            if (userKey != null) {
                return userKey.equals(userList.user_key);
            } else if (screenName != null) {
                return TextUtils.equals(screenName, userList.user_screen_name);
            }
        }
        return false;
    }
}
