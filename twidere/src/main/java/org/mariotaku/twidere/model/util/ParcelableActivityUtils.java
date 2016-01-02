package org.mariotaku.twidere.model.util;

import org.apache.commons.collections.primitives.ArrayLongList;
import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableUser;

/**
 * Created by mariotaku on 16/1/2.
 */
public class ParcelableActivityUtils {
    public static void getAfterFilteredSourceIds(ParcelableActivity activity, long[] filteredUserIds, boolean followingOnly) {
        if (activity.after_filtered_source_ids != null) return;
        if (followingOnly || !ArrayUtils.isEmpty(filteredUserIds)) {
            ArrayLongList list = new ArrayLongList();
            for (ParcelableUser user : activity.sources) {
                if (followingOnly && !user.is_following) {
                    continue;
                }
                if (!ArrayUtils.contains(filteredUserIds, user.id)) {
                    list.add(user.id);
                }
            }
            activity.after_filtered_source_ids = list.toArray();
        } else {
            activity.after_filtered_source_ids = activity.source_ids;
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
                if (user.id == activity.after_filtered_source_ids[i]) {
                    result[i] = user;
                }
            }
        }
        return activity.after_filtered_sources = result;
    }
}
