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
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.fragment_messages_conversation_info.view.*
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.getFrame
import org.mariotaku.twidere.extension.getFrameRelatedTo
import org.mariotaku.twidere.extension.getLocationOnScreen
import org.mariotaku.twidere.extension.origin

/**
 * Created by mariotaku on 2017/2/20.
 */

class ConversationInfoAvatarBehavior(
        context: Context,
        attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<View>(context, attrs) {
    private val marginBottom: Float
    private val marginStart: Float

    private val appBarRect = Rect()

    private val sourceRect = Rect()
    private val destRect = Rect()
    private val tempRect = Rect()

    private var toolbarLayoutHeight = 0f

    var dependentPercent: Float = 0f
        private set

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ConversationInfoAvatarBehavior)
        marginBottom = a.getDimension(R.styleable.ConversationInfoAvatarBehavior_behavior_avatarMarginBottom, 0f)
        marginStart = a.getDimension(R.styleable.ConversationInfoAvatarBehavior_behavior_avatarMarginStart, 0f)
        a.recycle()
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val appBar = parent.getDependencies(child).first()
        // Get AppBar frame (and reset origin to 0, 0)
        appBar.getFrame(appBarRect)
        val dependencyOrigin = appBarRect.origin
        appBarRect.offsetTo(0, 0)

        // Layout avatar
        val left: Int
        val right: Int
        if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
            right = appBarRect.right - Math.round(marginStart)
            left = right - child.measuredWidth
        } else {
            left = Math.round(marginStart)
            right = left + child.measuredWidth
        }
        val bottom = Math.round(appBarRect.bottom - marginBottom)
        val top = bottom - child.measuredHeight
        child.layout(left, top, right, bottom)


        val toolbarLayout = appBar.toolbarLayout
        toolbarLayoutHeight = toolbarLayout.height.toFloat()

        child.getFrameRelatedTo(sourceRect)
        appBar.conversationAvatar.getFrameRelatedTo(destRect)
        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val behavior = (dependency.layoutParams as CoordinatorLayout.LayoutParams).behavior as AppBarLayout.Behavior
        val toolbar = dependency.toolbar
        toolbar.getLocationOnScreen(tempRect)
        val percent = behavior.topAndBottomOffset / (tempRect.bottom - toolbarLayoutHeight)
        child.pivotX = child.width.toFloat()
        child.pivotY = child.height.toFloat()
        child.scaleX = 1 - (sourceRect.width() - destRect.width()) * percent / sourceRect.width()
        child.scaleY = 1 - (sourceRect.height() - destRect.height()) * percent / sourceRect.height()
        child.translationX = (destRect.right - sourceRect.right) * percent
        child.translationY = (destRect.bottom - sourceRect.bottom) * percent

        if (percent >= 1) {
            child.visibility = View.INVISIBLE
            dependency.conversationAvatar.visibility = View.VISIBLE
        } else {
            child.visibility = View.VISIBLE
            dependency.conversationAvatar.visibility = View.INVISIBLE
        }
        dependentPercent = percent
        return true
    }
}
