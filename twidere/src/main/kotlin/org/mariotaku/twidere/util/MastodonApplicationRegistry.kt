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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.RegisteredApplication
import org.mariotaku.microblog.library.twitter.auth.EmptyAuthorization
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import java.io.IOException

/**
 * Created by mariotaku on 2017/4/17.
 */

class MastodonApplicationRegistry(private val context: Context) {
    private val preferences = context.getSharedPreferences(ETAG_MASTODON_APPS_PREFERENCES_NAME,
            Context.MODE_PRIVATE)

    operator fun get(host: String): RegisteredApplication? {
        val json = preferences.getString(host, null) ?: return null
        return try {
            JsonSerializer.parse(json, RegisteredApplication::class.java)
        } catch (e: IOException) {
            null
        }
    }

    operator fun set(host: String, app: RegisteredApplication?): Boolean {
        val editor = preferences.edit()
        if (app != null) {
            editor.putString(host, JsonSerializer.serialize(app, RegisteredApplication::class.java))
        } else {
            return false
        }
        editor.apply()
        return true
    }

    @Throws(MicroBlogException::class)
    fun fetch(host: String, scopes: Array<String>): RegisteredApplication {
        val endpoint = Endpoint("https://$host/api/")
        val mastodon = newMicroBlogInstance(context, endpoint, EmptyAuthorization(),
                AccountType.MASTODON, Mastodon::class.java)
        val registered = mastodon.registerApplication("Twidere for Android", MASTODON_CALLBACK_URL,
                scopes, TWIDERE_PROJECT_URL)
        this[host] = registered
        return registered
    }
}
