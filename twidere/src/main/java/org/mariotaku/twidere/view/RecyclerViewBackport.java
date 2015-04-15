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
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.MotionEvent;

/**
 * Created by mariotaku on 15/3/30.
 */
public class RecyclerViewBackport extends RecyclerView {

    // This value is used when handling generic motion events.
    private float mScrollFactor = Float.MIN_VALUE;

    public RecyclerViewBackport(Context context) {
        super(context);
    }

    public RecyclerViewBackport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewBackport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        final LayoutManager lm = getLayoutManager();
        if (lm == null) {
            return false;
        }
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            if (event.getAction() == MotionEventCompat.ACTION_SCROLL) {
                final float vScroll, hScroll;
                if (lm.canScrollVertically()) {
                    vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                } else {
                    vScroll = 0f;
                }
                if (lm.canScrollHorizontally()) {
                    hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
                } else {
                    hScroll = 0f;
                }

                if (vScroll != 0 || hScroll != 0) {
                    final float scrollFactor = getScrollFactorBackport();
                    scrollBy((int) (hScroll * scrollFactor), (int) (vScroll * scrollFactor));
                }
            }
        }
        return false;
    }

    /**
     * Ported from View.getVerticalScrollFactor.
     */
    private float getScrollFactorBackport() {
        if (mScrollFactor == Float.MIN_VALUE) {
            TypedValue outValue = new TypedValue();
            if (getContext().getTheme().resolveAttribute(
                    android.R.attr.listPreferredItemHeight, outValue, true)) {
                mScrollFactor = outValue.getDimension(
                        getContext().getResources().getDisplayMetrics());
            } else {
                return 0; //listPreferredItemHeight is not defined, no generic scrolling
            }

        }
        return mScrollFactor;
    }

}
