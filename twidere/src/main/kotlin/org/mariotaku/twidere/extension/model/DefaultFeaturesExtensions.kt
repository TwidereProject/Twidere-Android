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

package org.mariotaku.twidere.extension.model

import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import com.bluelinelabs.logansquare.LoganSquare
import com.fasterxml.jackson.core.JsonToken
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder
import org.mariotaku.restfu.RestFuUtils
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.model.DefaultFeatures
import java.io.IOException

/**
 * Created by mariotaku on 2017/4/20.
 */

private const val REMOTE_SETTINGS_URL = "https://twidere.mariotaku.org/assets/data/default_features.json"
private const val KEY_DEFAULT_TWITTER_CONSUMER_KEY = "default_twitter_consumer_key"
private const val KEY_DEFAULT_TWITTER_CONSUMER_SECRET = "default_twitter_consumer_secret"

@WorkerThread
@Throws(IOException::class)
fun DefaultFeatures.loadRemoteSettings(client: RestHttpClient): Boolean {
    val request = HttpRequest.Builder().method(GET.METHOD).url(REMOTE_SETTINGS_URL).build()
    val response = client.newCall(request).execute()
    try {
        val mapper = LoganSquareMapperFinder.mapperFor(DefaultFeatures::class.java)
        val jsonParser = LoganSquare.JSON_FACTORY.createParser(response.body.stream())
        if (jsonParser.currentToken == null) {
            jsonParser.nextToken()
        }
        if (jsonParser.currentToken != JsonToken.START_OBJECT) {
            jsonParser.skipChildren()
            return false
        }
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            val fieldName = jsonParser.currentName
            jsonParser.nextToken()
            mapper.parseField(this, fieldName, jsonParser)
            jsonParser.skipChildren()
        }
    } finally {
        RestFuUtils.closeSilently(response)
    }
    return true
}

fun DefaultFeatures.load(preferences: SharedPreferences) {
    defaultTwitterConsumerKey = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, null)
    defaultTwitterConsumerSecret = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, null)
}

fun DefaultFeatures.save(preferences: SharedPreferences) {
    val editor = preferences.edit()
    editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, defaultTwitterConsumerKey)
    editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, defaultTwitterConsumerSecret)
    editor.apply()
}