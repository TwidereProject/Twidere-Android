package org.mariotaku.twidere.util.sync

import android.content.SharedPreferences

/**
 * Created by mariotaku on 2017/1/2.
 */

object UserNicknamesSyncProcessor : FileBasedPreferencesValuesSyncAction.Processor {
    override fun loadValue(map: MutableMap<String, String>, key: String, value: Any?) {
        if (value is String) {
            map[key] = value
        }
    }

    override fun saveValue(editor: SharedPreferences.Editor, key: String, value: String) {
        editor.putString(key, value)
    }

    override val whatData: String = "user nicknames"

    override val snapshotFileName: String = "user_nicknames.xml"

}
