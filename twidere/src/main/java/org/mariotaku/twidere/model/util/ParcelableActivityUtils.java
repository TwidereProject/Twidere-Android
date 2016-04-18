package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Processing ParcelableActivity
 * <p/>
 * Created by mariotaku on 16/1/2.
 */
public class ParcelableActivityUtils {

    private ParcelableActivityUtils() {
    }

    /**
     * @param activity        Activity for processing
     * @param filteredUserIds Those ids will be removed from source_ids.
     * @param followingOnly   Limit following users in sources
     * @return true if source ids changed, false otherwise
     */
    public static boolean initAfterFilteredSourceIds(ParcelableActivity activity, String[] filteredUserIds,
                                                     boolean followingOnly) {
        if (activity.sources == null) return false;
        if (activity.after_filtered_source_ids != null) return false;
        if (followingOnly || !ArrayUtils.isEmpty(filteredUserIds)) {
            List<UserKey> list = new ArrayList<>();
            for (ParcelableUser user : activity.sources) {
                if (followingOnly && !user.is_following) {
                    continue;
                }
                if (!ArrayUtils.contains(filteredUserIds, user.key.getId())) {
                    list.add(user.key);
                }
            }
            activity.after_filtered_source_ids = list.toArray(new UserKey[list.size()]);
            return true;
        } else {
            activity.after_filtered_source_ids = activity.source_ids;
            return false;
        }
    }

    public static ParcelableUser[] getAfterFilteredSources(ParcelableActivity activity) {
        if (activity.after_filtered_sources != null) return activity.after_filtered_sources;
        if (activity.after_filtered_source_ids == null || activity.sources.length == activity.after_filtered_source_ids.length) {
            return activity.sources;
        }
        ParcelableUser[] result = new ParcelableUser[activity.after_filtered_source_ids.length];
        for (int i = 0; i < activity.after_filtered_source_ids.length; i++) {
            for (ParcelableUser user : activity.sources) {
                if (user.key.equals(activity.after_filtered_source_ids[i])) {
                    result[i] = user;
                }
            }
        }
        return activity.after_filtered_sources = result;
    }

    public static ParcelableActivity fromActivity(final Activity activity,
                                                  final UserKey accountKey,
                                                  final boolean isGap) {
        ParcelableActivity result = new ParcelableActivity();
        result.account_key = accountKey;
        result.timestamp = activity.getCreatedAt().getTime();
        result.action = activity.getAction();
        result.max_sort_position = activity.getMaxSortPosition();
        result.min_sort_position = activity.getMinSortPosition();
        result.max_position = activity.getMaxPosition();
        result.min_position = activity.getMinPosition();
        result.sources = ParcelableUserUtils.fromUsers(activity.getSources(), accountKey);
        result.target_users = ParcelableUserUtils.fromUsers(activity.getTargetUsers(), accountKey);
        result.target_user_lists = ParcelableUserListUtils.fromUserLists(activity.getTargetUserLists(), accountKey);
        result.target_statuses = ParcelableStatusUtils.fromStatuses(activity.getTargetStatuses(), accountKey);
        result.target_object_statuses = ParcelableStatusUtils.fromStatuses(activity.getTargetObjectStatuses(), accountKey);
        result.target_object_user_lists = ParcelableUserListUtils.fromUserLists(activity.getTargetObjectUserLists(), accountKey);
        result.target_object_users = ParcelableUserUtils.fromUsers(activity.getTargetObjectUsers(), accountKey);
        if (result.sources != null) {
            result.source_ids = new UserKey[result.sources.length];
            for (int i = 0; i < result.sources.length; i++) {
                result.source_ids[i] = result.sources[i].key;
            }
        }
        result.is_gap = isGap;
        return result;
    }


    @Nullable
    public static ParcelableStatus getActivityStatus(@NonNull ParcelableActivity activity) {
        final ParcelableStatus status;
        if (Activity.Action.MENTION.equals(activity.action)) {
            status = activity.target_object_statuses[0];
        } else if (Activity.Action.REPLY.equals(activity.action)) {
            status = activity.target_statuses[0];
        } else if (Activity.Action.QUOTE.equals(activity.action)) {
            status = activity.target_statuses[0];
        } else {
            return null;
        }
        status.account_color = activity.account_color;
        status.user_color = activity.status_user_color;
        status.retweet_user_color = activity.status_retweet_user_color;
        status.quoted_user_color = activity.status_quoted_user_color;

        status.user_nickname = activity.status_user_nickname;
        status.in_reply_to_user_nickname = activity.status_in_reply_to_user_nickname;
        status.retweet_user_nickname = activity.status_retweet_user_nickname;
        status.quoted_user_nickname = activity.status_quoted_user_nickname;
        return status;
    }
}
