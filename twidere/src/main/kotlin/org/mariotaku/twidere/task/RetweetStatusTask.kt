package org.mariotaku.twidere.task

import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import org.apache.commons.collections.primitives.ArrayIntList
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.draft.StatusObjectActionExtras
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.event.StatusRetweetedEvent
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.updateActivityStatus

/**
 * Retweet status
 *
 * Created by mariotaku on 2017/2/7.
 */
class RetweetStatusTask(
        context: Context,
        accountKey: UserKey,
        private val status: ParcelableStatus
) : AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {

    private val statusId = status.id

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        val draftId = UpdateStatusTask.saveDraft(context, Draft.Action.RETWEET) {
            this@saveDraft.account_keys = arrayOf(accountKey)
            this@saveDraft.action_extras = StatusObjectActionExtras().apply {
                this@apply.status = this@RetweetStatusTask.status
            }
        }
        microBlogWrapper.addSendingDraftId(draftId)
        val resolver = context.contentResolver
        try {
            val result = when (account.type) {
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(context, cls = Mastodon::class.java)
                    mastodon.reblogStatus(statusId).toParcelable(account)
                }
                else -> {
                    val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                    microBlog.retweetStatus(statusId).toParcelable(account)
                }
            }
            ParcelableStatusUtils.updateExtraInformation(result, account)
            Utils.setLastSeen(context, result.mentions, System.currentTimeMillis())
            val values = ContentValues()
            values.put(Statuses.MY_RETWEET_ID, result.id)
            values.put(Statuses.REPLY_COUNT, result.reply_count)
            values.put(Statuses.RETWEET_COUNT, result.retweet_count)
            values.put(Statuses.FAVORITE_COUNT, result.favorite_count)
            val where = Expression.or(
                    Expression.equalsArgs(Statuses.STATUS_ID),
                    Expression.equalsArgs(Statuses.RETWEET_ID)
            )
            val whereArgs = arrayOf(statusId, statusId)
            for (uri in DataStoreUtils.STATUSES_URIS) {
                resolver.update(uri, values, where.sql, whereArgs)
            }
            resolver.updateActivityStatus(account.key, statusId) { activity ->
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
            return result
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

    override fun afterExecute(callback: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        creatingRetweetIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result != null) {
            bus.post(StatusRetweetedEvent(result))
        } else {
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val creatingRetweetIds = ArrayIntList()
        fun isCreatingRetweet(accountKey: UserKey?, statusId: String?): Boolean {
            return creatingRetweetIds.contains(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        }

    }

}
