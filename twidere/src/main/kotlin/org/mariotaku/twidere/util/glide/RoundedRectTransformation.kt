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

import android.content.Context
import android.graphics.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import java.security.MessageDigest


class RoundedRectTransformation(
        private val bitmapPool: BitmapPool,
        private val radius: Float,
        private val radiusPercent: Float
) : Transformation<Bitmap> {
    val rectF = RectF()

    constructor(context: Context, radius: Float, radiusPercent: Float) :
            this(Glide.get(context).bitmapPool, radius, radiusPercent)

    override fun transform(context: Context, resource: Resource<Bitmap>, outWidth: Int, outHeight: Int): Resource<Bitmap> {
        val source = resource.get()

        val width = source.width
        val height = source.height

        val bitmap = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        rectF.right = width.toFloat()
        rectF.bottom = height.toFloat()

        val calculatedRadius = if (radiusPercent != 0f) {
            width * radiusPercent
        } else {
            radius
        }
        drawRoundRect(canvas, calculatedRadius, paint)
        return BitmapResource.obtain(bitmap, bitmapPool)!!
    }

    private fun drawRoundRect(canvas: Canvas, radius: Float, paint: Paint) {
        canvas.drawRoundRect(rectF, radius, radius, paint)
    }

    fun getId(): String {
        return "RoundedRectTransformation(radius=$radius, radiusPercent=$radius)"
    }

    override fun equals(other: Any?): Boolean {
        return other is RoundedRectTransformation
    }

    override fun hashCode(): Int {
        return getId().hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(getId().toByteArray())
    }
}
