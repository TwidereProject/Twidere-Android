package org.mariotaku.twidere.fragment

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.TextView
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
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.media.MediaExtra
import java.util.*
import java.util.concurrent.TimeUnit

class VideoPageFragment : CacheDownloadMediaViewerFragment(), MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, View.OnClickListener {

    private var mPlayAudio: Boolean = false
    private var mVideoProgressRunnable: VideoPlayProgressRunnable? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mMediaPlayerError: Int = 0
    private var mediaDownloadEvent: MediaDownloadEvent? = null

    override fun getDownloadExtra(): Any? {
        val extra = MediaExtra()
        extra.isUseThumbor = false
        val fallbackUrlAndType = getBestVideoUrlAndType(media, FALLBACK_VIDEO_TYPES)
        if (fallbackUrlAndType != null) {
            extra.fallbackUrl = fallbackUrlAndType.first
        }
        return extra
    }

    val isLoopEnabled: Boolean
        get() = arguments.getBoolean(EXTRA_LOOP, false)

    override fun isAbleToLoad(): Boolean {
        return downloadUri != null
    }

    override fun getDownloadUri(): Uri? {
        val bestVideoUrlAndType = getBestVideoUrlAndType(media,
                SUPPORTED_VIDEO_TYPES)
        if (bestVideoUrlAndType != null && bestVideoUrlAndType.first != null) {
            return Uri.parse(bestVideoUrlAndType.first)
        }
        return arguments.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }


    override fun displayMedia(result: CacheDownloadLoader.Result) {
        videoView.setVideoURI(result.cacheUri)
        videoControl.visibility = View.GONE
        setMediaViewVisible(true)
        val activity = activity
        activity?.supportInvalidateOptionsMenu()
    }

    override fun recycleMedia() {

    }

    override fun onCompletion(mp: MediaPlayer) {
        updatePlayerState()
        //            mVideoViewProgress.removeCallbacks(mVideoProgressRunnable);
        //            mVideoViewProgress.setVisibility(View.GONE);
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        mediaPlayer = null
        videoViewProgress.removeCallbacks(mVideoProgressRunnable)
        videoViewProgress.visibility = View.GONE
        videoControl.visibility = View.GONE
        mMediaPlayerError = what
        return true
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (userVisibleHint) {
            mediaPlayer = mp
            mMediaPlayerError = 0
            mp.setScreenOnWhilePlaying(true)
            updateVolume()
            mp.isLooping = isLoopEnabled
            mp.start()
            videoViewProgress.visibility = View.VISIBLE
            videoViewProgress.post(mVideoProgressRunnable)
            updatePlayerState()
            videoControl.visibility = View.VISIBLE
        }
    }

    private fun updateVolume() {

        volumeButton.setImageResource(if (mPlayAudio) R.drawable.ic_action_speaker_max else R.drawable.ic_action_speaker_muted)
        val mp = mediaPlayer ?: return
        try {
            if (mPlayAudio) {
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        var handler: Handler? = videoViewProgress.handler
        if (handler == null) {
            handler = Handler(activity.mainLooper)
        }

        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Play audio by default if ringer mode on
        mPlayAudio = am.ringerMode == AudioManager.RINGER_MODE_NORMAL

        mVideoProgressRunnable = VideoPlayProgressRunnable(handler, videoViewProgress,
                durationLabel, positionLabel, videoView)


        videoViewOverlay.setOnClickListener(this)
        videoView.setOnPreparedListener(this)
        videoView.setOnErrorListener(this)
        videoView.setOnCompletionListener(this)

        playPauseButton.setOnClickListener(this)
        volumeButton.setOnClickListener(this)
        videoControl.visibility = View.GONE
        startLoading(false)
        setMediaViewVisible(false)
        updateVolume()
    }

    @SuppressLint("SwitchIntDef")
    private fun getBestVideoUrlAndType(media: ParcelableMedia?,
                                       supportedTypes: Array<String>): Pair<String, String>? {
        if (media == null) return null
        when (media.type) {
            ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF -> {
                if (media.video_info == null) {
                    return Pair.create<String, String>(media.media_url, null)
                }
                val firstMatch = media.video_info.variants.first { variant ->
                    supportedTypes.any { it.equals(variant.content_type, ignoreCase = true) }
                } ?: return null
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


    override fun onClick(v: View) {
        when (v.id) {
            R.id.volumeButton -> {
                mPlayAudio = !mPlayAudio
                updateVolume()
            }
            R.id.playPauseButton -> {
                val mp = mediaPlayer
                if (mp != null) {
                    if (mp.isPlaying) {
                        mp.pause()
                    } else {
                        mp.start()
                    }
                }
                updatePlayerState()
            }
            R.id.videoViewOverlay -> {
                val activity = activity as MediaViewerActivity
                if (videoControl.visibility == View.VISIBLE) {
                    videoControl.visibility = View.GONE
                    activity.setBarVisibility(false)
                } else {
                    videoControl.visibility = View.VISIBLE
                    activity.setBarVisibility(true)
                }
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

    public override fun onCreateMediaView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    private val media: ParcelableMedia?
        get() = arguments.getParcelable<ParcelableMedia>(EXTRA_MEDIA)

    private val accountKey: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)

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
        private val SUPPORTED_VIDEO_TYPES: Array<String>
        private val FALLBACK_VIDEO_TYPES: Array<String>

        init {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                SUPPORTED_VIDEO_TYPES = arrayOf("video/mp4")
            } else {
                SUPPORTED_VIDEO_TYPES = arrayOf("video/webm", "video/mp4")
            }
            FALLBACK_VIDEO_TYPES = arrayOf("video/mp4")
        }
    }
}