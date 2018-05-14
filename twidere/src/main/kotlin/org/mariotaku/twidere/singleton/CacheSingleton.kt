package org.mariotaku.twidere.singleton

import okhttp3.Cache
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.extension.getCacheDir
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder

object CacheSingleton : ApplicationContextSingletonHolder<Cache>(creator@{
    val preferences = PreferencesSingleton.get(it)
    val cacheSizeMB = preferences.getInt(SharedPreferenceConstants.KEY_CACHE_SIZE_LIMIT, 300).coerceIn(100..500)
    // Convert to bytes
    return@creator Cache(it.getCacheDir("network", cacheSizeMB * 1048576L), cacheSizeMB * 1048576L)
})