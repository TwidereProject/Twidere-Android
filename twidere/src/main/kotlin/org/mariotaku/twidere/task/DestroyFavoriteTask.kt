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
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.AsyncTwitterWrapper.calculateHashCode
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 2017/2/7.
 */
class DestroyFavoriteTask(
        context: Context,
        private val accountKey: UserKey,
        private val statusId: String
) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableStatus>>(context) {


    override fun doInBackground(vararg params: Any): SingleResponse<ParcelableStatus> {
        val resolver = context.contentResolver
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
                ?: return SingleResponse.getInstance<ParcelableStatus>()
        val microBlog = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val result: ParcelableStatus
            when (details.type) {
                AccountType.FANFOU -> {
                    result = ParcelableStatusUtils.fromStatus(microBlog.destroyFanfouFavorite(statusId),
                            accountKey, false)
                }
                else -> {
                    result = ParcelableStatusUtils.fromStatus(microBlog.destroyFavorite(statusId),
                            accountKey, false)
                }
            }
            val values = ContentValues()
            values.put(TwidereDataStore.Statuses.IS_FAVORITE, false)
            values.put(TwidereDataStore.Statuses.FAVORITE_COUNT, result.favorite_count - 1)
            values.put(TwidereDataStore.Statuses.RETWEET_COUNT, result.retweet_count)
            values.put(TwidereDataStore.Statuses.REPLY_COUNT, result.reply_count)

            val where = Expression.and(Expression.equalsArgs(TwidereDataStore.Statuses.ACCOUNT_KEY),
                    Expression.or(Expression.equalsArgs(TwidereDataStore.Statuses.STATUS_ID),
                            Expression.equalsArgs(TwidereDataStore.Statuses.RETWEET_ID)))
            val whereArgs = arrayOf(accountKey.toString(), statusId, statusId)
            for (uri in DataStoreUtils.STATUSES_URIS) {
                resolver.update(uri, values, where.sql, whereArgs)
            }

            DataStoreUtils.updateActivityStatus(resolver, accountKey, statusId, DataStoreUtils.UpdateActivityAction { activity ->
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                for (statusesArray in statusesMatrix) {
                    if (statusesArray == null) continue
                    for (status in statusesArray) {
                        if (result.id != status.id) continue
                        status.is_favorite = false
                        status.reply_count = result.reply_count
                        status.retweet_count = result.retweet_count
                        status.favorite_count = result.favorite_count - 1
                    }
                }
            })
            return SingleResponse.getInstance(result)
        } catch (e: MicroBlogException) {
            return SingleResponse.getInstance<ParcelableStatus>(e)
        }

    }

    override fun onPreExecute() {
        super.onPreExecute()
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!destroyingFavoriteIds.contains(hashCode)) {
            destroyingFavoriteIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun onPostExecute(result: SingleResponse<ParcelableStatus>) {
        destroyingFavoriteIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        val taskEvent = FavoriteTaskEvent(FavoriteTaskEvent.Action.DESTROY,
                accountKey, statusId)
        taskEvent.isFinished = true
        if (result.hasData()) {
            val status = result.data
            taskEvent.status = status
            taskEvent.isSucceeded = true
            // BEGIN HotMobi
            val tweetEvent = TweetEvent.create(context, status, TimelineType.OTHER)
            tweetEvent.action = TweetEvent.Action.UNFAVORITE
            HotMobiLogger.getInstance(context).log(accountKey, tweetEvent)
            // END HotMobi
            Utils.showInfoMessage(context, R.string.message_toast_status_unfavorited, false)
        } else {
            taskEvent.isSucceeded = false
            Utils.showErrorMessage(context, R.string.action_unfavoriting, result.exception, true)
        }
        bus.post(taskEvent)
        bus.post(StatusListChangedEvent())
        super.onPostExecute(result)
    }

    companion object {
        private val destroyingFavoriteIds = ArrayIntList()

        fun isDestroyingFavorite(accountKey: UserKey?, statusId: String?): Boolean {
            return destroyingFavoriteIds.contains(calculateHashCode(accountKey, statusId))
        }

    }
}
