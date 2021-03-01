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

package org.mariotaku.twidere.util.sync

import android.content.Context
import android.net.Uri
import androidx.collection.ArrayMap
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import okio.ByteString.Companion.encodeUtf8
import org.mariotaku.twidere.TwidereConstants.TIMELINE_SYNC_CACHE_PREFERENCES_NAME
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.util.DebugLog
import java.util.*

/**
 * Created by mariotaku on 2017/4/13.
 */

abstract class TimelineSyncManager(val context: Context) {

    private val stagedCommits = ArrayMap<TimelineKey, Long>()
    private val cachedPositions = context.getSharedPreferences(TIMELINE_SYNC_CACHE_PREFERENCES_NAME,
            Context.MODE_PRIVATE)

    fun setPosition(@ReadPositionTag positionTag: String, currentTag: String?, positionKey: Long) {
        stagedCommits[TimelineKey(positionTag, currentTag)] = positionKey
    }

    fun commit() {
        val data = stagedCommits.map { (key, value) ->
            PositionData(key.positionTag, key.currentTag, value)
        }.toTypedArray()
        stagedCommits.clear()
        if (data.isEmpty()) return
        putAllPosition(data)
    }

    fun fetchSingle(@ReadPositionTag positionTag: String, currentTag: String?) {
        getSinglePosition(positionTag, currentTag).then { position ->
            cachedPositions.edit().putLong(cacheKey(positionTag, currentTag), position).apply()
        }.fail {
            DebugLog.w(tr = it)
        }
    }

    fun peekPosition(@ReadPositionTag positionTag: String, currentTag: String?): Long {
        val cacheKey = cacheKey(positionTag, currentTag)
        val position = cachedPositions.getLong(cacheKey, -1)
        cachedPositions.edit().remove(cacheKey).apply()
        return position
    }

    protected abstract fun putAllPosition(data: Array<PositionData>): Promise<Unit, Exception>

    protected abstract fun getSinglePosition(@ReadPositionTag positionTag: String, currentTag: String?):
            Promise<Long, Exception>

    data class TimelineKey(val positionTag: String, val currentTag: String?)
    data class PositionData(val positionTag: String, val currentTag: String?, val positionKey: Long)

    abstract class Factory {
        protected var manager: TimelineSyncManager? = null

        fun get(): TimelineSyncManager? = manager

        fun setup(context: Context) {
            manager = create(context)
        }

        protected abstract fun create(context: Context): TimelineSyncManager?

    }

    object DummyFactory : Factory() {
        override fun create(context: Context): Nothing? = null
    }

    companion object {
        fun newFactory(): Factory = ServiceLoader.load(Factory::class.java).firstOrNull() ?: DummyFactory

        private fun cacheKey(@ReadPositionTag positionTag: String, currentTag: String?): String {
            if (currentTag == null) return positionTag
            return "$positionTag:${Uri.encode(currentTag)}".encodeUtf8().sha1().hex()
        }
    }


}
