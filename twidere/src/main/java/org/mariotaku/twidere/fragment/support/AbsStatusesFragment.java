package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.util.SimpleDrawerCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;

/**
 * Created by mariotaku on 14/11/5.
 */
public abstract class AbsStatusesFragment<Data> extends BaseSupportFragment
        implements LoaderCallbacks<Data>, OnRefreshListener, DrawerCallback {

    private View mContentView;
    private SharedPreferences mPreferences;

    @Override
    public void fling(float velocity) {
        mDrawerCallback.fling(velocity);
    }

    public abstract int getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds);

    @Override
    public void scrollBy(float dy) {
        mDrawerCallback.scrollBy(dy);
    }

    @Override
    public boolean shouldLayoutHeaderBottom() {
        return mDrawerCallback.shouldLayoutHeaderBottom();
    }

    @Override
    public boolean canScroll(float dy) {
        return mDrawerCallback.canScroll(dy);
    }

    @Override
    public boolean isScrollContent(float x, float y) {
        return mDrawerCallback.isScrollContent(x, y);
    }

    @Override
    public void cancelTouch() {
        mDrawerCallback.cancelTouch();
    }

    @Override
    public void topChanged(int offset) {
        mDrawerCallback.topChanged(offset);
    }

    private View mProgressContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private AbsStatusesAdapter<Data> mAdapter;

    private SimpleDrawerCallback mDrawerCallback;

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            final LoaderManager lm = getLoaderManager();
            if (lm.hasRunningLoaders()) return;
            if (layoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                final long[] maxIds = new long[]{mAdapter.getStatus(mAdapter.getStatusCount() - 1).id};
                getStatuses(null, maxIds, null);
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        final boolean compact = Utils.isCompactCards(context);
        mDrawerCallback = new SimpleDrawerCallback(mRecyclerView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ThemeUtils.getUserAccentColor(context));
        mAdapter = onCreateAdapter(context, compact);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        if (compact) {
            mRecyclerView.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));
        }
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(mOnScrollListener);
        getLoaderManager().initLoader(0, getArguments(), this);
        setListShown(false);
    }

    protected abstract AbsStatusesAdapter<Data> onCreateAdapter(Context context, boolean compact);

    private void setListShown(boolean shown) {
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(shown ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mContentView = view.findViewById(R.id.fragment_content);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        mContentView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

    protected Data getAdapterData() {
        return mAdapter.getData();
    }

    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    public void onLoadFinished(Loader<Data> loader, Data data) {
        setRefreshing(false);
        mAdapter.setData(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
    }


    @Override
    public void onRefresh() {
        if (mAdapter.getStatusCount() > 0) {
            final long[] sinceIds = new long[]{mAdapter.getStatus(0).id};
            getStatuses(null, null, sinceIds);
        } else {
            getStatuses(null, null, null);
        }
    }

    public SharedPreferences getSharedPreferences() {
        if (mPreferences != null) return mPreferences;
        return mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }


}
