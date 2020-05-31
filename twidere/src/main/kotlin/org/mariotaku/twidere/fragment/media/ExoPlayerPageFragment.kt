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

import android.accounts.AccountManager
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import kotlinx.android.synthetic.main.layout_media_viewer_exo_player_view.*
import kotlinx.android.synthetic.main.layout_media_viewer_video_overlay.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.mariotaku.ktextension.contains
import org.mariotaku.mediaviewer.library.MediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.annotation.CacheFileType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_POSITION
import org.mariotaku.twidere.extension.model.authorizationHeader
import org.mariotaku.twidere.extension.model.bannerExtras
import org.mariotaku.twidere.extension.model.getBestVideoUrlAndType
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.EXTRA_PAUSED_BY_USER
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.EXTRA_PLAY_AUDIO
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.SUPPORTED_VIDEO_TYPES
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.accountKey
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.isControlDisabled
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.isLoopEnabled
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.isMutedByDefault
import org.mariotaku.twidere.fragment.media.VideoPageFragment.Companion.media
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.CacheProvider
import org.mariotaku.twidere.task.SaveFileTask
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.media.TwidereMediaDownloader
import org.mariotaku.twidere.util.promotion.PromotionService
import java.io.InputStream
import javax.inject.Inject


/**
 * Successor of `VideoPageFragment`, backed by `ExoPlayer`
 * Created by mariotaku on 2017/2/28.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ExoPlayerPageFragment : MediaViewerFragment(), IBaseFragment<ExoPlayerPageFragment> {

    @Inject
    internal lateinit var dataSourceFactory: DataSource.Factory

    @Inject
    internal lateinit var extractorsFactory: ExtractorsFactory

    @Inject
    internal lateinit var okHttpClient: OkHttpClient

    @Inject
    internal lateinit var promotionService: PromotionService

    private lateinit var mainHandler: Handler

    private var playAudio: Boolean = false
    private var pausedByUser: Boolean = false
    private var playbackCompleted: Boolean = false
    private var positionBackup: Long = -1L
    private var playerHasError: Boolean = false

    private val account by lazy {
        AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
    }

    private val playerListener = object : Player.EventListener {
        override fun onLoadingChanged(isLoading: Boolean) {

        }

        override fun onPlayerError(error: ExoPlaybackException) {
            playerHasError = true
            hideProgress()
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    playerView.keepScreenOn = true
                    showProgress(true, 0f)
                }
                ExoPlayer.STATE_ENDED -> {
                    playbackCompleted = true
                    positionBackup = -1L
                    playerView.keepScreenOn = false

                    // Reset position
                    playerView.player?.let { player ->
                        player.seekTo(0)
                        player.playWhenReady = false
                    }

                    hideProgress()
                    val activity = activity as? MediaViewerActivity
                    activity?.setBarVisibility(true)

                    adContainer.setVisible(true)
                }
                ExoPlayer.STATE_READY -> {
                    playbackCompleted = playWhenReady
                    playerHasError = false
                    playerView.keepScreenOn = playWhenReady
                    hideProgress()

                    adContainer.setVisible(!playWhenReady)
                }
                ExoPlayer.STATE_IDLE -> {
                    playerView.keepScreenOn = false
                    hideProgress()

                    adContainer.setVisible(true)
                }
            }
        }

        override fun onPositionDiscontinuity(position: Int) {
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        }

        override fun onSeekProcessed() {
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
        }

        override fun onShuffleModeEnabledChanged(shuffleMode: Boolean) {
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainHandler = Handler()


        if (savedInstanceState != null) {
            positionBackup = savedInstanceState.getLong(EXTRA_POSITION)
            pausedByUser = savedInstanceState.getBoolean(EXTRA_PAUSED_BY_USER)
            playAudio = savedInstanceState.getBoolean(EXTRA_PLAY_AUDIO)
        } else {
            val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Play audio by default if ringer mode on
            playAudio = !isMutedByDefault && am.ringerMode == AudioManager.RINGER_MODE_NORMAL
        }

        volumeButton.setOnClickListener {
            this.playAudio = !this.playAudio
            updateVolume()
        }
        playerView.useController = !isControlDisabled
        playerView.controllerShowTimeoutMs = 0
        playerView.setOnSystemUiVisibilityChangeListener {
            val visible = MediaViewerActivity.FLAG_SYSTEM_UI_HIDE_BARS !in
                    requireActivity().window.decorView.systemUiVisibility
            if (visible) {
                playerView.showController()
            } else {
                playerView.hideController()
            }
        }
        playerView.setOnTouchListener { _, event ->
            if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false
            val activity = activity as? MediaViewerActivity ?: return@setOnTouchListener false
            val visible = !activity.isBarShowing
            activity.setBarVisibility(visible)
            if (visible) {
                playerView.showController()
            } else {
                playerView.hideController()
            }
            return@setOnTouchListener true
        }
        updateVolume()

        promotionService.loadBanner(adContainer, media?.bannerExtras)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponent.get(context).inject(this)
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

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (activity != null && !isDetached) {
            if (isVisibleToUser) {
                initializePlayer()
            } else {
                releasePlayer()
            }
        }
    }

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_exo_player_view, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        promotionService.setupBanner(adContainer, PromotionService.BannerType.MEDIA_PAUSE)
    }

    override fun onApplySystemWindowInsets(insets: Rect) {
        // HACK: Apply maximum reported system inset to avoid drawing under systum UI
        (videoControl.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
          bottomMargin = maxOf(insets.bottom, bottomMargin)
          leftMargin = maxOf(insets.left, leftMargin)
          rightMargin = maxOf(insets.right, rightMargin)
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
        requestApplyInsets()
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (ExoPlayerPageFragment) -> Unit) = TODO()

    override fun isMediaLoaded(): Boolean {
        return !playerHasError
    }

    override fun isMediaLoading(): Boolean {
        return false
    }

    private fun releasePlayer() {
        val player = playerView.player ?: return
        positionBackup = player.currentPosition
        pausedByUser = !player.playWhenReady
        player.removeListener(playerListener)
        player.release()
        playerView.player = null
    }

    private fun initializePlayer() {
        if (playerView.player != null) return
        playerView.player = run {
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
            val trackSelector = DefaultTrackSelector(requireContext(), videoTrackSelectionFactory)
            val player = SimpleExoPlayer.Builder(requireContext())
              .setTrackSelector(trackSelector)
              .build()
            if (positionBackup >= 0) {
                player.seekTo(positionBackup)
            }
            player.playWhenReady = !pausedByUser
            playerHasError = false
            player.addListener(playerListener)
            return@run player
        }

        val uri = media?.getDownloadUri() ?: return
        val factory = AuthDelegatingDataSourceFactory(uri, account, dataSourceFactory)
        val uriSource = ProgressiveMediaSource.Factory(factory, extractorsFactory).createMediaSource(uri)
        (playerView.player as? SimpleExoPlayer)?.apply {
          repeatMode = if (isLoopEnabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
          prepare(uriSource)
        }
        updateVolume()
    }

    private fun updateVolume() {
        volumeButton.setImageResource(if (playAudio) R.drawable.ic_action_speaker_max else R.drawable.ic_action_speaker_muted)
        (playerView.player as? SimpleExoPlayer)?.apply {
          volume = if (playAudio) 1f else 0f
        }
    }

    private fun ParcelableMedia.getDownloadUri(): Uri? {
        val bestVideoUrlAndType = this.getBestVideoUrlAndType(SUPPORTED_VIDEO_TYPES)
        if (bestVideoUrlAndType != null) {
            return Uri.parse(bestVideoUrlAndType.first)
        }
        return arguments?.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }


    fun getRequestFileInfo(): RequestFileInfo? {
        val uri = media?.getDownloadUri() ?: return null
        return RequestFileInfo(uri, account, okHttpClient)
    }

    class AuthDelegatingDataSourceFactory(
            val uri: Uri,
            val account: AccountDetails?,
            val delegate: DataSource.Factory
    ) : DataSource.Factory {

        override fun createDataSource(): DataSource {
            val source = delegate.createDataSource()
            if (source is HttpDataSource) {
                setAuthorizationHeader(source)
            }
            return source
        }

        private fun setAuthorizationHeader(dataSource: HttpDataSource) {
            val credentials = account?.credentials
            if (credentials != null && TwidereMediaDownloader.isAuthRequired(credentials, uri)) {
                dataSource.setRequestProperty("Authorization", credentials.authorizationHeader(uri))
            }
        }
    }

    class RequestFileInfo(
            val uri: Uri,
            val account: AccountDetails?,
            val okHttpClient: OkHttpClient
    ) : SaveFileTask.FileInfo, CacheProvider.CacheFileTypeSupport {

        private var response: Response? = null

        override val cacheFileType: String? = CacheFileType.VIDEO

        override val fileName: String? = uri.lastPathSegment

        override val mimeType: String?
            get() = request().body()?.contentType()?.toString()

        override val specialCharacter: Char = '_'

        override fun inputStream(): InputStream {
            return request().body()!!.byteStream()
        }

        override fun close() {
            response?.close()
        }

        private fun request(): Response {
            if (response != null) return response!!
            val builder = Request.Builder()
            builder.url(uri.toString())
            val credentials = account?.credentials
            if (credentials != null && TwidereMediaDownloader.isAuthRequired(credentials, uri)) {
                builder.addHeader("Authorization", credentials.authorizationHeader(uri))
            }
            response = okHttpClient.newCall(builder.build()).execute()
            return response!!
        }

    }

}
