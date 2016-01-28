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

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.sprylab.android.widget.TextureVideoView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.mediaviewer.library.AbsMediaViewerActivity;
import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.FileCache;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.mediaviewer.library.MediaViewerFragment;
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;


public final class MediaViewerActivity extends AbsMediaViewerActivity implements Constants {

    @Inject
    FileCache mFileCache;
    @Inject
    MediaDownloader mMediaDownloader;

    private ParcelableMedia[] mMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GeneralComponentHelper.build(this).inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getInitialPosition() {
        return ArrayUtils.indexOf(getMedia(), getCurrentMedia());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_media_viewer;
    }

    @Override
    protected ViewPager findViewPager() {
        return (ViewPager) findViewById(R.id.view_pager);
    }

    @Override
    public boolean isBarShowing() {
        return false;
    }

    @Override
    public void setBarVisibility(boolean visible) {

    }

    @Override
    public MediaDownloader getDownloader() {
        return mMediaDownloader;
    }

    @Override
    public FileCache getFileCache() {
        return mFileCache;
    }

    @Override
    protected MediaViewerFragment instantiateMediaFragment(int position) {
        final ParcelableMedia media = getMedia()[position];
        switch (media.type) {
            case ParcelableMedia.Type.TYPE_IMAGE: {
                final Bundle args = new Bundle();
                args.putParcelable(ImagePageFragment.EXTRA_MEDIA_URI, Uri.parse(media.media_url));
                return (MediaViewerFragment) Fragment.instantiate(this,
                        ImagePageFragment.class.getName(), args);
            }
            case ParcelableMedia.Type.TYPE_ANIMATED_GIF:
            case ParcelableMedia.Type.TYPE_CARD_ANIMATED_GIF: {
                final Bundle args = new Bundle();
                args.putBoolean(VideoPageFragment.EXTRA_LOOP, true);
                args.putParcelable(EXTRA_MEDIA, media);
                return (MediaViewerFragment) Fragment.instantiate(this,
                        VideoPageFragment.class.getName(), args);
            }
            case ParcelableMedia.Type.TYPE_VIDEO: {
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_MEDIA, media);
                return (MediaViewerFragment) Fragment.instantiate(this,
                        VideoPageFragment.class.getName(), args);
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    protected int getMediaCount() {
        return getMedia().length;
    }

    private ParcelableMedia getCurrentMedia() {
        return getIntent().getParcelableExtra(EXTRA_CURRENT_MEDIA);
    }

    private ParcelableMedia[] getMedia() {
        if (mMedia != null) return mMedia;
        return mMedia = Utils.newParcelableArray(getIntent().getParcelableArrayExtra(EXTRA_MEDIA),
                ParcelableMedia.CREATOR);
    }

    private Object getDownloadExtra() {
        return null;
    }

    public static class ImagePageFragment extends SubsampleImageViewerFragment {
        @Override
        protected Object getDownloadExtra() {
            return ((MediaViewerActivity) getActivity()).getDownloadExtra();
        }
    }

    public static class VideoPageFragment extends MediaViewerFragment implements MediaPlayer.OnPreparedListener,
            MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, View.OnClickListener {

        private static final String EXTRA_LOOP = "loop";

        @Override
        protected Object getDownloadExtra() {
            return null;
        }


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
        private MediaPlayer mMediaPlayer;
        private int mMediaPlayerError;

        public boolean isLoopEnabled() {
            return getArguments().getBoolean(EXTRA_LOOP, false);
        }


        @Override
        protected void showProgress(boolean indeterminate, float progress) {
            mProgressBar.setVisibility(View.VISIBLE);
            if (indeterminate) {
                mProgressBar.spin();
            } else {
                mProgressBar.setProgress(progress);
            }
        }

        @Override
        protected void hideProgress() {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected boolean isAbleToLoad() {
            return getDownloadUri() != null;
        }

        @Override
        protected Uri getDownloadUri() {
            final Pair<String, String> bestVideoUrlAndType = getBestVideoUrlAndType(getMedia());
            if (bestVideoUrlAndType == null || bestVideoUrlAndType.first == null) return null;
            return Uri.parse(bestVideoUrlAndType.first);
        }

        private ParcelableMedia getMedia() {
            return getArguments().getParcelable(EXTRA_MEDIA);
        }

        @Override
        protected void displayMedia(CacheDownloadLoader.Result result) {
            mVideoView.setVideoURI(result.cacheUri);
        }

        @Override
        protected void recycleMedia() {

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
            updatePlayerState();
//            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
//            mVideoViewProgress.setVisibility(View.GONE);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mMediaPlayer = null;
            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
            mVideoViewProgress.setVisibility(View.GONE);
            mMediaPlayerError = what;
//            invalidateOptionsMenu();
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
//                invalidateOptionsMenu();
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
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
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
            startLoading();
            updateVolume();
        }

        private Pair<String, String> getBestVideoUrlAndType(ParcelableMedia media) {
            if (media == null) return null;
            switch (media.type) {
                case ParcelableMedia.Type.TYPE_VIDEO:
                case ParcelableMedia.Type.TYPE_ANIMATED_GIF: {
                    if (media.video_info == null) {
                        return Pair.create(media.media_url, null);
                    }
                    for (String supportedType : SUPPORTED_VIDEO_TYPES) {
                        for (ParcelableMedia.VideoInfo.Variant variant : media.video_info.variants) {
                            if (supportedType.equalsIgnoreCase(variant.content_type))
                                return Pair.create(variant.url, variant.content_type);
                        }
                    }
                    return null;
                }
                case ParcelableMedia.Type.TYPE_CARD_ANIMATED_GIF: {
                    return Pair.create(media.media_url, "video/mp4");
                }
                default: {
                    return null;
                }
            }
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

        @Override
        public View onCreateMediaView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_media_viewer_texture_video_view, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_media_viewer_video_page, menu);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            final boolean isLoading = getLoaderManager().hasRunningLoaders();
            final boolean isDownloaded = hasDownloadedData();
            MenuUtils.setMenuItemAvailability(menu, R.id.save, !isLoading && isDownloaded);
            MenuUtils.setMenuItemAvailability(menu, R.id.share, !isLoading && isDownloaded);
            MenuUtils.setMenuItemAvailability(menu, R.id.refresh, !isLoading && !isDownloaded);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.save: {
//                    requestAndSaveToStorage();
                    return true;
                }
                case R.id.refresh: {
                    startLoading();
                    return true;
                }
                case R.id.share: {
//                    shareMedia();
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
    }
}
