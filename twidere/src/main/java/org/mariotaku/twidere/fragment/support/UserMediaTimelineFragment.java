package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.StaggeredGridParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.loader.MediaTimelineLoader;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 14/11/5.
 */
public class UserMediaTimelineFragment extends AbsContentRecyclerViewFragment<StaggeredGridParcelableStatusesAdapter, StaggeredGridLayoutManager>
        implements LoaderCallbacks<List<ParcelableStatus>>, DrawerCallback, ParcelableStatusesAdapter.StatusAdapterListener {


    @Override
    protected void scrollToPositionWithOffset(int position, int offset) {
        getLayoutManager().scrollToPositionWithOffset(position, offset);
    }


    @Override
    public boolean isRefreshing() {
        if (getContext() == null || isDetached()) return false;
        return getLoaderManager().hasRunningLoaders();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ParcelableStatusesAdapter adapter = getAdapter();
        adapter.setListener(this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
        showProgress();
    }

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {

    }

    @NonNull
    @Override
    protected StaggeredGridLayoutManager onCreateLayoutManager(Context context) {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }


    public int getStatuses(final String maxId, final String sinceId) {
        if (getContext() == null) return -1;
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_MAKE_GAP, false);
        args.putString(EXTRA_MAX_ID, maxId);
        args.putString(EXTRA_SINCE_ID, sinceId);
        args.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().restartLoader(0, args, this);
        return 0;
    }


    @NonNull
    @Override
    protected StaggeredGridParcelableStatusesAdapter onCreateAdapter(Context context, boolean compact) {
        return new StaggeredGridParcelableStatusesAdapter(context, compact);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_recyclerview, container, false);
    }

    @Override
    public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
        final Context context = getActivity();
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String maxId = args.getString(EXTRA_MAX_ID);
        final String sinceId = args.getString(EXTRA_SINCE_ID);
        final String userId = args.getString(EXTRA_USER_ID);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        final boolean loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false);
        return new MediaTimelineLoader(context, accountKey, userId, screenName, sinceId, maxId,
                getAdapter().getData(), null, tabPosition, fromUser, loadingMore);
    }

    @Override
    public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
        final StaggeredGridParcelableStatusesAdapter adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupportedPosition(hasMoreData(data) ? IndicatorPosition.END : IndicatorPosition.NONE);
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        showContent();
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }

    private boolean hasMoreData(List<ParcelableStatus> data) {
        return true;
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
        getAdapter().setData(null);
    }

    @Override
    public boolean isReachingEnd() {
        final StaggeredGridLayoutManager lm = getLayoutManager();
        return ArrayUtils.contains(lm.findLastCompletelyVisibleItemPositions(null), lm.getItemCount() - 1);
    }

    @Override
    public boolean isReachingStart() {
        final StaggeredGridLayoutManager lm = getLayoutManager();
        return ArrayUtils.contains(lm.findFirstCompletelyVisibleItemPositions(null), 0);
    }

    @Override
    public void onLoadMoreContents(int position) {
        // Only supports load from end, skip START flag
        if ((position & IndicatorPosition.START) != 0) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        final IStatusesAdapter<List<ParcelableStatus>> adapter = getAdapter();
        final String maxId = adapter.getStatusId(adapter.getStatusCount() - 1);
        getStatuses(maxId, null);
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {

    }

    @Override
    public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {

    }

    @Override
    public void onStatusActionClick(IStatusViewHolder holder, int id, int position) {

    }

    @Override
    public void onStatusClick(IStatusViewHolder holder, int position) {
        IntentUtils.openStatus(getContext(), getAdapter().getStatus(position), null);
    }

    @Override
    public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onStatusMenuClick(IStatusViewHolder holder, View menuView, int position) {

    }

    @Override
    public void onUserProfileClick(IStatusViewHolder holder, ParcelableStatus status, int position) {

    }
}
