package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableMessage

/**
 * Created by mariotaku on 2017/2/9.
 */

val ParcelableMessage.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp
