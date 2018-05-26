package org.mariotaku.twidere.util

import android.content.Context
import android.net.Uri

object UriCreator {
    fun resourceId(packageName: String, resId: Int): Uri {
        return Uri.parse("android.resource://$packageName/$resId")
    }
    fun resourceIdString(packageName: String, resId: Int): String {
        return "android.resource://$packageName/$resId"
    }

    fun asset(path: String): Uri {
        return Uri.parse("asset://$path")
    }

    fun resourceId(context: Context, resId: Int): Uri {
        return resourceId(context.packageName, resId)
    }
}