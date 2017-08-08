package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.parcelableMediaTypeString
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/1/7.
 */

data class StatusView(
        @AccountType override val accountType: String? = null,
        @ParcelableMedia.Type val mediaType: Int
) : Analyzer.Event {

    override val name: String = "Status View"
    var type: String? = null
    var source: String? = null

    override fun forEachValues(action: (String, String?) -> Unit) {
        val mediaType = parcelableMediaTypeString(mediaType)
        if (mediaType != null) {
            action("Media Type", mediaType)
        }
        if (type != null) {
            action("Type", type)
        }
        if (source != null) {
            action("Source", source)
        }
    }

    companion object {
        @JvmStatic
        fun getStatusType(status: ParcelableStatus): String {
            if (status.is_retweet) {
                return "retweet"
            } else if (status.is_quote) {
                return "quote"
            }
            return "tweet"
        }
    }
}
