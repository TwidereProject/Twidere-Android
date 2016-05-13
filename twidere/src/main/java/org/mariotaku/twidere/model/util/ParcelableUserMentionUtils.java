package org.mariotaku.twidere.model.util;

import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/7.
 */
public class ParcelableUserMentionUtils {
    private ParcelableUserMentionUtils() {
    }

    public static ParcelableUserMention fromMentionEntity(final User user,
                                                          final UserMentionEntity entity) {
        ParcelableUserMention obj = new ParcelableUserMention();
        obj.key = new UserKey(entity.getId(), UserKeyUtils.getUserHost(user));
        obj.name = entity.getName();
        obj.screen_name = entity.getScreenName();
        return obj;
    }

    public static ParcelableUserMention[] fromUserMentionEntities(final User user,
                                                                  final UserMentionEntity[] entities) {
        if (entities == null) return null;
        final ParcelableUserMention[] mentions = new ParcelableUserMention[entities.length];
        for (int i = 0, j = entities.length; i < j; i++) {
            mentions[i] = fromMentionEntity(user, entities[i]);
        }
        return mentions;
    }
}
