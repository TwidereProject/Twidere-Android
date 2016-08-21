/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.loader

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import android.util.Log
import com.nostra13.universalimageloader.cache.disc.DiskCache
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

abstract class MicroBlogAPIStatusesLoader(
        context: Context,
        val accountKey: UserKey?,
        val sinceId: String?,
        val maxId: String?,
        val page: Int,
        adapterData: List<ParcelableStatus>?,
        private val savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        private val loadingMore: Boolean
) : ParcelableStatusesLoader(context, adapterData, tabPosition, fromUser) {
    // Statuses sorted descending by default
    var comparator: Comparator<ParcelableStatus>? = ParcelableStatus.REVERSE_COMPARATOR
    private val exceptionRef = AtomicReference<MicroBlogException?>()

    var exception: MicroBlogException?
        get() = exceptionRef.get()
        private set(value) {
            exceptionRef.set(value)
        }
    @Inject
    lateinit var fileCache: DiskCache
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @SuppressWarnings("unchecked")
    override fun loadInBackground(): ListResponse<ParcelableStatus> {
        val context = context
        val accountKey = accountKey ?: return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))
        val credentials = ParcelableCredentialsUtils.getCredentials(context,
                accountKey) ?: return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))

        var data: MutableList<ParcelableStatus>? = data
        if (data == null) {
            data = CopyOnWriteArrayList<ParcelableStatus>()
        }
        if (isFirstLoad && tabPosition >= 0) {
            val cached = cachedData
            if (cached != null) {
                data.addAll(cached)
                if (comparator != null) {
                    Collections.sort(data, comparator)
                } else {
                    Collections.sort(data)
                }
                return ListResponse.getListInstance(CopyOnWriteArrayList(data))
            }
        }
        if (!fromUser) return ListResponse.getListInstance(data)
        val twitter = MicroBlogAPIFactory.getInstance(context, credentials, true,
                true) ?: return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))
        val statuses: List<Status>
        val noItemsBefore = data.isEmpty()
        val loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT)
        try {
            val paging = Paging()
            processPaging(credentials, loadItemLimit, paging)
            statuses = getStatuses(twitter, credentials, paging)
        } catch (e: MicroBlogException) {
            // mHandler.post(new ShowErrorRunnable(e));
            exception = e
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e)
            }
            return ListResponse.getListInstance(CopyOnWriteArrayList(data), e)
        }

        val statusIds = arrayOfNulls<String>(statuses.size)
        var minIdx = -1
        var rowsDeleted = 0
        for (i in 0 until statuses.size) {
            val status = statuses[i]
            if (minIdx == -1 || status < statuses[minIdx]) {
                minIdx = i
            }
            statusIds[i] = status.id
            if (deleteStatus(data, status.id)) {
                rowsDeleted++
            }
        }

        // Insert a gap.
        val deletedOldGap = rowsDeleted > 0 && statusIds.contains(maxId)
        val noRowsDeleted = rowsDeleted == 0
        val insertGap = minIdx != -1 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && statuses.size >= loadItemLimit && !loadingMore
        for (i in 0 until statuses.size) {
            val status = statuses[i]
            val item = ParcelableStatusUtils.fromStatus(status, accountKey, insertGap && isGapEnabled && minIdx == i)
            ParcelableStatusUtils.updateExtraInformation(item, credentials, userColorNameManager)
            data.add(item)
        }

        val db = TwidereApplication.getInstance(context).sqLiteDatabase
        val array = data.toTypedArray()
        var i = 0
        val size = array.size
        while (i < size) {
            val status = array[i]
            val filtered = shouldFilterStatus(db, status)
            if (filtered) {
                if (!status.is_gap && i != size - 1) {
                    data.remove(status)
                } else {
                    status.is_filtered = true
                }
            }
            i++
        }
        if (comparator != null) {
            Collections.sort(data, comparator)
        } else {
            Collections.sort(data)
        }
        saveCachedData(data)
        return ListResponse.getListInstance(CopyOnWriteArrayList(data))
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getStatuses(microBlog: MicroBlog,
                                       credentials: ParcelableCredentials,
                                       paging: Paging): List<Status>

    @WorkerThread
    protected abstract fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean


    override fun onStartLoading() {
        exception = null
        super.onStartLoading()
    }

    protected open fun processPaging(credentials: ParcelableCredentials, loadItemLimit: Int, paging: Paging) {
        paging.setCount(loadItemLimit)
        if (maxId != null) {
            paging.setMaxId(maxId)
        }
        if (sinceId != null) {
            paging.setSinceId(sinceId)
            if (maxId == null) {
                paging.setLatestResults(true)
            }
        }
    }

    protected open val isGapEnabled: Boolean
        get() = true

    private val cachedData: List<ParcelableStatus>?
        get() {
            val key = serializationKey ?: return null
            val file = fileCache.get(key) ?: return null
            return JsonSerializer.parseList(file, ParcelableStatus::class.java)
        }

    private val serializationKey: String?
        get() {
            if (savedStatusesArgs == null) return null
            return TwidereArrayUtils.toString(savedStatusesArgs, '_', false)
        }

    private fun saveCachedData(data: List<ParcelableStatus>?) {
        val key = serializationKey
        if (key == null || data == null) return
        val databaseItemLimit = preferences.getInt(KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT)
        try {
            val statuses = data.subList(0, Math.min(databaseItemLimit, data.size))
            val pos = PipedOutputStream()
            val pis = PipedInputStream(pos)
            val future = pool.submit(Callable<kotlin.Any> {
                LoganSquareMapperFinder.mapperFor(ParcelableStatus::class.java).serialize(statuses, pos)
                null
            })
            val saved = fileCache.save(key, pis) { current, total -> !future.isDone }
            if (BuildConfig.DEBUG) {
                Log.v(LOGTAG, key + " saved: " + saved)
            }
        } catch (e: Exception) {
            // Ignore
            if (BuildConfig.DEBUG && e !is IOException) {
                Log.w(LOGTAG, e)
            }
        }

    }

    companion object {

        private val pool = Executors.newSingleThreadExecutor()
    }

}
