package org.mariotaku.twidere.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.commonsware.cwac.layouts.AspectLockedFrameLayout.AspectRatioSource
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaDownloadEvent
import kotlinx.android.synthetic.main.layout_media_viewer_texture_video_view.*
import org.mariotaku.mediaviewer.library.CacheDownloadLoader
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.TwidereConstants.EXTRA_MEDIA
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_POSITION
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.media.MediaExtra
import java.util.*
import java.util.concurrent.TimeUnit

class VideoPageFragment : CacheDownloadMediaViewerFragment(), IBaseFragment<VideoPageFragment>,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        View.OnClickListener, IControlBarActivity.ControlBarOffsetListener {

    private val isLoopEnabled: Boolean get() = arguments.getBoolean(EXTRA_LOOP, false)
    private val isControlDisabled: Boolean get() = arguments.getBoolean(EXTRA_DISABLE_CONTROL, false)
    private val isMutedByDefault: Boolean get() = arguments.getBoolean(EXTRA_DEFAULT_MUTE, false)
    private val media: ParcelableMedia? get() = arguments.getParcelable<ParcelableMedia>(EXTRA_MEDIA)
    private val accountKey: UserKey get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)

    private var playAudio: Boolean = false
    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayerError: Int = 0
    private var positionBackup: Int = -1
    private var videoProgressRunnable: VideoPlayProgressRunnable? = null
    private var mediaDownloadEvent: MediaDownloadEvent? = null


    private var aspectRatioSource = object : AspectRatioSource {
        override fun getHeight(): Int {
            val height = media?.height ?: 0
            if (height <= 0) return view!!.measuredHeight
            return height
        }

        override fun getWidth(): Int {
            val width = media?.width ?: 0
            if (width <= 0) return view!!.measuredWidth
            return width
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        var handler: Handler? = videoViewProgress.handler
        if (handler == null) {
            handler = Handler(activity.mainLooper)
        }

        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Play audio by default if ringer mode on
        playAudio = !isMutedByDefault && am.ringerMode == AudioManager.RINGER_MODE_NORMAL

        videoProgressRunnable = VideoPlayProgressRunnable(handler, videoViewProgress,
                durationLabel, positionLabel, videoView)

        if (savedInstanceState != null) {
            positionBackup = savedInstanceState.getInt(EXTRA_POSITION)
        }

        videoViewOverlay.setOnClickListener(this)
        videoView.setOnPreparedListener(this)
        videoView.setOnErrorListener(this)
        videoView.setOnCompletionListener(this)

        playPauseButton.setOnClickListener(this)
        volumeButton.setOnClickListener(this)
        videoControl.visibility = View.GONE
        videoContainer.setAspectRatioSource(aspectRatioSource)
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
                val mp = mediaPlayer ?: return
                val duration = mp.duration
                if (duration <= 0) return
                mp.seekTo(Math.round(duration * (progress.toFloat() / seekBar.max)))
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
    }

    override fun onPause() {
        mediaPlayer?.let { mp ->
            positionBackup = mp.currentPosition
        }
        super.onPause()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
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
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        requestFitSystemWindows()
    }


    override fun getDownloadExtra(): Any? {
        val extra = MediaExtra()
        extra.isUseThumbor = false
        val fallbackUrlAndType = getBestVideoUrlAndType(media, FALLBACK_VIDEO_TYPES)
        if (fallbackUrlAndType != null) {
            extra.fallbackUrl = fallbackUrlAndType.first
        }
        return extra
    }

    override fun isAbleToLoad(): Boolean {
        return downloadUri != null
    }

    override fun getDownloadUri(): Uri? {
        val bestVideoUrlAndType = getBestVideoUrlAndType(media, SUPPORTED_VIDEO_TYPES)
        if (bestVideoUrlAndType != null && bestVideoUrlAndType.first != null) {
            return Uri.parse(bestVideoUrlAndType.first)
        }
        return arguments.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }

    override fun displayMedia(result: CacheDownloadLoader.Result) {
        videoView.setVideoURI(result.cacheUri)
        videoControl.visibility = View.GONE
        setMediaViewVisible(true)
        activity.supportInvalidateOptionsMenu()
    }

    override fun recycleMedia() {

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
            mp.setScreenOnWhilePlaying(true)
            updateVolume()
            mp.isLooping = isLoopEnabled
            if (mp.duration > 0 && positionBackup > 0) {
                mp.seekTo(positionBackup)
            }
            mp.start()
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
            activity.supportInvalidateOptionsMenu()
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
                val mp = mediaPlayer ?: return
                if (mp.isPlaying) {
                    mp.pause()
                } else {
                    mp.start()
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


    override fun onDownloadRequested(nonce: Long) {
        super.onDownloadRequested(nonce)
        val context = context
        if (context != null) {
            mediaDownloadEvent = MediaDownloadEvent.create(context, media, nonce)
        } else {
            mediaDownloadEvent = null
        }
    }

    override fun onDownloadStart(total: Long, nonce: Long) {
        super.onDownloadStart(total, nonce)
        mediaDownloadEvent?.let {
            if (it.nonce == nonce) {
                it.setOpenedTime(System.currentTimeMillis())
                it.setSize(total)
            }
        }
    }

    override fun onDownloadFinished(nonce: Long) {
        super.onDownloadFinished(nonce)
        if (mediaDownloadEvent != null && mediaDownloadEvent!!.nonce == nonce) {
            mediaDownloadEvent!!.markEnd()
            HotMobiLogger.getInstance(context).log(accountKey, mediaDownloadEvent!!)
            mediaDownloadEvent = null
        }
    }

    override fun fitSystemWindows(insets: Rect) {
        val lp = videoControl.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.bottomMargin = insets.bottom
            lp.leftMargin = insets.left
            lp.rightMargin = insets.right
        }
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (VideoPageFragment) -> Unit) {
        // No-op
    }

    @SuppressLint("SwitchIntDef")
    private fun getBestVideoUrlAndType(media: ParcelableMedia?, supportedTypes: Array<String>): Pair<String, String>? {
        if (media == null) return null
        when (media.type) {
            ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF -> {
                if (media.video_info == null) {
                    return Pair.create<String, String>(media.media_url, null)
                }
                val firstMatch = media.video_info.variants.filter { variant ->
                    supportedTypes.any { it.equals(variant.content_type, ignoreCase = true) }
                }.sortedByDescending(ParcelableMedia.VideoInfo.Variant::bitrate).firstOrNull() ?: return null
                return Pair.create(firstMatch.url, firstMatch.content_type)
            }
            ParcelableMedia.Type.CARD_ANIMATED_GIF -> {
                return Pair.create<String, String>(media.media_url, "video/mp4")
            }
            else -> {
                return null
            }
        }
    }

    private fun updatePlayerState() {
        val mp = mediaPlayer
        if (mp != null) {
            val playing = mp.isPlaying
            playPauseButton.contentDescription = getString(if (playing) R.string.pause else R.string.play)
            playPauseButton.setImageResource(if (playing) R.drawable.ic_action_pause else R.drawable.ic_action_play_arrow)
        } else {
            playPauseButton.contentDescription = getString(R.string.play)
            playPauseButton.setImageResource(R.drawable.ic_action_play_arrow)
        }
    }

    private fun pauseVideo(): Boolean {
        val mp = mediaPlayer ?: return false
        var result = false
        if (mp.isPlaying) {
            mp.pause()
            result = true
        }
        updatePlayerState()
        return result
    }

    private fun resumeVideo(): Boolean {
        val mp = mediaPlayer ?: return false
        var result = false
        if (!mp.isPlaying) {
            mp.start()
            result = true
        }
        updatePlayerState()
        return result
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
            progressBar.progress = Math.round(1000 * position / duration.toFloat())
            val durationSecs = TimeUnit.SECONDS.convert(duration.toLong(), TimeUnit.MILLISECONDS)
            val positionSecs = TimeUnit.SECONDS.convert(position.toLong(), TimeUnit.MILLISECONDS)
            durationLabel.text = String.format(Locale.ROOT, "%02d:%02d", durationSecs / 60, durationSecs % 60)
            positionLabel.text = String.format(Locale.ROOT, "%02d:%02d", positionSecs / 60, positionSecs % 60)
            handler.postDelayed(this, 16)
        }
    }

    companion object {

        const val EXTRA_LOOP = "loop"
        const val EXTRA_DISABLE_CONTROL = "disable_control"
        const val EXTRA_DEFAULT_MUTE = "default_mute"
        private val SUPPORTED_VIDEO_TYPES: Array<String>
        private val FALLBACK_VIDEO_TYPES: Array<String> = arrayOf("video/mp4")

        init {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                SUPPORTED_VIDEO_TYPES = arrayOf("video/mp4")
            } else {
                SUPPORTED_VIDEO_TYPES = arrayOf("video/webm", "video/mp4")
            }
        }
    }
}