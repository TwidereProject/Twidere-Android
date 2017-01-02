package org.mariotaku.twidere.util.sync

import android.content.Context
import android.content.SharedPreferences
import java.io.Closeable
import java.util.*

/**
 * Created by mariotaku on 2017/1/2.
 */

abstract class FileBasedPreferencesValuesSyncHelper<DownloadSession : Closeable, UploadSession : Closeable>(
        context: Context,
        var preferences: SharedPreferences,
        val processor: Processor
) : FileBasedKeyValueSyncHelper<DownloadSession, UploadSession>(context) {

    override final val snapshotFileName: String = processor.snapshotFileName

    override final val whatData: String = processor.whatData

    override final fun MutableMap<String, String>.saveToLocal() {
        val editor = preferences.edit()
        editor.clear()
        for ((k, v) in this) {
            processor.saveValue(editor, k, v)
        }
        editor.apply()
    }

    override final fun loadFromLocal(): MutableMap<String, String> {
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
