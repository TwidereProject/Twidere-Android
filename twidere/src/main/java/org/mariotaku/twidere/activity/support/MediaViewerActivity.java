/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.activity.support;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.sprylab.android.widget.TextureVideoView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.support.SupportFixedFragmentStatePagerAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.fragment.support.ViewStatusDialogFragment;
import org.mariotaku.twidere.loader.support.TileImageLoader;
import org.mariotaku.twidere.loader.support.TileImageLoader.DownloadListener;
import org.mariotaku.twidere.loader.support.TileImageLoader.Result;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMedia.VideoInfo.Variant;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.SaveImageTask;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.VideoLoader;
import org.mariotaku.twidere.util.VideoLoader.VideoLoadingListener;

import java.io.File;


public final class MediaViewerActivity extends ThemedActionBarActivity implements Constants, OnPageChangeListener {

    private ViewPager mViewPager;
    private MediaPagerAdapter mAdapter;
    private ActionBar mActionBar;
    private View mMediaStatusContainer;


    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getViewerThemeResource(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setBarVisibility(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mMediaStatusContainer = findViewById(R.id.media_status_container);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_media_viewer);
        mAdapter = new MediaPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.element_spacing_normal));
        mViewPager.setOnPageChangeListener(this);
        final Intent intent = getIntent();
        final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        final ParcelableMedia[] media = Utils.newParcelableArray(intent.getParcelableArrayExtra(EXTRA_MEDIA), ParcelableMedia.CREATOR);
        final ParcelableMedia currentMedia = intent.getParcelableExtra(EXTRA_CURRENT_MEDIA);
        mAdapter.setMedia(accountId, media);
        final int currentIndex = ArrayUtils.indexOf(media, currentMedia);
        if (currentIndex != -1) {
            mViewPager.setCurrentItem(currentIndex, false);
        }
        if (intent.hasExtra(EXTRA_STATUS)) {
            mMediaStatusContainer.setVisibility(View.VISIBLE);
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            final Fragment f = new ViewStatusDialogFragment();
            final Bundle args = new Bundle();
            args.putParcelable(EXTRA_STATUS, intent.getParcelableExtra(EXTRA_STATUS));
            args.putBoolean(EXTRA_SHOW_MEDIA_PREVIEW, false);
            args.putBoolean(EXTRA_SHOW_EXTRA_TYPE, false);
            f.setArguments(args);
            ft.replace(R.id.media_status, f);
            ft.commit();
        } else {
            mMediaStatusContainer.setVisibility(View.GONE);
        }
    }

    private boolean isBarShowing() {
        if (mActionBar == null) return false;
        return mActionBar.isShowing();
    }

    private void setBarVisibility(boolean visible) {
        if (mActionBar == null) return;
        if (visible) {
            mActionBar.show();
        } else {
            mActionBar.hide();
        }

        mMediaStatusContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void toggleBar() {
        setBarVisibility(!isBarShowing());
    }

    public boolean hasStatus() {
        return getIntent().hasExtra(EXTRA_STATUS);
    }

    public static final class VideoPageFragment extends BaseSupportFragment
            implements VideoLoadingListener {

        private static final String[] SUPPORTED_VIDEO_TYPES;

        static {
            SUPPORTED_VIDEO_TYPES = new String[]{"video/mp4"};
        }

        private TextureVideoView mVideoView;
        private VideoLoader mVideoLoader;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_media_page_video, container, false);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mVideoLoader = TwidereApplication.getInstance(getActivity()).getVideoLoader();
            final String url = getBestVideoUrl(getMedia());
            if (url != null) {
                mVideoLoader.loadVideo(url, this);
            }
        }

        private String getBestVideoUrl(ParcelableMedia media) {
            if (media == null || media.video_info == null) return null;
            for (Variant variant : media.video_info.variants) {
                if (ArrayUtils.contains(SUPPORTED_VIDEO_TYPES, variant.content_type)) {
                    return variant.url;
                }
            }
            return null;
        }

        @Override
        public void onBaseViewCreated(View view, Bundle savedInstanceState) {
            super.onBaseViewCreated(view, savedInstanceState);
            mVideoView = (TextureVideoView) view.findViewById(R.id.video_view);
        }

        @Override
        public void onVideoLoadingCancelled(String uri, VideoLoadingListener listener) {

        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
//            if (mVideoView != null && mVideoView.isPlaying()) {
//                mVideoView.pause();
//            }
        }

        @Override
        public void onVideoLoadingComplete(String uri, VideoLoadingListener listener, File file) {
            mVideoView.setVideoURI(Uri.fromFile(file));
            mVideoView.start();
        }

        @Override
        public void onVideoLoadingFailed(String uri, VideoLoadingListener listener, Exception e) {
        }

        @Override
        public void onVideoLoadingProgressUpdate(String uri, VideoLoadingListener listener, int current, int total) {

        }

        private ParcelableMedia getMedia() {
            final Bundle args = getArguments();
            return args.getParcelable(EXTRA_MEDIA);
        }

        @Override
        public void onVideoLoadingStarted(String uri, VideoLoadingListener listener) {

        }
    }

    public static final class ImagePageFragment extends BaseSupportFragment
            implements DownloadListener, LoaderCallbacks<Result>, OnLayoutChangeListener, OnClickListener {

        private SubsamplingScaleImageView mImageView;
        private ProgressWheel mProgressBar;
        private boolean mLoaderInitialized;
        private float mContentLength;
        private SaveImageTask mSaveImageTask;

        @Override
        public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onBaseViewCreated(view, savedInstanceState);
            mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
            mProgressBar = (ProgressWheel) view.findViewById(R.id.progress);
        }

        @Override
        public void onClick(View v) {
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            activity.toggleBar();
        }

        @Override
        public Loader<Result> onCreateLoader(final int id, final Bundle args) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.spin();
            invalidateOptionsMenu();
            final ParcelableMedia media = getMedia();
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            return new TileImageLoader(getActivity(), this, accountId, Uri.parse(media.media_url));
        }

        private ParcelableMedia getMedia() {
            final Bundle args = getArguments();
            return args.getParcelable(EXTRA_MEDIA);
        }

        @Override
        public void onLoadFinished(final Loader<TileImageLoader.Result> loader, final TileImageLoader.Result data) {
            if (data.hasData()) {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setTag(data.file);
                if (data.useDecoder) {
                    mImageView.setImage(ImageSource.uri(Uri.fromFile(data.file)));
                } else if ("image/gif".equals(data.options.outMimeType)) {
//                    try {
//                        final FileDescriptor fd = new RandomAccessFile(data.file, "r").getFD();
//                        mImageView.setImageDrawable(new GifDrawable(fd));
//                    } catch (IOException e) {
//                        mImageView.setImage(null);
//                        mImageView.setTag(null);
//                        mImageView.setVisibility(View.GONE);
//                        Utils.showErrorMessage(getActivity(), null, e, true);
//                    }
                    updateScaleLimit();
                } else {
                    mImageView.setImage(ImageSource.bitmap(data.bitmap));
                    updateScaleLimit();
                }
            } else {
                mImageView.setImage(null);
                mImageView.setTag(null);
                mImageView.setVisibility(View.GONE);
                Utils.showErrorMessage(getActivity(), null, data.exception, true);
            }
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setProgress(0);
            invalidateOptionsMenu();
        }

        @Override
        public void onLoaderReset(final Loader<TileImageLoader.Result> loader) {
//            final Drawable drawable = mImageView.getDrawable();
//            if (drawable instanceof GifDrawable) {
//                ((GifDrawable) drawable).recycle();
//            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_media_page_image, container, false);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            final Object imageTag = mImageView.getTag();
            final boolean isLoading = getLoaderManager().hasRunningLoaders();
            final boolean hasImage = imageTag instanceof File;
            Utils.setMenuItemAvailability(menu, R.id.refresh, !hasImage && !isLoading);
            Utils.setMenuItemAvailability(menu, R.id.share, hasImage && !isLoading);
            Utils.setMenuItemAvailability(menu, R.id.save, hasImage && !isLoading);
            if (hasImage) {
                final MenuItem shareItem = menu.findItem(R.id.share);
                final ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
                final File file = (File) imageTag;
                final Intent intent = new Intent(Intent.ACTION_SEND);
                final Uri fileUri = Uri.fromFile(file);
                intent.setDataAndType(fileUri, Utils.getImageMimeType(file));
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
                if (activity.hasStatus()) {
                    final ParcelableStatus status = activity.getStatus();
                    intent.putExtra(Intent.EXTRA_TEXT, Utils.getStatusShareText(activity, status));
                    intent.putExtra(Intent.EXTRA_SUBJECT, Utils.getStatusShareSubject(activity, status));
                }
                shareProvider.setShareIntent(intent);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_media_viewer_image_page, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.open_in_browser: {
                    openInBrowser();
                    return true;
                }
                case R.id.save: {
                    saveToGallery();
                    return true;
                }
                case R.id.refresh: {
                    loadImage();
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
        }

        private void saveToGallery() {
            if (mSaveImageTask != null && mSaveImageTask.getStatus() == Status.RUNNING) return;
            final Object imageTag = mImageView.getTag();
            final boolean hasImage = imageTag instanceof File;
            if (hasImage) {
                mSaveImageTask = new SaveImageTask(getActivity(), (File) imageTag);
                mSaveImageTask.execute();
            } else {

            }
        }

        private void openInBrowser() {
            final ParcelableMedia media = getMedia();
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            if (media.page_url != null) {
                intent.setData(Uri.parse(media.page_url));
            } else {
                intent.setData(Uri.parse(media.media_url));
            }
            startActivity(intent);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            mImageView.setOnClickListener(this);
            loadImage();
        }

        @Override
        public void onStart() {
            super.onStart();
            mImageView.addOnLayoutChangeListener(this);
        }

        @Override
        public void onStop() {
            super.onStop();
            mImageView.removeOnLayoutChangeListener(this);
        }

        @Override
        public void onDownloadError(final Throwable t) {
            mContentLength = 0;
        }

        @Override
        public void onDownloadFinished() {
            mContentLength = 0;
        }

        @Override
        public void onDownloadStart(final long total) {
            mContentLength = total;
            mProgressBar.spin();
        }

        @Override
        public void onProgressUpdate(final long downloaded) {
            if (mContentLength <= 0) {
                if (!mProgressBar.isSpinning()) {
                    mProgressBar.spin();
                }
                return;
            }
            mProgressBar.setProgress(downloaded / mContentLength);
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

        }

        public void onZoomOut() {
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            activity.setBarVisibility(true);
        }

        public void onZoomIn() {
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            activity.setBarVisibility(false);
        }

        private void loadImage() {
            getLoaderManager().destroyLoader(0);
            if (!mLoaderInitialized) {
                getLoaderManager().initLoader(0, getArguments(), this);
                mLoaderInitialized = true;
            } else {
                getLoaderManager().restartLoader(0, getArguments(), this);
            }
        }

        private void updateScaleLimit() {
//            final int viewWidth = mImageView.getWidth(), viewHeight = mImageView.getHeight();
//            final Drawable drawable = mImageView.getDrawable();
//            if (drawable == null || viewWidth <= 0 || viewHeight <= 0) return;
//            final int drawableWidth = drawable.getIntrinsicWidth();
//            final int drawableHeight = drawable.getIntrinsicHeight();
//            if (drawableWidth <= 0 || drawableHeight <= 0) return;
//            final float widthRatio = viewWidth / (float) drawableWidth;
//            final float heightRatio = viewHeight / (float) drawableHeight;
//            mImageView.setMaxScale(Math.max(1, Math.max(heightRatio, widthRatio)));
//            mImageView.resetScale();
        }
    }

    private ParcelableStatus getStatus() {
        return getIntent().getParcelableExtra(EXTRA_STATUS);
    }

    private static class MediaPagerAdapter extends SupportFixedFragmentStatePagerAdapter {

        private final MediaViewerActivity mActivity;
        private long mAccountId;
        private ParcelableMedia[] mMedia;

        public MediaPagerAdapter(MediaViewerActivity activity) {
            super(activity.getSupportFragmentManager());
            mActivity = activity;
        }

        @Override
        public int getCount() {
            if (mMedia == null) return 0;
            return mMedia.length;
        }

        @Override
        public Fragment getItem(int position) {
            final ParcelableMedia media = mMedia[position];
            final Bundle args = new Bundle();
            args.putLong(EXTRA_ACCOUNT_ID, mAccountId);
            args.putParcelable(EXTRA_MEDIA, media);
            switch (media.type) {
                case ParcelableMedia.TYPE_VIDEO: {
                    return Fragment.instantiate(mActivity, VideoPageFragment.class.getName(), args);
                }
                default: {
                    return Fragment.instantiate(mActivity, ImagePageFragment.class.getName(), args);
                }
            }
        }

        public void setMedia(long accountId, ParcelableMedia[] media) {
            mAccountId = accountId;
            mMedia = media;
            notifyDataSetChanged();
        }
    }
}
