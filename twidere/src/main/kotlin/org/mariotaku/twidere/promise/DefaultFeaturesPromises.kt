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

import android.app.Application
import android.content.SharedPreferences
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.defaultFeatureLastUpdated
import org.mariotaku.twidere.dagger.component.PromisesComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.model.loadRemoteSettings
import org.mariotaku.twidere.extension.model.save
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class DefaultFeaturesPromises(private val application: Application) {

    @Inject
    lateinit var defaultFeatures: DefaultFeatures
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var restHttpClient: RestHttpClient

    init {
        PromisesComponent.get(application).inject(this)
    }

    fun fetch(): Promise<Boolean, Exception> = task {
        val lastUpdated = preferences[defaultFeatureLastUpdated]
        if (lastUpdated > 0 && TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastUpdated) < 12) {
            return@task false
        }
        defaultFeatures.loadRemoteSettings(restHttpClient)

    }.success {
        defaultFeatures.save(preferences)
        DebugLog.d(TwidereConstants.LOGTAG, "Loaded remote features")
    }.fail {
        DebugLog.w(TwidereConstants.LOGTAG, "Unable to load remote features", it)
    }.always {
        preferences[defaultFeatureLastUpdated] = System.currentTimeMillis()
    }

    companion object : ApplicationContextSingletonHolder<DefaultFeaturesPromises>(::DefaultFeaturesPromises)
}
