package org.mariotaku.twidere.data.status

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.model.microblog.ErrorInfo
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.data.ComputableExceptionLiveData
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.getDetails
import org.mariotaku.twidere.extension.model.updateExtraInformation
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.deleteActivityStatus
import org.mariotaku.twidere.util.deleteStatus

class StatusLiveData(
        val context: Context,
        private val omitIntentExtra: Boolean,
        private val extras: Bundle?,
        private val accountKey: UserKey?,
        private val statusId: String?
) : ComputableExceptionLiveData<Pair<AccountDetails, ParcelableStatus>>(false) {
    override fun compute(): Pair<AccountDetails, ParcelableStatus> {
        if (accountKey == null || statusId == null) {
            throw RequiredFieldNotFoundException("account_key", "status_id")
        }
        val details = AccountManager.get(context).getDetails(accountKey,
                true) ?: throw AccountNotFoundException()
        if (!omitIntentExtra && extras != null) {
            val cache: ParcelableStatus? = extras.getParcelable(IntentConstants.EXTRA_STATUS)
            if (cache != null) {
                return Pair(details, cache)
            }
        }
        try {
            val status = DataStoreUtils.findStatus(context, accountKey, statusId)
            status.updateExtraInformation(details)
            return Pair(details, status)
        } catch (e: MicroBlogException) {
            if (e.errorCode == ErrorInfo.STATUS_NOT_FOUND) {
                // Delete all deleted status
                val cr = context.contentResolver
                cr.deleteStatus(accountKey, statusId, null)
                cr.deleteActivityStatus(accountKey, statusId, null)
            }
            throw e
        }

    }
}
