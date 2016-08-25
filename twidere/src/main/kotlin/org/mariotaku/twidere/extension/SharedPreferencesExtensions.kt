package org.mariotaku.twidere.extension

import android.content.SharedPreferences
import android.text.TextUtils

/**
 * Created by mariotaku on 16/8/25.
 */
fun SharedPreferences.getNonEmptyString(key: String, def: String): String {
    val v = getString(key, def)
    return if (TextUtils.isEmpty(v)) def else v
}