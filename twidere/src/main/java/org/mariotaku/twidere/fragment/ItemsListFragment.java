package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.adapter.VariousItemsAdapter;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;

import java.util.List;

/**
 * Created by mariotaku on 16/3/20.
 */
public class ItemsListFragment extends AbsContentListRecyclerViewFragment<VariousItemsAdapter>
        implements LoaderCallbacks<List<?>> {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        setRefreshEnabled(false);
        showContent();
    }

    @NonNull
    @Override
    protected VariousItemsAdapter onCreateAdapter(Context context, boolean compact) {
        return new VariousItemsAdapter(context, compact);
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
