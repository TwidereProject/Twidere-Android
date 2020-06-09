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

package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import org.mariotaku.twidere.R
import kotlin.math.min

class MaxHeightScrollView(context: Context, attrs: AttributeSet? = null) : ScrollView(context, attrs) {

    private var maxHeight: Int = -1

    init {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
        // 200 is a default value
        maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightScrollView_android_maxHeight, -1)
        styledAttrs.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hSpec = if (maxHeight >= 0) {
            val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
            if (measuredHeight > 0) {
                MeasureSpec.makeMeasureSpec(
                    min(measuredHeight, maxHeight),
                        MeasureSpec.AT_MOST)
            } else {
                maxHeight
            }
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, hSpec)
    }
}