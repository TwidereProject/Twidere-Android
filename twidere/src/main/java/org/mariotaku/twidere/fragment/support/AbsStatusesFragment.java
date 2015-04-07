package org.mariotaku.twidere.fragment.support;

import android.app.Activity;
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
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IControlBarActivity;
import org.mariotaku.twidere.activity.iface.IControlBarActivity.ControlBarOffsetListener;
import org.mariotaku.twidere.activity.support.BaseActionBarActivity;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter.StatusAdapterListener;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ColorUtils;
import org.mariotaku.twidere.util.ContentListScrollListener;
import org.mariotaku.twidere.util.ContentListScrollListener.ContentListSupport;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SimpleDrawerCallback;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.message.StatusListChangedEvent;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;
import edu.tsinghua.spice.Utilies.TypeMappingUtil;

import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

/**
 * Created by mariotaku on 14/11/5.
 */
public abstract class AbsStatusesFragment<Data> extends BaseSupportFragment implements LoaderCallbacks<Data>,
        OnRefreshListener, DrawerCallback, RefreshScrollTopInterface, StatusAdapterListener,
        ControlBarOffsetListener, ContentListSupport {

    private final Object mStatusesBusCallback;
    private AbsStatusesAdapter<Data> mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences mPreferences;
    private View mProgressContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private SimpleDrawerCallback mDrawerCallback;
    private Rect mSystemWindowsInsets = new Rect();
    private int mControlBarOffsetPixels;
    private PopupMenu mPopupMenu;
    private ReadStateManager mReadStateManager;
    private ParcelableStatus mSelectedStatus;
    private OnMenuItemClickListener mOnStatusMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final ParcelableStatus status = mSelectedStatus;
            if (status == null) return false;
            if (item.getItemId() == MENU_SHARE) {
                final Intent shareIntent = Utils.createStatusShareIntent(getActivity(), status);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_status)));
                return true;
            }
            return Utils.handleMenuItemClick(getActivity(), AbsStatusesFragment.this,
                    getFragmentManager(), getTwitterWrapper(), status, item);
        }
    };

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

    public SharedPreferences getSharedPreferences() {
        if (mPreferences != null) return mPreferences;
        return mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public abstract int getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds);

    public abstract boolean isRefreshing();

    public AbsStatusesAdapter<Data> getAdapter() {
        return mAdapter;
    }

    public void setControlVisible(boolean visible) {
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActionBarActivity) {
            ((BaseActionBarActivity) activity).setControlBarVisibleAnimate(visible);
        }
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing == mSwipeRefreshLayout.isRefreshing()) return;
//        if (!refreshing) updateRefreshProgressOffset();
        mSwipeRefreshLayout.setRefreshing(refreshing && !mAdapter.isLoadMoreIndicatorVisible());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).registerControlBarOffsetListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mReadStateManager = getReadStateManager();
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        final boolean compact = Utils.isCompactCards(context);
        mDrawerCallback = new SimpleDrawerCallback(mRecyclerView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(ThemeUtils.getUserAccentColor(context));
        final int backgroundColor = ThemeUtils.getThemeBackgroundColor(context);
        final int colorRes = ColorUtils.getContrastYIQ(backgroundColor,
                R.color.bg_refresh_progress_color_light, R.color.bg_refresh_progress_color_dark);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(colorRes);
        mAdapter = onCreateAdapter(context, compact);
        mLayoutManager = new FixedLinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        if (compact) {
            mRecyclerView.addItemDecoration(new DividerItemDecoration(context, mLayoutManager.getOrientation()));
        }
        mRecyclerView.setAdapter(mAdapter);

        final ContentListScrollListener scrollListener = new ContentListScrollListener(this);
        scrollListener.setTouchSlop(ViewConfiguration.get(context).getScaledTouchSlop());
        scrollListener.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    saveReadPosition();
                }
            }
        });
        mRecyclerView.setOnScrollListener(scrollListener);
        mAdapter.setListener(this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
        setListShown(false);
    }

    @Override
    public void onLoadMoreContents() {
        setLoadMoreIndicatorVisible(true);
        setRefreshEnabled(false);
    }

    public void setLoadMoreIndicatorVisible(boolean visible) {
        mAdapter.setLoadMoreIndicatorVisible(visible);
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
    public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    public void onDetach() {
        final FragmentActivity activity = getActivity();
        if (activity instanceof IControlBarActivity) {
            ((IControlBarActivity) activity).unregisterControlBarOffsetListener(this);
        }
        super.onDetach();
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        mRecyclerView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        mProgressContainer.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        mSystemWindowsInsets.set(insets);
        updateRefreshProgressOffset();
    }

    @Override
    public void onControlBarOffsetChanged(IControlBarActivity activity, float offset) {
        mControlBarOffsetPixels = Math.round(activity.getControlBarHeight() * (1 - offset));
        updateRefreshProgressOffset();
    }

    @Override
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateStatusesLoader(getActivity(), args, fromUser);
    }

    @Override
    public final void onLoadFinished(Loader<Data> loader, Data data) {
        final SharedPreferences preferences = getSharedPreferences();
        final boolean rememberPosition = preferences.getBoolean(KEY_REMEMBER_POSITION, false);
        final boolean readFromBottom = preferences.getBoolean(KEY_READ_FROM_BOTTOM, false);
        final long lastReadId;
        final int lastVisiblePos, lastVisibleTop;
        final String tag = getCurrentReadPositionTag();
        if (readFromBottom) {
            lastVisiblePos = mLayoutManager.findLastVisibleItemPosition();
        } else {
            lastVisiblePos = mLayoutManager.findFirstVisibleItemPosition();
        }
        if (lastVisiblePos != -1) {
            lastReadId = mAdapter.getStatusId(lastVisiblePos);
            if (readFromBottom) {
                lastVisibleTop = mLayoutManager.getChildAt(mLayoutManager.getChildCount() - 1).getTop();
            } else {
                lastVisibleTop = mLayoutManager.getChildAt(0).getTop();
            }
        } else if (rememberPosition && tag != null) {
            lastReadId = mReadStateManager.getPosition(tag);
            lastVisibleTop = 0;
        } else {
            lastReadId = -1;
            lastVisibleTop = 0;
        }
        mAdapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            mAdapter.setLoadMoreSupported(hasMoreData(data));
            int pos = -1;
            for (int i = 0, j = mAdapter.getItemCount(); i < j; i++) {
                if (lastReadId != -1 && lastReadId == mAdapter.getStatusId(i)) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1 && mAdapter.isStatus(pos) && (readFromBottom || lastVisiblePos != 0)) {
                mLayoutManager.scrollToPositionWithOffset(pos, lastVisibleTop);
            }
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        setListShown(true);
        onLoadingFinished();
    }

    protected abstract void onLoadingFinished();

    public void setRefreshEnabled(boolean enabled) {
        mSwipeRefreshLayout.setEnabled(enabled);
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    public abstract Loader<Data> onCreateStatusesLoader(final Context context, final Bundle args,
                                                        final boolean fromUser);

    @Override
    public void onGapClick(GapViewHolder holder, int position) {
        final ParcelableStatus status = mAdapter.getStatus(position);
        final long sinceId = position + 1 < mAdapter.getStatusesCount() ? mAdapter.getStatus(position + 1).id : -1;
        final long[] accountIds = {status.account_id};
        final long[] maxIds = {status.id};
        final long[] sinceIds = {sinceId};
        getStatuses(accountIds, maxIds, sinceIds);
    }

    @Override
    public void onStatusActionClick(StatusViewHolder holder, int id, int position) {
        final ParcelableStatus status = mAdapter.getStatus(position);
        if (status == null) return;
        final FragmentActivity activity = getActivity();
        switch (id) {
            case R.id.reply_count: {
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                intent.setPackage(activity.getPackageName());
                intent.putExtra(EXTRA_STATUS, status);
                activity.startActivity(intent);
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
                    SpiceProfilingUtil.profile(activity, status.account_id, status.id + ",Unfavor,"
                            + status.account_id + "," + status.user_id + "," + status.reply_count
                            + "," + status.retweet_count + "," + status.favorite_count + "," + status.timestamp);
                    SpiceProfilingUtil.log(activity, status.id + ",Unfavor,"
                            + status.account_id + "," + status.user_id + "," + status.reply_count
                            + "," + status.retweet_count + "," + status.favorite_count + "," + status.timestamp);
                    //end
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                    //spice
                    SpiceProfilingUtil.profile(activity, status.account_id, status.id + ",Favor,"
                            + status.account_id + "," + status.user_id + "," + status.reply_count
                            + "," + status.retweet_count + "," + status.favorite_count + "," + status.timestamp);
                    SpiceProfilingUtil.log(activity, status.id + ",Favor,"
                            + status.account_id + "," + status.user_id + "," + status.reply_count
                            + "," + status.retweet_count + "," + status.favorite_count + "," + status.timestamp);
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
        popupMenu.setOnMenuItemClickListener(mOnStatusMenuItemClickListener);
        popupMenu.inflate(R.menu.action_status);
        final ParcelableStatus status = mAdapter.getStatus(position);
        setMenuForStatus(mAdapter.getContext(), popupMenu.getMenu(), status);
        popupMenu.show();
        mPopupMenu = popupMenu;
        mSelectedStatus = status;
    }

    @Override
    public void onRefresh() {
        triggerRefresh();
    }

    @Override
    public boolean scrollToStart() {
        saveReadPosition();
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

    protected String getReadPositionTag() {
        return null;
    }

    protected abstract boolean hasMoreData(Data data);

    protected abstract AbsStatusesAdapter<Data> onCreateAdapter(Context context, boolean compact);

    protected void saveReadPosition() {
        final String readPositionTag = getReadPositionTagWithAccounts();
        if (readPositionTag == null) return;
        final int position = mLayoutManager.findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) return;
        final ParcelableStatus status = mAdapter.getStatus(position);
        if (status == null) return;
        mReadStateManager.setPosition(readPositionTag, status.id);
        mReadStateManager.setPosition(getCurrentReadPositionTag(), status.id, true);
    }

    private String getCurrentReadPositionTag() {
        final String tag = getReadPositionTagWithAccounts();
        if (tag == null) return null;
        return tag + "_current";
    }

    private String getReadPositionTagWithAccounts() {
        return Utils.getReadPositionTagWithAccounts(getReadPositionTag(), getAccountIds());
    }

    private void setListShown(boolean shown) {
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
        mSwipeRefreshLayout.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMediaClick(StatusViewHolder holder, ParcelableMedia media, int position) {
        final ParcelableStatus status = mAdapter.getStatus(position);
        if (status == null) return;
        Utils.openMedia(getActivity(), status, media);
        //spice
        SpiceProfilingUtil.log(getActivity(),
                status.id + ",Clicked," + status.account_id + "," + status.user_id + "," + status.text_plain.length()
                        + "," + media.media_url + "," + TypeMappingUtil.getMediaType(media.type)
                        + "," + mAdapter.isMediaPreviewEnabled() + "," + status.timestamp);
        SpiceProfilingUtil.profile(getActivity(), status.account_id,
                status.id + ",Clicked," + status.account_id + "," + status.user_id + "," + status.text_plain.length()
                        + "," + media.media_url + "," + TypeMappingUtil.getMediaType(media.type)
                        + "," + mAdapter.isMediaPreviewEnabled() + "," + status.timestamp);
        //end
    }

    private void updateRefreshProgressOffset() {
        if (mSystemWindowsInsets.top == 0 || mSwipeRefreshLayout == null || isRefreshing()) return;
        final float density = getResources().getDisplayMetrics().density;
        final int progressCircleDiameter = mSwipeRefreshLayout.getProgressCircleDiameter();
        final int swipeStart = (mSystemWindowsInsets.top - mControlBarOffsetPixels) - progressCircleDiameter;
        // 64: SwipeRefreshLayout.DEFAULT_CIRCLE_TARGET
        final int swipeDistance = Math.round(64 * density);
        mSwipeRefreshLayout.setProgressViewOffset(true, swipeStart, swipeStart + swipeDistance);
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
