package org.mariotaku.twidere.extension.model

import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableStatusUtils

/**
 * Created by mariotaku on 2017/1/7.
 */
val ParcelableStatus.media_type: Int
    get() = media?.firstOrNull()?.type ?: 0

val ParcelableStatus.user: ParcelableUser
    get() = ParcelableUser(account_key, user_key, user_name, user_screen_name, user_profile_image_url)

val ParcelableStatus.referenced_users: Array<ParcelableUser>
    get() {
        val resultList = mutableSetOf(user)
        if (quoted_user_key != null) {
            resultList.add(ParcelableUser(account_key, quoted_user_key, quoted_user_name,
                    quoted_user_screen_name, quoted_user_profile_image))
        }
        if (retweeted_by_user_key != null) {
            resultList.add(ParcelableUser(account_key, retweeted_by_user_key, retweeted_by_user_name,
                    retweeted_by_user_screen_name, retweeted_by_user_profile_image))
        }
        mentions?.forEach { mention ->
            resultList.add(ParcelableUser(account_key, mention.key, mention.name,
                    mention.screen_name, null))
        }
        return resultList.toTypedArray()
    }


fun Array<Status>.toParcelables(accountKey: UserKey, accountType: String, profileImageSize: String) = Array(size) { i ->
    ParcelableStatusUtils.fromStatus(this[i], accountKey, accountType, false, profileImageSize)
}