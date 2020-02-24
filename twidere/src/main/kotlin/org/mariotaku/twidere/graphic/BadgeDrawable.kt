package org.mariotaku.twidere.graphic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.appcompat.graphics.drawable.DrawableWrapper

/**
 * Created by mariotaku on 2017/2/1.
 */

class BadgeDrawable(drawable: Drawable, color: Int, val badgeSize: Int) : DrawableWrapper(drawable) {

    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    }
    private val badgeBounds = RectF()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawOval(badgeBounds, badgePaint)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        badgeBounds.set((bounds.right - badgeSize).toFloat(), 0f, bounds.right.toFloat(), badgeSize.toFloat())
    }
}
