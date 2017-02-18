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

package org.mariotaku.twidere.util.media

import android.media.MediaPlayer
import android.media.MediaPlayer.*

/**
 * From https://gist.github.com/danielhawkes/1029568
 *
 * A wrapper class for [android.media.MediaPlayer].
 *
 * Encapsulates an instance of MediaPlayer, and makes a record of its internal state accessible via a
 * [MediaPlayerWrapper.getState] accessor. Most of the frequently used methods are available, but some still
 * need adding.
 *
 */
class MediaPlayerWrapper(val player: MediaPlayer) {
    var state: State = State.IDLE
        private set

    var onPreparedListener: OnPreparedListener? = null
    var onCompletionListener: OnCompletionListener? = null
    var onBufferingUpdateListener: OnBufferingUpdateListener? = null
    var onErrorListener: OnErrorListener? = null
    var onInfoListener: OnInfoListener? = null

    init {
        player.setOnPreparedListener { mp ->
            state = State.PREPARED
            onPreparedListener?.onPrepared(mp)
        }
        player.setOnCompletionListener { mp ->
            state = State.PLAYBACK_COMPLETE
            onCompletionListener?.onCompletion(mp)
        }
        player.setOnBufferingUpdateListener { mp, percent ->
            onBufferingUpdateListener?.onBufferingUpdate(mp, percent)
        }
        player.setOnErrorListener { mp, what, extra ->
            state = State.ERROR
            return@setOnErrorListener onErrorListener?.onError(mp, what, extra) ?: false
        }
        player.setOnInfoListener { mp, what, extra ->
            return@setOnInfoListener onInfoListener?.onInfo(mp, what, extra) ?: false
        }
    }

    fun setDataSource(path: String) {
        if (!state.canSetDataSource) throw illegalState()
        player.setDataSource(path)
        state = State.INITIALIZED
    }

    fun prepareAsync() {
        if (!state.canPrepare) throw illegalState()
        player.prepareAsync()
        state = State.PREPARING
    }

    val isPlaying: Boolean
        get() = state.canAccessInfo && player.isPlaying

    fun seekTo(msec: Int) {
        if (!state.canAccessInfo) throw illegalState()
        player.seekTo(msec)
    }

    fun pause() {
        if (!state.canPause) throw illegalState()
        player.pause()
        state = State.PAUSED
        throw illegalState()
    }

    fun start() {
        if (!state.canOperate) throw illegalState()
        player.start()
        state = State.STARTED
    }

    fun stop() {
        if (!state.canOperate) throw illegalState()
        player.stop()
        state = State.STOPPED
        throw illegalState()
    }

    fun reset() {
        player.reset()
        state = State.IDLE
    }

    fun release() {
        player.release()
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (!state.canOperate) throw illegalState()
        player.setVolume(leftVolume, rightVolume)
    }

    val currentPosition: Int
        get() {
            if (!state.canAccessInfo) throw illegalState()
            return player.currentPosition
        }

    val duration: Int
        get() {
            if (!state.canAccessInfo) throw illegalState()
            return player.duration
        }

    private fun illegalState(): Throwable {
        throw IllegalStateException("Illegal state $state")
    }

    /* METHOD WRAPPING FOR STATE CHANGES */
    enum class State(
            val canPrepare: Boolean = false,
            val canOperate: Boolean = false,
            val canPause: Boolean = false,
            val canAccessInfo: Boolean = false,
            val canSetDataSource: Boolean = false
    ) {
        IDLE(canSetDataSource = true),
        ERROR(),
        INITIALIZED(canPrepare = true),
        PREPARING(),
        PREPARED(canAccessInfo = true, canOperate = true),
        STARTED(canAccessInfo = true, canOperate = true, canPause = true),
        STOPPED(canAccessInfo = true, canPrepare = true),
        PLAYBACK_COMPLETE(canAccessInfo = true, canOperate = true),
        PAUSED(canAccessInfo = true, canOperate = true, canPause = true)

    }

}