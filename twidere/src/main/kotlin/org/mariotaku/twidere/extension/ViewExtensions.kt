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

import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.UiThread
import android.view.View
import android.view.ViewGroup
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
        other == null -> {
            offsetToRoot(this, rect)
        }
        other === this -> {
            rect.offsetTo(0, 0)
        }
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

private fun offsetToRoot(view: View, rect: Rect) {
    var parent = view.parent as? View
    while (parent != null) {
        rect.offset(parent.left, parent.top)
        parent = parent.parent as? View
    }
}
