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

package org.mariotaku.twidere.view

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import org.mariotaku.twidere.R

import org.mariotaku.twidere.util.MouseScrollDirectionDecider

/**
 * Created by mariotaku on 15/3/30.
 */
class ExtendedRecyclerView(context: Context, attrs: AttributeSet? = null) :
        RecyclerView(context, attrs, 0) {

    private val mouseScrollDirectionDecider: MouseScrollDirectionDecider
    // This value is used when handling generic motion events.
    private var scrollFactor = java.lang.Float.MIN_VALUE
    private var contextMenuInfo: ContextMenuInfo? = null
    private var usePaddingBackup: Boolean = false
    private val paddingBackup = Rect()

    init {
        mouseScrollDirectionDecider = MouseScrollDirectionDecider(scrollFactorBackport)
        val a = context.obtainStyledAttributes(attrs, R.styleable.IExtendedViewPadding)
        usePaddingBackup = (0 until a.indexCount).any { a.hasValue(it) }
        a.recycle()
        paddingBackup.set(paddingLeft, paddingTop, paddingRight, paddingBottom)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mouseScrollDirectionDecider.attach(this)
    }

    override fun onDetachedFromWindow() {
        mouseScrollDirectionDecider.detach()
        super.onDetachedFromWindow()
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val lm = layoutManager ?: return false
        if (event.source and InputDevice.SOURCE_CLASS_POINTER != 0) {
            if (event.action == MotionEvent.ACTION_SCROLL) {
                val vScroll: Float
                val hScroll: Float
                if (lm.canScrollVertically()) {
                    vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                    if (!mouseScrollDirectionDecider.isVerticalAvailable) {
                        mouseScrollDirectionDecider.guessDirection(event)
                    }
                } else {
                    vScroll = 0f
                }
                if (lm.canScrollHorizontally()) {
                    hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL)
                    if (!mouseScrollDirectionDecider.isHorizontalAvailable) {
                        mouseScrollDirectionDecider.guessDirection(event)
                    }
                } else {
                    hScroll = 0f
                }
                if (vScroll != 0f || hScroll != 0f) {
                    val scrollFactor = scrollFactorBackport
                    val horizontalDirection = mouseScrollDirectionDecider.horizontalDirection
                    val verticalDirection = mouseScrollDirectionDecider.verticalDirection
                    val hFactor = scrollFactor * if (horizontalDirection != 0f) horizontalDirection else -1f
                    val vFactor = scrollFactor * if (verticalDirection != 0f) verticalDirection else -1f
                    scrollBy((hScroll * hFactor).toInt(), (vScroll * vFactor).toInt())
                }
            }
        }
        return false
    }

    override fun computeVerticalScrollRange(): Int {
        if (layoutManager == null) return 0
        return super.computeVerticalScrollRange()
    }

    override fun computeHorizontalScrollRange(): Int {
        if (layoutManager == null) return 0
        return super.computeHorizontalScrollRange()
    }

    override fun computeHorizontalScrollOffset(): Int {
        if (layoutManager == null) return 0
        return super.computeHorizontalScrollOffset()
    }

    override fun computeHorizontalScrollExtent(): Int {
        if (layoutManager == null) return 0
        return super.computeHorizontalScrollExtent()
    }

    override fun computeVerticalScrollOffset(): Int {
        if (layoutManager == null) return 0
        return super.computeVerticalScrollOffset()
    }

    override fun computeVerticalScrollExtent(): Int {
        if (layoutManager == null) return 0
        return super.computeVerticalScrollExtent()
    }

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return contextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View): Boolean {
        if (originalView.parent !== this) {
            return false
        }
        val position = getChildLayoutPosition(originalView)
        if (position == NO_POSITION) return false
        contextMenuInfo = ContextMenuInfo(id, position)
        return super.showContextMenuForChild(originalView)
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (fitsSystemWindows && usePaddingBackup) {
            setPadding(paddingBackup.left + insets.systemWindowInsetLeft,
                    paddingBackup.top + insets.systemWindowInsetTop,
                    paddingBackup.right + insets.systemWindowInsetRight,
                    paddingBackup.bottom + insets.systemWindowInsetBottom)
            return insets.consumeSystemWindowInsets()
        }
        return super.onApplyWindowInsets(insets)
    }

    /**
     * Ported from View.getVerticalScrollFactor.
     */
    private val scrollFactorBackport: Float
            //listPreferredItemHeight is not defined, no generic scrolling
        get() {
            if (scrollFactor == java.lang.Float.MIN_VALUE) {
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(
                        android.R.attr.listPreferredItemHeight, outValue, true)) {
                    scrollFactor = outValue.getDimension(
                            context.resources.displayMetrics)
                } else {
                    return 0f
                }

            }
            return scrollFactor
        }

    class ContextMenuInfo(val recyclerViewId: Int, val position: Int) : ContextMenu.ContextMenuInfo

}
