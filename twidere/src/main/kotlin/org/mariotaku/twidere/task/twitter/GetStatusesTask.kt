package org.mariotaku.twidere.task.twitter

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY_CHANGE
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.applyLoadLimit
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.GetStatusesTaskEvent
import org.mariotaku.twidere.model.task.GetTimelineResult
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.task.cache.CacheTimelineResultTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.TimelineSyncManager

/**
 * Created by mariotaku on 16/1/2.
 */
abstract class GetStatusesTask(
        context: Context
) : BaseAbstractTask<RefreshTaskParam, List<Pair<GetTimelineResult<ParcelableStatus>?, Exception?>>,
        (Boolean) -> Unit>(context) {

    protected abstract val contentUri: Uri

    @FilterScope
    protected abstract val filterScopes: Int

    protected abstract val errorInfoKey: String

    override fun doLongOperation(param: RefreshTaskParam): List<Pair<GetTimelineResult<ParcelableStatus>?, Exception?>> {
        if (param.shouldAbort) return emptyList()
        val accountKeys = param.accountKeys.takeIf { it.isNotEmpty() } ?: return emptyList()
        val loadItemLimit = preferences[loadItemLimitKey]
        val result = accountKeys.mapIndexed { i, accountKey ->
            try {
                val account = AccountUtils.getAccountDetails(AccountManager.get(context),
                        accountKey, true) ?: throw AccountNotFoundException()
                val paging = Paging()
                paging.applyLoadLimit(account, loadItemLimit)
                val maxId = param.getMaxId(i)
                val sinceId = param.getSinceId(i)
                val maxSortId = param.getMaxSortId(i)
                val sinceSortId = param.getSinceSortId(i)
                if (maxId != null) {
                    paging.maxId(maxId)
                }
                if (sinceId != null) {
                    val sinceIdLong = sinceId.toLongOr(-1L)
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1L) {
                        paging.sinceId((sinceIdLong - 1).toString())
                    } else {
                        paging.sinceId(sinceId)
                    }

                    if (maxId == null) {
                        paging.setLatestResults(true)
                    }
                }
                val timelineResult = getStatuses(account, paging)
                val storeResult = storeStatus(account, timelineResult.data, sinceId, maxId,
                        sinceSortId, maxSortId, loadItemLimit, false)
                // TODO cache related data and preload
                errorInfoStore.remove(errorInfoKey, accountKey.id)
                if (storeResult != 0) {
                    throw GetTimelineException(storeResult)
                }
                return@mapIndexed Pair(timelineResult, null)
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
                if (e.isCausedByNetworkIssue) {
                    errorInfoStore[errorInfoKey, accountKey.id] = ErrorInfoStore.CODE_NETWORK_ERROR
                } else if (e.statusCode == 401) {
                    // Unauthorized
                }
                return@mapIndexed Pair(null, e)
            } catch (e: GetTimelineException) {
                return@mapIndexed Pair(null, e)
            }
        }
        val manager = timelineSyncManagerFactory.get()
        if (manager != null && syncPreferences.isSyncEnabled(SyncTaskRunner.SYNC_TYPE_TIMELINE_POSITIONS)) {
            if (param.isBackground) {
                syncFetchReadPosition(manager, accountKeys)
            }
        }
        return result
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, results: List<Pair<GetTimelineResult<ParcelableStatus>?, Exception?>>) {
        context.contentResolver.notifyChange(contentUri, null)
        val exception = results.firstOrNull { it.second != null }?.second
        bus.post(GetStatusesTaskEvent(contentUri, false, exception))
        cacheItems(context, results)
        handler?.invoke(true)
    }


    override fun beforeExecute() {
        bus.post(GetStatusesTaskEvent(contentUri, true, null))
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getStatuses(account: AccountDetails, paging: Paging): GetTimelineResult<ParcelableStatus>

    protected abstract fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>)

    private fun storeStatus(account: AccountDetails, statuses: List<ParcelableStatus>,
            sinceId: String?, maxId: String?, sinceSortId: Long, maxSortId: Long,
            loadItemLimit: Int, notify: Boolean): Int {
        val accountKey = account.key
        val uri = contentUri
        val writeUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY_CHANGE, notify)
        val resolver = context.contentResolver
        val noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountKey) <= 0
        val values = arrayOfNulls<ContentValues>(statuses.size)
        val statusIds = arrayOfNulls<String>(statuses.size)
        var minIdx = -1
        var minPositionKey: Long = -1
        var hasIntersection = false
        if (statuses.isNotEmpty()) {
            val firstSortId = statuses.first().sort_id
            val lastSortId = statuses.last().sort_id
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId

            val creator = ObjectCursor.valuesCreatorFrom(ParcelableStatus::class.java)
            statuses.forEachIndexed { i, status ->
                status.position_key = getPositionKey(status.timestamp, status.sort_id, lastSortId,
                        sortDiff, i, statuses.size)
                status.inserted_date = System.currentTimeMillis()
                mediaPreloader.preloadStatus(status)
                values[i] = creator.create(status)
                if (minIdx == -1 || status < statuses[minIdx]) {
                    minIdx = i
                    minPositionKey = status.position_key
                }
                if (sinceId != null && status.sort_id <= sinceSortId) {
                    hasIntersection = true
                }
                statusIds[i] = status.id
            }
        }
        // Delete all rows conflicting before new data inserted.
        val accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY)
        val statusWhere = Expression.inArgs(Columns.Column(Statuses.ID),
                statusIds.size)
        val deleteWhere = Expression.and(accountWhere, statusWhere).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), *statusIds)
        var olderCount = -1
        if (minPositionKey > 0) {
            olderCount = DataStoreUtils.getStatusesCount(context, preferences, uri, null,
                    Statuses.POSITION_KEY, minPositionKey, false, arrayOf(accountKey),
                    filterScopes)
        }
        val rowsDeleted = resolver.delete(writeUri, deleteWhere, deleteWhereArgs)

        // Insert a gap.
        val deletedOldGap = rowsDeleted > 0 && maxId in statusIds
        val noRowsDeleted = rowsDeleted == 0
        // Why loadItemLimit / 2? because it will not acting strange in most cases
        val insertGap = minIdx != -1 && olderCount > 0 && (noRowsDeleted || deletedOldGap)
                && !noItemsBefore && !hasIntersection && statuses.size > loadItemLimit / 2
        if (insertGap) {
            values[minIdx]!!.put(Statuses.IS_GAP, true)
        }
        // Insert previously fetched items.
        ContentResolverUtils.bulkInsert(resolver, writeUri, values)

        // Remove gap flag
        if (maxId != null && sinceId == null) {
            if (statuses.isNotEmpty()) {
                // Only remove when actual result returned, otherwise it seems that gap is too old to load
                val noGapValues = ContentValues()
                noGapValues.put(Statuses.IS_GAP, false)
                val noGapWhere = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                        Expression.equalsArgs(Statuses.ID)).sql
                val noGapWhereArgs = arrayOf(accountKey.toString(), maxId)
                resolver.update(writeUri, noGapValues, noGapWhere, noGapWhereArgs)
            } else {
                return ERROR_LOAD_GAP
            }
        }
        return 0
    }

    class GetTimelineException(val code: Int) : Exception() {
        fun getToastMessage(context: Context): String {
            when (code) {
                ERROR_LOAD_GAP -> return context.getString(R.string.message_toast_unable_to_load_more_statuses)
            }
            return context.getString(R.string.error_unknown_error)
        }
    }

    companion object {

        const val ERROR_LOAD_GAP = 1

        fun getPositionKey(timestamp: Long, sortId: Long, lastSortId: Long, sortDiff: Long,
                position: Int, count: Int): Long {
            if (sortDiff == 0L) return timestamp
            val extraValue: Int = if (sortDiff > 0) {
                // descent sorted by time
                count - 1 - position
            } else {
                // ascent sorted by time
                position
            }
            return timestamp + (sortId - lastSortId) * (499 - count) / sortDiff + extraValue.toLong()
        }

        fun cacheItems(context: Context, results: List<Pair<GetTimelineResult<*>?, Exception?>>) {
            results.forEach { (result, _) ->
                if (result == null) return@forEach
                val account = result.account
                val task = CacheTimelineResultTask(context, result,
                        account.type == AccountType.STATUSNET || account.isOfficial(context))
                TaskStarter.execute(task)
            }
        }
    }

}
