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

class ReadStateManager(context: Context) {

    private val preferences = SharedPreferencesWrapper.getInstance(context,
            TIMELINE_POSITIONS_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getPosition(key: String): Long {
        if (TextUtils.isEmpty(key)) return -1
        return preferences.getLong(key, -1)
    }

    fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun setPosition(key: String, position: Long, acceptOlder: Boolean = false): Boolean {
        if (TextUtils.isEmpty(key) || !acceptOlder && getPosition(key) >= position) return false
        val editor = preferences.edit()
        editor.putLong(key, position)
        editor.apply()
        return true
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
