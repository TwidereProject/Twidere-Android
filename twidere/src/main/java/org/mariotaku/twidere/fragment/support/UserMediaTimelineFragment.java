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

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter;
import org.mariotaku.twidere.adapter.StaggeredGridParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.loader.support.MediaTimelineLoader;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 14/11/5.
 */
public class UserMediaTimelineFragment extends AbsContentRecyclerViewFragment<StaggeredGridParcelableStatusesAdapter, StaggeredGridLayoutManager>
        implements LoaderCallbacks<List<ParcelableStatus>>, DrawerCallback, AbsStatusesAdapter.StatusAdapterListener {


    @Override
    protected void scrollToPositionWithOffset(int position, int offset) {
        getLayoutManager().scrollToPositionWithOffset(position, offset);
    }


    @Override
    public boolean isRefreshing() {
        return getLoaderManager().hasRunningLoaders();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final AbsStatusesAdapter<List<ParcelableStatus>> adapter = getAdapter();
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


    public int getStatuses(final long maxId, final long sinceId) {
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_MAKE_GAP, false);
        args.putLong(EXTRA_MAX_ID, maxId);
        args.putLong(EXTRA_SINCE_ID, sinceId);
        args.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().restartLoader(0, args, this);
        return -1;
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
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        return new MediaTimelineLoader(context, accountId, userId, screenName, sinceId, maxId,
                getAdapter().getData(), null, tabPosition, fromUser);
    }

    @Override
    public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
        final StaggeredGridParcelableStatusesAdapter adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupported(hasMoreData(data));
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        showContent();
        setLoadMoreIndicatorVisible(false);
    }

    private boolean hasMoreData(List<ParcelableStatus> data) {
        return true;
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
        getAdapter().setData(null);
    }

    @Override
    public int[] findLastVisibleItemPositions() {
        return getLayoutManager().findLastVisibleItemPositions(null);
    }

    @Override
    public int[] findFirstVisibleItemPositions() {
        return getLayoutManager().findFirstVisibleItemPositions(null);
    }

    @Override
    public int getItemCount() {
        return getLayoutManager().getItemCount();
    }

    @Override
    public void onLoadMoreContents(boolean fromStart) {
        if (fromStart) return;
        //noinspection ConstantConditions
        super.onLoadMoreContents(fromStart);
        final IStatusesAdapter<List<ParcelableStatus>> adapter = getAdapter();
        final long maxId = adapter.getStatusId(adapter.getStatusesCount() - 1);
        getStatuses(maxId, -1);
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {

    }

    @Override
    public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int position) {

    }

    @Override
    public void onStatusActionClick(IStatusViewHolder holder, int id, int position) {

    }

    @Override
    public void onStatusClick(IStatusViewHolder holder, int position) {
        Utils.openStatus(getContext(), getAdapter().getStatus(position), null);
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
