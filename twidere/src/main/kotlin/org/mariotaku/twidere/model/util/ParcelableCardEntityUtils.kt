package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.util.ThreadLocalSimpleDateFormat
import java.text.DateFormat
import java.util.*

object ParcelableCardEntityUtils {

    internal val sISOFormat: DateFormat = ThreadLocalSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.ENGLISH)

    init {
        sISOFormat.isLenient = true
        sISOFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

}
