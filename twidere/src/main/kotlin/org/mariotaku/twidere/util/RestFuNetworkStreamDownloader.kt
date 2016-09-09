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

import android.content.Context
import android.net.Uri
import org.mariotaku.pickncrop.library.ImagePickerActivity
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.twidere.util.dagger.DependencyHolder
import java.io.IOException

/**
 * Created by mariotaku on 15/6/17.
 */
class RestFuNetworkStreamDownloader(context: Context) : ImagePickerActivity.NetworkStreamDownloader(context) {

    @Throws(IOException::class)
    override operator fun get(uri: Uri): ImagePickerActivity.NetworkStreamDownloader.DownloadResult {
        val client = DependencyHolder.get(context).restHttpClient
        val builder = HttpRequest.Builder()
        builder.method(GET.METHOD)
        builder.url(uri.toString())
        val response = client.newCall(builder.build()).execute()
        if (response.isSuccessful) {
            val body = response.body
            val contentType = body.contentType()
            return ImagePickerActivity.NetworkStreamDownloader.DownloadResult.get(body.stream(), if (contentType != null) contentType!!.contentType else "image/*")
        } else {
            throw IOException("Unable to get " + uri)
        }
    }

}
