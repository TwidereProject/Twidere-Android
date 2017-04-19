package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.StatusDestroyedEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.deleteActivityStatus

/**
 * Created by mariotaku on 2016/12/9.
 */
class DestroyStatusTask(
        context: Context,
        private val accountKey: UserKey,
        private val statusId: String
) : BaseAbstractTask<Any?, SingleResponse<ParcelableStatus>, Any?>(context) {

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableStatus> {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
                ?: return SingleResponse()
        val microBlog = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        var status: ParcelableStatus? = null
        var deleteStatus: Boolean = false
        try {
            status = microBlog.destroyStatus(statusId).toParcelable(accountKey, details.type)
            ParcelableStatusUtils.updateExtraInformation(status, details)
            deleteStatus = true
            return SingleResponse(status)
        } catch (e: MicroBlogException) {
            deleteStatus = e.errorCode == ErrorInfo.STATUS_NOT_FOUND
            return SingleResponse(exception = e)
        } finally {
            if (deleteStatus) {
                DataStoreUtils.deleteStatus(context.contentResolver, accountKey, statusId, status)
                context.contentResolver.deleteActivityStatus(accountKey, statusId, status)
            }
        }
    }

    override fun beforeExecute() {
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!microBlogWrapper.destroyingStatusIds.contains(hashCode)) {
            microBlogWrapper.destroyingStatusIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableStatus>) {

        microBlogWrapper.destroyingStatusIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result.hasData()) {
            val status = result.data!!
            if (status.retweet_id != null) {
                Utils.showInfoMessage(context, R.string.message_toast_retweet_cancelled, false)
            } else {
                Utils.showInfoMessage(context, R.string.message_toast_status_deleted, false)
            }
            bus.post(StatusDestroyedEvent(status))
        } else {
            Utils.showErrorMessage(context, R.string.action_deleting, result.exception, true)
        }
    }

}
