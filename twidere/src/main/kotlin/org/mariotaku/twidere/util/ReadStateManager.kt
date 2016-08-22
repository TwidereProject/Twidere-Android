/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.text.TextUtils
import org.mariotaku.twidere.TwidereConstants.TIMELINE_POSITIONS_PREFERENCES_NAME
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.NotificationType
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.model.StringLongPair
import org.mariotaku.twidere.util.collection.CompactHashSet

class ReadStateManager(context: Context) {

    private val preferences: SharedPreferencesWrapper

    init {
        preferences = SharedPreferencesWrapper.getInstance(context,
                TIMELINE_POSITIONS_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    fun getPosition(key: String): Long {
        if (TextUtils.isEmpty(key)) return -1
        return preferences.getLong(key, -1)
    }

    fun getPositionPairs(key: String): Array<StringLongPair> {
        if (TextUtils.isEmpty(key)) return emptyArray()
        val set = preferences.getStringSet(key, null) ?: return emptyArray()
        try {
            return set.map { StringLongPair.valueOf(it) }.toTypedArray()
        } catch (e: NumberFormatException) {
            return emptyArray()
        }
    }

    fun getPosition(key: String, keyId: String): Long {
        if (TextUtils.isEmpty(key)) return -1
        val set = preferences.getStringSet(key, null) ?: return -1
        val prefix = keyId + ":"
        val first = set.firstOrNull { it.startsWith(prefix) } ?: return -1
        return StringLongPair.valueOf(first).value
    }

    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    @JvmOverloads fun setPosition(key: String, keyId: String, position: Long, acceptOlder: Boolean = false): Boolean {
        if (TextUtils.isEmpty(key)) return false
        val set: MutableSet<String> = preferences.getStringSet(key, null) ?: CompactHashSet<String>()
        val prefix = keyId + ":"
        val keyValue: String? = set.firstOrNull { it.startsWith(prefix) }
        val pair: StringLongPair
        if (keyValue != null) {
            // Found value
            pair = StringLongPair.valueOf(keyValue)
            if (!acceptOlder && pair.value > position) return false
            set.remove(keyValue)
            pair.value = position
        } else {
            pair = StringLongPair(keyId, position)
        }
        set.add(pair.toString())
        val editor = preferences.edit()
        editor.putStringSet(key, set)
        editor.apply()
        return true
    }


    fun setPositionPairs(key: String, pairs: Array<StringLongPair>?): Boolean {
        if (TextUtils.isEmpty(key)) return false
        val editor = preferences.edit()
        if (pairs == null) {
            editor.remove(key)
        } else {
            val set = CompactHashSet<String>()
            for (pair in pairs) {
                set.add(pair.toString())
            }
            editor.putStringSet(key, set)
        }
        editor.apply()
        return true
    }

    @JvmOverloads fun setPosition(key: String, position: Long, acceptOlder: Boolean = false): Boolean {
        if (TextUtils.isEmpty(key) || !acceptOlder && getPosition(key) >= position) return false
        val editor = preferences.edit()
        editor.putLong(key, position)
        editor.apply()
        return true
    }

    interface OnReadStateChangeListener {
        fun onReadStateChanged()
    }

    companion object {

        @ReadPositionTag
        fun getReadPositionTagForNotificationType(@NotificationType notificationType: String?): String? {
            if (notificationType == null) return null
            when (notificationType) {
                NotificationType.HOME_TIMELINE -> {
                    return ReadPositionTag.HOME_TIMELINE
                }
                NotificationType.DIRECT_MESSAGES -> {
                    return ReadPositionTag.DIRECT_MESSAGES
                }
                NotificationType.INTERACTIONS -> {
                    return ReadPositionTag.ACTIVITIES_ABOUT_ME
                }
            }
            return null
        }

        @ReadPositionTag
        fun getReadPositionTagForTabType(@CustomTabType tabType: String?): String? {
            if (tabType == null) return null
            when (tabType) {
                CustomTabType.HOME_TIMELINE -> {
                    return ReadPositionTag.HOME_TIMELINE
                }
                CustomTabType.NOTIFICATIONS_TIMELINE -> {
                    return ReadPositionTag.ACTIVITIES_ABOUT_ME
                }
                CustomTabType.DIRECT_MESSAGES -> {
                    return ReadPositionTag.DIRECT_MESSAGES
                }
            }
            return null
        }
    }

}
