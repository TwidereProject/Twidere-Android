package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.StatusDestroyedEvent
import org.mariotaku.twidere.model.message.StatusListChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 2016/12/9.
 */
class DestroyStatusTask(
        context: Context,
        private val accountKey: UserKey,
        private val statusId: String
) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableStatus>>(context) {

    override fun doInBackground(vararg params: Any): SingleResponse<ParcelableStatus> {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
                ?: return SingleResponse()
        val microBlog = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        var status: ParcelableStatus? = null
        var deleteStatus: Boolean = false
        try {
            status = ParcelableStatusUtils.fromStatus(microBlog.destroyStatus(statusId),
                    accountKey, false)
            ParcelableStatusUtils.updateExtraInformation(status, details)
            deleteStatus = true
            return SingleResponse(status)
        } catch (e: MicroBlogException) {
            deleteStatus = e.errorCode == ErrorInfo.STATUS_NOT_FOUND
            return SingleResponse(exception = e)
        } finally {
            if (deleteStatus) {
                DataStoreUtils.deleteStatus(context.contentResolver, accountKey, statusId, status)
                DataStoreUtils.deleteActivityStatus(context.contentResolver, accountKey, statusId, status)
            }
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!asyncTwitterWrapper.destroyingStatusIds.contains(hashCode)) {
            asyncTwitterWrapper.destroyingStatusIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun onPostExecute(result: SingleResponse<ParcelableStatus>) {
        asyncTwitterWrapper.destroyingStatusIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result.hasData()) {
            val status = result.data!!
            if (status.retweet_id != null) {
                Utils.showInfoMessage(context, R.string.retweet_cancelled, false)
            } else {
                Utils.showInfoMessage(context, R.string.status_deleted, false)
            }
            bus.post(StatusDestroyedEvent(status))
        } else {
            Utils.showErrorMessage(context, R.string.action_deleting, result.exception, true)
        }
        super.onPostExecute(result)
    }

}
