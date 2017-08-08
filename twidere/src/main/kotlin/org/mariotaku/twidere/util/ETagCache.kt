package org.mariotaku.twidere.util

import android.content.Context
import org.mariotaku.twidere.TwidereConstants.ETAG_CACHE_PREFERENCES_NAME

/**
 * Created by mariotaku on 2017/1/12.
 */

class ETagCache(context: Context) {
    private val prefs = context.getSharedPreferences(ETAG_CACHE_PREFERENCES_NAME, Context.MODE_PRIVATE)
    operator fun get(url: String): String? {
        return prefs.getString(url, null)
    }

    operator fun set(url: String, etag: String?) {
        prefs.edit().putString(url, etag).apply()
    }

    operator fun contains(url: String): Boolean {
        return prefs.contains(url)
    }
}
