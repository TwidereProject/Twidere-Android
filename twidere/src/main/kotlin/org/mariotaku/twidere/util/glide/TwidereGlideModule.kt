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
import android.os.Build
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.mariotaku.twidere.model.media.AuthenticatedUri
import org.mariotaku.twidere.util.HttpClientFactory
import org.mariotaku.twidere.util.UserAgentUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.media.ThumborWrapper
import org.mariotaku.twidere.util.okhttp.ModifyRequestInterceptor
import java.io.InputStream

class TwidereGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Do nothing.
    }

    override fun registerComponents(context: Context, glide: Glide) {
        val holder = DependencyHolder.get(context)
        val builder = OkHttpClient.Builder()
        val conf = HttpClientFactory.HttpClientConfiguration(holder.preferences)
        val thumbor = holder.thumbor
        HttpClientFactory.initOkHttpClient(conf, builder, holder.dns, holder.connectionPool, holder.cache)
        val userAgent = UserAgentUtils.getDefaultUserAgentStringSafe(context) ?: ""
        builder.addInterceptor(ModifyRequestInterceptor(ThumborModifier(thumbor), UserAgentModifier(userAgent)))
        val client = builder.build()
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
        glide.register(AuthenticatedUri::class.java, InputStream::class.java, AuthenticatedUrlLoader.Factory(client))
    }

    class ThumborModifier(val thumbor: ThumborWrapper) : ModifyRequestInterceptor.RequestModifier {
        override fun modify(original: Request, builder: Request.Builder): Boolean {
            if (!thumbor.available) return false
            // Since Thumbor doesn't support Authorization header, disable for requests with authorization
            if (original.header("Authorization") != null) {
                return false
            }
            builder.url(thumbor.buildUri(original.url().toString()))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                builder.header("Accept", "image/webp, */*")
            }
            return true
        }

    }

    class UserAgentModifier(val userAgent: String) : ModifyRequestInterceptor.RequestModifier {
        override fun modify(original: Request, builder: Request.Builder): Boolean {
            builder.header("User-Agent", userAgent)
            return true
        }

    }
}

