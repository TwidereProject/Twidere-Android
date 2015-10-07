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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnGenericMotionListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.TaskRunnable;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.sprylab.android.widget.TextureVideoView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.support.SupportFixedFragmentStatePagerAdapter;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.fragment.support.ViewStatusDialogFragment;
import org.mariotaku.twidere.loader.support.TileImageLoader;
import org.mariotaku.twidere.loader.support.TileImageLoader.DownloadListener;
import org.mariotaku.twidere.loader.support.TileImageLoader.Result;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMedia.VideoInfo.Variant;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.SaveFileTask;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.VideoLoader.VideoLoadingListener;

import java.io.File;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifSupportChecker;
import pl.droidsonroids.gif.GifTextureView;
import pl.droidsonroids.gif.InputSource.FileSource;


public final class MediaViewerActivity extends BaseAppCompatActivity implements Constants, OnPageChangeListener {

    private static final String EXTRA_LOOP = "loop";
    private static boolean ANIMATED_GIF_SUPPORTED = GifSupportChecker.isSupported();
    private ViewPager mViewPager;
    private MediaPagerAdapter mPagerAdapter;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_media_viewer);
        mPagerAdapter = new MediaPagerAdapter(this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.element_spacing_normal));
        mViewPager.addOnPageChangeListener(this);
        final Intent intent = getIntent();
        final long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        final ParcelableMedia[] media = Utils.newParcelableArray(intent.getParcelableArrayExtra(EXTRA_MEDIA), ParcelableMedia.CREATOR);
        final ParcelableMedia currentMedia = intent.getParcelableExtra(EXTRA_CURRENT_MEDIA);
        mPagerAdapter.setMedia(accountId, media);
        final int currentIndex = ArrayUtils.indexOf(media, currentMedia);
        if (currentIndex != -1) {
            mViewPager.setCurrentItem(currentIndex, false);
        }
        if (isMediaStatusEnabled() && intent.hasExtra(EXTRA_STATUS)) {
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
        updatePositionTitle();
    }

    public boolean hasStatus() {
        return getIntent().hasExtra(EXTRA_STATUS);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mMediaStatusContainer = findViewById(R.id.media_status_container);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        updatePositionTitle();
        setBarVisibility(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action != null) {
            switch (action) {
                case ACTION_NAVIGATION_PREVIOUS_TAB: {
                    final int previous = mViewPager.getCurrentItem() - 1;
                    if (previous < 0) {
                    } else if (previous < mPagerAdapter.getCount()) {
                        mViewPager.setCurrentItem(previous, true);
                    }
                    return true;
                }
                case ACTION_NAVIGATION_NEXT_TAB: {
                    final int next = mViewPager.getCurrentItem() + 1;
                    if (next >= mPagerAdapter.getCount()) {
                    } else if (next >= 0) {
                        mViewPager.setCurrentItem(next, true);
                    }
                    return true;
                }
                case ACTION_NAVIGATION_BACK: {
                    onBackPressed();
                    return true;
                }
            }
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    protected boolean shouldApplyWindowBackground() {
        return false;
    }

    private ParcelableStatus getStatus() {
        return getIntent().getParcelableExtra(EXTRA_STATUS);
    }

    private boolean isBarShowing() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return false;
        return actionBar.isShowing();
    }

    private boolean isMediaStatusEnabled() {
        return Boolean.parseBoolean("false");
    }

    private void setBarVisibility(boolean visible) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (visible) {
            actionBar.show();
        } else {
            actionBar.hide();
        }

        mMediaStatusContainer.setVisibility(isMediaStatusEnabled() && visible ? View.VISIBLE : View.GONE);
    }

    private void toggleBar() {
        setBarVisibility(!isBarShowing());
    }

    private void updatePositionTitle() {
        setTitle(String.format("%d / %d", mViewPager.getCurrentItem() + 1, mPagerAdapter.getCount()));
    }

    public static class BaseImagePageFragment extends BaseSupportFragment
            implements DownloadListener, LoaderCallbacks<Result>, OnClickListener {

        private SubsamplingScaleImageView mImageView;
        private ProgressWheel mProgressBar;
        private boolean mLoaderInitialized;
        private float mContentLength;
        private SaveFileTask mSaveFileTask;

        private File mImageFile;

        @Override
        public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onBaseViewCreated(view, savedInstanceState);
            mImageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
            mProgressBar = (ProgressWheel) view.findViewById(R.id.load_progress);
        }

        @Override
        public void onClick(View v) {
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            if (activity == null) return;
            activity.toggleBar();
        }

        @Override
        public Loader<Result> onCreateLoader(final int id, final Bundle args) {
            setLoadProgressVisibility(View.VISIBLE);
            mProgressBar.spin();
            invalidateOptionsMenu();
            final ParcelableMedia media = getMedia();
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            return new TileImageLoader(getActivity(), this, accountId, Uri.parse(media.media_url));
        }

        @Override
        public void onLoadFinished(final Loader<TileImageLoader.Result> loader, final TileImageLoader.Result data) {
            if (data.hasData()) {
                mImageFile = data.file;
                if (data.useDecoder) {
                    setImageViewVisibility(View.VISIBLE);
                    mImageView.setImage(ImageSource.uri(Uri.fromFile(data.file)));
                } else {
                    setImageViewVisibility(View.VISIBLE);
                    mImageView.setImage(ImageSource.bitmap(data.bitmap));
                }
            } else {
                mImageView.recycle();
                mImageFile = null;
                setImageViewVisibility(View.GONE);
                Utils.showErrorMessage(getActivity(), null, data.exception, true);
            }
            setLoadProgressVisibility(View.GONE);
            setLoadProgress(0);
            invalidateOptionsMenu();
        }

        @Override
        public void onLoaderReset(final Loader<TileImageLoader.Result> loader) {
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
            setLoadProgress(downloaded / mContentLength);
        }

        protected void setImageViewVisibility(int visible) {
            mImageView.setVisibility(visible);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_media_page_image_compat, container, false);
        }

        protected void setLoadProgress(float progress) {
            mProgressBar.setProgress(progress);
        }

        protected void setLoadProgressVisibility(int visibility) {
            mProgressBar.setVisibility(visibility);
        }

        private ParcelableMedia getMedia() {
            final Bundle args = getArguments();
            return args.getParcelable(EXTRA_MEDIA);
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

        private void saveToGallery() {
            if (mSaveFileTask != null && mSaveFileTask.getStatus() == Status.RUNNING) return;
            final File file = mImageFile;
            final boolean hasImage = file != null && file.exists();
            if (!hasImage) return;
            mSaveFileTask = SaveFileTask.saveImage(getActivity(), file);
            AsyncTaskUtils.executeTask(mSaveFileTask);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            final boolean isLoading = getLoaderManager().hasRunningLoaders();
            final TaskRunnable<File, Pair<Boolean, Intent>, Pair<Fragment, Menu>> checkState = new TaskRunnable<File, Pair<Boolean, Intent>, Pair<Fragment, Menu>>() {
                @Override
                public Pair<Boolean, Intent> doLongOperation(File file) throws InterruptedException {
                    final boolean hasImage = file != null && file.exists();
                    if (!hasImage) {
                        return Pair.create(false, null);
                    }
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
                    return Pair.create(true, intent);
                }

                @Override
                public void callback(Pair<Fragment, Menu> callback, Pair<Boolean, Intent> result) {
                    if (callback.first.isDetached() || callback.first.getActivity() == null) return;
                    final Menu menu = callback.second;
                    final boolean hasImage = result.first;
                    MenuUtils.setMenuItemAvailability(menu, R.id.refresh, !hasImage && !isLoading);
                    MenuUtils.setMenuItemAvailability(menu, R.id.share, hasImage && !isLoading);
                    MenuUtils.setMenuItemAvailability(menu, R.id.save, hasImage && !isLoading);
                    if (!hasImage) return;
                    final MenuItem shareItem = menu.findItem(R.id.share);
                    shareItem.setIntent(Intent.createChooser(result.second, callback.first.getString(R.string.share)));
                }
            };
            checkState.setParams(mImageFile);
            checkState.setResultHandler(Pair.<Fragment, Menu>create(this, menu));
            AsyncManager.runBackgroundTask(checkState);
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


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            mImageView.setOnClickListener(this);
            mImageView.setOnGenericMotionListener(new OnGenericMotionListener() {
                @Override
                public boolean onGenericMotion(View v, MotionEvent event) {
                    final SubsamplingScaleImageView iv = (SubsamplingScaleImageView) v;
                    return false;
                }
            });
            loadImage();
        }


    }

    public static final class ImagePageFragment extends BaseImagePageFragment
            implements DownloadListener, LoaderCallbacks<Result>, OnClickListener {

        private GifTextureView mGifImageView;

        @Override
        public void onBaseViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onBaseViewCreated(view, savedInstanceState);
            mGifImageView = (GifTextureView) view.findViewById(R.id.gif_image_view);
        }


        @Override
        public void onLoadFinished(final Loader<TileImageLoader.Result> loader, final TileImageLoader.Result data) {
            if (data.hasData() && "image/gif".equals(data.options.outMimeType)) {
                mGifImageView.setVisibility(View.VISIBLE);
                setImageViewVisibility(View.GONE);
                mGifImageView.setInputSource(new FileSource(data.file));
                setLoadProgressVisibility(View.GONE);
                setLoadProgress(0);
                invalidateOptionsMenu();
                return;
            }
            super.onLoadFinished(loader, data);
        }


        @Override
        public void onLoaderReset(final Loader<TileImageLoader.Result> loader) {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_media_page_image, container, false);
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
            final ParcelableMedia media = mMedia[position];
            final Bundle args = new Bundle();
            args.putLong(EXTRA_ACCOUNT_ID, mAccountId);
            args.putParcelable(EXTRA_MEDIA, media);
            switch (media.type) {
                case ParcelableMedia.TYPE_ANIMATED_GIF:
                case ParcelableMedia.TYPE_CARD_ANIMATED_GIF: {
                    args.putBoolean(EXTRA_LOOP, true);
                    return Fragment.instantiate(mActivity, VideoPageFragment.class.getName(), args);
                }
                case ParcelableMedia.TYPE_VIDEO: {
                    return Fragment.instantiate(mActivity, VideoPageFragment.class.getName(), args);
                }
                default: {
                    if (ANIMATED_GIF_SUPPORTED) {
                        return Fragment.instantiate(mActivity, ImagePageFragment.class.getName(), args);
                    }
                    return Fragment.instantiate(mActivity, BaseImagePageFragment.class.getName(), args);
                }
            }
        }

        public void setMedia(long accountId, ParcelableMedia[] media) {
            mAccountId = accountId;
            mMedia = media;
            notifyDataSetChanged();
        }
    }

    public static final class VideoPageFragment extends BaseSupportFragment
            implements VideoLoadingListener, OnPreparedListener, OnErrorListener, OnCompletionListener, OnClickListener {

        private static final String[] SUPPORTED_VIDEO_TYPES;

        static {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                SUPPORTED_VIDEO_TYPES = new String[]{"video/mp4"};
            } else {
                SUPPORTED_VIDEO_TYPES = new String[]{"video/webm", "video/mp4"};
            }
        }

        private TextureVideoView mVideoView;
        private View mVideoViewOverlay;
        private ProgressBar mVideoViewProgress;
        private TextView mDurationLabel, mPositionLabel;
        private ImageButton mPlayPauseButton, mVolumeButton;
        private ProgressWheel mProgressBar;
        private View mVideoControl;

        private boolean mPlayAudio;
        private VideoPlayProgressRunnable mVideoProgressRunnable;
        private SaveFileTask mSaveFileTask;
        private File mVideoFile;
        private Pair<String, String> mVideoUrlAndType;
        private MediaPlayer mMediaPlayer;
        private int mMediaPlayerError;

        public boolean isLoopEnabled() {
            return getArguments().getBoolean(EXTRA_LOOP, false);
        }

        public void loadVideo(boolean forceReload) {
            Pair<String, String> urlAndType = getBestVideoUrlAndType(getMedia());
            if (urlAndType == null) return;
            mVideoUrlAndType = urlAndType;
            mVideoLoader.loadVideo(urlAndType.first, forceReload, this);
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mMediaPlayer = null;
//            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
//            mVideoViewProgress.setVisibility(View.GONE);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mMediaPlayer = null;
            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
            mVideoViewProgress.setVisibility(View.GONE);
            mMediaPlayerError = what;
            invalidateOptionsMenu();
            return true;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (getUserVisibleHint()) {
                mMediaPlayer = mp;
                mMediaPlayerError = 0;
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setScreenOnWhilePlaying(true);
                updateVolume();
                mp.setLooping(isLoopEnabled());
                mp.start();
                mVideoViewProgress.setVisibility(View.VISIBLE);
                mVideoViewProgress.post(mVideoProgressRunnable);
                updatePlayerState();
                mVideoControl.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
            }
        }

        private void updateVolume() {
            final ImageButton b = mVolumeButton;
            if (b != null) {
                b.setImageResource(mPlayAudio ? R.drawable.ic_action_speaker_max : R.drawable.ic_action_speaker_muted);
            }
            final MediaPlayer mp = mMediaPlayer;
            if (mp == null) return;
            if (mPlayAudio) {
                mp.setVolume(1, 1);
            } else {
                mp.setVolume(0, 0);
            }
        }

        @Override
        public void onVideoLoadingCancelled(String uri, VideoLoadingListener listener) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setProgress(0);
            invalidateOptionsMenu();
        }

        @Override
        public void onBaseViewCreated(View view, Bundle savedInstanceState) {
            super.onBaseViewCreated(view, savedInstanceState);
            mVideoView = (TextureVideoView) view.findViewById(R.id.video_view);
            mVideoViewOverlay = view.findViewById(R.id.video_view_overlay);
            mVideoViewProgress = (ProgressBar) view.findViewById(R.id.video_view_progress);
            mProgressBar = (ProgressWheel) view.findViewById(R.id.load_progress);
            mDurationLabel = (TextView) view.findViewById(R.id.duration_label);
            mPositionLabel = (TextView) view.findViewById(R.id.position_label);
            mPlayPauseButton = (ImageButton) view.findViewById(R.id.play_pause_button);
            mVolumeButton = (ImageButton) view.findViewById(R.id.volume_button);
            mVideoControl = view.findViewById(R.id.video_control);
        }

        @Override
        public void onVideoLoadingComplete(String uri, VideoLoadingListener listener, File file) {
            mVideoView.setVideoURI(Uri.fromFile(file));
            mVideoFile = file;
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setProgress(0);
            invalidateOptionsMenu();
        }

        @Override
        public void onVideoLoadingFailed(String uri, VideoLoadingListener listener, Exception e) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setProgress(0);
            invalidateOptionsMenu();
        }

        @Override
        public void onVideoLoadingProgressUpdate(String uri, VideoLoadingListener listener, int current, int total) {
            if (total <= 0) {
                if (!mProgressBar.isSpinning()) {
                    mProgressBar.spin();
                }
                return;
            }
            mProgressBar.setProgress(current / (float) total);
        }

        @Override
        public void onVideoLoadingStarted(String uri, VideoLoadingListener listener) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.spin();
            invalidateOptionsMenu();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (!isVisibleToUser && mVideoView != null && mVideoView.isPlaying()) {
                mVideoView.pause();
                updatePlayerState();
            }
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);

            Handler handler = mVideoViewProgress.getHandler();
            if (handler == null) {
                handler = new Handler(getActivity().getMainLooper());
            }
            mVideoProgressRunnable = new VideoPlayProgressRunnable(handler, mVideoViewProgress,
                    mDurationLabel, mPositionLabel, mVideoView);


            mVideoViewOverlay.setOnClickListener(this);
            mVideoView.setOnPreparedListener(this);
            mVideoView.setOnErrorListener(this);
            mVideoView.setOnCompletionListener(this);

            mPlayPauseButton.setOnClickListener(this);
            mVolumeButton.setOnClickListener(this);
            loadVideo(false);
            updateVolume();
        }

        private Pair<String, String> getBestVideoUrlAndType(ParcelableMedia media) {
            if (media == null) return null;
            switch (media.type) {
                case ParcelableMedia.TYPE_VIDEO:
                case ParcelableMedia.TYPE_ANIMATED_GIF: {
                    if (media.video_info == null) return null;
                    for (String supportedType : SUPPORTED_VIDEO_TYPES) {
                        for (Variant variant : media.video_info.variants) {
                            if (supportedType.equalsIgnoreCase(variant.content_type))
                                return new Pair<>(variant.url, variant.content_type);
                        }
                    }
                    return null;
                }
                case ParcelableMedia.TYPE_CARD_ANIMATED_GIF: {
                    return new Pair<>(media.media_url, "video/mp4");
                }
                default: {
                    return null;
                }
            }
        }

        private ParcelableMedia getMedia() {
            final Bundle args = getArguments();
            return args.getParcelable(EXTRA_MEDIA);
        }

        private void saveToGallery() {
            if (mSaveFileTask != null && mSaveFileTask.getStatus() == Status.RUNNING) return;
            final File file = mVideoFile;
            final Pair<String, String> urlAndType = mVideoUrlAndType;
            final boolean hasVideo = file != null && file.exists() && urlAndType != null;
            if (!hasVideo) return;
            final String mimeType = urlAndType.second;
            final MimeTypeMap map = MimeTypeMap.getSingleton();
            final String extension = map.getExtensionFromMimeType(mimeType);
            if (extension == null) return;
            final File pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            final File saveDir = new File(pubDir, "Twidere");
            mSaveFileTask = AsyncTaskUtils.executeTask(new SaveFileTask(getActivity(), file, mimeType, saveDir));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.volume_button: {
                    mPlayAudio = !mPlayAudio;
                    updateVolume();
                    break;
                }
                case R.id.play_pause_button: {
                    final MediaPlayer mp = mMediaPlayer;
                    if (mp != null) {
                        if (mp.isPlaying()) {
                            mp.pause();
                        } else {
                            mp.start();
                        }
                    }
                    updatePlayerState();
                    break;
                }
                case R.id.video_view_overlay: {
                    if (mVideoControl.getVisibility() == View.VISIBLE) {
                        mVideoControl.setVisibility(View.GONE);
                    } else {
                        mVideoControl.setVisibility(View.VISIBLE);
                    }
                    break;
                }
            }
        }

        private void updatePlayerState() {
            final MediaPlayer mp = mMediaPlayer;
            if (mp != null) {
                final boolean playing = mp.isPlaying();
                mPlayPauseButton.setContentDescription(getString(playing ? R.string.pause : R.string.play));
                mPlayPauseButton.setImageResource(playing ? R.drawable.ic_action_pause : R.drawable.ic_action_play_arrow);
            } else {
                mPlayPauseButton.setContentDescription(getString(R.string.play));
                mPlayPauseButton.setImageResource(R.drawable.ic_action_play_arrow);
            }
        }

        private static class VideoPlayProgressRunnable implements Runnable {

            private final Handler mHandler;
            private final ProgressBar mProgressBar;
            private final TextView mDurationLabel, mPositionLabel;
            private final MediaController.MediaPlayerControl mMediaPlayerControl;

            VideoPlayProgressRunnable(Handler handler, ProgressBar progressBar, TextView durationLabel,
                                      TextView positionLabel, MediaController.MediaPlayerControl mediaPlayerControl) {
                mHandler = handler;
                mProgressBar = progressBar;
                mDurationLabel = durationLabel;
                mPositionLabel = positionLabel;
                mMediaPlayerControl = mediaPlayerControl;
                mProgressBar.setMax(1000);
            }

            @Override
            public void run() {
                final int duration = mMediaPlayerControl.getDuration();
                final int position = mMediaPlayerControl.getCurrentPosition();
                if (duration <= 0 || position < 0) return;
                mProgressBar.setProgress(Math.round(1000 * position / (float) duration));
                final long durationSecs = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS),
                        positionSecs = TimeUnit.SECONDS.convert(position, TimeUnit.MILLISECONDS);
                mDurationLabel.setText(String.format("%02d:%02d", durationSecs / 60, durationSecs % 60));
                mPositionLabel.setText(String.format("%02d:%02d", positionSecs / 60, positionSecs % 60));
                mHandler.postDelayed(this, 16);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_media_page_video, container, false);
        }


        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            final File file = mVideoFile;
            final Pair<String, String> linkAndType = mVideoUrlAndType;
            final boolean isLoading = linkAndType != null && mVideoLoader.isLoading(linkAndType.first);
            final boolean hasVideo = file != null && file.exists() && linkAndType != null && mMediaPlayerError == 0;
            MenuUtils.setMenuItemAvailability(menu, R.id.refresh, !hasVideo && !isLoading);
            MenuUtils.setMenuItemAvailability(menu, R.id.share, hasVideo && !isLoading);
            MenuUtils.setMenuItemAvailability(menu, R.id.save, hasVideo && !isLoading);
            if (!hasVideo) return;
            final MenuItem shareItem = menu.findItem(R.id.share);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            final Uri fileUri = Uri.fromFile(file);
            intent.setDataAndType(fileUri, linkAndType.second);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            final MediaViewerActivity activity = (MediaViewerActivity) getActivity();
            if (activity.hasStatus()) {
                final ParcelableStatus status = activity.getStatus();
                intent.putExtra(Intent.EXTRA_TEXT, Utils.getStatusShareText(activity, status));
                intent.putExtra(Intent.EXTRA_SUBJECT, Utils.getStatusShareSubject(activity, status));
            }
            shareItem.setIntent(Intent.createChooser(intent, getString(R.string.share)));
        }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_media_viewer_video_page, menu);
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.save: {
                    saveToGallery();
                    return true;
                }
                case R.id.refresh: {
                    loadVideo(true);
                    return true;
                }
            }
            return super.onOptionsItemSelected(item);
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

    }
}
