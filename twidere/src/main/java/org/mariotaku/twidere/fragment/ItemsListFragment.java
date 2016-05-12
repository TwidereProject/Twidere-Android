package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.DummyItemAdapter;
import org.mariotaku.twidere.adapter.VariousItemsAdapter;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ExtendedRecyclerView;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.UserViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import java.util.List;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.MediaEvent;
import edu.tsinghua.hotmobi.model.TimelineType;

/**
 * Created by mariotaku on 16/3/20.
 */
public class ItemsListFragment extends AbsContentListRecyclerViewFragment<VariousItemsAdapter>
        implements LoaderCallbacks<List<?>> {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getRecyclerView());
        getLoaderManager().initLoader(0, null, this);
        setRefreshEnabled(false);
        showContent();
    }

    @NonNull
    @Override
    protected VariousItemsAdapter onCreateAdapter(Context context, boolean compact) {
        final VariousItemsAdapter adapter = new VariousItemsAdapter(context, compact);
        final DummyItemAdapter dummyItemAdapter = adapter.getDummyAdapter();
        dummyItemAdapter.setStatusClickListener(new IStatusViewHolder.SimpleStatusClickListener() {
            @Override
            public void onStatusClick(IStatusViewHolder holder, int position) {
                final ParcelableStatus status = dummyItemAdapter.getStatus(position);
                if (status == null) return;
                IntentUtils.openStatus(getContext(), status, null);
            }

            @Override
            public void onItemActionClick(RecyclerView.ViewHolder holder, int id, int position) {
                final Context context = getContext();
                if (context == null) return;
                final ParcelableStatus status = dummyItemAdapter.getStatus(position);
                if (status == null) return;
                AbsStatusesFragment.handleStatusActionClick(context, getFragmentManager(),
                        mTwitterWrapper, (StatusViewHolder) holder, status, id);
            }

            @Override
            public void onItemMenuClick(RecyclerView.ViewHolder holder, View menuView, int position) {
                if (getActivity() == null) return;
                final View view = getLayoutManager().findViewByPosition(position);
                if (view == null) return;
                getRecyclerView().showContextMenuForChild(view);
            }

            @Override
            public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {
                final ParcelableStatus status = dummyItemAdapter.getStatus(statusPosition);
                if (status == null || media == null) return;
                IntentUtils.openMedia(getActivity(), status, media, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API));
                // BEGIN HotMobi
                final MediaEvent event = MediaEvent.create(getActivity(), status, media,
                        TimelineType.OTHER, dummyItemAdapter.isMediaPreviewEnabled());
                HotMobiLogger.getInstance(getActivity()).log(status.account_key, event);
                // END HotMobi
            }

            @Override
            public void onUserProfileClick(IStatusViewHolder holder, int position) {
                final FragmentActivity activity = getActivity();
                final ParcelableStatus status = dummyItemAdapter.getStatus(position);
                if (status == null) return;
                IntentUtils.openUserProfile(activity, status.account_key, status.user_key,
                        status.user_screen_name, null, mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        UserFragment.Referral.TIMELINE_STATUS);
            }
        });
        dummyItemAdapter.setUserClickListener(new IUsersAdapter.SimpleUserClickListener() {
            @Override
            public void onUserClick(UserViewHolder holder, int position) {
                final ParcelableUser user = dummyItemAdapter.getUser(position);
                if (user == null) return;
                IntentUtils.openUserProfile(getContext(), user, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        UserFragment.Referral.TIMELINE_STATUS);
            }
        });
        return adapter;
    }

    @Override
    public Loader<List<?>> onCreateLoader(int id, Bundle args) {
        return new ItemsLoader(getContext(), getArguments());
    }

    @Override
    public void onLoadFinished(Loader<List<?>> loader, List<?> data) {
        getAdapter().setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<?>> loader) {
        getAdapter().setData(null);
    }

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {
        if (compact) {
            super.setupRecyclerView(context, true);
            return;
        }
        final RecyclerView recyclerView = getRecyclerView();
        final VariousItemsAdapter adapter = getAdapter();
        // Dividers are drawn on bottom of view
        recyclerView.addItemDecoration(new DividerItemDecoration(context, getLayoutManager().getOrientation()) {

            @Override
            protected boolean isDividerEnabled(int childPos) {
                // Don't draw for last item
                if (childPos == RecyclerView.NO_POSITION || childPos == adapter.getItemCount() - 1) {
                    return false;
                }
                final int itemViewType = adapter.getItemViewType(childPos);
                // Draw only if current item and next item is TITLE_SUMMARY
                if (shouldUseDividerFor(itemViewType)) {
                    if (shouldUseDividerFor(adapter.getItemViewType(childPos + 1))) {
                        return true;
                    }
                }
                return false;
            }

            private boolean shouldUseDividerFor(int itemViewType) {
                switch (itemViewType) {
                    case VariousItemsAdapter.VIEW_TYPE_USER:
                    case VariousItemsAdapter.VIEW_TYPE_USER_LIST:
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (!getUserVisibleHint() || menuInfo == null) return;
        final MenuInflater inflater = new MenuInflater(getContext());
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) menuInfo;
        final int position = contextMenuInfo.getPosition();
        final VariousItemsAdapter adapter = getAdapter();
        switch (adapter.getItemViewType(position)) {
            case VariousItemsAdapter.VIEW_TYPE_STATUS: {
                final DummyItemAdapter dummyAdapter = getAdapter().getDummyAdapter();
                final ParcelableStatus status = dummyAdapter.getStatus(contextMenuInfo.getPosition());
                if (status == null) break;
                inflater.inflate(R.menu.action_status, menu);
                MenuUtils.setupForStatus(getContext(), mPreferences, menu, status,
                        mTwitterWrapper);
                break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!getUserVisibleHint()) return false;
        final ExtendedRecyclerView.ContextMenuInfo contextMenuInfo =
                (ExtendedRecyclerView.ContextMenuInfo) item.getMenuInfo();
        final int position = contextMenuInfo.getPosition();
        final VariousItemsAdapter adapter = getAdapter();
        switch (adapter.getItemViewType(position)) {
            case VariousItemsAdapter.VIEW_TYPE_STATUS: {
                final DummyItemAdapter dummyAdapter = adapter.getDummyAdapter();
                final ParcelableStatus status = dummyAdapter.getStatus(position);
                if (status == null) return false;
                if (item.getItemId() == R.id.share) {
                    final Intent shareIntent = Utils.createStatusShareIntent(getActivity(), status);
                    final Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_status));
                    Utils.addCopyLinkIntent(getContext(), chooser, LinkCreator.getStatusWebLink(status));
                    startActivity(chooser);
                    return true;
                }
                return MenuUtils.handleStatusClick(getActivity(), this, getFragmentManager(),
                        mUserColorNameManager, mTwitterWrapper, status, item);
            }
        }
        return false;
    }

    public static class ItemsLoader extends AsyncTaskLoader<List<?>> {
        private final Bundle mArguments;

        public ItemsLoader(Context context, Bundle args) {
            super(context);
            mArguments = args;
        }

        @Override
        public List<?> loadInBackground() {
            return mArguments.<Parcelable>getParcelableArrayList(EXTRA_ITEMS);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }
}
