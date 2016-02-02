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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.MotionEvent;
import android.view.View;

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;

/**
 * Created by mariotaku on 15/3/15.
 */
public class ContentListScrollListener extends OnScrollListener {

    private int mScrollState;
    private int mScrollSum;
    private int mTouchSlop;

    private final ContentListSupport mContentListSupport;
    private final TouchListener mTouchListener;
    private int mScrollDirection;

    public ContentListScrollListener(@NonNull ContentListSupport contentListSupport) {
        mContentListSupport = contentListSupport;
        mTouchListener = new TouchListener(this);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (mScrollState != RecyclerView.SCROLL_STATE_IDLE) {
            notifyScrollStateChanged();
        }
        mScrollState = newState;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        //Reset mScrollSum when scrolling in reverse direction
        if (dy * mScrollSum < 0) {
            mScrollSum = 0;
        }
        mScrollSum += dy;
        if (Math.abs(mScrollSum) > mTouchSlop) {
            mContentListSupport.setControlVisible(dy < 0);
            mScrollSum = 0;
        }
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            notifyScrollStateChanged();
        }
    }

    public void setTouchSlop(int touchSlop) {
        mTouchSlop = touchSlop;
    }

    private void notifyScrollStateChanged() {
        final Object adapter = mContentListSupport.getAdapter();
        if (!(adapter instanceof ILoadMoreSupportAdapter)) return;
        final ILoadMoreSupportAdapter loadMoreAdapter = (ILoadMoreSupportAdapter) adapter;
        if (!mContentListSupport.isRefreshing() && loadMoreAdapter.isLoadMoreSupported()
                && loadMoreAdapter.getLoadMoreIndicatorPosition() == IndicatorPosition.NONE) {
            int position = 0;
            if (mContentListSupport.isReachingEnd() && mScrollDirection >= 0) {
                position |= IndicatorPosition.END;
            }
            if (mContentListSupport.isReachingStart() && mScrollDirection <= 0) {
                position |= IndicatorPosition.START;
            }
            mContentListSupport.onLoadMoreContents(position);
        }
    }

    public View.OnTouchListener getOnTouchListener() {
        return mTouchListener;
    }

    static class TouchListener implements View.OnTouchListener {

        private final ContentListScrollListener listener;
        private float mLastY;

        TouchListener(ContentListScrollListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    listener.resetScrollDirection();
                    mLastY = Float.NaN;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (!Float.isNaN(mLastY)) {
                        float delta = mLastY - event.getRawY();
                        listener.setScrollDirection(delta < 0 ? -1 : 1);
                    } else {
                        mLastY = event.getRawY();
                    }
                    break;
                }
            }
            return false;
        }
    }

    private void setScrollDirection(int direction) {
        mScrollDirection = direction;
    }

    private void resetScrollDirection() {
        mScrollDirection = 0;
    }

    public interface ContentListSupport {

        Object getAdapter();

        boolean isRefreshing();

        void onLoadMoreContents(@IndicatorPosition int position);

        void setControlVisible(boolean visible);

        boolean isReachingStart();

        boolean isReachingEnd();

    }
}
