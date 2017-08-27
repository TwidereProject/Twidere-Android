package org.mariotaku.twidere.model.util

import org.mariotaku.twidere.model.ParcelableLiteUser
import org.mariotaku.twidere.model.UserKey

/**
 * Processing ParcelableActivity
 *
 *
 * Created by mariotaku on 16/1/2.
 */
object ParcelableActivityUtils {

    /**
     * @param sources Source users
     * *
     * @param filtered Those ids will be removed from source_ids.
     * *
     * @param followingOnly Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun filterSources(sources: Array<ParcelableLiteUser>?, filtered: Array<UserKey>?,
            followingOnly: Boolean): Array<ParcelableLiteUser>? {
        return sources?.filterNot { user ->
            if (filtered != null && user.key in filtered) {
                return@filterNot true
            }

            if (followingOnly && !user.is_following) {
                return@filterNot true
            }

            return@filterNot false
        }?.toTypedArray()
    }


}
