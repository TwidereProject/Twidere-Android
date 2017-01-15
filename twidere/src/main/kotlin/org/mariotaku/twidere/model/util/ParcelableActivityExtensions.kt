package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableStatus

/**
 * Created by mariotaku on 16/6/29.
 */
fun ParcelableActivity.getActivityStatus(): ParcelableStatus? {
    val status: ParcelableStatus
    when (action) {
        Activity.Action.MENTION -> {
            if (target_object_statuses?.isEmpty() ?: true) return null
            status = target_object_statuses[0]
        }
        Activity.Action.REPLY -> {
            if (target_statuses?.isEmpty() ?: true) return null
            status = target_statuses[0]
        }
        Activity.Action.QUOTE -> {
            if (target_statuses?.isEmpty() ?: true) return null
            status = target_statuses[0]
        }
        else -> return null
    }
    status.account_color = account_color
    return status
}
