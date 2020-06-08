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

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.view.MotionEvent
import android.view.View

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import kotlin.math.abs

/**
 * Created by mariotaku on 15/3/15.
 */
open class ContentScrollHandler<A>(
        private val contentListSupport: ContentListSupport<A>,
        private val viewCallback: ViewCallback?
) {
    val touchListener: View.OnTouchListener
    var touchSlop: Int = 0
    var reversed: Boolean = false

    protected var scrollState: Int = 0
        private set
    private var scrollSum: Int = 0

    private var scrollDirection: Int = 0

    init {
        touchListener = TouchListener(this)
    }

    private fun postNotifyScrollStateChanged() {
        if (contentListSupport is Fragment) {
            if (contentListSupport.context == null) return
        }
        if (viewCallback != null) {
            viewCallback.post(object : Runnable {
                override fun run() {
                    if (viewCallback.computingLayout) {
                        viewCallback.post(this)
                    } else {
                        notifyScrollStateChanged()
                    }
                }
            })
        } else {
            notifyScrollStateChanged()
        }
    }

    private fun notifyScrollStateChanged() {
        if (contentListSupport is Fragment) {
            if (contentListSupport.context == null) return
        }
        val adapter = contentListSupport.adapter as? ILoadMoreSupportAdapter ?: return
        if (!contentListSupport.refreshing && adapter.loadMoreSupportedPosition != ILoadMoreSupportAdapter.NONE
                && adapter.loadMoreIndicatorPosition == ILoadMoreSupportAdapter.NONE) {
            var position: Long = 0
            if (contentListSupport.reachingEnd && scrollDirection < 0) {
                position = position or ILoadMoreSupportAdapter.END
            }
            if (contentListSupport.reachingStart && scrollDirection > 0) {
                position = position or ILoadMoreSupportAdapter.START
            }
            resetScrollDirection()
            if (position != 0L) {
                contentListSupport.onLoadMoreContents(position)
            }
        }
    }

    fun handleScrollStateChanged(scrollState: Int, idleState: Int) {
        if (contentListSupport is Fragment) {
            if (contentListSupport.context == null) return
        }
        if (this.scrollState != idleState) {
            postNotifyScrollStateChanged()
        }
        this.scrollState = scrollState
    }

    fun handleScroll(dy: Int, scrollState: Int, oldState: Int, idleState: Int) {
        if (contentListSupport is Fragment) {
            if (contentListSupport.context == null) return
        }
        //Reset mScrollSum when scrolling in reverse direction
        if (dy * scrollSum < 0) {
            scrollSum = 0
        }
        scrollSum += dy
        if (abs(scrollSum) > touchSlop) {
            contentListSupport.setControlVisible(reversed xor (dy < 0))
            scrollSum = 0
        }
        if (scrollState == idleState && oldState != scrollState) {
            postNotifyScrollStateChanged()
        }
    }

    private fun setScrollDirection(direction: Int) {
        scrollDirection = direction
    }

    private fun resetScrollDirection() {
        scrollDirection = 0
    }

    internal class TouchListener<A>(private val listener: ContentScrollHandler<A>) : View.OnTouchListener {
        private var lastY: Float = 0f

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    listener.resetScrollDirection()
                    lastY = Float.NaN
                }
                MotionEvent.ACTION_MOVE -> {
                    if (lastY.isNaN()) {
                        lastY = event.y
                    } else {
                        val delta = event.y - lastY
                        listener.setScrollDirection(if (delta < 0) -1 else 1)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    lastY = Float.NaN
                }
            }
            return false
        }
    }

    interface ViewCallback {
        val computingLayout: Boolean

        fun post(runnable: Runnable)
    }

    interface ContentListSupport<out A> {

        val adapter: A?

        val refreshing: Boolean

        val reachingStart: Boolean

        val reachingEnd: Boolean

        fun onLoadMoreContents(@IndicatorPosition position: Long)

        fun setControlVisible(visible: Boolean)

    }

}
