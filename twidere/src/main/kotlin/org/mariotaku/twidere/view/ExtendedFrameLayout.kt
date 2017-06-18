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

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import org.mariotaku.twidere.view.iface.IExtendedView

open class ExtendedFrameLayout(context: Context, attrs: AttributeSet? = null) :
        FrameLayout(context, attrs), IExtendedView {

    override var touchInterceptor: IExtendedView.TouchInterceptor? = null
    override var onSizeChangedListener: IExtendedView.OnSizeChangedListener? = null
    override var onFitSystemWindowsListener: IExtendedView.OnFitSystemWindowsListener? = null

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (touchInterceptor != null) {
            val ret = touchInterceptor!!.dispatchTouchEvent(this, event)
            if (ret) return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (touchInterceptor != null) {
            val ret = touchInterceptor!!.onInterceptTouchEvent(this, event)
            if (ret) return true
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun fitSystemWindows(insets: Rect): Boolean {
        if (onFitSystemWindowsListener != null) {
            onFitSystemWindowsListener!!.onFitSystemWindows(insets)
        }
        return super.fitSystemWindows(insets)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (touchInterceptor != null) {
            val ret = touchInterceptor!!.onTouchEvent(this, event)
            if (ret) return true
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (onSizeChangedListener != null) {
            onSizeChangedListener!!.onSizeChanged(this, w, h, oldw, oldh)
        }
    }

}
