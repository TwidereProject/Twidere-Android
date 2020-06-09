/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Static utility methods for Transitions.
 */
object TransitionUtils {
    private const val MAX_IMAGE_SIZE = 1024 * 1024

    /**
     * Creates a Bitmap of the given view, using the Matrix matrix to transform to the local
     * coordinates. `matrix` will be modified during the bitmap creation.
     *
     * If the bitmap is large, it will be scaled uniformly down to at most 1MB size.
     *
     * @param view   The view to create a bitmap for.
     *
     * @param matrix The matrix converting the view local coordinates to the coordinates that
     *               the bitmap will be displayed in. `matrix` will be modified before
     *               returning.
     *
     * @param bounds The bounds of the bitmap in the destination coordinate system (where the
     *               view should be presented. Typically, this is matrix.mapRect(viewBounds);
     *
     * @return A bitmap of the given view or null if bounds has no width or height.
     */
    fun createViewBitmap(view: View, matrix: Matrix, bounds: RectF): Bitmap? {
        if (bounds.isEmpty) return null
        var bitmapWidth = bounds.width().roundToInt()
        var bitmapHeight = bounds.height().roundToInt()
        val scale = min(1f, MAX_IMAGE_SIZE.toFloat() / (bitmapWidth * bitmapHeight))
        bitmapWidth *= scale.toInt()
        bitmapHeight *= scale.toInt()
        matrix.postTranslate(-bounds.left, -bounds.top)
        matrix.postScale(scale, scale)
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)
        canvas.concat(matrix)
        view.draw(canvas)
        return bitmap
    }

}
