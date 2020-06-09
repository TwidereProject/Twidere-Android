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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.util.support.ViewSupport
import kotlin.math.roundToInt

/**
 * Created by mariotaku on 15/1/1.
 */
class TwitterCardContainer(context: Context, attrs: AttributeSet? = null) : ContainerView(context, attrs) {

    private var cardWidth: Int = 0
    private var cardHeight: Int = 0

    fun setCardSize(width: Int, height: Int) {
        cardWidth = width
        cardHeight = height
        if (!ViewSupport.isInLayout(this)) {
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (cardWidth <= 0 || cardHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = (measuredWidth * (cardHeight / cardWidth.toFloat())).roundToInt()
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
        val newHeightMeasureSpec: Int
        newHeightMeasureSpec = if (measuredHeight != 0) {
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
}
