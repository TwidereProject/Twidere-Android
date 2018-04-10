package org.mariotaku.twidere.extension.model

import android.support.v13.view.inputmethod.InputContentInfoCompat
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.promotion.PromotionService


val ParcelableMedia.aspectRatio: Double
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