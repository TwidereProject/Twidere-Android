package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.TimelineType
import edu.tsinghua.hotmobi.model.TweetEvent
import org.apache.commons.collections.primitives.ArrayIntList
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.draft.StatusObjectExtras
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.event.StatusRetweetedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.*

/**
 * Created by mariotaku on 2017/2/7.
 */
class RetweetStatusTask(
        context: Context,
        private val accountKey: UserKey,
        private val status: ParcelableStatus
) : BaseAbstractTask<Any, SingleResponse<ParcelableStatus>, Any?>(context) {

    private val statusId = status.id

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableStatus> {
        val draftId = UpdateStatusTask.saveDraft(context, Draft.Action.RETWEET) {
            this@saveDraft.account_keys = arrayOf(accountKey)
            this@saveDraft.action_extras = StatusObjectExtras().apply {
                this@apply.status = this@RetweetStatusTask.status
            }
        }
        microBlogWrapper.addSendingDraftId(draftId)
        val resolver = context.contentResolver
        val details = AccountUtils.getAccountDetails(AccountManager.get(context),
                accountKey, true) ?: return SingleResponse.getInstance<ParcelableStatus>(MicroBlogException("No account"))
        val microBlog = details.newMicroBlogInstance(
                context, false, false, MicroBlog::class.java)
        try {
            val result = ParcelableStatusUtils.fromStatus(microBlog.retweetStatus(statusId),
                    accountKey, false)
            ParcelableStatusUtils.updateExtraInformation(result, details)
            Utils.setLastSeen(context, result.mentions, System.currentTimeMillis())
            val values = ContentValues()
            values.put(TwidereDataStore.Statuses.MY_RETWEET_ID, result.id)
            values.put(TwidereDataStore.Statuses.REPLY_COUNT, result.reply_count)
            values.put(TwidereDataStore.Statuses.RETWEET_COUNT, result.retweet_count)
            values.put(TwidereDataStore.Statuses.FAVORITE_COUNT, result.favorite_count)
            val where = Expression.or(
                    Expression.equalsArgs(TwidereDataStore.Statuses.STATUS_ID),
                    Expression.equalsArgs(TwidereDataStore.Statuses.RETWEET_ID)
            )
            val whereArgs = arrayOf(statusId, statusId)
            for (uri in DataStoreUtils.STATUSES_URIS) {
                resolver.update(uri, values, where.sql, whereArgs)
            }
            updateActivityStatus(resolver, accountKey, statusId) { activity ->
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                activity.status_my_retweet_id = result.my_retweet_id
                for (statusesArray in statusesMatrix) {
                    if (statusesArray == null) continue
                    for (status in statusesArray) {
                        if (statusId == status.id || statusId == status.retweet_id
                                || statusId == status.my_retweet_id) {
                            status.my_retweet_id = result.id
                            status.reply_count = result.reply_count
                            status.retweet_count = result.retweet_count
                            status.favorite_count = result.favorite_count
                        }
                    }
                }
            }
            UpdateStatusTask.deleteDraft(context, draftId)
            return SingleResponse(result)
        } catch (e: MicroBlogException) {
            DebugLog.w(TwidereConstants.LOGTAG, tr = e)
            return SingleResponse(e)
        } finally {
            microBlogWrapper.removeSendingDraftId(draftId)
        }

    }

    override fun beforeExecute() {
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!creatingRetweetIds.contains(hashCode)) {
            creatingRetweetIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }


    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableStatus>) {
        creatingRetweetIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result.hasData()) {
            val status = result.data
            // BEGIN HotMobi
            val event = TweetEvent.create(context, status, TimelineType.OTHER)
            event.action = TweetEvent.Action.RETWEET
            HotMobiLogger.getInstance(context).log(accountKey, event)
            // END HotMobi

            bus.post(StatusRetweetedEvent(status))
        } else {
            Utils.showErrorMessage(context, R.string.action_retweeting, result.exception, true)
        }
    }

    companion object {
        private val creatingRetweetIds = ArrayIntList()
        fun isCreatingRetweet(accountKey: UserKey?, statusId: String?): Boolean {
            return creatingRetweetIds.contains(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        }

    }

}
