package org.mariotaku.twidere.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.AbsListView;

import org.mariotaku.twidere.util.support.ViewSupport;

/**
 * Created by mariotaku on 16/3/1.
 */
public class ListViewScrollHandler extends ContentScrollHandler implements AbsListView.OnScrollListener,
        ListScrollDistanceCalculator.ScrollDistanceListener {
    private final ListScrollDistanceCalculator mCalculator;
    @Nullable
    private AbsListView.OnScrollListener mOnScrollListener;
    private int mDy;
    private int mOldState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    public ListViewScrollHandler(@NonNull ContentListSupport contentListSupport, @Nullable ViewCallback viewCallback) {
        super(contentListSupport, viewCallback);
        mCalculator = new ListScrollDistanceCalculator();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCalculator.onScrollStateChanged(view, scrollState);
        mCalculator.setScrollDistanceListener(this);
        handleScrollStateChanged(scrollState, SCROLL_STATE_IDLE);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mCalculator.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        final int scrollState = getScrollState();
        handleScroll(mDy, scrollState, mOldState, SCROLL_STATE_IDLE);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Nullable
    public AbsListView.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void setOnScrollListener(@Nullable AbsListView.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public int getTotalScrollDistance() {
        return mCalculator.getTotalScrollDistance();
    }

    @Override
    public void onScrollDistanceChanged(int delta, int total) {
        mDy = -delta;
        final int scrollState = getScrollState();
        handleScroll(mDy, scrollState, mOldState, SCROLL_STATE_IDLE);
        mOldState = scrollState;
    }

    public static class ListViewCallback implements ViewCallback {
        private final AbsListView listView;

        public ListViewCallback(AbsListView listView) {
            this.listView = listView;
        }

        @Override
        public boolean getComputingLayout() {
            return ViewSupport.isInLayout(listView);
        }

        @Override
        public void post(@NonNull Runnable runnable) {
            listView.post(runnable);
        }
    }
}
