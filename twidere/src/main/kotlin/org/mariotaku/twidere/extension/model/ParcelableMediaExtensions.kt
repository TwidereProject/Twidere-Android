package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableMedia

/**
 * Created by mariotaku on 2017/1/7.
 */

fun parcelableMediaTypeString(@ParcelableMedia.Type type: Int): String? {
    return when (type) {
        ParcelableMedia.Type.IMAGE -> "image"
        ParcelableMedia.Type.VIDEO -> "video"
        ParcelableMedia.Type.ANIMATED_GIF -> "gif"
        ParcelableMedia.Type.CARD_ANIMATED_GIF -> "gif"
        ParcelableMedia.Type.EXTERNAL_PLAYER -> "external"
        ParcelableMedia.Type.VARIABLE_TYPE -> "variable"
        else -> null
    }
}