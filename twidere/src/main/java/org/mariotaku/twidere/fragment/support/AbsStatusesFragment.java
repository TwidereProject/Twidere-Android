package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.BaseSupportActivity;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter.StatusAdapterListener;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ColorUtils;
import org.mariotaku.twidere.util.SimpleDrawerCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.message.StatusListChangedEvent;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;

import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

/**
 * Created by mariotaku on 14/11/5.
 */
public abstract class AbsStatusesFragment<Data> extends BaseSupportFragment implements LoaderCallbacks<Data>,
        OnRefreshListener, DrawerCallback, RefreshScrollTopInterface, StatusAdapterListener {


    private final Object mStatusesBusCallback;
    private AbsStatusesAdapter<Data> mAdapter;
    private LinearLayoutManager mLayoutManager;
    private View mContentView;
    private SharedPreferences mPreferences;
    private View mProgressContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private SimpleDrawerCallback mDrawerCallback;

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        private int mScrollState;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            mScrollState = newState;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            if (Math.abs(dy) > mTouchSlop) {
                setControlVisible(dy < 0);
            }
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (!isRefreshing() && mAdapter.hasLoadMoreIndicator() && mScrollState != RecyclerView.SCROLL_STATE_IDLE
                    && layoutManager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                onLoadMoreStatuses();
            }
        }
    };
    private int mTouchSlop;

    private void setControlVisible(boolean visible) {
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseSupportActivity) {
            ((BaseSupportActivity) activity).setControlBarVisibleAnimate(visible);
        }
    }

    private PopupMenu mPopupMenu;

    protected AbsStatusesFragment() {
        mStatusesBusCallback = createMessageBusCallback();
    }

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
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateStatusesLoader(getActivity(), args, fromUser);
    }

    @Override
    public final void onLoadFinished(Loader<Data> loader, Data data) {
        setRefreshing(false);
        final SharedPreferences preferences = getSharedPreferences();
        final boolean readFromBottom = preferences.getBoolean(KEY_READ_FROM_BOTTOM, false);
        final long lastReadId;
        final int lastVisiblePos, lastVisibleTop;
        if (readFromBottom) {
            lastVisiblePos = mLayoutManager.findLastVisibleItemPosition();
        } else {
            lastVisiblePos = mLayoutManager.findFirstVisibleItemPosition();
        }
        if (lastVisiblePos != -1) {
            lastReadId = mAdapter.getItemId(lastVisiblePos);
            if (readFromBottom) {
                lastVisibleTop = mLayoutManager.getChildAt(mLayoutManager.getChildCount() - 1).getTop();
            } else {
                lastVisibleTop = mLayoutManager.getChildAt(0).getTop();
            }
        } else {
            lastReadId = -1;
            lastVisibleTop = 0;
        }
        mAdapter.setData(data);
        if (!(data instanceof IExtendedLoader) || ((IExtendedLoader) data).isFromUser()) {
            mAdapter.setLoadMoreIndicatorEnabled(hasMoreData(data));
            int pos = -1;
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                if (lastReadId == mAdapter.getItemId(i)) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1 && mAdapter.isStatus(pos) && (readFromBottom || lastVisiblePos != 0)) {
                mLayoutManager.scrollToPositionWithOffset(pos, lastVisibleTop - mLayoutManager.getPaddingTop());
            }
        }
        if (data instanceof IExtendedLoader) {
            ((IExtendedLoader) data).setFromUser(false);
        }
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
    }

    public abstract Loader<Data> onCreateStatusesLoader(final Context context, final Bundle args,
                                                        final boolean fromUser);

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
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mDrawerCallback = new SimpleDrawerCallback(mRecyclerView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ThemeUtils.getUserAccentColor(context));
        final int backgroundColor = ThemeUtils.getThemeBackgroundColor(context);
        final int colorRes = ColorUtils.getContrastYIQ(backgroundColor,
                R.color.bg_refresh_progress_color_light, R.color.bg_refresh_progress_color_dark);
        mSwipeRefreshLayout.setProgressBackgroundColor(colorRes);
        mAdapter = onCreateAdapter(context, compact);
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        if (compact) {
            mRecyclerView.addItemDecoration(new DividerItemDecoration(context, mLayoutManager.getOrientation()));
        }
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(mOnScrollListener);
        mAdapter.setListener(this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
        setListShown(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.register(mStatusesBusCallback);
    }

    @Override
    public void onStop() {
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(mStatusesBusCallback);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        super.onDestroyView();
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
    public void onStatusActionClick(StatusViewHolder holder, int id, int position) {
        final ParcelableStatus status = mAdapter.getStatus(position);
        if (status == null) return;
        switch (id) {
            case R.id.reply_count: {
                final Context context = getActivity();
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                intent.setPackage(context.getPackageName());
                intent.putExtra(EXTRA_STATUS, status);
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
                    //spice
                    SpiceProfilingUtil.profile(getActivity(), status.account_id, status.id + ",Unfavor," + status.account_id + "," + status.user_id + "," + status.timestamp);
                    SpiceProfilingUtil.log(getActivity(),status.id + ",Unfavor,"  + status.account_id + "," + status.user_id  + "," + status.timestamp);
                    //end
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                    //spice
                    SpiceProfilingUtil.profile(getActivity(),status.account_id, status.id + ",Favor," + status.account_id + "," + status.user_id + "," + status.timestamp);
                    SpiceProfilingUtil.log(getActivity(),status.id + ",Favor,"  + status.account_id + "," + status.user_id  + "," + status.timestamp);
                    //end
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
    public void onStatusMenuClick(StatusViewHolder holder, View menuView, int position) {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        final PopupMenu popupMenu = new PopupMenu(mAdapter.getContext(), menuView,
                Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
        popupMenu.inflate(R.menu.action_status);
        setMenuForStatus(mAdapter.getContext(), popupMenu.getMenu(), mAdapter.getStatus(position));
        popupMenu.show();
        mPopupMenu = popupMenu;
    }

    @Override
    public void onRefresh() {
        triggerRefresh();
    }

    @Override
    public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mContentView = view.findViewById(R.id.fragment_content);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        mRecyclerView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        float density = getResources().getDisplayMetrics().density;
        // 40: SwipeRefreshLayout.CIRCLE_DIAMETER
        final int swipeStart = insets.top - Math.round(40 * density);
        // 64: SwipeRefreshLayout.DEFAULT_CIRCLE_TARGET
        final int swipeDistance = Math.round(64 * density);
        mSwipeRefreshLayout.setProgressViewOffset(false, swipeStart, swipeStart + swipeDistance);
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

    protected Object createMessageBusCallback() {
        return new StatusesBusCallback();
    }

    protected abstract long[] getAccountIds();

    protected Data getAdapterData() {
        return mAdapter.getData();
    }

    protected void setAdapterData(Data data) {
        mAdapter.setData(data);
    }

    protected abstract boolean hasMoreData(Data data);

    protected abstract AbsStatusesAdapter<Data> onCreateAdapter(Context context, boolean compact);

    protected abstract void onLoadMoreStatuses();

    private void setListShown(boolean shown) {
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(shown ? View.VISIBLE : View.GONE);
    }


    protected final class StatusesBusCallback {

        protected StatusesBusCallback() {
        }

        @Subscribe
        public void notifyStatusListChanged(StatusListChangedEvent event) {
            mAdapter.notifyDataSetChanged();
        }

    }
}
