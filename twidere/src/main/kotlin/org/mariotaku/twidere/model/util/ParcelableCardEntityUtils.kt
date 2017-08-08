package org.mariotaku.twidere.model.util


import org.mariotaku.microblog.library.twitter.util.ThreadLocalSimpleDateFormat
import java.text.DateFormat
import java.util.*

/**
 * Created by mariotaku on 16/2/24.
 */
object ParcelableCardEntityUtils {

    internal val sISOFormat: DateFormat = ThreadLocalSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.ENGLISH)

    init {
        sISOFormat.isLenient = true
        sISOFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

}
