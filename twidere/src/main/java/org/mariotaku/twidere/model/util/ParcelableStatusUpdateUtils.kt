package org.mariotaku.twidere.model.util

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.twidere.extension.model.unique_id_non_null

import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableStatusUpdate
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras

/**
 * Created by mariotaku on 16/2/12.
 */
object ParcelableStatusUpdateUtils {

    fun fromDraftItem(context: Context, draft: Draft): ParcelableStatusUpdate {
        val statusUpdate = ParcelableStatusUpdate()
        if (draft.account_keys != null) {
            statusUpdate.accounts = AccountUtils.getAllAccountDetails(AccountManager.get(context), draft.account_keys!!, true)
        } else {
            statusUpdate.accounts = arrayOfNulls<AccountDetails>(0)
        }
        statusUpdate.text = draft.text
        statusUpdate.location = draft.location
        statusUpdate.media = draft.media
        if (draft.action_extras is UpdateStatusActionExtras) {
            val extra = draft.action_extras as UpdateStatusActionExtras?
            statusUpdate.in_reply_to_status = extra!!.inReplyToStatus
            statusUpdate.is_possibly_sensitive = extra.isPossiblySensitive
            statusUpdate.display_coordinates = extra.displayCoordinates
            statusUpdate.attachment_url = extra.attachmentUrl
        }
        statusUpdate.draft_unique_id = draft.unique_id_non_null
        return statusUpdate
    }

}
