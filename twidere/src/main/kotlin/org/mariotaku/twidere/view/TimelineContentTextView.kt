/*
 *                 Twidere - Twitter client for Android
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

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.method.MovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TextView
import org.mariotaku.chameleon.view.ChameleonTextView
import org.mariotaku.twidere.extension.setupEmojiFactory
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

/**
 * Returns true when not clicking links
 * Created by mariotaku on 15/11/20.
 */
class TimelineContentTextView(
        context: Context,
        attrs: AttributeSet? = null
) : ChameleonTextView(context, attrs) {

    init {
        setupEmojiFactory()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        // Android clears TextView when setText(), so setText before touch
        if (event.actionMasked == MotionEvent.ACTION_DOWN && isTextSelectable) {
            if (selectionEnd != selectionStart) {
                val text = text
                setText(null)
                setText(text)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTextSelectable) {
            return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    override fun getDefaultMovementMethod(): MovementMethod {
        return InternalMovementMethod()
    }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable && isTextSelectable)
    }

    override fun setLongClickable(longClickable: Boolean) {
        super.setLongClickable(longClickable && isTextSelectable)
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            true
        }
    }

    internal class InternalMovementMethod : BaseMovementMethod() {
        private var targetSpan: WeakReference<ClickableSpan?>? = null

        override fun canSelectArbitrarily() = true

        override fun onTouchEvent(widget: TextView, text: Spannable, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val layout = widget.layout
                    val x = event.x - widget.paddingLeft + widget.scrollX
                    val y = event.y - widget.paddingTop + widget.scrollY
                    val line = layout.getLineForVertical(y.roundToInt())
                    val offset = layout.getOffsetForHorizontal(line, x)
                    targetSpan = if (x <= layout.getLineWidth(line)) {
                        WeakReference(text.getSpans(offset, offset, ClickableSpan::class.java).firstOrNull())
                    } else {
                        null
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val span = targetSpan?.get() ?: return false
                    span.onClick(widget)
                    targetSpan = null
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    targetSpan = null
                }
            }
            return targetSpan?.get() != null
        }

    }
}
