package org.mariotaku.ktextension

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import java.io.File

val Context.hasApplication: Boolean
    get() = applicationContext is Application

val Context.preferExternalCacheDir: File
    get() = externalCacheDir ?: cacheDir

fun Context.checkAllSelfPermissionsGranted(vararg permissions: String): Boolean {
    return permissions.none { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
}

fun Context.checkAnySelfPermissionsGranted(vararg permissions: String): Boolean {
    return permissions.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
}

fun Context.unregisterReceiverSafe(receiver: BroadcastReceiver?): Boolean {
    if (receiver == null) return false
    try {
        unregisterReceiver(receiver)
        return true
    } catch (e: IllegalArgumentException) {
        return false
    }
}
