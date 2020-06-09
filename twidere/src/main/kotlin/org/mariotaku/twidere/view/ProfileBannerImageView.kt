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

package org.mariotaku.twidere.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.core.view.createWindowInsetsCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.view.iface.IExtendedView
import kotlin.math.roundToInt

class ProfileBannerImageView(context: Context, attrs: AttributeSet) :
        ForegroundImageView(context, attrs), IExtendedView {

    override var onSizeChangedListener: IExtendedView.OnSizeChangedListener? = null
    override var touchInterceptor: IExtendedView.TouchInterceptor? = null
    override var onApplyWindowInsetsCompatListener: IExtendedView.OnApplyWindowInsetsCompatListener? = null

    var bannerAspectRatio: Float = 0.toFloat()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProfileBannerImageView)
        bannerAspectRatio = a.getFraction(R.styleable.ProfileBannerImageView_bannerAspectRatio, 1, 1, 2f)
        a.recycle()
        scaleType = ScaleType.CENTER_CROP
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (touchInterceptor != null) {
            val ret = touchInterceptor!!.dispatchTouchEvent(this, event)
            if (ret) return true
        }
        return super.dispatchTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (touchInterceptor != null) {
            val ret = touchInterceptor!!.onTouchEvent(this, event)
            if (ret) return true
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / bannerAspectRatio).roundToInt()
        setMeasuredDimension(width, height)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (onSizeChangedListener != null) {
            onSizeChangedListener!!.onSizeChanged(this, w, h, oldw, oldh)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        onApplyWindowInsetsCompatListener?.onApplyWindowInsets(createWindowInsetsCompat(insets))
        return super.onApplyWindowInsets(insets)
    }
}
