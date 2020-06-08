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

package org.mariotaku.twidere.util.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import androidx.annotation.Keep
import androidx.annotation.StyleableRes
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.*

/**
 * Created by mariotaku on 2017/2/20.
 */
class AppBarChildBehavior(
        context: Context,
        attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<View>(context, attrs) {
    private val appBarId: Int
    private val toolbarId: Int
    private val dependencyViewId: Int
    private val targetViewId: Int
    private val alignmentRule: Int

    private val marginTop: Int
    private val marginBottom: Int
    private val marginLeft: Int
    private val marginRight: Int
    private val marginStart: Int
    private val marginEnd: Int

    private val transformation: ChildTransformation

    private val dependencyRect = Rect()

    private val layoutRect = Rect()

    private val thisRect = Rect()
    private val targetRect = Rect()

    private val tempLocation = IntArray(2)


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AppBarChildBehavior)
        appBarId = a.getResourceIdOrThrow(R.styleable.AppBarChildBehavior_behavior_appBarId, "appBarId")
        toolbarId = a.getResourceIdOrThrow(R.styleable.AppBarChildBehavior_behavior_toolbarId, "toolbarId")
        dependencyViewId = a.getResourceIdOrThrow(R.styleable.AppBarChildBehavior_behavior_dependencyViewId, "dependencyViewId")
        targetViewId = a.getResourceIdOrThrow(R.styleable.AppBarChildBehavior_behavior_targetViewId, "targetViewId")
        alignmentRule = a.getIntegerOrThrow(R.styleable.AppBarChildBehavior_behavior_alignmentRule, "alignmentRule")

        marginTop = a.getDimensionPixelSize(R.styleable.AppBarChildBehavior_behavior_marginTop, 0)
        marginBottom = a.getDimensionPixelSize(R.styleable.AppBarChildBehavior_behavior_marginBottom, 0)
        marginLeft = a.getDimensionPixelSize(R.styleable.AppBarChildBehavior_behavior_marginLeft, 0)
        marginRight = a.getDimensionPixelSize(R.styleable.AppBarChildBehavior_behavior_marginRight, 0)
        marginStart = a.getDimensionPixelSize(R.styleable.AppBarChildBehavior_behavior_marginStart, 0)
        marginEnd = a.getDimensionPixelSize(R.styleable.AppBarChildBehavior_behavior_marginEnd, 0)

        transformation = a.getTransformation(R.styleable.AppBarChildBehavior_behavior_childTransformation)

        a.recycle()
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency.id == dependencyViewId
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val target = parent.findViewById<View>(targetViewId)
        val dependency = parent.getDependencies(child).first()

        dependency.getFrameRelatedTo(dependencyRect, parent)
        layoutRect.layoutRelatedTo(child, parent, dependencyRect, layoutDirection)
        child.layout(layoutRect.left, layoutRect.top, layoutRect.right, layoutRect.bottom)

        child.getFrameRelatedTo(thisRect, parent)
        target.getFrameRelatedTo(targetRect, parent)

        transformation.onChildLayoutChanged(child, dependency, target)
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val appBar = parent.findViewById<View>(appBarId)
        val target = parent.findViewById<View>(targetViewId)
        val toolbar = parent.findViewById<View>(toolbarId)
        val behavior = (appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior as AppBarLayout.Behavior
        toolbar.getLocationOnScreen(tempLocation)
        val offset = behavior.topAndBottomOffset
        val percent = offset / (tempLocation[1] + toolbar.height - appBar.height).toFloat()
        transformation.onTargetChanged(child, thisRect, target, targetRect, percent, offset)
        return true
    }

    internal fun Rect.layoutRelatedTo(view: View, parent: CoordinatorLayout, frame: Rect, layoutDirection: Int) {
        val verticalRule = alignmentRule and VERTICAL_MASK
        val horizontalRule = alignmentRule and HORIZONTAL_MASK
        set(0, 0, view.measuredWidth, view.measuredHeight)
        when (verticalRule) {
            ALIGNMENT_CENTER_VERTICAL -> {
                offsetTopTo(frame.centerY() - view.measuredHeight / 2 + marginTop - marginBottom)
            }
            0, ALIGNMENT_TOP -> {
                offsetTopTo(frame.top + marginTop)
            }
            ALIGNMENT_BOTTOM -> {
                offsetBottomTo(frame.bottom - marginBottom)
            }
            ALIGNMENT_ABOVE -> {
                offsetBottomTo(frame.top + marginTop - marginBottom)
            }
            ALIGNMENT_BELOW -> {
                offsetTopTo(frame.bottom + marginTop - marginBottom)
            }
            ALIGNMENT_ABOVE_CENTER -> {
                offsetBottomTo(frame.centerY() + marginTop - marginBottom)
            }
            ALIGNMENT_BELOW_CENTER -> {
                offsetTopTo(frame.centerY() + marginTop - marginBottom)
            }
            else -> {
                throw IllegalArgumentException("Illegal alignment flag ${Integer.toHexString(alignmentRule)}")
            }
        }
        when (horizontalRule) {
            ALIGNMENT_CENTER_HORIZONTAL -> {
                offsetLeftTo(frame.centerX() - view.measuredWidth / 2
                        + absoluteMarginLeft(layoutDirection) - absoluteMarginRight(layoutDirection))
            }
            0, ALIGNMENT_LEFT -> {
                offsetLeftTo(frame.left + absoluteMarginLeft(layoutDirection))
            }
            ALIGNMENT_RIGHT -> {
                offsetRightTo(frame.right - absoluteMarginRight(layoutDirection))
            }
            ALIGNMENT_TO_LEFT_OF -> {
                offsetRightTo(frame.left + absoluteMarginLeft(layoutDirection)
                        - absoluteMarginRight(layoutDirection))
            }
            ALIGNMENT_TO_RIGHT_OF -> {
                offsetLeftTo(frame.right + absoluteMarginLeft(layoutDirection)
                        - absoluteMarginRight(layoutDirection))
            }
            ALIGNMENT_TO_LEFT_OF_CENTER -> {
                offsetRightTo(frame.centerX() + absoluteMarginLeft(layoutDirection)
                        - absoluteMarginRight(layoutDirection))
            }
            ALIGNMENT_TO_RIGHT_OF_CENTER -> {
                offsetLeftTo(frame.centerX() + absoluteMarginLeft(layoutDirection)
                        - absoluteMarginRight(layoutDirection))
            }
            ALIGNMENT_START -> {
                offsetStartTo(frame.getStart(layoutDirection)
                        + relativeMarginStart(layoutDirection), layoutDirection)
            }
            ALIGNMENT_END -> {
                offsetEndTo(frame.getEnd(layoutDirection)
                        - relativeMarginEnd(layoutDirection), layoutDirection)
            }
            ALIGNMENT_TO_START_OF -> {
                offsetEndTo(frame.getStart(layoutDirection)
                        + relativeMarginStart(layoutDirection) - relativeMarginEnd(layoutDirection),
                        layoutDirection)
            }
            ALIGNMENT_TO_END_OF -> {
                offsetStartTo(frame.getEnd(layoutDirection)
                        + relativeMarginStart(layoutDirection) - relativeMarginEnd(layoutDirection),
                        layoutDirection)
            }
            ALIGNMENT_TO_START_OF_CENTER -> {
                offsetEndTo(frame.centerX() + relativeMarginStart(layoutDirection)
                        - relativeMarginEnd(layoutDirection), layoutDirection)
            }
            ALIGNMENT_TO_END_OF_CENTER -> {
                offsetStartTo(frame.centerX() + relativeMarginStart(layoutDirection)
                        - relativeMarginEnd(layoutDirection), layoutDirection)
            }
            else -> {
                throw IllegalArgumentException("Illegal alignment flag ${Integer.toHexString(alignmentRule)}")
            }
        }
        left = left.coerceAtLeast(absoluteMarginLeft(layoutDirection) - frame.left)
        right = right.coerceAtMost(parent.measuredWidth - absoluteMarginRight(layoutDirection) - frame.left)
//        tempRect.top = tempRect.top.coerceAtLeast(marginTop - frame.top)
//        tempRect.bottom = tempRect.bottom.coerceAtLeast(parent.measuredHeight - marginBottom - frame.top)
    }

    private fun absoluteMarginLeft(layoutDirection: Int): Int {
        if (marginStart == 0 && marginEnd == 0) return marginLeft
        return if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            marginEnd
        } else {
            marginStart
        }
    }

    private fun absoluteMarginRight(layoutDirection: Int): Int {
        if (marginStart == 0 && marginEnd == 0) return marginRight
        return if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            marginStart
        } else {
            marginEnd
        }
    }

    private fun relativeMarginStart(layoutDirection: Int): Int {
        return if (marginStart != 0) {
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                -marginStart
            } else {
                marginStart
            }
        } else {
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                marginRight
            } else {
                marginLeft
            }
        }
    }

    private fun relativeMarginEnd(layoutDirection: Int): Int {
        return if (marginEnd != 0) {
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                -marginEnd
            } else {
                marginEnd
            }
        } else {
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                marginLeft
            } else {
                marginRight
            }
        }
    }

    interface ChildTransformation {
        fun onChildLayoutChanged(child: View, dependency: View, target: View) {}
        fun onTargetChanged(child: View, frame: Rect, target: View, targetFrame: Rect, percent: Float, offset: Int)
    }

    open class ScaleTransformation : ChildTransformation {

        override fun onTargetChanged(child: View, frame: Rect, target: View, targetFrame: Rect, percent: Float, offset: Int) {
            child.pivotX = child.width.toFloat()
            child.pivotY = child.height.toFloat()
            child.scaleX = 1 - (frame.width() - targetFrame.width()) * percent / frame.width()
            child.scaleY = 1 - (frame.height() - targetFrame.height()) * percent / frame.height()
            child.translationX = (targetFrame.right - frame.right) * percent
            child.translationY = -offset - (frame.bottom - offset - targetFrame.bottom) * percent
        }

    }

    @Keep
    open class TextViewTransformation : ChildTransformation {

        private var sourceSize: Float = Float.NaN
        private var destSize: Float = Float.NaN

        private var viewLaidOut: Boolean = false

        override fun onChildLayoutChanged(child: View, dependency: View, target: View) {
            if (viewLaidOut) return
            child as TextView
            target as TextView
            sourceSize = child.textSize
            destSize = target.textSize
            viewLaidOut = true
        }

        override fun onTargetChanged(child: View, frame: Rect, target: View, targetFrame: Rect, percent: Float, offset: Int) {
            child as TextView
            child.pivotX = child.width.toFloat()
            child.pivotY = child.height.toFloat()
            child.translationX = (targetFrame.left - frame.left) * percent
            child.translationY = -offset - (frame.bottom - offset - targetFrame.bottom) * percent
            child.setTextSize(TypedValue.COMPLEX_UNIT_PX, sourceSize + (destSize - sourceSize) * percent)
        }

    }

    companion object {
        private const val HORIZONTAL_MASK: Int = 0x0000FFFF
        private const val VERTICAL_MASK: Int = 0xFFFF0000.toInt()
        private const val HORIZONTAL_RELATIVE_FLAG: Int = 0x00001000
        const val ALIGNMENT_LEFT: Int = 0x00000001
        const val ALIGNMENT_RIGHT: Int = 0x00000002
        const val ALIGNMENT_TO_LEFT_OF: Int = 0x00000004
        const val ALIGNMENT_TO_RIGHT_OF: Int = 0x00000008
        const val ALIGNMENT_TO_LEFT_OF_CENTER: Int = 0x00000010
        const val ALIGNMENT_TO_RIGHT_OF_CENTER: Int = 0x00000020
        const val ALIGNMENT_CENTER_HORIZONTAL: Int = ALIGNMENT_LEFT or ALIGNMENT_RIGHT
        const val ALIGNMENT_START: Int = ALIGNMENT_LEFT or HORIZONTAL_RELATIVE_FLAG
        const val ALIGNMENT_END: Int = ALIGNMENT_RIGHT or HORIZONTAL_RELATIVE_FLAG
        const val ALIGNMENT_TO_START_OF: Int = ALIGNMENT_TO_LEFT_OF or HORIZONTAL_RELATIVE_FLAG
        const val ALIGNMENT_TO_END_OF: Int = ALIGNMENT_TO_RIGHT_OF or HORIZONTAL_RELATIVE_FLAG
        const val ALIGNMENT_TO_START_OF_CENTER: Int = ALIGNMENT_TO_LEFT_OF_CENTER or HORIZONTAL_RELATIVE_FLAG
        const val ALIGNMENT_TO_END_OF_CENTER: Int = ALIGNMENT_TO_RIGHT_OF_CENTER or HORIZONTAL_RELATIVE_FLAG
        const val ALIGNMENT_TOP: Int = 0x00010000
        const val ALIGNMENT_BOTTOM: Int = 0x00020000
        const val ALIGNMENT_ABOVE: Int = 0x00040000
        const val ALIGNMENT_BELOW: Int = 0x00080000
        const val ALIGNMENT_ABOVE_CENTER: Int = 0x00100000
        const val ALIGNMENT_BELOW_CENTER: Int = 0x00200000
        const val ALIGNMENT_CENTER_VERTICAL: Int = ALIGNMENT_TOP or ALIGNMENT_BOTTOM

        private fun TypedArray.getIntegerOrThrow(@StyleableRes index: Int, name: String): Int {
            if (!hasValue(index)) {
                throw IllegalArgumentException("$name required")
            }
            return getInteger(index, 0)
        }

        private fun TypedArray.getResourceIdOrThrow(@StyleableRes index: Int, name: String): Int {
            if (!hasValue(index)) {
                throw IllegalArgumentException("$name required")
            }
            return getResourceId(index, 0)
        }

        private fun TypedArray.getTransformation(@StyleableRes index: Int): ChildTransformation {
            val className = getString(index) ?: return ScaleTransformation()
            return Class.forName(className).newInstance() as ChildTransformation
        }
    }
}
