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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ProgressBar;

import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable;
import com.diegocarloslima.byakugallery.lib.TileBitmapDrawable.OnInitializeListener;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.support.SupportFixedFragmentStatePagerAdapter;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.loader.support.TileImageLoader;
import org.mariotaku.twidere.loader.support.TileImageLoader.DownloadListener;
import org.mariotaku.twidere.loader.support.TileImageLoader.Result;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.SaveImageTask;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.TouchImageView;
import org.mariotaku.twidere.view.TouchImageView.ZoomListener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;

import pl.droidsonroids.gif.GifDrawable;

public final class MediaViewerActivity extends ThemedActionBarActivity implements Constants, OnPageChangeListener {

    private ViewPager mViewPager;
    private MediaPagerAdapter mAdapter;
    private ActionBar mActionBar;

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
    }

    private void toggleBar() {
        setBarVisibility(!isBarShowing());
    }

    public static final class ImagePageFragment extends BaseSupportFragment
            implements DownloadListener, LoaderCallbacks<Result>, OnLayoutChangeListener, OnClickListener, ZoomListener {

        private TouchImageView mImageView;
        private ProgressBar mProgressBar;
        private boolean mLoaderInitialized;
        private long mContentLength;
        private SaveImageTask mSaveImageTask;

        @Override
        public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onBaseViewCreated(view, savedInstanceState);
            mImageView = (TouchImageView) view.findViewById(R.id.image_view);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
        }

        @Override
        public void onClick(View v) {
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            activity.toggleBar();
        }

        @Override
        public Loader<Result> onCreateLoader(final int id, final Bundle args) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
            invalidateOptionsMenu();
            final ParcelableMedia media = args.getParcelable(EXTRA_MEDIA);
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            return new TileImageLoader(getActivity(), this, accountId, Uri.parse(media.media_url));
        }

        @Override
        public void onLoadFinished(final Loader<TileImageLoader.Result> loader, final TileImageLoader.Result data) {
            if (data.hasData()) {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setTag(data.file);
                if (data.useDecoder) {
                    TileBitmapDrawable.attachTileBitmapDrawable(mImageView, data.file.getAbsolutePath(),
                            null, new OnInitializeListener() {
                                @Override
                                public void onStartInitialization() {

                                }

                                @Override
                                public void onEndInitialization() {
                                    mImageView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateScaleLimit();
                                        }
                                    });
                                }
                            });
                } else if ("image/gif".equals(data.options.outMimeType)) {
                    try {
                        final FileDescriptor fd = new RandomAccessFile(data.file, "r").getFD();
                        mImageView.setImageDrawable(new GifDrawable(fd));
                    } catch (IOException e) {
                        mImageView.setImageDrawable(null);
                        mImageView.setTag(null);
                        mImageView.setVisibility(View.GONE);
                        Utils.showErrorMessage(getActivity(), null, e, true);
                    }
                    updateScaleLimit();
                } else {
                    mImageView.setImageBitmap(data.bitmap);
                    updateScaleLimit();
                }
            } else {
                mImageView.setImageDrawable(null);
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
            final Drawable drawable = mImageView.getDrawable();
            if (drawable instanceof GifDrawable) {
                ((GifDrawable) drawable).recycle();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_media_page, container, false);
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

        private ParcelableMedia getMedia() {
            final Bundle args = getArguments();
            return args.getParcelable(EXTRA_MEDIA);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            mImageView.setOnClickListener(this);
            mImageView.setZoomListener(this);
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
            mProgressBar.setIndeterminate(total <= 0);
            mProgressBar.setMax(total > 0 ? (int) (total / 1024) : 0);
        }

        @Override
        public void onProgressUpdate(final long downloaded) {
            if (mContentLength == 0) {
                mProgressBar.setIndeterminate(true);
                return;
            }
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress((int) (downloaded / 1024));
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

        }

        @Override
        public void onZoomOut() {
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            activity.setBarVisibility(true);
        }

        @Override
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
            final int viewWidth = mImageView.getWidth(), viewHeight = mImageView.getHeight();
            final Drawable drawable = mImageView.getDrawable();
            if (drawable == null || viewWidth <= 0 || viewHeight <= 0) return;
            final int drawableWidth = drawable.getIntrinsicWidth();
            final int drawableHeight = drawable.getIntrinsicHeight();
            if (drawableWidth <= 0 || drawableHeight <= 0) return;
            final float widthRatio = viewWidth / (float) drawableWidth;
            final float heightRatio = viewHeight / (float) drawableHeight;
            mImageView.setMaxScale(Math.max(1, Math.max(heightRatio, widthRatio)));
            mImageView.resetScale();
        }
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
            final Bundle args = new Bundle();
            args.putLong(EXTRA_ACCOUNT_ID, mAccountId);
            args.putParcelable(EXTRA_MEDIA, mMedia[position]);
            return Fragment.instantiate(mActivity, ImagePageFragment.class.getName(), args);
        }

        public void setMedia(long accountId, ParcelableMedia[] media) {
            mAccountId = accountId;
            mMedia = media;
            notifyDataSetChanged();
        }
    }
}
