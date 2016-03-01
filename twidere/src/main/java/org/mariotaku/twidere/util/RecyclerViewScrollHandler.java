package org.mariotaku.twidere.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.mariotaku.twidere.util.ContentScrollHandler.ContentListSupport;
import org.mariotaku.twidere.util.ContentScrollHandler.ViewCallback;

/**
 * Created by mariotaku on 16/3/1.
 */
public class RecyclerViewScrollHandler extends RecyclerView.OnScrollListener {

    final ContentScrollHandler mScrollHandler;

    public RecyclerViewScrollHandler(@NonNull ContentListSupport contentListSupport, @Nullable ViewCallback viewCallback) {
        mScrollHandler = new ContentScrollHandler(contentListSupport, viewCallback);
    }

    public void setTouchSlop(int touchSlop) {
        mScrollHandler.setTouchSlop(touchSlop);
    }

    public View.OnTouchListener getOnTouchListener() {
        return mScrollHandler.getOnTouchListener();
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        mScrollHandler.handleScrollStateChanged(newState, RecyclerView.SCROLL_STATE_IDLE);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        final int scrollState = recyclerView.getScrollState();
        final int idleState = RecyclerView.SCROLL_STATE_IDLE;
        mScrollHandler.handleScroll(dy, scrollState, idleState);
    }

    public static class RecyclerViewCallback implements ViewCallback {
        private final RecyclerView recyclerView;

        public RecyclerViewCallback(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public boolean isComputingLayout() {
            return recyclerView.isComputingLayout();
        }

        @Override
        public void post(Runnable action) {
            recyclerView.post(action);
        }
    }
}
