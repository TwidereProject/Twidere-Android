package org.mariotaku.twidere.extension

import android.content.SharedPreferences

/**
 * Created by mariotaku on 16/8/25.
 */
fun SharedPreferences.getNonEmptyString(key: String, def: String): String {
    return getString(key, def)?.let { if (it.isEmpty()) null else it } ?: def
}