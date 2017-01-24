package org.mariotaku.twidere.util

import android.util.Log
import org.mariotaku.twidere.BuildConfig

/**
 * Created by mariotaku on 2017/1/24.
 */
object DebugLog {

    fun d(tag: String, msg: String, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        if (tr != null) {
            return Log.d(tag, msg, tr)
        } else {
            return Log.d(tag, msg)
        }
    }

    fun w(tag: String, msg: String? = null, tr: Throwable? = null): Int {
        if (!BuildConfig.DEBUG) return 0
        if (msg != null && tr != null) {
            return Log.w(tag, msg, tr)
        } else if (msg != null) {
            return Log.w(tag, msg)
        } else {
            return Log.w(tag, tr)
        }
    }
}