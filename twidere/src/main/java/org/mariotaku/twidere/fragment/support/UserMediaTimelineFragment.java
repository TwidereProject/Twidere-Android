package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter;
import org.mariotaku.twidere.loader.support.MediaTimelineLoader;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.SimpleDrawerCallback;
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;
import org.mariotaku.twidere.view.MediaSizeImageView;

import java.util.List;

/**
 * Created by mariotaku on 14/11/5.
 */
public class UserMediaTimelineFragment extends BaseSupportFragment
        implements LoaderCallbacks<List<ParcelableStatus>>, DrawerCallback {

    private View mProgressContainer;
    private RecyclerView mRecyclerView;

    private MediaTimelineAdapter mAdapter;

    @Override
    public void fling(float velocity) {
        mDrawerCallback.fling(velocity);
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

    private SimpleDrawerCallback mDrawerCallback;

    private final OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        mAdapter = new MediaTimelineAdapter(context);
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mDrawerCallback = new SimpleDrawerCallback(mRecyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(mOnScrollListener);
        getLoaderManager().initLoader(0, getArguments(), this);
        setListShown(false);
    }

    public void setListShown(boolean shown) {
        mRecyclerView.setVisibility(shown ? View.VISIBLE : View.GONE);
        mProgressContainer.setVisibility(shown ? View.GONE : View.VISIBLE);
    }

    public int getStatuses(final long maxId, final long sinceId) {
        final Bundle args = new Bundle(getArguments());
        args.putLong(EXTRA_MAX_ID, maxId);
        args.putLong(EXTRA_SINCE_ID, sinceId);
        getLoaderManager().restartLoader(0, args, this);
        return -1;
    }

    @Override
    public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mProgressContainer = view.findViewById(R.id.progress_container);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        if (mRecyclerView != null) {
            mRecyclerView.setClipToPadding(false);
            mRecyclerView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
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
        return new MediaTimelineLoader(context, accountId, userId, screenName, sinceId, maxId, null,
                null, tabPosition, true);
    }

    @Override
    public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
        mAdapter.setData(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
        mAdapter.setData(null);
    }

    private static class MediaTimelineAdapter extends BaseRecyclerViewAdapter<MediaTimelineViewHolder> {

        private final LayoutInflater mInflater;
        private final MediaLoadingHandler mLoadingHandler;
        private List<ParcelableStatus> mData;

        MediaTimelineAdapter(Context context) {
            super(context);
            mInflater = LayoutInflater.from(context);
            mLoadingHandler = new MediaLoadingHandler(R.id.media_image_progress);
        }

        @Override
        public MediaTimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.adapter_item_media_status, parent, false);
            return new MediaTimelineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MediaTimelineViewHolder holder, int position) {
            if (mData == null) return;
            holder.setMedia(mMediaLoader, mLoadingHandler, mData.get(position));
        }

        public void setData(List<ParcelableStatus> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            if (mData == null) return 0;
            return mData.size();
        }
    }

    private static class MediaTimelineViewHolder extends ViewHolder {

        private final MediaSizeImageView mediaImageView;
        private final ImageView mediaProfileImageView;
        private final TextView mediaTextView;

        public MediaTimelineViewHolder(View itemView) {
            super(itemView);
            mediaImageView = (MediaSizeImageView) itemView.findViewById(R.id.media_image);
            mediaProfileImageView = (ImageView) itemView.findViewById(R.id.media_profile_image);
            mediaTextView = (TextView) itemView.findViewById(R.id.media_text);
        }

        public void setMedia(MediaLoaderWrapper loader, MediaLoadingHandler loadingHandler, ParcelableStatus status) {
            final ParcelableMedia[] media = status.media;
            if (media == null || media.length < 1) return;
            final ParcelableMedia firstMedia = media[0];
            if (status.text_plain.codePointCount(0, status.text_plain.length()) == firstMedia.end) {
                mediaTextView.setText(status.text_unescaped.substring(0, firstMedia.start));
            } else {
                mediaTextView.setText(status.text_unescaped);
            }
            loader.displayProfileImage(mediaProfileImageView, status.user_profile_image_url);
            mediaImageView.setMediaSize(firstMedia.width, firstMedia.height);
            loader.displayPreviewImageWithCredentials(mediaImageView, firstMedia.media_url, status.account_id, loadingHandler);
        }
    }
}
