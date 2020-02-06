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
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.view.createWindowInsetsCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowInsets
import android.widget.RelativeLayout
import org.mariotaku.twidere.R
import org.mariotaku.twidere.view.iface.IExtendedView

open class ExtendedRelativeLayout(context: Context, attrs: AttributeSet? = null) :
        RelativeLayout(context, attrs), IExtendedView {

    override var touchInterceptor: IExtendedView.TouchInterceptor? = null
    override var onSizeChangedListener: IExtendedView.OnSizeChangedListener? = null
    override var onApplyWindowInsetsCompatListener: IExtendedView.OnApplyWindowInsetsCompatListener? = null
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


    @SuppressLint("ClickableViewAccessibility")
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
        @Suppress("DEPRECATION")
        super.setBackgroundDrawable(background)
        restorePadding()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        onApplyWindowInsetsCompatListener?.onApplyWindowInsets(createWindowInsetsCompat(insets))
        return super.onApplyWindowInsets(insets)
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
