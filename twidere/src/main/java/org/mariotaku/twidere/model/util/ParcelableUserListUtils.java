package org.mariotaku.twidere.model.util;

import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.TwitterContentUtils;

/**
 * Created by mariotaku on 16/3/5.
 */
public class ParcelableUserListUtils {
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
}
