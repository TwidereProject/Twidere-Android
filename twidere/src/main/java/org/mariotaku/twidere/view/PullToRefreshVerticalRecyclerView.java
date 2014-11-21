package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * Created by mariotaku on 14/11/19.
 */
public class PullToRefreshVerticalRecyclerView extends PullToRefreshBase<RecyclerView> {

    @IdRes
    public static final int REFRESHABLE_VIEW_ID = 0x7f200001;

    public PullToRefreshVerticalRecyclerView(Context context) {
        super(context);
    }

    public PullToRefreshVerticalRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshVerticalRecyclerView(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshVerticalRecyclerView(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
        final RecyclerView recyclerView = new RecyclerView(context, attrs);
        recyclerView.setId(REFRESHABLE_VIEW_ID);
        return recyclerView;
    }

    @Override
    protected boolean isReadyForPullStart() {
        final RecyclerView recyclerView = getRefreshableView();
        if (recyclerView.getChildCount() <= 0)
            return true;
        int firstVisiblePosition = recyclerView.getChildPosition(recyclerView.getChildAt(0));
        if (firstVisiblePosition == 0)
            return recyclerView.getChildAt(0).getTop() == 0;
        else
            return false;

    }

    @Override
    protected boolean isReadyForPullEnd() {
        final RecyclerView recyclerView = getRefreshableView();
        int lastVisiblePosition = recyclerView.getChildPosition(recyclerView.getChildAt(recyclerView.getChildCount() - 1));
        if (lastVisiblePosition >= recyclerView.getAdapter().getItemCount() - 1) {
            return recyclerView.getChildAt(recyclerView.getChildCount() - 1).getBottom() <= recyclerView.getBottom();
        }
        return false;
    }
}
