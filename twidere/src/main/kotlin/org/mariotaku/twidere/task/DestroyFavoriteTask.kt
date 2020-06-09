package org.mariotaku.twidere.task

import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.AsyncTwitterWrapper.Companion.calculateHashCode
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.updateStatusInfo

/**
 * Created by mariotaku on 2017/2/7.
 */
class DestroyFavoriteTask(
        context: Context,
        accountKey: UserKey,
        private val statusId: String
) : AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {
    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        val resolver = context.contentResolver
        val result = when (account.type) {
            AccountType.FANFOU -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.destroyFanfouFavorite(statusId).toParcelable(account)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, cls = Mastodon::class.java)
                mastodon.unfavouriteStatus(statusId).toParcelable(account)
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.destroyFavorite(statusId).toParcelable(account)
            }
        }

        resolver.updateStatusInfo(DataStoreUtils.STATUSES_ACTIVITIES_URIS, Statuses.COLUMNS,
                account.key, statusId, ParcelableStatus::class.java) { item ->
            item.is_favorite = false
            item.reply_count = result.reply_count
            item.retweet_count = result.retweet_count
            item.favorite_count = result.favorite_count - 1
            return@updateStatusInfo item
        }
        return result

    }

    override fun beforeExecute() {
        val hashCode = calculateHashCode(accountKey, statusId)
        if (!destroyingFavoriteIds.contains(hashCode)) {
            destroyingFavoriteIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        destroyingFavoriteIds.remove(calculateHashCode(accountKey, statusId))
        val taskEvent = FavoriteTaskEvent(FavoriteTaskEvent.Action.DESTROY, accountKey, statusId)
        taskEvent.isFinished = true
        if (result != null) {
            taskEvent.status = result
            taskEvent.isSucceeded = true
            Toast.makeText(context, R.string.message_toast_status_unfavorited, Toast.LENGTH_SHORT).show()
        } else {
            taskEvent.isSucceeded = false
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
        bus.post(taskEvent)
        bus.post(StatusListChangedEvent())
    }

    companion object {
        private val destroyingFavoriteIds = ArrayList<Int>()

        fun isDestroyingFavorite(accountKey: UserKey?, statusId: String?): Boolean {
            return destroyingFavoriteIds.contains(calculateHashCode(accountKey, statusId))
        }

    }
}
