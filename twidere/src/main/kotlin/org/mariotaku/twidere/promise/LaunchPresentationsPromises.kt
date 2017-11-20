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

package org.mariotaku.twidere.promise

import android.content.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.dagger.component.PromisesComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.model.presentation.LaunchPresentation
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.cache.JsonCache
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Inject


class LaunchPresentationsPromises private constructor(context: Context) {

    @Inject
    lateinit var restHttpClient: RestHttpClient
    @Inject
    lateinit var jsonCache: JsonCache

    init {
        PromisesComponent.get(context).inject(this)
    }

    fun refresh(): Promise<Boolean, Exception> = task {
        val builder = HttpRequest.Builder()
        builder.method(GET.METHOD)
        if (BuildConfig.DEBUG) {
            builder.url("https://twidere.mariotaku.org/assets/data/launch_presentations_debug.json")
        } else {
            builder.url("https://twidere.mariotaku.org/assets/data/launch_presentations.json")
        }
        val request = builder.build()
        val presentations = restHttpClient.newCall(request).execute().use {
            if (!it.isSuccessful) return@use null
            return@use JsonSerializer.parseList(it.body.stream(), LaunchPresentation::class.java)
        }
        jsonCache.saveList(JSON_CACHE_KEY, presentations, LaunchPresentation::class.java)
        return@task true
    }

    companion object : ApplicationContextSingletonHolder<LaunchPresentationsPromises>(::LaunchPresentationsPromises) {
        const val JSON_CACHE_KEY = "launch_presentations"
    }
}

