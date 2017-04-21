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

package org.mariotaku.twidere.loader.statuses

import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.support.annotation.WorkerThread
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.task.twitter.GetStatusesTask
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.cache.JsonCache
import org.mariotaku.twidere.util.dagger.GeneralComponent
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

abstract class AbsRequestStatusesLoader(
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

    var exception: MicroBlogException?
        get() = exceptionRef.get()
        private set(value) {
            exceptionRef.set(value)
        }

    @Inject
    lateinit var jsonCache: JsonCache
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    private val exceptionRef = AtomicReference<MicroBlogException?>()
    protected val profileImageSize: String = context.getString(R.string.profile_image_size)

    private val cachedData: List<ParcelableStatus>?
        get() {
            val key = serializationKey ?: return null
            return jsonCache.getList(key, ParcelableStatus::class.java)
        }

    private val serializationKey: String?
        get() = savedStatusesArgs?.joinToString("_")

    init {
        GeneralComponent.get(context).inject(this)
    }

    @SuppressWarnings("unchecked")
    override final fun loadInBackground(): ListResponse<ParcelableStatus> {
        val context = context
        val accountKey = accountKey ?: return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))

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
        val noItemsBefore = data.isEmpty()
        val loadItemLimit = preferences[loadItemLimitKey]
        val statuses = try {
            val paging = Paging().apply {
                processPaging(details, loadItemLimit, this)
            }
            getStatuses(details, paging)
        } catch (e: MicroBlogException) {
            // mHandler.post(new ShowErrorRunnable(e));
            exception = e
            DebugLog.w(tr = e)
            return ListResponse.getListInstance(CopyOnWriteArrayList(data), e)
        }

        var minIdx = -1
        var rowsDeleted = 0
        for (i in 0 until statuses.size) {
            val status = statuses[i]
            if (minIdx == -1 || status < statuses[minIdx]) {
                minIdx = i
            }
            if (deleteStatus(data, status.id)) {
                rowsDeleted++
            }
        }

        // Insert a gap.
        val deletedOldGap = rowsDeleted > 0 && statuses.any { it.id == maxId }
        val noRowsDeleted = rowsDeleted == 0
        val insertGap = minIdx != -1 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && statuses.size >= loadItemLimit && !loadingMore

        if (statuses.isNotEmpty()) {
            val firstSortId = statuses.first().sort_id
            val lastSortId = statuses.last().sort_id
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId
            statuses.forEachIndexed { i, status ->
                status.is_gap = insertGap && isGapEnabled && minIdx == i
                status.position_key = GetStatusesTask.getPositionKey(status.timestamp, status.sort_id,
                        lastSortId, sortDiff, i, statuses.size)
                ParcelableStatusUtils.updateExtraInformation(status, details)
            }
            data.addAll(statuses)
        }

        val db = TwidereApplication.getInstance(context).sqLiteDatabase
        data.forEach { it.is_filtered = shouldFilterStatus(db, it) }

        if (comparator != null) {
            data.sortWith(comparator!!)
        } else {
            data.sort()
        }
        saveCachedData(data)
        return ListResponse.getListInstance(CopyOnWriteArrayList(data))
    }

    override final fun onStartLoading() {
        exception = null
        super.onStartLoading()
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus>


    @WorkerThread
    protected abstract fun shouldFilterStatus(database: SQLiteDatabase, status: ParcelableStatus): Boolean

    protected open fun processPaging(details: AccountDetails, loadItemLimit: Int, paging: Paging) {
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

    private fun saveCachedData(data: List<ParcelableStatus>?) {
        val key = serializationKey
        if (key == null || data == null) return
        val databaseItemLimit = preferences[loadItemLimitKey]
        try {
            val statuses = data.subList(0, Math.min(databaseItemLimit, data.size))
            jsonCache.saveList(key, statuses, ParcelableStatus::class.java)
        } catch (e: Exception) {
            // Ignore
            if (e !is IOException) {
                DebugLog.w(LOGTAG, "Error saving cached data", e)
            }
        }

    }

}
