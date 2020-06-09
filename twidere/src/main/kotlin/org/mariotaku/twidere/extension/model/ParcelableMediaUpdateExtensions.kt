package org.mariotaku.twidere.extension.model

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableMediaUpdate
import org.mariotaku.twidere.util.BitmapFactoryUtils

/**
 * Created by mariotaku on 2016/12/7.
 */
fun ParcelableMediaUpdate.getMimeType(resolver: ContentResolver): String? {
    val uri = Uri.parse(this.uri)
    return resolver.getType(uri) ?: return when (type) {
        ParcelableMedia.Type.ANIMATED_GIF -> {
            "image/gif"
        }
        ParcelableMedia.Type.IMAGE -> {
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactoryUtils.decodeUri(resolver, uri, opts = o)
            o.outMimeType
        }
        else -> null
    }
}