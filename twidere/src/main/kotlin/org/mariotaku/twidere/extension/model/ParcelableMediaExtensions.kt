package org.mariotaku.twidere.extension.model

import android.annotation.SuppressLint
import android.support.v13.view.inputmethod.InputContentInfoCompat
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.promotion.PromotionService


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

@SuppressLint("SwitchIntDef")
fun ParcelableMedia.getBestVideoUrlAndType(supportedTypes: Array<String>): Pair<String, String?>? {
    val mediaUrl = media_url ?: return null
    when (type) {
        ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF -> {
            val videoInfo = video_info ?: return Pair(mediaUrl, null)
            val firstMatch = videoInfo.variants.filter { variant ->
                supportedTypes.any { it.equals(variant.content_type, ignoreCase = true) }
            }.sortedByDescending(ParcelableMedia.VideoInfo.Variant::bitrate).firstOrNull()
                    ?: return null
            return Pair(firstMatch.url, firstMatch.content_type)
        }
        ParcelableMedia.Type.CARD_ANIMATED_GIF -> {
            return Pair(mediaUrl, "video/mp4")
        }
        else -> {
            return null
        }
    }
}

val ParcelableMedia.aspect_ratio: Double
    get() {
        if (this.height <= 0 || this.width <= 0) return Double.NaN
        return this.width / this.height.toDouble()
    }


val ParcelableMedia.bannerExtras: PromotionService.BannerExtras?
    get() {
        val contentUrl = this.page_url ?: this.url ?: return null
        return PromotionService.BannerExtras(contentUrl)
    }

val Array<ParcelableMedia?>.type: Int
    get() {
        forEach { if (it != null) return it.type }
        return 0
    }

@ParcelableMedia.Type
val InputContentInfoCompat.inferredMediaType: Int
    get() = if (description.mimeTypeCount > 0) {
        ParcelableMediaUtils.inferMediaType(description.getMimeType(0))
    } else {
        ParcelableMedia.Type.IMAGE
    }