package org.mariotaku.twidere.util;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Created by mariotaku on 14/10/22.
 */
public class ListScrollDistanceCalculator implements OnScrollListener {

    private ScrollDistanceListener mScrollDistanceListener;

    private boolean mListScrollStarted;
    private int mFirstVisibleItem;
    private int mFirstVisibleHeight;
    private int mFirstVisibleTop, mFirstVisibleBottom;
    private int mTotalScrollDistance;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getCount() == 0) return;
        switch (scrollState) {
            case SCROLL_STATE_IDLE: {
                mListScrollStarted = false;
                break;
            }
            case SCROLL_STATE_TOUCH_SCROLL: {
                final View firstChild = view.getChildAt(0);
                mFirstVisibleItem = view.getFirstVisiblePosition();
                mFirstVisibleTop = firstChild.getTop();
                mFirstVisibleBottom = firstChild.getBottom();
                mFirstVisibleHeight = firstChild.getHeight();
                mListScrollStarted = true;
                mTotalScrollDistance = 0;
                break;
            }
        }
    }

    public int getTotalScrollDistance() {
        return mTotalScrollDistance;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount == 0 || !mListScrollStarted) return;
        final View firstChild = view.getChildAt(0);
        final int firstVisibleTop = firstChild.getTop(), firstVisibleBottom = firstChild.getBottom();
        final int firstVisibleHeight = firstChild.getHeight();
        final int delta;
        if (firstVisibleItem > mFirstVisibleItem) {
            mFirstVisibleTop += mFirstVisibleHeight;
            delta = firstVisibleTop - mFirstVisibleTop;
        } else if (firstVisibleItem < mFirstVisibleItem) {
            mFirstVisibleBottom -= mFirstVisibleHeight;
            delta = firstVisibleBottom - mFirstVisibleBottom;
        } else {
            delta = firstVisibleBottom - mFirstVisibleBottom;
        }
        mTotalScrollDistance += delta;
        if (mScrollDistanceListener != null) {
            mScrollDistanceListener.onScrollDistanceChanged(delta, mTotalScrollDistance);
        }
        mFirstVisibleTop = firstVisibleTop;
        mFirstVisibleBottom = firstVisibleBottom;
        mFirstVisibleHeight = firstVisibleHeight;
        mFirstVisibleItem = firstVisibleItem;
    }

    public void setScrollDistanceListener(ScrollDistanceListener listener) {
        mScrollDistanceListener = listener;
    }

    public interface ScrollDistanceListener {
        void onScrollDistanceChanged(int delta, int total);
    }
}
