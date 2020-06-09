package org.mariotaku.ktextension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Created by mariotaku on 2016/12/13.
 */

fun Context.checkAllSelfPermissionsGranted(vararg permissions: String): Boolean {
    return permissions.none { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
}

fun Context.checkAnySelfPermissionsGranted(vararg permissions: String): Boolean {
    return permissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
}

fun Context.unregisterReceiverSafe(receiver: BroadcastReceiver?): Boolean {
    if (receiver == null) return false
    return try {
        unregisterReceiver(receiver)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

val Context.preferExternalCacheDir: File
    get() = externalCacheDir ?: cacheDir