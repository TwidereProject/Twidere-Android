/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import android.util.LruCache
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_EMOJI_SUPPORT

/**
 * Created by mariotaku on 15/12/20.
 */
class ExternalThemeManager(private val context: Context, private val preferences: SharedPreferences) {

    var emoji: Emoji? = null
        private set
    var emojiPackageName: String? = null
        private set

    init {
        reloadEmojiPreferences()
    }

    fun reloadEmojiPreferences() {
        val emojiComponentName = preferences.getString(KEY_EMOJI_SUPPORT, null)
        emojiPackageName = if (emojiComponentName != null) {
            ComponentName.unflattenFromString(emojiComponentName)?.packageName
        } else {
            null
        }
        initEmojiSupport()
    }

    fun initEmojiSupport() {
        val pkgName = emojiPackageName
        if (pkgName == null) {
            emoji = null
            return
        }
        emoji = Emoji(context, pkgName)
    }

    class Emoji(context: Context, private val packageName: String) {
        private var useMipmap: Boolean = false
        private var resources: Resources? = null
        private val identifierCache = LruCache<IntArray, Int>(512)

        init {
            initResources(context, packageName)
        }

        private fun initResources(context: Context, packageName: String?) {
            if (packageName == null) {
                useMipmap = false
                resources = null
                return
            }
            try {
                val pm = context.packageManager
                val info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                if (info.metaData != null) {
                    this.useMipmap = info.metaData.getBoolean("org.mariotaku.twidere.extension.emoji.mipmap")
                }
                this.resources = pm.getResourcesForApplication(info)
            } catch (e: PackageManager.NameNotFoundException) {
                // Ignore
            }

        }

        fun getEmojiDrawableFor(vararg codePoints: Int): Drawable? {
            val resources = resources ?: return null
            val cached = identifierCache.get(codePoints)
            if (cached == null) {
                val sb = StringBuilder("emoji_u")
                for (i in codePoints.indices) {
                    if (i != 0) {
                        sb.append("_")
                    }
                    val hex = Integer.toHexString(codePoints[i])
                    for (j in 0 until 4 - hex.length) {
                        sb.append("0")
                    }
                    sb.append(hex)
                }
                val identifier = resources.getIdentifier(sb.toString(),
                        if (useMipmap) "mipmap" else "drawable", packageName)
                identifierCache.put(codePoints, identifier)
                if (identifier == 0) return null
                return ResourcesCompat.getDrawable(resources, identifier, null)
            } else if (cached != 0) {
                return ResourcesCompat.getDrawable(resources, cached, null)
            }
            return null
        }

        val isSupported: Boolean
            get() = resources != null
    }
}
