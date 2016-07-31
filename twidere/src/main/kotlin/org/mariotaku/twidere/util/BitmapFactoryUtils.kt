package org.mariotaku.twidere.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import java.io.IOException
import java.io.InputStream

/**
 * Created by mariotaku on 16/7/31.
 */
object BitmapFactoryUtils {

    @Throws(IOException::class)
    fun decodeUri(contentResolver: ContentResolver, uri: Uri, outPadding: Rect?,
                  opts: BitmapFactory.Options?, close: Boolean = true): Bitmap? {
        var st: InputStream? = null
        try {
            st = contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(st, outPadding, opts)
        } finally {
            if (close) {
                Utils.closeSilently(st)
            }
        }
    }

}
