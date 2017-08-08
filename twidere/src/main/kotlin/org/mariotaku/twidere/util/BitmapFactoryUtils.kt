package org.mariotaku.twidere.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import java.io.IOException

/**
 * Created by mariotaku on 16/7/31.
 */
object BitmapFactoryUtils {

    @Throws(IOException::class)
    fun decodeUri(contentResolver: ContentResolver, uri: Uri, outPadding: Rect? = null,
                  opts: BitmapFactory.Options? = null): Bitmap? {
        return contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, outPadding, opts)
        }
    }

}
