package org.mariotaku.twidere.util

import android.util.Log
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.LOGTAG

/**
 * Created by mariotaku on 2017/1/24.
 */
object DebugLog {

    @JvmStatic
    fun v(tag: String = LOGTAG, msg: String, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        return if (tr != null) {
            Log.v(tag, msg, tr)
        } else {
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun d(tag: String = LOGTAG, msg: String, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        return if (tr != null) {
            Log.d(tag, msg, tr)
        } else {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String = LOGTAG, msg: String? = null, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        return if (msg != null && tr != null) {
            Log.w(tag, msg, tr)
        } else if (msg != null) {
            Log.w(tag, msg)
        } else {
            Log.w(tag, tr)
        }
    }
}