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

package org.mariotaku.twidere.util.api

import android.os.Build
import org.mariotaku.ktextension.bcp47Tag
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.twidere.extension.restfu.contains
import org.mariotaku.twidere.util.MicroBlogAPIFactory.ExtraHeaders
import java.util.*

/**
 * Created by mariotaku on 2017/2/25.
 */
object TwitterAndroidExtraHeaders : ExtraHeaders {

    const val clientName = "TwitterAndroid"
    const val versionName = "8.50.0-release.02"
    const val apiVersion = "5"
    const val internalVersionName = "18500002-r-2"

    override fun get(headers: MultiValueMap<String>): List<Pair<String, String>> {
        val result = ArrayList<Pair<String, String>>()
        val language = Locale.getDefault().bcp47Tag
        if ("User-Agent" !in headers) {
            result.add(Pair("User-Agent", userAgent))
        }
        result.add(Pair("Accept-Language", language))
        result.add(Pair("X-Twitter-Client", clientName))
        result.add(Pair("X-Twitter-Client-Language", language))
        result.add(Pair("X-Twitter-Client-Version", versionName))
        result.add(Pair("X-Twitter-API-Version", apiVersion))
        return result
    }

    val userAgent: String get() {
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val sdkRelease = Build.VERSION.RELEASE
        val brand = Build.BRAND
        val product = Build.PRODUCT
        return "$clientName/$versionName ($internalVersionName) $model/$sdkRelease ($manufacturer;$model;$brand;$product;0;;0)"
    }

}
