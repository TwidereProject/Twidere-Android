package org.mariotaku.twidere.task

import android.content.Context
import android.widget.Toast
import org.mariotaku.kpreferences.get
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.TWITTER_ERROR_ALREADY_FAVORITED
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.draft.StatusObjectActionExtras
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.util.AsyncTwitterWrapper.Companion.calculateHashCode
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.updateStatusInfo

/**
 * Created by mariotaku on 2017/2/7.
 */
class CreateFavoriteTask(context: Context, accountKey: UserKey, private val status: ParcelableStatus) :
        AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {

    private val statusId = status.id

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        val resolver = context.contentResolver
        val result = when (account.type) {
            AccountType.FANFOU -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.createFanfouFavorite(statusId).toParcelable(account)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, cls = Mastodon::class.java)
                mastodon.favouriteStatus(statusId).toParcelable(account)
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.createFavorite(statusId).toParcelable(account)
            }
        }
        Utils.setLastSeen(context, result.mentions, System.currentTimeMillis())

        resolver.updateStatusInfo(DataStoreUtils.STATUSES_ACTIVITIES_URIS, Statuses.COLUMNS,
                account.key, statusId, ParcelableStatus::class.java) { status ->
            if (result.id != status.id) return@updateStatusInfo status
            status.is_favorite = true
            status.reply_count = result.reply_count
            status.retweet_count = result.retweet_count
            status.favorite_count = result.favorite_count
            return@updateStatusInfo status
        }
        return result
    }

    override fun beforeExecute() {
        val hashCode = calculateHashCode(accountKey, statusId)
        if (!creatingFavoriteIds.contains(hashCode)) {
            creatingFavoriteIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        creatingFavoriteIds.remove(calculateHashCode(accountKey, statusId))
        val taskEvent = FavoriteTaskEvent(FavoriteTaskEvent.Action.CREATE, accountKey, statusId)
        taskEvent.isFinished = true
        if (result != null) {
            taskEvent.status = result
            taskEvent.isSucceeded = true
            if (preferences[iWantMyStarsBackKey]) {
                Toast.makeText(context, R.string.message_toast_status_favorited,
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.message_toast_status_liked,
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            taskEvent.isSucceeded = false
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
        bus.post(taskEvent)
        bus.post(StatusListChangedEvent())
    }

    override fun onCleanup(account: AccountDetails, params: Any?, exception: MicroBlogException) {
        if (exception.errorCode == TWITTER_ERROR_ALREADY_FAVORITED) {
            val resolver = context.contentResolver

            resolver.updateStatusInfo(DataStoreUtils.STATUSES_ACTIVITIES_URIS, Statuses.COLUMNS,
                    account.key, statusId, ParcelableStatus::class.java) { status ->
                if (statusId != status.id) return@updateStatusInfo status
                status.is_favorite = true
                return@updateStatusInfo status
            }
        }
    }

    override fun createDraft() = UpdateStatusTask.createDraft(Draft.Action.FAVORITE) {
        account_keys = arrayOf(accountKey)
        action_extras = StatusObjectActionExtras().also { extras ->
            extras.status = this@CreateFavoriteTask.status
        }
    }

    override fun deleteDraftOnException(account: AccountDetails, params: Any?, exception: MicroBlogException): Boolean {
        return exception.errorCode == TWITTER_ERROR_ALREADY_FAVORITED
    }

    companion object {

        private val creatingFavoriteIds = ArrayList<Int>()

        fun isCreatingFavorite(accountKey: UserKey?, statusId: String?): Boolean {
            return creatingFavoriteIds.contains(calculateHashCode(accountKey, statusId))
        }
    }

}
