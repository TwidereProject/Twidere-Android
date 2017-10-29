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

package org.mariotaku.twidere.text.style

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.setBoundsFitCenter

class CustomEmojiSpan(
        uri: String,
        requestManager: RequestManager,
        textView: TextView
) : ReplacementSpan() {

    private val emojiSize = textView.textSize.toInt()
    private val target = GlideTarget(textView, emojiSize, emojiSize)

    init {
        requestManager.load(uri)
                .asBitmap()
                .placeholder(R.mipmap.ic_emoji_loading)
                .error(R.mipmap.ic_emoji_error)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .fitCenter()
                .into(target)
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int,
            fm: Paint.FontMetricsInt?): Int {
        if (fm != null) {
            fm.ascent = -target.height
            fm.descent = 0

            fm.top = fm.ascent
            fm.bottom = 0
        }

        return target.width
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int,
            y: Int, bottom: Int, paint: Paint) {
        val b = target.drawable ?: return

        canvas.save()

        val transY = bottom - b.bounds.bottom

        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

    private class GlideTarget(
            val textView: TextView,
            val width: Int,
            val height: Int
    ) : SimpleTarget<Bitmap>(width, height) {

        var drawable: Drawable? = null
            set(value) {
                field = value
                value?.setBoundsFitCenter(0, 0, width, height)
                textView.postInvalidate()
            }

        override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
            drawable = BitmapDrawable(textView.resources, resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            drawable = placeholder
        }

        override fun onLoadStarted(placeholder: Drawable?) {
            drawable = placeholder
        }

        override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
            drawable = errorDrawable
        }

    }


}
