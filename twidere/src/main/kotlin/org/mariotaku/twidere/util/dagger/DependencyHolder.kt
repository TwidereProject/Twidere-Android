/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.dagger

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dns
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.media.ThumborWrapper
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.sync.TimelineSyncManager
import javax.inject.Inject

/**
 * Created by mariotaku on 15/12/31.
 */
class DependencyHolder internal constructor(context: Context) {
    @Inject
    lateinit var readStateManager: ReadStateManager
        internal set
    @Inject
    lateinit var restHttpClient: RestHttpClient
        internal set
    @Inject
    lateinit var externalThemeManager: ExternalThemeManager
        internal set
    @Inject
    lateinit var activityTracker: ActivityTracker
        internal set
    @Inject
    lateinit var dns: Dns
        internal set
    @Inject
    lateinit var preferences: SharedPreferences
        internal set
    @Inject
    lateinit var connectionPool: ConnectionPool
        internal set
    @Inject
    lateinit var cache: Cache
        internal set
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
        internal set
    @Inject
    lateinit var mediaPreloader: MediaPreloader
        internal set
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
        internal set
    @Inject
    lateinit var kPreferences: KPreferences
        internal set
    @Inject
    lateinit var thumbor: ThumborWrapper
        internal set
    @Inject
    lateinit var timelineSyncManagerFactory: TimelineSyncManager.Factory
        internal set

    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
        internal set
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
        internal set

    init {
        GeneralComponent.get(context).inject(this)
    }

    companion object {

        private var sInstance: DependencyHolder? = null

        fun get(context: Context): DependencyHolder {
            if (sInstance != null) return sInstance!!
            sInstance = DependencyHolder(context)
            return sInstance!!
        }
    }
}
