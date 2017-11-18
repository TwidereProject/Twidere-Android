package org.mariotaku.twidere.task.twitter

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.support.annotation.UiThread
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.addTo
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.InternalActivityCreator
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY_CHANGE
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.data.fetcher.ActivitiesFetcher
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.api.batchGetRelationships
import org.mariotaku.twidere.extension.bulkInsert
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.GetActivitiesTaskEvent
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.task.GetTimelineResult
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.task.statuses.GetStatusesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.util.sync.TimelineSyncManager
import java.util.*

abstract class GetActivitiesTask(
        context: Context
) : BaseAbstractTask<ContentRefreshParam, List<Pair<GetTimelineResult<ParcelableActivity>?, Exception?>>,
        (Boolean) -> Unit>(context) {

    protected abstract val errorInfoKey: String

    @FilterScope
    protected abstract val filterScopes: Int

    protected abstract val contentUri: Uri

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override fun doLongOperation(param: ContentRefreshParam): List<Pair<GetTimelineResult<ParcelableActivity>?, Exception?>> {
        if (param.shouldAbort) return emptyList()
        val accountKeys = param.accountKeys.takeIf { it.isNotEmpty() } ?: return emptyList()
        val loadItemLimit = preferences[loadItemLimitKey]
        val result = accountKeys.mapIndexed { i, accountKey ->
            val noItemsBefore = DataStoreUtils.getActivitiesCount(context, contentUri, accountKey) <= 0
            val credentials = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey,
                    true) ?: throw AccountNotFoundException()
            val paging = Paging()
            paging.count(loadItemLimit)
            val maxId = param.getMaxId(i)
            val maxSortId = param.getMaxSortId(i)
            if (maxId != null) {
                paging.maxId(maxId)
            }
            val sinceId = param.getSinceId(i)
            if (sinceId != null) {
                paging.sinceId(sinceId)
                if (maxId == null) {
                    paging.setLatestResults(true)
                }
            }
            // We should delete old activities has intersection with new items
            try {
                val timelineResult = getActivities(credentials, paging)
                val storeResult = storeActivities(credentials, timelineResult.data, sinceId, maxId,
                        loadItemLimit, noItemsBefore, false)
                errorInfoStore.remove(errorInfoKey, accountKey)
                if (storeResult != 0) {
                    throw GetStatusesTask.GetTimelineException(storeResult)
                }
                return@mapIndexed Pair(timelineResult, null)
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
                if (e.errorCode == 220) {
                    errorInfoStore[errorInfoKey, accountKey] = ErrorInfoStore.CODE_NO_ACCESS_FOR_CREDENTIALS
                } else if (e.isCausedByNetworkIssue) {
                    errorInfoStore[errorInfoKey, accountKey] = ErrorInfoStore.CODE_NETWORK_ERROR
                }
                return@mapIndexed Pair(null, e)
            } catch (e: GetStatusesTask.GetTimelineException) {
                return@mapIndexed Pair(null, e)
            }
        }
        val manager = dataSyncProvider.newTimelineSyncManager()
        if (manager != null && syncPreferences.isSyncEnabled(SyncTaskRunner.SYNC_TYPE_TIMELINE_POSITIONS)) {
            if (param.isBackground) {
                syncFetchReadPosition(manager, accountKeys)
            }
        }
        return result
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, results: List<Pair<GetTimelineResult<ParcelableActivity>?, Exception?>>) {
        context.contentResolver.notifyChange(contentUri, null)
        val exception = results.firstOrNull { it.second != null }?.second
        bus.post(GetActivitiesTaskEvent(contentUri, false, exception))
        GetStatusesTask.cacheItems(context, results)
        handler?.invoke(true)
    }

    @UiThread
    override fun beforeExecute() {
        bus.post(GetActivitiesTaskEvent(contentUri, true, null))
    }

    @Throws(MicroBlogException::class)
    protected fun getActivities(account: AccountDetails, paging: Paging): GetTimelineResult<ParcelableActivity> {
        val fetcher = getActivitiesFetcher(params)
        when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                val notifications = fetcher.forMastodon(account, mastodon, paging)
                val userIds = notifications.flatMapTo(HashSet()) {
                    val mapResult = mutableSetOf<String>()
                    it?.account?.id?.addTo(mapResult)
                    it.status?.account?.id?.addTo(mapResult)
                    return@flatMapTo mapResult
                }
                val relationships = mastodon.batchGetRelationships(userIds)
                val activities = notifications.mapNotNull {
                    val activity = it.toParcelable(account, relationships)
                    if (activity.action == Activity.Action.INVALID) return@mapNotNull null
                    return@mapNotNull activity
                }
                return GetTimelineResult(account, activities, activities.flatMap {
                    it.sources?.toList().orEmpty()
                }, notifications.flatMapTo(HashSet()) { notification ->
                    notification.status?.tags?.map { it.name }.orEmpty()
                })
            }
            AccountType.TWITTER -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                if (account.isOfficial(context)) {
                    val timeline = fetcher.forTwitterOfficial(account, microBlog, paging)
                    val activities = timeline.map {
                        it.toParcelable(account, profileImageSize = profileImageSize)
                    }

                    return GetTimelineResult(account, activities, activities.flatMap {
                        it.sources?.toList().orEmpty()
                    }, timeline.flatMapTo(HashSet()) { activity ->
                        val mapResult = mutableSetOf<String>()
                        activity.targetStatuses?.flatMapTo(mapResult) { status ->
                            status.entities?.hashtags?.map { it.text }.orEmpty()
                        }
                        activity.targetObjectStatuses?.flatMapTo(mapResult) { status ->
                            status.entities?.hashtags?.map { it.text }.orEmpty()
                        }
                        return@flatMapTo mapResult
                    })
                } else {
                    val timeline = fetcher.forTwitter(account, microBlog, paging)
                    val activities = timeline.map {
                        InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                                profileImageSize = profileImageSize)
                    }
                    return GetTimelineResult(account, activities, activities.flatMap {
                        it.sources?.toList().orEmpty()
                    }, timeline.flatMap {
                        it.entities?.hashtags?.map { it.text }.orEmpty()
                    })
                }
            }
            AccountType.FANFOU -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val activities = fetcher.forFanfou(account, microBlog, paging).map {
                    InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                            profileImageSize = profileImageSize)
                }
                return GetTimelineResult(account, activities, activities.flatMap {
                    it.sources?.toList().orEmpty()
                }, activities.flatMap { it.extractFanfouHashtags() })
            }
            AccountType.STATUSNET -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                val timeline = fetcher.forStatusNet(account, microBlog, paging)
                val activities = timeline.map {
                    InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                            profileImageSize = profileImageSize)
                }
                return GetTimelineResult(account, activities, activities.flatMap {
                    it.sources?.toList().orEmpty()
                }, timeline.flatMap {
                    it.entities?.hashtags?.map { it.text }.orEmpty()
                })
            }
            else -> throw UnsupportedOperationException()
        }
    }

    protected abstract fun getActivitiesFetcher(params: ContentRefreshParam?): ActivitiesFetcher

    protected abstract fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>)

    private fun storeActivities(details: AccountDetails, activities: List<ParcelableActivity>,
            sinceId: String?, maxId: String?, loadItemLimit: Int, noItemsBefore: Boolean,
            notify: Boolean): Int {
        val cr = context.contentResolver
        val deleteBound = LongArray(2) { -1 }
        var minIdx = -1
        var minPositionKey: Long = -1
        if (!activities.isEmpty()) {
            val firstSortId = activities.first().timestamp
            val lastSortId = activities.last().timestamp
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId
            activities.forEachIndexed { i, activity ->
                mediaPreloader.preloadActivity(activity)
                activity.position_key = GetStatusesTask.getPositionKey(activity.timestamp,
                        activity.timestamp, lastSortId, sortDiff, i, activities.size)
                if (deleteBound[0] < 0) {
                    deleteBound[0] = activity.min_sort_position
                } else {
                    deleteBound[0] = Math.min(deleteBound[0], activity.min_sort_position)
                }
                if (deleteBound[1] < 0) {
                    deleteBound[1] = activity.max_sort_position
                } else {
                    deleteBound[1] = Math.max(deleteBound[1], activity.max_sort_position)
                }
                if (minIdx == -1 || activity < activities[minIdx]) {
                    minIdx = i
                    minPositionKey = activity.position_key
                }
            }
        }
        var olderCount = -1
        if (minPositionKey > 0) {
            olderCount = DataStoreUtils.getActivitiesCount(context, preferences, contentUri,
                    Activities.POSITION_KEY, minPositionKey, false, arrayOf(details.key), filterScopes)
        }
        val writeUri = UriUtils.appendQueryParameters(contentUri, QUERY_PARAM_NOTIFY_CHANGE, notify)
        if (deleteBound[0] > 0 && deleteBound[1] > 0) {
            val where = Expression.and(
                    Expression.equalsArgs(Activities.ACCOUNT_KEY),
                    Expression.greaterEquals(Activities.MIN_SORT_POSITION, deleteBound[0]),
                    Expression.lesserEquals(Activities.MAX_SORT_POSITION, deleteBound[1])
            )
            val whereArgs = arrayOf(details.key.toString())
            // First item after gap doesn't count
            val localDeleted = if (maxId != null && sinceId == null) 1 else 0
            val rowsDeleted = cr.delete(writeUri, where.sql, whereArgs) - localDeleted
            // Why loadItemLimit / 2? because it will not acting strange in most cases
            val insertGap = !noItemsBefore && olderCount > 0 && rowsDeleted <= 0 && activities.size > loadItemLimit / 2
            if (insertGap && !activities.isEmpty()) {
                activities.last().is_gap = true
            }
        }
        // Insert previously fetched items.
        cr.bulkInsert(writeUri, activities, ParcelableActivity::class.java)

        // Remove gap flag
        if (maxId != null && sinceId == null) {
            if (activities.isNotEmpty()) {
                // Only remove when actual result returned, otherwise it seems that gap is too old to load
                if (params.extraId != -1L) {
                    val noGapValues = ContentValues()
                    noGapValues.put(Activities.IS_GAP, false)
                    val noGapWhere = Expression.equals(Activities._ID, params.extraId).sql
                    cr.update(writeUri, noGapValues, noGapWhere, null)
                }
            } else {
                return GetStatusesTask.ERROR_LOAD_GAP
            }
        }
        return 0
    }
}
