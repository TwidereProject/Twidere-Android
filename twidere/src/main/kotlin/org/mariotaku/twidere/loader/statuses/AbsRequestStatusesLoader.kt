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
import androidx.annotation.WorkerThread
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.extension.model.api.applyLoadLimit
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.PaginatedArrayList
import org.mariotaku.twidere.model.pagination.PaginatedList
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.task.twitter.GetStatusesTask
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.cache.JsonCache
import org.mariotaku.twidere.util.dagger.GeneralComponent
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.math.min

abstract class AbsRequestStatusesLoader(
        context: Context,
        val accountKey: UserKey?,
        adapterData: List<ParcelableStatus>?,
        private val savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        protected val loadingMore: Boolean
) : ParcelableStatusesLoader(context, adapterData, tabPosition, fromUser), IPaginationLoader {
    // Statuses sorted descending by default
    open val comparator: Comparator<ParcelableStatus>? = ParcelableStatus.REVERSE_COMPARATOR

    var exception: MicroBlogException?
        get() = exceptionRef.get()
        private set(value) {
            exceptionRef.set(value)
        }

    override var pagination: Pagination? = null
    override var nextPagination: Pagination? = null
        protected set
    override var prevPagination: Pagination? = null
        protected set

    protected open val isGapEnabled: Boolean = true

    protected val profileImageSize: String = context.getString(R.string.profile_image_size)

    @Inject
    lateinit var jsonCache: JsonCache
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    private val exceptionRef = AtomicReference<MicroBlogException?>()

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
    final override fun loadInBackground(): ListResponse<ParcelableStatus> {
        val context = context
        val comparator = this.comparator
        val accountKey = accountKey ?: return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                return ListResponse.getListInstance<ParcelableStatus>(MicroBlogException("No Account"))

        if (isFirstLoad && tabPosition >= 0) {
            val cached = cachedData
            if (cached != null) {
                data.addAll(cached)
                if (comparator != null) {
                    data.sortWith(comparator)
                } else {
                    data.sort()
                }
                data.forEach { it.is_filtered = shouldFilterStatus(it) }
                return ListResponse.getListInstance(data)
            }
        }
        if (!fromUser) {
            data.forEach { it.is_filtered = shouldFilterStatus(it) }
            return ListResponse.getListInstance(data)
        }
        val noItemsBefore = data.isEmpty()
        val loadItemLimit = preferences[loadItemLimitKey]
        val statuses = try {
            val paging = Paging().apply {
                processPaging(this, details, loadItemLimit)
            }
            getStatuses(details, paging)
        } catch (e: MicroBlogException) {
            // mHandler.post(new ShowErrorRunnable(e));
            exception = e
            DebugLog.w(tr = e)
            return ListResponse.getListInstance(data, e)
        }
        nextPagination = statuses.nextPage
        prevPagination = statuses.previousPage
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
        val deletedOldGap = rowsDeleted > 0 && statuses.foundInPagination()
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
            }
            data.addAll(statuses)
        }

        data.forEach { it.is_filtered = shouldFilterStatus(it) }

        if (comparator != null) {
            data.sortWith(comparator)
        } else {
            data.sort()
        }
        saveCachedData(data)
        return ListResponse.getListInstance(data)
    }

    final override fun onStartLoading() {
        exception = null
        super.onStartLoading()
    }

    @WorkerThread
    protected abstract fun shouldFilterStatus(status: ParcelableStatus): Boolean

    protected open fun processPaging(paging: Paging, details: AccountDetails, loadItemLimit: Int) {
        paging.applyLoadLimit(details, loadItemLimit)
        pagination?.applyTo(paging)
    }

    protected open fun List<ParcelableStatus>.foundInPagination(): Boolean {
        return when (val pagination = this@AbsRequestStatusesLoader.pagination) {
            is SinceMaxPagination -> return any { it.id == pagination.maxId }
            else -> false
        }
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getStatuses(account: AccountDetails, paging: Paging): PaginatedList<ParcelableStatus>

    private fun saveCachedData(data: List<ParcelableStatus>?) {
        val key = serializationKey
        if (key == null || data == null) return
        val databaseItemLimit = preferences[loadItemLimitKey]
        try {
            val statuses = data.subList(0, min(databaseItemLimit, data.size))
            jsonCache.saveList(key, statuses, ParcelableStatus::class.java)
        } catch (e: Exception) {
            // Ignore
            if (e !is IOException) {
                DebugLog.w(LOGTAG, "Error saving cached data", e)
            }
        }

    }

    companion object {
        inline fun <R> List<Status>.mapMicroBlogToPaginated(transform: (Status) -> R): PaginatedList<R> {
            val result = mapTo(PaginatedArrayList(size), transform)
            result.nextPage = SinceMaxPagination().apply { maxId = lastOrNull()?.id }
            return result
        }
    }

}
