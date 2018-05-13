/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.support.v7.widget.AppCompatTextView
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateUtils
import android.util.AttributeSet
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.text.style.PlaceholderLineSpan
import org.mariotaku.twidere.util.Utils.formatSameDayTime
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class ShortTimeView(
        context: Context,
        attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs, android.R.attr.textViewStyle), Constants {

    private val ticker = TickerRunnable(this)

    private val invalidateTimeRunnable = Runnable {
        if (!isAttachedToWindow) return@Runnable
        val time = this.time
        if (time < 0) return@Runnable
        val label = getTimeLabel(context, time, showAbsoluteTime)
        post {
            setTextIfChanged(label)
        }
    }

    var showAbsoluteTime: Boolean = false
        set(value) {
            field = value
            invalidateTime()
        }

    var time: Long = -1
        set(value) {
            field = value
            invalidateTime()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    private fun invalidateTime() {
        if (time == PLACEHOLDER) {
            text = placeholderText
        } else {
            updateHandler.post(invalidateTimeRunnable)
        }
    }

    private fun setTextIfChanged(text: CharSequence?) {
        if (text == this.text) return
        setText(text)
    }

    private class TickerRunnable(view: ShortTimeView) : Runnable {

        private val viewRef = WeakReference(view)

        override fun run() {
            val view = viewRef.get() ?: return
            val handler = view.handler ?: return
            view.invalidateTime()
            val now = SystemClock.uptimeMillis()
            val next = now + TICKER_DURATION - now % TICKER_DURATION
            handler.postAtTime(this, next)
        }
    }

    companion object {

        const val INVALID: Long = -1
        const val PLACEHOLDER: Long = -2

        private val placeholderText = SpannableString(" ").apply {
            setSpan(PlaceholderLineSpan(3.5f, true), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        private const val TICKER_DURATION = 5000L
        private val ONE_MINUTE_MILLIS = TimeUnit.MINUTES.toMillis(1)

        // TODO: Use an universal executor across app
        private val updateThread = HandlerThread("ShortTimeUpdate").apply {
            start()
        }
        private val updateHandler = Handler(updateThread.looper)

        fun getTimeLabel(context: Context, time: Long, showAbsoluteTime: Boolean): CharSequence {
            if (showAbsoluteTime) return formatSameDayTime(context, time)
            return if (Math.abs(System.currentTimeMillis() - time) > ONE_MINUTE_MILLIS) {
                DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
            } else {
                context.getString(R.string.just_now)
            }
        }
    }

}
