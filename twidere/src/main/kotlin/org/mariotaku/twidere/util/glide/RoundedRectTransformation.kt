/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.roundToInt

class RoundedRectTransformation(
        private val radius: Float,
        private val radiusPercent: Float
) : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val calculatedRadius = if (radiusPercent != 0f) {
            toTransform.width * radiusPercent
        } else {
            radius
        }.roundToInt()
        return TransformationUtils.roundedCorners(pool, toTransform, calculatedRadius)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)

        val radiusData = ByteBuffer.allocate(4).putFloat(radius).putFloat(radiusPercent).array()
        messageDigest.update(radiusData)
    }

    override fun equals(other: Any?): Boolean {
        if (other is RoundedRectTransformation) {
            return radius == other.radius
        }
        return false
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(),
                Util.hashCode(radius, Util.hashCode(radiusPercent)))
    }

    companion object {
        private val ID = "org.mariotaku.twidere.util.glide.RoundedRectTransformation"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }
}
