package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ContentType
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.LinkCreator

/**
 * Created by mariotaku on 2016/12/15.
 */

data class Share(
        val id: String,
        @ContentType val type: String,
        @AccountType override val accountType: String?,
        override val accountHost: String? = null
) : Analyzer.Event {
    companion object {
        fun status(accountType: String?, status: ParcelableStatus): Share {
            val uri = LinkCreator.getStatusWebLink(status).toString()
            return Share(uri, ContentType.STATUS, accountType, status.account_key.host)
        }
    }
}
