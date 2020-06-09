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

package org.mariotaku.twidere.util.glide

import android.content.Context
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.*
import com.bumptech.glide.signature.ObjectKey
import okhttp3.OkHttpClient
import org.mariotaku.twidere.model.media.NoThumborUrl
import java.io.InputStream

class NoThumborUrlLoader(
        val context: Context,
        val client: OkHttpClient
) : ModelLoader<NoThumborUrl, InputStream> {


    class Factory(val context: Context, val client: OkHttpClient) : ModelLoaderFactory<NoThumborUrl, InputStream> {
        override fun build(factory: MultiModelLoaderFactory) = NoThumborUrlLoader(context, client)

        override fun teardown() {}
    }

    companion object {
        const val HEADER_NO_THUMBOR = "X-Twidere-No-Thumbor"
    }

    override fun buildLoadData(model: NoThumborUrl, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream>? {
        val headersBuilder = LazyHeaders.Builder()
        headersBuilder.addHeader(HEADER_NO_THUMBOR, "true")
        val glideUrl = GlideUrl(model.url, headersBuilder.build())
        val fetcher = OkHttpStreamFetcher(client, glideUrl)
        return ModelLoader.LoadData(ObjectKey(model), fetcher)
    }

    override fun handles(model: NoThumborUrl): Boolean {
        return true
    }

}