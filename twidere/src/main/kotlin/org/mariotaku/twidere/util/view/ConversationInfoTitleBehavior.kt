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
import android.graphics.Rect
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.getFrame
import org.mariotaku.twidere.extension.getFrameRelatedTo

/**
 * Created by mariotaku on 2017/2/20.
 */

class ConversationInfoTitleBehavior(
        context: Context,
        attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<TextView>(context, attrs) {
    private val marginBottom: Float
    private val marginStart: Float

    private val avatarRect = Rect()

    private val sourceRect = Rect()
    private val destRect = Rect()

    private var sourceSize: Float = Float.NaN
    private var destSize: Float = Float.NaN
    private var viewLaidOut: Boolean = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ConversationInfoTitleBehavior)
        marginBottom = a.getDimension(R.styleable.ConversationInfoTitleBehavior_behavior_titleMarginBottom, 0f)
        marginStart = a.getDimension(R.styleable.ConversationInfoTitleBehavior_behavior_titleMarginStart, 0f)
        a.recycle()
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: TextView, dependency: View): Boolean {
        return dependency.id == R.id.appBarConversationAvatar
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: TextView, layoutDirection: Int): Boolean {
        val conversationAvatar = parent.getDependencies(child).first()
        val appBar = conversationAvatar.parent as View
        conversationAvatar.getFrame(avatarRect)
        if (!viewLaidOut) {
            sourceSize = child.textSize
            destSize = parent.conversationName.textSize
            viewLaidOut = true
        }
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            child.right = Math.round(avatarRect.left - marginStart)
            child.left = child.right - child.measuredWidth
        } else {
            child.left = Math.round(avatarRect.right + marginStart)
            child.right = child.left + child.measuredWidth
        }
        child.bottom = Math.round(avatarRect.centerY() - marginBottom)
        child.top = child.bottom - child.measuredHeight

        child.getFrameRelatedTo(sourceRect)
        appBar.conversationName.getFrameRelatedTo(destRect)
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: TextView, dependency: View): Boolean {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior as ConversationInfoAvatarBehavior
        val percent = behavior.dependentPercent
        child.pivotX = child.width.toFloat()
        child.pivotY = child.height.toFloat()
        child.translationX = (destRect.left - sourceRect.left) * percent
        child.translationY = (destRect.top - sourceRect.top) * percent
        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, sourceSize + (destSize - sourceSize) * percent)
        if (percent >= 1) {
            child.visibility = View.INVISIBLE
            parent.conversationName.visibility = View.VISIBLE
        } else {
            child.visibility = View.VISIBLE
            parent.conversationName.visibility = View.INVISIBLE
        }
        return true
    }
}
