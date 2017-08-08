package org.mariotaku.ktextension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

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
    try {
        unregisterReceiver(receiver)
        return true
    } catch (e: IllegalArgumentException) {
        return false
    }
}