package org.mariotaku.twidere.model.sync

import android.content.SharedPreferences

/**
 * Created by mariotaku on 2017/1/2.
 */

abstract class SyncProviderInfo(val type: String) {
    abstract fun writeToPreferences(editor: SharedPreferences.Editor)
}
