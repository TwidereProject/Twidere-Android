package org.mariotaku.twidere.model.util;

import org.mariotaku.microblog.library.statusnet.model.Group;
import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.model.UserKey;

import java.util.Date;

/**
 * Created by mariotaku on 16/3/9.
 */
public class ParcelableGroupUtils {
    private ParcelableGroupUtils() {
    }

    public static ParcelableGroup from(Group group, UserKey accountKey, int position, boolean member) {
        ParcelableGroup obj = new ParcelableGroup();
        obj.account_key = accountKey;
        obj.member = member;
        obj.position = position;
        obj.id = group.getId();
        obj.nickname = group.getNickname();
        obj.homepage = group.getHomepage();
        obj.fullname = group.getFullname();
        obj.url = group.getUrl();
        obj.description = group.getDescription();
        obj.location = group.getLocation();
        obj.created = getTime(group.getCreated());
        obj.modified = getTime(group.getModified());
        obj.admin_count = group.getAdminCount();
        obj.member_count = group.getMemberCount();
        obj.original_logo = group.getOriginalLogo();
        obj.homepage_logo = group.getHomepageLogo();
        obj.stream_logo = group.getStreamLogo();
        obj.mini_logo = group.getMiniLogo();
        obj.blocked = group.isBlocked();
        obj.id = group.getId();
        return obj;
    }

    private static long getTime(Date date) {
        if (date == null) return -1;
        return date.getTime();
    }
}
