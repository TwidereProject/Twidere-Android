package org.mariotaku.twidere.task.twitter

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.squareup.otto.Bus
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.RefreshEvent
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.ParcelableStatusValuesCreator
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.GetStatusesTaskEvent
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.CacheUsersStatusesTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.util.*
import javax.inject.Inject

/**
 * Created by mariotaku on 16/1/2.
 */
abstract class GetStatusesTask(protected val context: Context) : AbstractTask<RefreshTaskParam, List<TwitterWrapper.StatusListResponse>, Any>(), Constants {
    @Inject
    lateinit var preferences: KPreferences
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    lateinit var manager: UserColorNameManager
    @Inject
    lateinit var wrapper: AsyncTwitterWrapper

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(MicroBlogException::class)
    abstract fun getStatuses(twitter: MicroBlog, paging: Paging): ResponseList<Status>

    protected abstract val contentUri: Uri

    protected abstract val timelineType: String

    public override fun afterExecute(handler: Any?, result: List<TwitterWrapper.StatusListResponse>?) {
        context.contentResolver.notifyChange(contentUri, null)
        bus.post(GetStatusesTaskEvent(contentUri, false, AsyncTwitterWrapper.getException(result)))
    }

    override fun beforeExecute() {
        bus.post(GetStatusesTaskEvent(contentUri, true, null))
    }

    protected abstract val errorInfoKey: String

    public override fun doLongOperation(param: RefreshTaskParam): List<TwitterWrapper.StatusListResponse> {
        if (param.shouldAbort) return emptyList()
        val accountKeys = param.accountKeys
        val maxIds = param.maxIds
        val sinceIds = param.sinceIds
        val maxSortIds = param.maxSortIds
        val sinceSortIds = param.sinceSortIds
        val result = ArrayList<TwitterWrapper.StatusListResponse>()
        val loadItemLimit = preferences[loadItemLimitKey]
        for (i in 0 until accountKeys.size) {
            val accountKey = accountKeys[i]
            val credentials = ParcelableCredentialsUtils.getCredentials(context,
                    accountKey) ?: continue
            val twitter = MicroBlogAPIFactory.getInstance(context, credentials,
                    true, true) ?: continue
            try {
                val paging = Paging()
                paging.count(loadItemLimit)
                val maxId: String?
                val sinceId: String?
                var maxSortId: Long = -1
                var sinceSortId: Long = -1
                if (maxIds != null && maxIds[i] != null) {
                    maxId = maxIds[i]
                    paging.maxId(maxId)
                    if (maxSortIds != null) {
                        maxSortId = maxSortIds[i]
                    }
                } else {
                    maxSortId = -1
                    maxId = null
                }
                if (sinceIds != null && sinceIds[i] != null) {
                    sinceId = sinceIds[i]
                    val sinceIdLong = NumberUtils.toLong(sinceId, -1)
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1L) {
                        paging.sinceId((sinceIdLong - 1).toString())
                    } else {
                        paging.sinceId(sinceId)
                    }
                    if (sinceSortIds != null) {
                        sinceSortId = sinceSortIds[i]
                    }
                    if (maxIds == null) {
                        paging.setLatestResults(true)
                    }
                } else {
                    sinceId = null
                }
                val statuses = getStatuses(twitter, paging)
                storeStatus(accountKey, credentials, statuses, sinceId, maxId, sinceSortId,
                        maxSortId, loadItemLimit, false)
                // TODO cache related data and preload
                val cacheTask = CacheUsersStatusesTask(context)
                cacheTask.params = TwitterWrapper.StatusListResponse(accountKey, statuses)
                TaskStarter.execute(cacheTask)
                errorInfoStore.remove(errorInfoKey, accountKey.id)
            } catch (e: MicroBlogException) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e)
                }
                if (e.isCausedByNetworkIssue) {
                    errorInfoStore.put(errorInfoKey, accountKey.id,
                            ErrorInfoStore.CODE_NETWORK_ERROR)
                }
                result.add(TwitterWrapper.StatusListResponse(accountKey, e))
            }
        }
        return result
    }

    private fun storeStatus(accountKey: UserKey, credentials: ParcelableCredentials,
                            statuses: List<Status>,
                            sinceId: String?, maxId: String?,
                            sinceSortId: Long, maxSortId: Long,
                            loadItemLimit: Int, notify: Boolean) {
        val uri = contentUri
        val writeUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify)
        val resolver = context.contentResolver
        val noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountKey) <= 0
        val values = arrayOfNulls<ContentValues>(statuses.size)
        val statusIds = arrayOfNulls<String>(statuses.size)
        var minIdx = -1
        var minPositionKey: Long = -1
        var hasIntersection = false
        if (!statuses.isEmpty()) {
            val firstSortId = statuses.first().sortId
            val lastSortId = statuses.last().sortId
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId

            for (i in 0 until statuses.size) {
                val item = statuses[i]
                val status = ParcelableStatusUtils.fromStatus(item, accountKey,
                        false)
                ParcelableStatusUtils.updateExtraInformation(status, credentials, manager)
                status.position_key = getPositionKey(status.timestamp, status.sort_id, lastSortId,
                        sortDiff, i, statuses.size)
                status.inserted_date = System.currentTimeMillis()
                values[i] = ParcelableStatusValuesCreator.create(status)
                if (minIdx == -1 || item < statuses[minIdx]) {
                    minIdx = i
                    minPositionKey = status.position_key
                }
                if (sinceId != null && item.sortId <= sinceSortId) {
                    hasIntersection = true
                }
                statusIds[i] = item.id
            }
        }
        // Delete all rows conflicting before new data inserted.
        val accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY)
        val statusWhere = Expression.inArgs(Columns.Column(Statuses.STATUS_ID),
                statusIds.size)
        val deleteWhere = Expression.and(accountWhere, statusWhere).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), *statusIds)
        var olderCount = -1
        if (minPositionKey > 0) {
            olderCount = DataStoreUtils.getStatusesCount(context, uri, null, minPositionKey,
                    Statuses.POSITION_KEY, false, arrayOf(accountKey))
        }
        val rowsDeleted = resolver.delete(writeUri, deleteWhere, deleteWhereArgs)

        // BEGIN HotMobi
        val event = RefreshEvent.create(context, statusIds, timelineType)
        HotMobiLogger.getInstance(context).log(accountKey, event)
        // END HotMobi

        // Insert a gap.
        val deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, maxId)
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
            val noGapValues = ContentValues()
            noGapValues.put(Statuses.IS_GAP, false)
            val noGapWhere = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.STATUS_ID)).sql
            val noGapWhereArgs = arrayOf(accountKey.toString(), maxId)
            resolver.update(writeUri, noGapValues, noGapWhere, noGapWhereArgs)
        }
    }

    companion object {

        fun getPositionKey(timestamp: Long, sortId: Long, lastSortId: Long, sortDiff: Long,
                           position: Int, count: Int): Long {
            if (sortDiff == 0L) return timestamp
            val extraValue: Int
            if (sortDiff > 0) {
                // descent sorted by time
                extraValue = count - 1 - position
            } else {
                // ascent sorted by time
                extraValue = position
            }
            return timestamp + (sortId - lastSortId) * (499 - count) / sortDiff + extraValue.toLong()
        }
    }

}
