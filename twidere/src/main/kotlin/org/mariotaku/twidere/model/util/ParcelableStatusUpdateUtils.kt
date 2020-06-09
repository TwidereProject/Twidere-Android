package org.mariotaku.twidere.model.util

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.unique_id_non_null
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableStatusUpdate
import org.mariotaku.twidere.model.draft.QuoteStatusActionExtras
import org.mariotaku.twidere.model.draft.StatusObjectActionExtras
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras
import org.mariotaku.twidere.util.LinkCreator

/**
 * Created by mariotaku on 16/2/12.
 */
object ParcelableStatusUpdateUtils {

    fun fromDraftItem(context: Context, draft: Draft): ParcelableStatusUpdate {
        val statusUpdate = ParcelableStatusUpdate()
        statusUpdate.accounts = draft.account_keys?.let {
            AccountUtils.getAllAccountDetails(AccountManager.get(context), it, true)
        } ?: emptyArray()
        statusUpdate.text = draft.text
        statusUpdate.location = draft.location
        statusUpdate.media = draft.media
        when (val actionExtras = draft.action_extras) {
            is UpdateStatusActionExtras -> {
                statusUpdate.in_reply_to_status = actionExtras.inReplyToStatus
                statusUpdate.is_possibly_sensitive = actionExtras.isPossiblySensitive
                statusUpdate.display_coordinates = actionExtras.displayCoordinates
                statusUpdate.attachment_url = actionExtras.attachmentUrl
                statusUpdate.excluded_reply_user_ids = actionExtras.excludedReplyUserIds
                statusUpdate.extended_reply_mode = actionExtras.isExtendedReplyMode
                statusUpdate.summary  = actionExtras.summaryText
                statusUpdate.visibility  = actionExtras.visibility
            }
            is QuoteStatusActionExtras -> {
                val onlyAccount = statusUpdate.accounts.singleOrNull()
                val status = actionExtras.status
                val quoteOriginalStatus = actionExtras.isQuoteOriginalStatus
                if (status != null && onlyAccount != null) {
                    when (onlyAccount.type) {
                        AccountType.FANFOU -> {
                            if (!status.is_quote || !quoteOriginalStatus) {
                                statusUpdate.repost_status_id = status.id
                                statusUpdate.text = context.getString(R.string.fanfou_repost_format,
                                        draft.text, status.user_screen_name, status.text_plain)
                            } else {
                                statusUpdate.text = context.getString(R.string.fanfou_repost_format,
                                        draft.text, status.quoted_user_screen_name,
                                        status.quoted_text_plain)
                                statusUpdate.repost_status_id = status.quoted_id
                            }
                        }
                        else -> {
                            val statusLink = if (!status.is_quote || !quoteOriginalStatus) {
                                LinkCreator.getStatusWebLink(status)
                            } else {
                                LinkCreator.getQuotedStatusWebLink(status)
                            }
                            statusUpdate.attachment_url = statusLink.toString()
                            statusUpdate.text = draft.text
                        }
                    }
                }
            }
            is StatusObjectActionExtras -> {
                when (draft.action_type) {
                    Draft.Action.QUOTE -> {
                        val link = LinkCreator.getStatusWebLink(actionExtras.status)
                        statusUpdate.attachment_url = link.toString()
                    }
                }
            }
        }
        statusUpdate.draft_action = draft.action_type
        statusUpdate.draft_unique_id = draft.unique_id_non_null
        return statusUpdate
    }

}
