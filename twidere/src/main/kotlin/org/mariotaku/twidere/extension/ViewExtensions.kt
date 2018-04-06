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

package org.mariotaku.twidere.extension

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.support.annotation.UiThread
import android.support.v4.view.accessibility.AccessibilityEventCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import org.mariotaku.ktextension.empty

private val tempLocation = IntArray(2)
private val tempRect = Rect()

@UiThread
fun View.getBounds(rect: RectF) {
    rect.set(x, y, x + width, y + height)
}

@UiThread
fun View.getFrame(rect: Rect) {
    rect.set(left, top, right, bottom)
}

@UiThread
fun View.getFrameRelatedTo(rect: Rect, other: View? = null) {
    this.getFrame(rect)
    when {
        other == null -> offsetToRoot(this, rect)
        other === this -> rect.offsetTo(0, 0)
        other !== parent -> {
            offsetToRoot(this, rect)
            other.getFrame(tempRect)
            offsetToRoot(other, tempRect)
            rect.offset(-tempRect.left, -tempRect.top)
        }
    }
}

fun View.getLocationOnScreen(rect: Rect) {
    getLocationOnScreen(tempLocation)
    rect.set(tempLocation[0], tempLocation[1], tempLocation[0] + width, tempLocation[1] + height)
}

fun View.getLocationInWindow(rect: Rect) {
    getLocationInWindow(tempLocation)
    rect.set(tempLocation[0], tempLocation[1], tempLocation[0] + width, tempLocation[1] + height)
}

fun View.addSystemUiVisibility(systemUiVisibility: Int) {
    this.systemUiVisibility = this.systemUiVisibility or systemUiVisibility
}

fun View.removeSystemUiVisibility(systemUiVisibility: Int) {
    this.systemUiVisibility = this.systemUiVisibility and systemUiVisibility.inv()
}

fun View.hideIfEmpty(dependency: TextView, hideVisibility: Int = View.GONE) {
    visibility = if (dependency.empty) {
        hideVisibility
    } else {
        View.VISIBLE
    }
}

fun View.setVisible(visible: Boolean, hiddenVisibility: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else hiddenVisibility
}

val ViewGroup.children: List<View>
    get() = (0 until childCount).map { getChildAt(it) }

fun <T : ViewGroup.LayoutParams> View.setupLayoutParams(action: (T) -> Unit): Boolean {
    @Suppress("UNCHECKED_CAST")
    action(layoutParams as? T ?: return false)
    return true
}

fun ViewGroup.showContextMenuForChild(originalView: View, anchor: View): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        return showContextMenuForChild(originalView)
    }
    val rect = Rect()
    anchor.getFrameRelatedTo(rect, originalView)
    return ViewExtensionsN.showContextMenuForChild(this, originalView,
            rect.centerX().toFloat(), rect.centerY().toFloat())
}

fun ViewGroup.showContextMenuForChildCompat(originalView: View, x: Float, y: Float): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        return showContextMenuForChild(originalView)
    }
    return ViewExtensionsN.showContextMenuForChild(this, originalView, x, y)
}


fun View.announceForAccessibilityCompat(text: CharSequence) {
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (!accessibilityManager.isEnabled) return
    // Prior to SDK 16, announcements could only be made through FOCUSED
    // events. Jelly Bean (SDK 16) added support for speaking text verbatim
    // using the ANNOUNCEMENT event type.
    val eventType: Int
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
        eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED
    } else {
        eventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT
    }

    // Construct an accessibility event with the minimum recommended
    // attributes. An event without a class name or package may be dropped.
    val event = AccessibilityEvent.obtain(eventType)
    event.text.add(text)
    event.className = javaClass.name
    event.packageName = context.packageName
    event.setSource(this)

    // Sends the event directly through the accessibility manager. If your
    // application only targets SDK 14+, you should just call
    // getParent().requestSendAccessibilityEvent(this, event);
    accessibilityManager.sendAccessibilityEvent(event)
}


fun View.findViewByText(text: CharSequence?): TextView? {
    if (this is TextView && TextUtils.equals(text, this.text))
        return this
    if (this is ViewGroup) {
        return (0 until childCount)
                .mapNotNull { getChildAt(it).findViewByText(text) }
                .firstOrNull()
    }
    return null
}

fun <T : View> View.findViewByType(cls: Class<T>): T? {
    @Suppress("UNCHECKED_CAST")
    if (cls.isAssignableFrom(javaClass)) return this as T?
    if (this is ViewGroup) {
        for (i in 0 until childCount) {
            val found = getChildAt(i).findViewByType(cls)
            if (found != null) return found
        }
    }
    return null
}

fun ViewGroup.inflate(res: Int, attachToRoot: Boolean = true): View {
    return LayoutInflater.from(context).inflate(res, this, attachToRoot)
}

private fun offsetToRoot(view: View, rect: Rect) {
    var parent = view.parent as? View
    while (parent != null) {
        rect.offset(parent.left, parent.top)
        parent = parent.parent as? View
    }
}

@TargetApi(Build.VERSION_CODES.N)
private object ViewExtensionsN {
    fun showContextMenuForChild(viewGroup: ViewGroup, originalView: View, x: Float, y: Float): Boolean {
        return viewGroup.showContextMenuForChild(originalView, x, y)
    }
}