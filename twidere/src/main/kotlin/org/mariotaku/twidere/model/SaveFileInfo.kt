/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model

import android.webkit.MimeTypeMap
import java.io.Closeable
import java.io.InputStream
import java.util.*

interface SaveFileInfo : Closeable {
    val name: String

    val mimeType: String?

    val extension: String?
        get() {
            val typeLowered = mimeType?.toLowerCase(Locale.US) ?: return null
            return when (typeLowered) {
            // Hack for fanfou image type
                "image/jpg" -> "jpg"
                else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(typeLowered)
            }
        }

    val specialCharacter: Char

    fun stream(): InputStream
}