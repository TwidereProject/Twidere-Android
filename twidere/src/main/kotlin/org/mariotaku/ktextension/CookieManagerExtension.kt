package org.mariotaku.ktextension

import android.annotation.TargetApi
import android.os.Build
import android.webkit.CookieManager


/**
 * Created by mariotaku on 2016/12/18.
 */

fun CookieManager.removeAllCookiesSupport(callback: ((Boolean) -> Unit)? = null) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        @Suppress("DEPRECATION")
        removeAllCookie()
        callback?.invoke(true)
        return
    }
    CookieManagerSupportL.removeAllCookiesL(this, callback)
}

internal object CookieManagerSupportL {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun removeAllCookiesL(manager: CookieManager, callback: ((Boolean) -> Unit)?) {
        if (callback != null) {
            manager.removeAllCookies { callback(it) }
        } else {
            manager.removeAllCookies(null)
        }
    }
}
