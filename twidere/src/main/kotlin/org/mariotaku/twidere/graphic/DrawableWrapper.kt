/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.graphic

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.DrawableCompat

open class DrawableWrapper(wrapped: Drawable? = null) : Drawable(), Drawable.Callback {

    var wrapped: Drawable? = null
        set(drawable) {
            if (wrapped != null) {
                field?.callback = null
            }

            field = drawable

            if (drawable != null) {
                drawable.callback = this
            }
        }

    init {
        this.wrapped = wrapped
    }

    override fun draw(canvas: Canvas) {
        wrapped?.draw(canvas)
    }

    override fun onBoundsChange(bounds: Rect) {
        wrapped?.bounds = bounds
    }

    override fun setChangingConfigurations(configs: Int) {
        wrapped?.changingConfigurations = configs
    }

    override fun getChangingConfigurations(): Int {
        return wrapped?.changingConfigurations ?: super.getChangingConfigurations()
    }

    override fun setDither(dither: Boolean) {
        wrapped?.setDither(dither)
    }

    override fun setFilterBitmap(filter: Boolean) {
        wrapped?.isFilterBitmap = filter
    }

    override fun setAlpha(alpha: Int) {
        wrapped?.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        wrapped?.colorFilter = cf
    }

    override fun isStateful(): Boolean {
        return wrapped?.isStateful == true
    }

    override fun setState(stateSet: IntArray): Boolean {
        return wrapped?.setState(stateSet) == true
    }

    override fun getState(): IntArray {
        return wrapped?.state ?: intArrayOf()
    }

    override fun jumpToCurrentState() {
        wrapped?.let { DrawableCompat.jumpToCurrentState(it) }
    }

    override fun getCurrent(): Drawable {
        return this
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        return if (wrapped != null) {
            super.setVisible(visible, restart) || wrapped!!.setVisible(visible, restart)
        } else super.setVisible(visible, restart)
    }

    override fun getOpacity(): Int {
        return if (wrapped != null) {
            wrapped!!.opacity
        } else PixelFormat.TRANSPARENT
    }

    override fun getTransparentRegion(): Region? {
        return if (wrapped != null) {
            wrapped!!.transparentRegion
        } else super.getTransparentRegion()
    }

    override fun getIntrinsicWidth(): Int {
        return if (wrapped != null) {
            wrapped!!.intrinsicWidth
        } else super.getIntrinsicWidth()
    }

    override fun getIntrinsicHeight(): Int {
        return if (wrapped != null) {
            wrapped!!.intrinsicHeight
        } else super.getIntrinsicHeight()
    }

    override fun getMinimumWidth(): Int {
        return if (wrapped != null) {
            wrapped!!.minimumWidth
        } else super.getMinimumWidth()
    }

    override fun getMinimumHeight(): Int {
        return if (wrapped != null) {
            wrapped!!.minimumHeight
        } else super.getMinimumHeight()
    }

    override fun getPadding(padding: Rect): Boolean {
        return if (wrapped != null) {
            wrapped!!.getPadding(padding)
        } else false
    }

    /**
     * {@inheritDoc}
     */
    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        scheduleSelf(what, `when`)
    }

    /**
     * {@inheritDoc}
     */
    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }

    override fun onLevelChange(level: Int): Boolean {
        return wrapped?.setLevel(level) == true
    }

    override fun setAutoMirrored(mirrored: Boolean) {
        wrapped?.let { wrapped ->
            DrawableCompat.setAutoMirrored(wrapped, mirrored)
        }
    }

    override fun isAutoMirrored(): Boolean {
        return wrapped?.let { wrapped ->
            DrawableCompat.isAutoMirrored(wrapped)
        } == true
    }

    override fun setTint(tint: Int) {
        wrapped?.let { wrapped ->
            DrawableCompat.setTint(wrapped, tint)
        }
    }

    override fun setTintList(tint: ColorStateList?) {
        wrapped?.let { wrapped ->
            DrawableCompat.setTintList(wrapped, tint)
        }
    }

    override fun setTintMode(tintMode: PorterDuff.Mode) {
        wrapped?.let { wrapped ->
            DrawableCompat.setTintMode(wrapped, tintMode)
        }
    }

    override fun setHotspot(x: Float, y: Float) {
        wrapped?.let { wrapped ->
            DrawableCompat.setHotspot(wrapped, x, y)
        }
    }

    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) {
        wrapped?.let { wrapped ->
            DrawableCompat.setHotspotBounds(wrapped, left, top, right, bottom)
        }
    }
}
