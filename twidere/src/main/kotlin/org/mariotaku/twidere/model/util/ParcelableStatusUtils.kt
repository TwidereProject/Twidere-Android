package org.mariotaku.twidere.model.util

import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableStatus.FilterFlags

/**
 * Created by mariotaku on 16/1/3.
 */
object ParcelableStatusUtils {

    fun makeOriginalStatus(status: ParcelableStatus) {
        if (!status.is_retweet) return
        status.id = status.retweet_id
        status.retweeted_by_user_key = null
        status.retweeted_by_user_name = null
        status.retweeted_by_user_screen_name = null
        status.retweeted_by_user_profile_image = null
        status.retweet_timestamp = -1
        status.retweet_id = null
    }

    fun ParcelableStatus.addFilterFlag(@FilterFlags flags: Long) {
        filter_flags = filter_flags or flags
    }

    fun updateExtraInformation(status: ParcelableStatus, details: AccountDetails) {
        status.account_color = details.color
    }

}

