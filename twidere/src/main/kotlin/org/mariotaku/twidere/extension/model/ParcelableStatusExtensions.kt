package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableStatus

/**
 * Created by mariotaku on 2017/1/7.
 */
val ParcelableStatus.media_type: Int
    get() = media?.firstOrNull()?.type ?: 0

