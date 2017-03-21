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
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import org.mariotaku.twidere.R
import org.mariotaku.twidere.view.iface.IExtendedView

open class ExtendedRelativeLayout(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs), IExtendedView {

    private var touchInterceptor: IExtendedView.TouchInterceptor? = null
    private var onSizeChangedListener: IExtendedView.OnSizeChangedListener? = null
    private var onFitSystemWindowsListener: IExtendedView.OnFitSystemWindowsListener? = null
    private var usePaddingBackup: Boolean = false

    private val paddingBackup = Rect()

    init {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            usePaddingBackup = false
        } else {
            val a = context.obtainStyledAttributes(attrs, R.styleable.IExtendedViewPadding)
            usePaddingBackup = (0 until a.indexCount).any { a.hasValue(it) }
            a.recycle()
        }
    }

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

    override fun setOnFitSystemWindowsListener(listener: IExtendedView.OnFitSystemWindowsListener) {
        onFitSystemWindowsListener = listener
    }

    override fun setOnSizeChangedListener(listener: IExtendedView.OnSizeChangedListener) {
        onSizeChangedListener = listener
    }

    override fun setTouchInterceptor(listener: IExtendedView.TouchInterceptor) {
        touchInterceptor = listener
    }

    @Deprecated("Deprecated in Android")
    override fun fitSystemWindows(insets: Rect): Boolean {
        onFitSystemWindowsListener?.onFitSystemWindows(insets)
        return super.fitSystemWindows(insets)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (touchInterceptor != null) {
            val ret = touchInterceptor!!.onTouchEvent(this, event)
            if (ret) return true
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (onSizeChangedListener != null) {
            onSizeChangedListener!!.onSizeChanged(this, w, h, oldw, oldh)
        }
    }

    override fun setBackgroundResource(@DrawableRes resid: Int) {
        backupPadding()
        super.setBackgroundResource(resid)
        restorePadding()
    }

    override fun setBackground(background: Drawable) {
        backupPadding()
        super.setBackground(background)
        restorePadding()
    }

    @Deprecated("Deprecated in Android")
    override fun setBackgroundDrawable(background: Drawable) {
        backupPadding()
        super.setBackgroundDrawable(background)
        restorePadding()
    }

    private fun backupPadding() {
        if (!usePaddingBackup) return
        paddingBackup.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    private fun restorePadding() {
        if (!usePaddingBackup) return
        setPadding(paddingBackup.left, paddingBackup.top, paddingBackup.right, paddingBackup.bottom)
    }

}
