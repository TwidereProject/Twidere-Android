/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.media

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlinx.android.synthetic.main.layout_media_viewer_exo_player_view.*
import kotlinx.android.synthetic.main.layout_media_viewer_video_overlay.*
import org.mariotaku.mediaviewer.library.MediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_POSITION
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.EXTRA_PAUSED_BY_USER
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.EXTRA_PLAY_AUDIO
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.isMutedByDefault
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.media
import org.mariotaku.twidere.util.UserAgentUtils


/**
 * Successor of `VideoPageFragment`, backed by `ExoPlayer`
 * Created by mariotaku on 2017/2/28.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ExoPlayerPageFragment : MediaViewerFragment(), IBaseFragment<ExoPlayerPageFragment> {
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private lateinit var mainHandler: Handler

    private var playAudio: Boolean = false
    private var pausedByUser: Boolean = false
    private var playbackCompleted: Boolean = false
    private var positionBackup: Long = -1L

    private val playerListener = object : ExoPlayer.EventListener {
        override fun onLoadingChanged(isLoading: Boolean) {

        }

        override fun onPlayerError(error: ExoPlaybackException) {
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    showProgress(true, 0f)
                }
                ExoPlayer.STATE_ENDED -> {
                    playbackCompleted = true
                    positionBackup = -1L

                    // Reset position
                    playerView.player?.let { player ->
                        player.seekTo(0)
                        player.playWhenReady = false
                    }

                    hideProgress()
                }
                ExoPlayer.STATE_READY -> {
                    playbackCompleted = playWhenReady
                    hideProgress()
                }
                else -> {
                    hideProgress()
                }
            }
        }

        override fun onPositionDiscontinuity() {
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?) {
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mediaDataSourceFactory = DefaultHttpDataSourceFactory(UserAgentUtils.getDefaultUserAgentString(context))
        mainHandler = Handler()


        if (savedInstanceState != null) {
            positionBackup = savedInstanceState.getLong(EXTRA_POSITION)
            pausedByUser = savedInstanceState.getBoolean(EXTRA_PAUSED_BY_USER)
            playAudio = savedInstanceState.getBoolean(EXTRA_PLAY_AUDIO)
        } else {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Play audio by default if ringer mode on
            playAudio = !isMutedByDefault && am.ringerMode == AudioManager.RINGER_MODE_NORMAL
        }

        volumeButton.setOnClickListener {
            this.playAudio = !this.playAudio
            updateVolume()
        }
        updateVolume()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }


    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            releasePlayer()
        }
    }

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_exo_player_view, parent, false)
    }

    override fun fitSystemWindows(insets: Rect) {
        val lp = videoControl.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.bottomMargin = insets.bottom
            lp.leftMargin = insets.left
            lp.rightMargin = insets.right
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(EXTRA_POSITION, positionBackup)
        outState.putBoolean(EXTRA_PAUSED_BY_USER, pausedByUser)
        outState.putBoolean(EXTRA_PLAY_AUDIO, playAudio)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        requestFitSystemWindows()
    }

    override fun recycleMedia() {
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (ExoPlayerPageFragment) -> Unit) {
        // No-op
    }

    private fun releasePlayer() {
        val player = playerView.player ?: return
        positionBackup = player.currentPosition
        pausedByUser = !player.playWhenReady
        player.removeListener(playerListener)
        player.release();
        playerView.player = null
    }

    private fun initializePlayer() {
        if (playerView.player != null) return
        playerView.player = run {
            val bandwidthMeter = DefaultBandwidthMeter()
            val videoTrackSelectionFactory = AdaptiveVideoTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, DefaultLoadControl())
            if (positionBackup >= 0) {
                player.seekTo(positionBackup)
            }
            player.playWhenReady = !pausedByUser
            player.addListener(playerListener)
            return@run player
        }

        val uri = getDownloadUri() ?: return
        val mediaSource = ExtractorMediaSource(uri, mediaDataSourceFactory, DefaultExtractorsFactory(),
                null, null)
        playerView.player.prepare(mediaSource)
        updateVolume()
    }

    private fun updateVolume() {
        volumeButton.setImageResource(if (playAudio) R.drawable.ic_action_speaker_max else R.drawable.ic_action_speaker_muted)
        val player = playerView.player ?: return
        if (playAudio) {
            player.volume = 1f
        } else {
            player.volume = 0f
        }
    }

    fun getDownloadUri(): Uri? {
        val bestVideoUrlAndType = VideoPageFragment.getBestVideoUrlAndType(media, VideoPageFragment.SUPPORTED_VIDEO_TYPES)
        if (bestVideoUrlAndType != null && bestVideoUrlAndType.first != null) {
            return Uri.parse(bestVideoUrlAndType.first)
        }
        return arguments.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }
}
