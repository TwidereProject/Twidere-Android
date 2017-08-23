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

package org.mariotaku.twidere.task.filter

import android.content.Context
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.twidere.model.presentation.LaunchPresentation
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.JsonSerializer
import java.io.IOException


/**
 * Created by mariotaku on 2017/8/22.
 */
class RefreshLaunchPresentationsTask(context: Context) : BaseAbstractTask<Unit?, Boolean, (Boolean) -> Unit>(context) {
    override fun doLongOperation(params: Unit?): Boolean {
        val request = HttpRequest.Builder()
                .method(GET.METHOD)
                .url("https://twidere.mariotaku.org/assets/data/launch_presentations.json")
                .build()
        try {
            val presentations = restHttpClient.newCall(request).execute().use {
                if (!it.isSuccessful) return@use null
                return@use JsonSerializer.parseList(it.body.stream(), LaunchPresentation::class.java)
            }
            jsonCache.saveList(JSON_CACHE_KEY, presentations, LaunchPresentation::class.java)
            return true
        } catch (e: IOException) {
            return false
        }
    }

    companion object {
        const val JSON_CACHE_KEY = "launch_presentations"
    }
}