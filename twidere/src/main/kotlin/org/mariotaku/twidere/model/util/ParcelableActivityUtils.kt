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
     * @param filteredKeys Those ids will be removed from source_ids.
     * *
     * @param followingOnly Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun filterSources(sources: Array<ParcelableLiteUser>?, filteredKeys: Array<UserKey>?,
            filteredNames: Array<String>?, filteredDescription: Array<String>?,
            followingOnly: Boolean): Array<ParcelableLiteUser>? {
        return sources?.filterNot { user ->
            if (followingOnly && !user.is_following) {
                return@filterNot true
            }

            if (filteredKeys != null && user.key in filteredKeys) {
                return@filterNot true
            }

            if (filteredNames != null && filteredNames.matchedName(user)) {
                return@filterNot true
            }

            if (filteredDescription != null && filteredDescription.matchedDescription(user)) {
                return@filterNot true
            }

            return@filterNot false
        }?.toTypedArray()
    }

    private fun Array<String>.matchedName(user: ParcelableLiteUser): Boolean {
        return any { rule -> user.name?.contains(rule, true) == true }
    }

    private fun Array<String>.matchedDescription(user: ParcelableLiteUser): Boolean {
        return any { rule ->
            user.description_unescaped?.contains(rule, true) == true ||
                    user.url_expanded?.contains(rule, true) == true ||
                    user.location?.contains(rule, true) == true
        }
    }
}
