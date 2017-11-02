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

package org.mariotaku.twidere.task.statuses

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.addTo
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY_CHANGE
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.applyLoadLimit
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.GetStatusesTaskEvent
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
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

abstract class GetStatusesTask<P : ContentRefreshParam>(
        context: Context
) : BaseAbstractTask<P, List<Pair<GetTimelineResult<ParcelableStatus>?, Exception?>>,
        (Boolean) -> Unit>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    protected abstract val contentUri: Uri

    @FilterScope
    protected abstract val filterScopes: Int

    protected abstract val errorInfoKey: String

    override fun doLongOperation(param: P): List<Pair<GetTimelineResult<ParcelableStatus>?, Exception?>> {
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
                val storeResult = storeStatus(account, timelineResult.data, param, sinceId, maxId,
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
    protected fun getStatuses(account: AccountDetails, paging: Paging): GetTimelineResult<ParcelableStatus> {
        val fetcher = getStatusesFetcher(params)
        when (account.type) {
            AccountType.TWITTER -> {
                val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val timeline = fetcher.forTwitter(account, twitter, paging, null)
                val statuses = timeline.map {
                    it.toParcelable(account, profileImageSize)
                }
                val hashtags = timeline.flatMap { status ->
                    status.entities?.hashtags?.map { it.text }.orEmpty()
                }
                return GetTimelineResult(account, statuses, extractMicroBlogUsers(timeline, account), hashtags)
            }
            AccountType.STATUSNET -> {
                val statusnet = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val timeline = fetcher.forStatusNet(account, statusnet, paging, null)
                val statuses = timeline.map {
                    it.toParcelable(account, profileImageSize)
                }
                val hashtags = timeline.flatMap { status ->
                    status.entities?.hashtags?.map { it.text }.orEmpty()
                }
                return GetTimelineResult(account, statuses, extractMicroBlogUsers(timeline, account), hashtags)
            }
            AccountType.FANFOU -> {
                val fanfou = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val timeline = fetcher.forFanfou(account, fanfou, paging, null)
                val statuses = timeline.map {
                    it.toParcelable(account, profileImageSize)
                }
                val hashtags = statuses.flatMap { status ->
                    return@flatMap status.extractFanfouHashtags()
                }
                return GetTimelineResult(account, statuses, extractMicroBlogUsers(timeline, account), hashtags)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                val timeline = fetcher.forMastodon(account, mastodon, paging, null)
                return GetTimelineResult(account, timeline.map {
                    it.toParcelable(account)
                }, timeline.flatMap { status ->
                    val mapResult = mutableListOf(status.account.toParcelable(account))
                    status.reblog?.account?.toParcelable(account)?.addTo(mapResult)
                    return@flatMap mapResult
                }, timeline.flatMap { status ->
                    status.tags?.map { it.name }.orEmpty()
                })
            }
            else -> throw UnsupportedOperationException()
        }
    }

    protected abstract fun getStatusesFetcher(params: P?): StatusesFetcher

    protected abstract fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>)

    private fun extractMicroBlogUsers(timeline: List<Status>, account: AccountDetails): List<ParcelableUser> {
        return timeline.flatMap { status ->
            val mapResult = mutableListOf(status.user.toParcelable(account,
                    profileImageSize = profileImageSize))
            status.retweetedStatus?.user?.toParcelable(account,
                    profileImageSize = profileImageSize)?.addTo(mapResult)
            status.quotedStatus?.user?.toParcelable(account,
                    profileImageSize = profileImageSize)?.addTo(mapResult)
            return@flatMap mapResult
        }
    }

    private fun storeStatus(account: AccountDetails, statuses: List<ParcelableStatus>,
            param: P, sinceId: String?, maxId: String?, sinceSortId: Long, maxSortId: Long,
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
        if (!statuses.isEmpty()) {
            val firstSortId = statuses.first().sort_id
            val lastSortId = statuses.last().sort_id
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId

            val creator = ObjectCursor.valuesCreatorFrom(ParcelableStatus::class.java)
            statuses.forEachIndexed { i, status ->
                status.position_key = getPositionKey(status.timestamp, status.sort_id, lastSortId,
                        sortDiff, i, statuses.size)
                status.tab_id = param.tabId
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
