package org.mariotaku.twidere.model.sync

import android.content.Context
import android.content.SharedPreferences
import org.mariotaku.twidere.util.sync.SyncController

/**
 * Created by mariotaku on 2017/1/2.
 */

abstract class SyncProviderInfo(val type: String) {
    abstract fun writeToPreferences(editor: SharedPreferences.Editor)
    abstract fun newSyncController(context: Context): SyncController
}
