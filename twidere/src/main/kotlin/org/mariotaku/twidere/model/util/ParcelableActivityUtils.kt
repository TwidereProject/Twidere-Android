package org.mariotaku.twidere.model.util

import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

/**
 * Processing ParcelableActivity
 *
 *
 * Created by mariotaku on 16/1/2.
 */
object ParcelableActivityUtils {

    /**
     * @param activity        Activity for processing
     * *
     * @param filteredUserKeys Those ids will be removed from source_ids.
     * *
     * @param followingOnly   Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun initAfterFilteredSourceIds(activity: ParcelableActivity, filteredUserKeys: Array<UserKey>,
            followingOnly: Boolean): Boolean {
        if (activity.sources == null) return false
        if (activity.after_filtered_source_ids != null) return false
        if (followingOnly || filteredUserKeys.isNotEmpty()) {
            val list = activity.sources.filter { user ->
                if (followingOnly && !user.is_following) {
                    return@filter false
                }

                if (!filteredUserKeys.contains(user.key)) {
                    return@filter true
                }
                return@filter false
            }.map { it.key }
            activity.after_filtered_source_ids = list.toTypedArray()
            return true
        } else {
            activity.after_filtered_source_ids = activity.source_keys
            return false
        }
    }

    fun getAfterFilteredSources(activity: ParcelableActivity): Array<ParcelableUser> {
        if (activity.after_filtered_sources != null) return activity.after_filtered_sources
        if (activity.after_filtered_source_ids == null || activity.sources.size == activity.after_filtered_source_ids.size) {
            return activity.sources
        }
        val result = Array(activity.after_filtered_source_ids.size) { idx ->
            return@Array activity.sources.find { it.key == activity.after_filtered_source_ids[idx] }!!
        }
        activity.after_filtered_sources = result
        return result
    }


}
