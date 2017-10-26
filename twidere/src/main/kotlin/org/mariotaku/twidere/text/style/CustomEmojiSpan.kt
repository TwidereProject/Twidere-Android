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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v4.widget.CircularProgressDrawable
import android.text.style.DynamicDrawableSpan
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import org.mariotaku.twidere.R
import org.mariotaku.twidere.graphic.DrawableWrapper

class CustomEmojiSpan(
        val uri: String,
        requestManager: RequestManager,
        callback: Drawable.Callback
) : DynamicDrawableSpan(DynamicDrawableSpan.ALIGN_BASELINE) {

    private val textSize = (callback as TextView).textSize.toInt()

    private val emojiDrawable = EmojiDrawable(textSize)
    private val emojiDirtyBounds = Rect()

    private val target = GlideTarget()

    init {
        emojiDrawable.callback = callback
        requestManager.load(uri)
                .placeholder(CircularProgressDrawable((callback as TextView).context))
                .error(R.mipmap.ic_emoji_error)
                .fitCenter()
                .into(target)
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int,
            y: Int, bottom: Int, paint: Paint) {

        var transY = bottom - emojiDrawable.bounds.bottom
        if (verticalAlignment == ALIGN_BASELINE) {
            transY -= paint.fontMetricsInt.descent
        }
        emojiDrawable.copyBounds(emojiDirtyBounds)
        emojiDirtyBounds.offsetTo(x.toInt(), transY)
        super.draw(canvas, text, start, end, x, top, y, bottom, paint)
    }

    override fun getDrawable(): Drawable = emojiDrawable

    fun verify(who: Drawable): Boolean = who === emojiDrawable

    private inner class GlideTarget : BaseTarget<GlideDrawable>() {

        override fun onResourceReady(resource: GlideDrawable, glideAnimation: GlideAnimation<in GlideDrawable>) {
            resource.setLoopCount(GlideDrawable.LOOP_FOREVER)
            emojiDrawable.setDrawable(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            emojiDrawable.setDrawable(placeholder)
        }

        override fun onLoadStarted(placeholder: Drawable?) {
            emojiDrawable.setDrawable(placeholder)
        }

        override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
            emojiDrawable.setDrawable(errorDrawable)
        }

        override fun getSize(cb: SizeReadyCallback) {
            cb.onSizeReady(textSize, textSize)
        }

    }

    private inner class EmojiDrawable(val textSize: Int) : DrawableWrapper() {

        override fun getDirtyBounds(): Rect {
            return emojiDirtyBounds
        }

        fun setDrawable(drawable: Drawable?) {
            wrapped = drawable
            drawable?.setBounds(0, 0, textSize, textSize)
            setBounds(0, 0, textSize, textSize)
            if (drawable is Animatable) {
                drawable.start()
            }
        }
    }

}
