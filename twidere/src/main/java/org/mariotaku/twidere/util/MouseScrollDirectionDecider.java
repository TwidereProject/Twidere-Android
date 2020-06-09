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

package org.mariotaku.twidere.util;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.Nullable;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

/**
 * Warning! Evil code ahead!
 * Guess mouse scroll direction by calculating scroll offset of system ScrollView
 */
public class MouseScrollDirectionDecider {

    private final float factor;
    @Nullable
    private View verticalView, horizontalView;

    private int horizontalDirection = 0, verticalDirection = 0;
    private float horizontalScroll, verticalScroll;

    public MouseScrollDirectionDecider(float factor) {
        this.factor = factor;
    }

    public float getHorizontalDirection() {
        return horizontalDirection;
    }

    public boolean isHorizontalAvailable() {
        return horizontalDirection != 0;
    }

    public boolean isVerticalAvailable() {
        return verticalDirection != 0;
    }

    private void setHorizontalDirection(int direction) {
        horizontalDirection = direction;
    }

    public float getVerticalDirection() {
        return verticalDirection;
    }

    private void setVerticalDirection(int direction) {
        verticalDirection = direction;
    }

    public boolean guessDirection(MotionEvent event) {
        if (verticalView == null || horizontalView == null) return false;
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) == 0) {
            return false;
        }
        if (event.getAction() != MotionEvent.ACTION_SCROLL) return false;
        verticalScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
        horizontalScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
        verticalView.onGenericMotionEvent(event);
        horizontalView.onGenericMotionEvent(event);
        return verticalScroll != 0 || horizontalScroll != 0;
    }

    public void attach(View view) {
        final Context context = view.getContext();
        verticalView = new InternalScrollView(context, this);
        horizontalView = new InternalHorizontalScrollView(context, this);
    }

    public void detach() {
        verticalView = null;
        horizontalView = null;
    }

    @SuppressLint("ViewConstructor")
    private static class InternalScrollView extends ScrollView {

        private final int factor;
        private final MouseScrollDirectionDecider decider;

        public InternalScrollView(Context context, MouseScrollDirectionDecider decider) {
            super(context);
            this.decider = decider;
            final View view = new View(context);
            addView(view);
            this.factor = Math.round(decider.factor);
            view.setTop(-factor);
            view.setBottom(factor);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.scrollTo(0, factor);
            if (t != factor) {
                float value = (t - oldt) * decider.verticalScroll;
                if (value > 0) {
                    decider.setVerticalDirection(1);
                } else if (value < 0) {
                    decider.setVerticalDirection(-1);
                } else {
                    decider.setVerticalDirection(0);
                }
            }
        }

        @Override
        public void setOverScrollMode(int mode) {
            super.setOverScrollMode(OVER_SCROLL_NEVER);
        }

    }

    @SuppressLint("ViewConstructor")
    private static class InternalHorizontalScrollView extends HorizontalScrollView {

        private final int factor;
        private final MouseScrollDirectionDecider decider;

        public InternalHorizontalScrollView(Context context, MouseScrollDirectionDecider decider) {
            super(context);
            this.decider = decider;
            final View view = new View(context);
            addView(view);
            this.factor = Math.round(decider.factor);
            view.setLeft(-factor);
            view.setRight(factor);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.scrollTo(factor, 0);
            if (t != factor) {
                float value = (t - oldt) * decider.horizontalScroll;
                if (value > 0) {
                    decider.setHorizontalDirection(1);
                } else if (value < 0) {
                    decider.setHorizontalDirection(-1);
                } else {
                    decider.setHorizontalDirection(0);
                }
            }
        }

        @Override
        public void setOverScrollMode(int mode) {
            super.setOverScrollMode(OVER_SCROLL_NEVER);
        }
    }

}
