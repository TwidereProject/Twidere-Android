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
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule
import okhttp3.OkHttpClient
import org.mariotaku.twidere.util.HttpClientFactory
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.media.ThumborInterceptor
import java.io.InputStream

class OkHttpGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Do nothing.
    }

    override fun registerComponents(context: Context, glide: Glide) {
        val holder = DependencyHolder.get(context)
        val builder = OkHttpClient.Builder()
        val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
        val thumbor = holder.thumbor
        HttpClientFactory.initOkHttpClient(conf, builder, holder.dns, holder.connectionPool, holder.cache)
        builder.addInterceptor(ThumborInterceptor(thumbor))
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(builder.build()))
    }

}