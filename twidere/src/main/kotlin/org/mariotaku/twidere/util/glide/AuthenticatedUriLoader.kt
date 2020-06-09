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

import android.accounts.AccountManager
import android.content.Context
import android.net.Uri
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.*
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import okhttp3.OkHttpClient
import org.mariotaku.twidere.extension.model.authorizationHeader
import org.mariotaku.twidere.extension.model.getCredentials
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.media.AuthenticatedUri
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.media.TwidereMediaDownloader
import java.io.InputStream

class AuthenticatedUriLoader(
        val context: Context,
        val client: OkHttpClient
) : ModelLoader<AuthenticatedUri, InputStream> {

    val UserKey.credentials: Credentials? get() {
        val am = AccountManager.get(context)
        return AccountUtils.findByAccountKey(am, this)?.getCredentials(am)
    }

    internal class AuthorizationHeaderFactory(val uri: Uri, val credentials: Credentials) : LazyHeaderFactory {
        override fun buildHeader() = credentials.authorizationHeader(uri)
    }

    class Factory(val context: Context, val client: OkHttpClient) : ModelLoaderFactory<AuthenticatedUri, InputStream> {
        override fun build(factory: MultiModelLoaderFactory) = AuthenticatedUriLoader(context, client)
        override fun teardown() {}
    }

    override fun buildLoadData(model: AuthenticatedUri, width: Int, height: Int, options: Options): LoadData<InputStream>? {

        val headersBuilder = LazyHeaders.Builder()
        val credentials = model.accountKey?.credentials
        if (credentials != null && TwidereMediaDownloader.isAuthRequired(credentials, model.uri)) {
            headersBuilder.addHeader("Authorization", AuthorizationHeaderFactory(model.uri, credentials))
        }
        val glideUrl = GlideUrl(model.uri.toString(), headersBuilder.build())
        return LoadData(ObjectKey(model), OkHttpStreamFetcher(client, glideUrl))
    }

    override fun handles(model: AuthenticatedUri): Boolean {
        return true
    }

}