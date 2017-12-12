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

package org.mariotaku.twidere.util

import android.content.Context
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.ConsumerKeyType
import java.nio.charset.Charset
import java.util.zip.CRC32

object InternalTwitterContentUtils {

    fun isOfficialKey(context: Context, consumerKey: String, consumerSecret: String): Boolean {
        return getOfficialKeyType(context, consumerKey, consumerSecret) != ConsumerKeyType.UNKNOWN
    }

    fun getOfficialKeyType(context: Context, consumerKey: String, consumerSecret: String): ConsumerKeyType {
        val keySecrets = context.resources.getStringArray(R.array.values_official_consumer_secret_crc32)
        val keyNames = context.resources.getStringArray(R.array.types_official_consumer_secret)
        val crc32 = CRC32()
        val consumerSecretBytes = consumerSecret.toByteArray(Charset.forName("UTF-8"))
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.size)
        val value = crc32.value
        crc32.reset()
        val index = keySecrets.indexOfFirst { secret ->
            return@indexOfFirst secret.toLong(16) == value
        }
        if (index < 0) return ConsumerKeyType.UNKNOWN
        return ConsumerKeyType.parse(keyNames[index])
    }
}
