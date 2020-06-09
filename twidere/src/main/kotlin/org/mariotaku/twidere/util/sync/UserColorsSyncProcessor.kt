package org.mariotaku.twidere.util.sync

import android.content.SharedPreferences
import android.graphics.Color
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor

/**
 * Created by mariotaku on 2017/1/2.
 */

object UserColorsSyncProcessor : FileBasedPreferencesValuesSyncAction.Processor {
    override fun loadValue(map: MutableMap<String, String>, key: String, value: Any?) {
        if (value is Int) {
            map[key] = toHexColor(value, format = HexColorFormat.RGB)
        }
    }

    override fun saveValue(editor: SharedPreferences.Editor, key: String, value: String) {
        try {
            editor.putInt(key, Color.parseColor(value))
        } catch (e: IllegalArgumentException) {
            // Ignore
        }
    }

    override val whatData: String = "user colors"

    override val snapshotFileName: String = "user_colors.xml"

}
