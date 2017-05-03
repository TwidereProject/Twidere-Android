package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.twitter.model.UserMentionEntity
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.model.UserKey

fun UserMentionEntity.toParcelable(host: String?): ParcelableUserMention {
    val obj = ParcelableUserMention()
    obj.key = UserKey(id, host)
    obj.name = name
    obj.screen_name = screenName
    return obj
}
