package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.parcelableMediaTypeString
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/1/7.
 */

data class StatusView(
        @AccountType override val accountType: String? = null,
        @ParcelableMedia.Type val mediaType: Int
) : Analyzer.Event {

    override val name: String = "Status View"

    override fun forEachValues(action: (String, String?) -> Unit) {
        action("Media Type", parcelableMediaTypeString(mediaType))
    }
}
