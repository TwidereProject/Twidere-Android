package org.mariotaku.twidere.task

import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.StatusDestroyedEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.deleteActivityStatus

/**
 * Created by mariotaku on 2016/12/9.
 */
class DestroyStatusTask(
        context: Context,
        accountKey: UserKey,
        private val statusId: String
) : AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        return when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, cls = Mastodon::class.java)
                val result = mastodon.fetchStatus(statusId)
                mastodon.deleteStatus(statusId)
                result.toParcelable(account)
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.destroyStatus(statusId).toParcelable(account)
            }
        }
    }

    override fun onCleanup(account: AccountDetails, params: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        if (result == null && exception?.errorCode != ErrorInfo.STATUS_NOT_FOUND) return
        DataStoreUtils.deleteStatus(context.contentResolver, account.key, statusId, result)
        context.contentResolver.deleteActivityStatus(account.key, statusId, result)
    }

    override fun beforeExecute() {
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!microBlogWrapper.destroyingStatusIds.contains(hashCode)) {
            microBlogWrapper.destroyingStatusIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        microBlogWrapper.destroyingStatusIds.remove(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result != null) {
            if (result.retweet_id != null) {
                Toast.makeText(context, R.string.message_toast_retweet_cancelled, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.message_toast_status_deleted, Toast.LENGTH_SHORT).show()
            }
            bus.post(StatusDestroyedEvent(result))
        } else {
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
    }

}
