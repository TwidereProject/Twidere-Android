package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableMedia

/**
 * Created by mariotaku on 2017/1/7.
 */

fun parcelableMediaTypeString(@ParcelableMedia.Type type: Int): String? = when (type) {
    ParcelableMedia.Type.IMAGE -> "image"
    ParcelableMedia.Type.VIDEO -> "video"
    ParcelableMedia.Type.ANIMATED_GIF -> "gif"
    ParcelableMedia.Type.CARD_ANIMATED_GIF -> "gif"
    ParcelableMedia.Type.EXTERNAL_PLAYER -> "external"
    ParcelableMedia.Type.VARIABLE_TYPE -> "variable"
    ParcelableMedia.Type.UNKNOWN -> null
    else -> null
}

val ParcelableMedia.aspect_ratio: Double
    get() {
        if (this.height <= 0 || this.width <= 0) return Double.NaN
        return this.width / this.height.toDouble()
    }