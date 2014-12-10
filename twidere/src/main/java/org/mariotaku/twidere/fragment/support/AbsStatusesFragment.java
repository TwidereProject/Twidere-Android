package org.mariotaku.twidere.fragment.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.mariotaku.twidere.adapter.AbsStatusesAdapter.StatusAdapterListener;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.SimpleDrawerCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 14/11/5.
 */
public abstract class AbsStatusesFragment<Data> extends BaseSupportFragment implements LoaderCallbacks<Data>,
        OnRefreshListener, DrawerCallback, RefreshScrollTopInterface, StatusAdapterListener {


    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            onReceivedBroadcast(intent, intent.getAction());
        }

    };
    private View mContentView;
    private SharedPreferences mPreferences;
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
            if (isRefreshing()) return;
            if (layoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                onLoadMoreStatuses();
            }
        }
    };

    @Override
    public boolean canScroll(float dy) {
        return mDrawerCallback.canScroll(dy);
    }

    @Override
    public void cancelTouch() {
        mDrawerCallback.cancelTouch();
    }

    @Override
    public void fling(float velocity) {
        mDrawerCallback.fling(velocity);
    }

    @Override
    public boolean isScrollContent(float x, float y) {
        return mDrawerCallback.isScrollContent(x, y);
    }

    @Override
    public void scrollBy(float dy) {
        mDrawerCallback.scrollBy(dy);
    }

    @Override
    public boolean shouldLayoutHeaderBottom() {
        return mDrawerCallback.shouldLayoutHeaderBottom();
    }

    @Override
    public void topChanged(int offset) {
        mDrawerCallback.topChanged(offset);
    }

    public AbsStatusesAdapter<Data> getAdapter() {
        return mAdapter;
    }

    public SharedPreferences getSharedPreferences() {
        if (mPreferences != null) return mPreferences;
        return mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public abstract int getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds);

    public boolean isRefreshing() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

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
        mAdapter.setEventListener(this);
        getLoaderManager().initLoader(0, getArguments(), this);
        setListShown(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter();
        onSetIntentFilter(filter);
        registerReceiver(mStateReceiver, filter);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mStateReceiver);
        super.onStop();
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
        triggerRefresh();
    }

    @Override
    public void onStatusActionClick(StatusViewHolder holder, int id, int position) {
        final ParcelableStatus status = mAdapter.getStatus(position);
        if (status == null) return;
        switch (id) {
            case R.id.reply_count: {
                final Context context = getActivity();
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_REPLY);
                intent.setPackage(context.getPackageName());
                intent.putExtra(IntentConstants.EXTRA_STATUS, status);
                context.startActivity(intent);
                break;
            }
            case R.id.retweet_count: {
                RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case R.id.favorite_count: {
                final AsyncTwitterWrapper twitter = getTwitterWrapper();
                if (twitter == null) return;
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_id, status.id);
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                }
                break;
            }
        }
    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {
        Utils.openStatus(getActivity(), mAdapter.getStatus(position), null);
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {
        final ParcelableStatus status = mAdapter.getStatus(position);
        final long sinceId = position + 1 < mAdapter.getStatusCount() ? mAdapter.getStatus(position + 1).id : -1;
        final long[] accountIds = {status.account_id};
        final long[] maxIds = {status.id};
        final long[] sinceIds = {sinceId};
        getStatuses(accountIds, maxIds, sinceIds);
    }

    @Override
    public void onStatusMenuClick(StatusViewHolder holder, int position) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, mAdapter.getStatus(position));
        final StatusMenuDialogFragment f = new StatusMenuDialogFragment();
        f.setArguments(args);
        f.show(getActivity().getSupportFragmentManager(), "status_menu");
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
    public boolean scrollToStart() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        final int tabPosition = getTabPosition();
        if (twitter != null && tabPosition != -1) {
            twitter.clearUnreadCountAsync(tabPosition);
        }
        mRecyclerView.smoothScrollToPosition(0);
        return true;
    }

    protected abstract long[] getAccountIds();

    protected Data getAdapterData() {
        return mAdapter.getData();
    }

    protected void setAdapterData(Data data) {
        mAdapter.setData(data);
    }

    protected abstract AbsStatusesAdapter<Data> onCreateAdapter(Context context, boolean compact);

    protected abstract void onLoadMoreStatuses();

    protected abstract void onReceivedBroadcast(Intent intent, String action);

    protected abstract void onSetIntentFilter(IntentFilter filter);

    private void setListShown(boolean shown) {
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(shown ? View.VISIBLE : View.GONE);
    }
}
