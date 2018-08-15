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

package org.mariotaku.twidere.loader

import android.content.Context
import android.support.v4.content.FixedAsyncTaskLoader
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

class DefaultAPIConfigLoader(context: Context) : FixedAsyncTaskLoader<List<CustomAPIConfig>>(context) {
    @Inject
    lateinit var client: RestHttpClient

    init {
        GeneralComponent.get(context).inject(this)
    }

    override fun loadInBackground(): List<CustomAPIConfig> {
        return CustomAPIConfig.listDefault(context)
    }

    override fun onStartLoading() {
        deliverResult(CustomAPIConfig.listDefault(context))
        forceLoad()
    }
}