package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.StatusFragment;
import org.mariotaku.twidere.fragment.support.StatusMenuDialogFragment;
import org.mariotaku.twidere.fragment.support.UserFragment;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 14/11/19.
 */
public abstract class AbsStatusesAdapter<D> extends Adapter<ViewHolder> implements Constants,
        IStatusesAdapter<D> {

    private static final int ITEM_VIEW_TYPE_STATUS = 1;
    private static final int ITEM_VIEW_TYPE_GAP = 2;
    private static final int ITEM_VIEW_TYPE_LOAD_INDICATOR = 3;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ImageLoaderWrapper mImageLoader;
    private final ImageLoadingHandler mLoadingHandler;
    private final int mCardLayoutResource;
    private boolean mLoadMoreIndicatorEnabled;

    public AbsStatusesAdapter(Context context, boolean compact) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
        mLoadingHandler = new ImageLoadingHandler(R.id.media_preview_progress);
        if (compact) {
            mCardLayoutResource = R.layout.card_item_list_status_compat;
        } else {
            mCardLayoutResource = R.layout.card_item_list_status;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_STATUS: {
                final View view = mInflater.inflate(mCardLayoutResource, parent, false);
                return new StatusViewHolder<>(this, view);
            }
            case ITEM_VIEW_TYPE_GAP: {
                final View view = mInflater.inflate(R.layout.card_item_gap, parent, false);
                return new GapViewHolder(this, view);
            }
            case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
                return new LoadIndicatorViewHolder(view);
            }
        }
        throw new IllegalStateException("Unknown view type " + viewType);
    }

    public void setLoadMoreIndicatorEnabled(boolean enabled) {
        if (mLoadMoreIndicatorEnabled == enabled) return;
        mLoadMoreIndicatorEnabled = enabled;
        notifyDataSetChanged();
    }

    public boolean hasLoadMoreIndicator() {
        return mLoadMoreIndicatorEnabled;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_STATUS: {
                bindStatus(((StatusViewHolder) holder), position);
                break;
            }
        }
    }

    protected abstract void bindStatus(StatusViewHolder holder, int position);

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        return ITEM_VIEW_TYPE_STATUS;
    }

    @Override
    public final int getItemCount() {
        return getStatusCount() + (mLoadMoreIndicatorEnabled ? 1 : 0);
    }

    @Override
    public ImageLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public ImageLoadingHandler getImageLoadingHandler() {
        return mLoadingHandler;
    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {
        final Context context = getContext();
//        final View cardView = holder.getCardView();
//        if (cardView != null && context instanceof FragmentActivity) {
//            final Bundle options = Utils.makeSceneTransitionOption((FragmentActivity) context,
//                    new Pair<>(cardView, StatusFragment.TRANSITION_NAME_CARD));
//            Utils.openStatus(context, getStatus(position), options);
//        } else {
            Utils.openStatus(context, getStatus(position), null);
//        }
    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, int position) {
        final Context context = getContext();
        final ParcelableStatus status = getStatus(position);
        final View profileImageView = holder.getProfileImageView();
        if (context instanceof FragmentActivity) {
            final Bundle options = Utils.makeSceneTransitionOption((FragmentActivity) context,
                    new Pair<>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE));
            Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name, options);
        } else {
            Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name, null);
        }
    }

    @Override
    public void onItemMenuClick(StatusViewHolder holder, int position) {
        final Context context = getContext();
        if (!(context instanceof FragmentActivity)) return;
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, getStatus(position));
        final StatusMenuDialogFragment f = new StatusMenuDialogFragment();
        f.setArguments(args);
        f.show(((FragmentActivity) context).getSupportFragmentManager(), "status_menu");
    }

    public abstract void setData(D data);

    public abstract D getData();

}
