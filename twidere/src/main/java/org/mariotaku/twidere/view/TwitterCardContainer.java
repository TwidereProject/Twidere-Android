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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.mariotaku.twidere.util.support.ViewSupport;

/**
 * Created by mariotaku on 15/1/1.
 */
public class TwitterCardContainer extends FrameLayout {

    private int mCardWidth, mCardHeight;

    public TwitterCardContainer(Context context) {
        super(context);
    }

    public TwitterCardContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwitterCardContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCardSize(int width, int height) {
        mCardWidth = width;
        mCardHeight = height;
        if (!ViewSupport.isInLayout(this)) {
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int height;
        if (mCardWidth != 0 && mCardHeight != 0) {
            height = Math.round(measuredWidth * (mCardHeight / (float) mCardWidth));
        } else {
            height = measuredHeight;
        }
        final int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        final int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}
