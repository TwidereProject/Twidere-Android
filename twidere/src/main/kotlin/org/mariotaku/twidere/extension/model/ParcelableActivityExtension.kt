package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableActivity

/**
 * Created by mariotaku on 2016/12/6.
 */
val ParcelableActivity.id: String
    get() = "$min_position-$max_position"