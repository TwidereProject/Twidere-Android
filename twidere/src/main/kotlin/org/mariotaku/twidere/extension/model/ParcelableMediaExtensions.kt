package org.mariotaku.twidere.extension.model

import android.annotation.SuppressLint
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.util.promotion.PromotionService

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

@SuppressLint("SwitchIntDef")
fun ParcelableMedia.getBestVideoUrlAndType(supportedTypes: Array<String>): Pair<String, String?>? {
    val mediaUrl = media_url ?: return null
    when (type) {
        ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF -> {
            val videoInfo = video_info ?: return Pair(mediaUrl, null)
            val firstMatch = videoInfo.variants.filter { variant ->
                supportedTypes.any { it.equals(variant.content_type, ignoreCase = true) }
            }.maxBy(ParcelableMedia.VideoInfo.Variant::bitrate) ?: return null
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
        val contentUrl = this.page_url ?: this.url
        return PromotionService.BannerExtras(contentUrl)
    }