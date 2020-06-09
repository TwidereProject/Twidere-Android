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

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.commonsware.cwac.layouts.AspectLockedFrameLayout.AspectRatioSource
import kotlinx.android.synthetic.main.layout_media_viewer_texture_video_view.*
import kotlinx.android.synthetic.main.layout_media_viewer_video_overlay.*
import org.mariotaku.mediaviewer.library.CacheDownloadLoader
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment
import org.mariotaku.mediaviewer.library.MediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.TwidereConstants.EXTRA_MEDIA
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_POSITION
import org.mariotaku.twidere.extension.model.bannerExtras
import org.mariotaku.twidere.extension.model.getBestVideoUrlAndType
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.media.MediaExtra
import org.mariotaku.twidere.util.promotion.PromotionService
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

class VideoPageFragment : CacheDownloadMediaViewerFragment(), IBaseFragment<VideoPageFragment>,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        View.OnClickListener, IControlBarActivity.ControlBarOffsetListener {

    @Inject
    lateinit var promotionService: PromotionService

    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayerError: Int = 0

    private var playAudio: Boolean = false
    private var pausedByUser: Boolean = false
    private var positionBackup: Int = -1

    private var videoProgressRunnable: VideoPlayProgressRunnable? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        var handler: Handler? = videoViewProgress.handler
        if (handler == null) {
            handler = Handler(requireActivity().mainLooper)
        }


        videoProgressRunnable = VideoPlayProgressRunnable(handler, videoViewProgress,
                durationLabel, positionLabel, videoView)

        if (savedInstanceState != null) {
            positionBackup = savedInstanceState.getInt(EXTRA_POSITION)
            pausedByUser = savedInstanceState.getBoolean(EXTRA_PAUSED_BY_USER)
            playAudio = savedInstanceState.getBoolean(EXTRA_PLAY_AUDIO)
        } else {
            val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Play audio by default if ringer mode on
            playAudio = !isMutedByDefault && am.ringerMode == AudioManager.RINGER_MODE_NORMAL
        }

        videoViewOverlay.setOnClickListener(this)
        videoView.setOnPreparedListener(this)
        videoView.setOnErrorListener(this)
        videoView.setOnCompletionListener(this)

        playPauseButton.setOnClickListener(this)
        volumeButton.setOnClickListener(this)
        videoControl.visibility = View.GONE
        videoContainer.setAspectRatioSource(MediaAspectRatioSource(media, this))
        if (isLoopEnabled) {
            videoViewProgress.thumb = ColorDrawable(Color.TRANSPARENT)
            videoViewProgress.isEnabled = false
        } else {
            videoViewProgress.isEnabled = true
        }

        videoViewProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var paused: Boolean = false

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val duration = videoView.duration
                if (duration <= 0) return
                videoView.seekTo((duration * (progress.toFloat() / seekBar.max)).roundToInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                paused = pauseVideo()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (paused) {
                    resumeVideo()
                }
            }

        })
        startLoading(false)
        setMediaViewVisible(false)
        updateVolume()

        promotionService.loadBanner(adContainer, media?.bannerExtras)
    }

    override fun onPause() {
        positionBackup = videoView.currentPosition
        videoView.pause()
        super.onPause()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponent.get(context).inject(this)
        if (context is IControlBarActivity) {
            context.registerControlBarOffsetListener(this)
        }
    }

    override fun onDetach() {
        val activity = activity
        if (activity is IControlBarActivity) {
            activity.unregisterControlBarOffsetListener(this)
        }
        super.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_POSITION, positionBackup)
        outState.putBoolean(EXTRA_PAUSED_BY_USER, pausedByUser)
        outState.putBoolean(EXTRA_PLAY_AUDIO, playAudio)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        requestApplyInsets()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        promotionService.setupBanner(adContainer, PromotionService.BannerType.MEDIA_PAUSE)
    }

    override fun getDownloadExtra(): Any? {
        val extra = MediaExtra()
        extra.isUseThumbor = false
        val fallbackUrlAndType = media?.getBestVideoUrlAndType(FALLBACK_VIDEO_TYPES)
        if (fallbackUrlAndType != null) {
            extra.fallbackUrl = fallbackUrlAndType.first
        }
        return extra
    }

    override fun isAbleToLoad(): Boolean {
        return downloadUri != null
    }

    override fun getDownloadUri(): Uri? {
        val bestVideoUrlAndType = media?.getBestVideoUrlAndType(SUPPORTED_VIDEO_TYPES)
        if (bestVideoUrlAndType != null) {
            return Uri.parse(bestVideoUrlAndType.first)
        }
        return arguments?.getParcelable(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }

    override fun displayMedia(result: CacheDownloadLoader.Result) {
        videoView.setVideoURI(result.cacheUri)
        videoControl.visibility = View.GONE
        setMediaViewVisible(true)
        activity?.invalidateOptionsMenu()
    }

    override fun releaseMediaResources() {
    }

    override fun onCompletion(mp: MediaPlayer) {
        updatePlayerState()
    }

    override fun onControlBarOffsetChanged(activity: IControlBarActivity, offset: Float) {
        videoControl.translationY = (1 - offset) * videoControl.height
        videoControl.alpha = offset
    }


    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mediaPlayer = null
        videoViewProgress.removeCallbacks(videoProgressRunnable)
        videoViewProgress.visibility = View.GONE
        videoControl.visibility = View.GONE
        mediaPlayerError = what
        return true
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (userVisibleHint) {
            mediaPlayer = mp
            mediaPlayerError = 0
            updateVolume()
            mp.isLooping = isLoopEnabled
            if (mp.duration > 0 && positionBackup > 0) {
                mp.seekTo(positionBackup)
            }
            if (!pausedByUser) {
                mp.start()
                pausedByUser = false
            }
            videoViewProgress.visibility = View.VISIBLE
            videoViewProgress.post(videoProgressRunnable)
            updatePlayerState()
            videoControl.visibility = if (isControlDisabled) View.GONE else View.VISIBLE
        }
    }

    private fun updateVolume() {
        volumeButton.setImageResource(if (playAudio) R.drawable.ic_action_speaker_max else R.drawable.ic_action_speaker_muted)
        val mp = mediaPlayer ?: return
        try {
            if (playAudio) {
                mp.setVolume(1f, 1f)
            } else {
                mp.setVolume(0f, 0f)
            }
        } catch (e: IllegalStateException) {
            // Ignore
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (activity == null) return
        if (isVisibleToUser) {
            activity?.invalidateOptionsMenu()
        } else if (videoView.isPlaying) {
            videoView.pause()
            updatePlayerState()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.volumeButton -> {
                playAudio = !playAudio
                updateVolume()
            }
            R.id.playPauseButton -> {
                pausedByUser = if (videoView.isPlaying) {
                    videoView.pause()
                    true
                } else {
                    videoView.start()
                    false
                }
                updatePlayerState()
            }
            R.id.videoViewOverlay -> {
                val activity = activity as MediaViewerActivity
                activity.setBarVisibility(!activity.isBarShowing)
            }
        }
    }

    override fun onCreateMediaView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_texture_video_view, container, false)
    }

    override fun onApplySystemWindowInsets(insets: Rect) {
        val lp = videoControl.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.bottomMargin = insets.bottom
            lp.leftMargin = insets.left
            lp.rightMargin = insets.right
        }
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (VideoPageFragment) -> Unit) = TODO()

    private fun updatePlayerState() {
        val playing = videoView.isPlaying
        playPauseButton.contentDescription = getString(if (playing) R.string.pause else R.string.play)
        playPauseButton.setImageResource(if (playing) R.drawable.ic_action_pause else R.drawable.ic_action_play_arrow)
        adContainer.setVisible(!playing)
    }

    private fun pauseVideo(): Boolean {
        videoView.pause()
        updatePlayerState()
        return true
    }

    private fun resumeVideo(): Boolean {
        if (!pausedByUser) {
            videoView.start()
            pausedByUser = false
        }
        updatePlayerState()
        return true
    }

    private class VideoPlayProgressRunnable internal constructor(
            private val handler: Handler,
            private val progressBar: ProgressBar,
            private val durationLabel: TextView,
            private val positionLabel: TextView,
            private val mediaPlayerControl: MediaController.MediaPlayerControl
    ) : Runnable {

        init {
            progressBar.max = 1000
        }

        override fun run() {
            val duration = mediaPlayerControl.duration
            val position = mediaPlayerControl.currentPosition
            if (duration <= 0 || position < 0) return
            progressBar.progress = (1000 * position / duration.toFloat()).roundToInt()
            val durationSecs = TimeUnit.SECONDS.convert(duration.toLong(), TimeUnit.MILLISECONDS)
            val positionSecs = TimeUnit.SECONDS.convert(position.toLong(), TimeUnit.MILLISECONDS)
            durationLabel.text = String.format(Locale.ROOT, "%02d:%02d", durationSecs / 60, durationSecs % 60)
            positionLabel.text = String.format(Locale.ROOT, "%02d:%02d", positionSecs / 60, positionSecs % 60)
            handler.postDelayed(this, 16)
        }
    }

    class MediaAspectRatioSource(val media: ParcelableMedia?, val fragment: Fragment) : AspectRatioSource {
        override fun getHeight(): Int {
            var height = media?.height ?: 0
            if (height <= 0) {
                height = fragment.requireView().measuredHeight
            }
            if (height <= 0) {
                height = 100
            }
            return height
        }

        override fun getWidth(): Int {
            var width = media?.width ?: 0
            if (width <= 0) {
                width = fragment.requireView().measuredWidth
            }
            if (width <= 0) {
                width = 100
            }
            return width
        }

    }

    companion object {

        const val EXTRA_LOOP = "loop"
        const val EXTRA_DISABLE_CONTROL = "disable_control"
        const val EXTRA_DEFAULT_MUTE = "default_mute"
        internal const val EXTRA_PAUSED_BY_USER = "paused_by_user"
        internal const val EXTRA_PLAY_AUDIO = "play_audio"
        internal val SUPPORTED_VIDEO_TYPES: Array<String> = arrayOf("video/webm", "video/mp4")
        internal val FALLBACK_VIDEO_TYPES: Array<String> = arrayOf("video/mp4")

        internal val MediaViewerFragment.isLoopEnabled: Boolean
            get() = arguments?.getBoolean(EXTRA_LOOP, false) ?: false
        internal val MediaViewerFragment.isControlDisabled: Boolean
            get() = arguments?.getBoolean(EXTRA_DISABLE_CONTROL, false) ?: false
        internal val MediaViewerFragment.isMutedByDefault: Boolean
            get() = arguments?.getBoolean(EXTRA_DEFAULT_MUTE, false) ?: false
        internal val MediaViewerFragment.media: ParcelableMedia?
            get() = arguments?.getParcelable(EXTRA_MEDIA)
        internal val MediaViewerFragment.accountKey: UserKey
            get() = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!

    }
}