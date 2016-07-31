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

import android.support.v4.app.Fragment
import android.view.MotionEvent
import android.view.View

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition

/**
 * Created by mariotaku on 15/3/15.
 */
open class ContentScrollHandler(
        private val mContentListSupport: ContentScrollHandler.ContentListSupport,
        private val mViewCallback: ContentScrollHandler.ViewCallback?
) {
    private val mTouchListener: TouchListener

    protected var scrollState: Int = 0
        private set
    private var mScrollSum: Int = 0
    private var mTouchSlop: Int = 0
    private var mReversed: Boolean = false

    private var mScrollDirection: Int = 0

    init {
        mTouchListener = TouchListener(this)
    }

    fun setTouchSlop(touchSlop: Int) {
        mTouchSlop = touchSlop
    }

    fun setReversed(inversed: Boolean) {
        mReversed = inversed
    }

    val onTouchListener: View.OnTouchListener
        get() = mTouchListener

    private fun postNotifyScrollStateChanged() {
        if (mContentListSupport is Fragment) {
            if (mContentListSupport.context == null) return
        }
        if (mViewCallback != null) {
            mViewCallback.post(object : Runnable {
                override fun run() {
                    if (mViewCallback.isComputingLayout) {
                        mViewCallback.post(this)
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
        if (mContentListSupport is Fragment) {
            if (mContentListSupport.context == null) return
        }
        val adapter = mContentListSupport.adapter
        if (adapter !is ILoadMoreSupportAdapter) return
        if (!mContentListSupport.refreshing && adapter.loadMoreSupportedPosition != ILoadMoreSupportAdapter.NONE
                && adapter.loadMoreIndicatorPosition == ILoadMoreSupportAdapter.NONE) {
            var position: Long = 0
            if (mContentListSupport.reachingEnd && mScrollDirection >= 0) {
                position = position or ILoadMoreSupportAdapter.END
            }
            if (mContentListSupport.reachingStart && mScrollDirection <= 0) {
                position = position or ILoadMoreSupportAdapter.START
            }
            resetScrollDirection()
            mContentListSupport.onLoadMoreContents(position.toLong())
        }
    }

    fun handleScrollStateChanged(scrollState: Int, idleState: Int) {
        if (mContentListSupport is Fragment) {
            if (mContentListSupport.context == null) return
        }
        if (this.scrollState != idleState) {
            postNotifyScrollStateChanged()
        }
        this.scrollState = scrollState
    }

    fun handleScroll(dy: Int, scrollState: Int, oldState: Int, idleState: Int) {
        if (mContentListSupport is Fragment) {
            if (mContentListSupport.context == null) return
        }
        //Reset mScrollSum when scrolling in reverse direction
        if (dy * mScrollSum < 0) {
            mScrollSum = 0
        }
        mScrollSum += dy
        if (Math.abs(mScrollSum) > mTouchSlop) {
            mContentListSupport.setControlVisible(mReversed xor (dy < 0))
            mScrollSum = 0
        }
        if (scrollState == idleState && oldState != scrollState) {
            postNotifyScrollStateChanged()
        }
    }

    private fun setScrollDirection(direction: Int) {
        mScrollDirection = direction
    }

    private fun resetScrollDirection() {
        mScrollDirection = 0
    }

    internal class TouchListener(private val listener: ContentScrollHandler) : View.OnTouchListener {
        private var mLastY: Float = 0.toFloat()

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    listener.resetScrollDirection()
                    mLastY = java.lang.Float.NaN
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!java.lang.Float.isNaN(mLastY)) {
                        val delta = mLastY - event.rawY
                        listener.setScrollDirection(if (delta < 0) -1 else 1)
                    } else {
                        mLastY = event.rawY
                    }
                }
            }
            return false
        }
    }

    interface ViewCallback {
        val isComputingLayout: Boolean

        fun post(runnable: Runnable)
    }

    interface ContentListSupport {

        val adapter: Any?

        val refreshing: Boolean

        val reachingStart: Boolean

        val reachingEnd: Boolean

        fun onLoadMoreContents(@IndicatorPosition position: Long)

        fun setControlVisible(visible: Boolean)

    }

}
