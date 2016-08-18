package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import java.util.*

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
     * @param filteredUserIds Those ids will be removed from source_ids.
     * *
     * @param followingOnly   Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun initAfterFilteredSourceIds(activity: ParcelableActivity, filteredUserIds: Array<UserKey>,
                                   followingOnly: Boolean): Boolean {
        if (activity.sources == null) return false
        if (activity.after_filtered_source_ids != null) return false
        if (followingOnly || filteredUserIds.isNotEmpty()) {
            val list = ArrayList<UserKey>()
            for (user in activity.sources) {
                if (followingOnly && !user.is_following) {
                    continue
                }

                if (!filteredUserIds.contains(user.key)) {
                    list.add(user.key)
                }
            }
            activity.after_filtered_source_ids = list.toTypedArray()
            return true
        } else {
            activity.after_filtered_source_ids = activity.source_ids
            return false
        }
    }

    fun getAfterFilteredSources(activity: ParcelableActivity): Array<ParcelableUser> {
        if (activity.after_filtered_sources != null) return activity.after_filtered_sources
        if (activity.after_filtered_source_ids == null || activity.sources.size == activity.after_filtered_source_ids.size) {
            return activity.sources
        }
        val result = Array<ParcelableUser>(activity.after_filtered_source_ids.size) { idx ->
            return@Array activity.sources.find { it.key == activity.after_filtered_source_ids[idx] }!!
        }
        activity.after_filtered_sources = result
        return result
    }

    fun fromActivity(activity: Activity,
                     accountKey: UserKey,
                     isGap: Boolean): ParcelableActivity {
        val result = ParcelableActivity()
        result.account_key = accountKey
        result.timestamp = activity.createdAt.time
        result.action = activity.action
        result.max_sort_position = activity.maxSortPosition
        result.min_sort_position = activity.minSortPosition
        result.max_position = activity.maxPosition
        result.min_position = activity.minPosition
        result.sources = ParcelableUserUtils.fromUsers(activity.sources, accountKey)
        result.target_users = ParcelableUserUtils.fromUsers(activity.targetUsers, accountKey)
        result.target_user_lists = ParcelableUserListUtils.fromUserLists(activity.targetUserLists, accountKey)
        result.target_statuses = ParcelableStatusUtils.fromStatuses(activity.targetStatuses, accountKey)
        result.target_object_statuses = ParcelableStatusUtils.fromStatuses(activity.targetObjectStatuses, accountKey)
        result.target_object_user_lists = ParcelableUserListUtils.fromUserLists(activity.targetObjectUserLists, accountKey)
        result.target_object_users = ParcelableUserUtils.fromUsers(activity.targetObjectUsers, accountKey)
        result.has_following_source = activity.sources.fold(false) { folded, item ->
            if (item.isFollowing) {
                return@fold true
            }
            return@fold folded
        }
        if (result.sources != null) {
            result.source_ids = arrayOfNulls<UserKey>(result.sources.size)
            for (i in result.sources.indices) {
                result.source_ids[i] = result.sources[i].key
            }
        }
        result.is_gap = isGap
        return result
    }


}
