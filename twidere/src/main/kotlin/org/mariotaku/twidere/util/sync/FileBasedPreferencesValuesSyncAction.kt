package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.SharedPreferences
import java.io.Closeable
import java.util.*

/**
 * Created by mariotaku on 2017/1/2.
 */

abstract class FileBasedPreferencesValuesSyncAction<DownloadSession : Closeable, UploadSession : Closeable>(
        context: Context,
        var preferences: SharedPreferences,
        val processor: Processor
) : FileBasedKeyValueSyncAction<DownloadSession, UploadSession>(context) {

    final override val snapshotFileName: String = processor.snapshotFileName

    final override val whatData: String = processor.whatData

    override fun addToLocal(data: MutableMap<String, String>) {
        val editor = preferences.edit()
        for ((k, v) in data) {
            processor.saveValue(editor, k, v)
        }
        editor.apply()
    }

    override fun removeFromLocal(data: MutableMap<String, String>) {
        val editor = preferences.edit()
        for (k in data.keys) {
            editor.remove(k)
        }
        editor.apply()
    }

    final override fun loadFromLocal(): MutableMap<String, String> {
        val result = HashMap<String, String>()
        for ((k, v) in preferences.all) {
            processor.loadValue(result, k, v)
        }
        return result
    }

    interface Processor {
        val snapshotFileName: String
        val whatData: String
        fun saveValue(editor: SharedPreferences.Editor, key: String, value: String)
        fun loadValue(map: MutableMap<String, String>, key: String, value: Any?)
    }
}
