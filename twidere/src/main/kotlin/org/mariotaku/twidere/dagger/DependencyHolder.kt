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

package org.mariotaku.twidere.dagger

import android.content.Context
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.util.ActivityTracker
import org.mariotaku.twidere.util.NotificationManagerWrapper
import org.mariotaku.twidere.util.ReadStateManager
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.media.ThumborWrapper
import javax.inject.Inject

/**
 * Created by mariotaku on 15/12/31.
 */
class DependencyHolder internal constructor(val context: Context) {
    @Inject
    lateinit var readStateManager: ReadStateManager
        internal set
    @Inject
    lateinit var restHttpClient: RestHttpClient
        internal set
    @Inject
    lateinit var activityTracker: ActivityTracker
        internal set
    @Inject
    lateinit var mediaPreloader: MediaPreloader
        internal set
    @Inject
    lateinit var thumbor: ThumborWrapper
        internal set

    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
        internal set

    init {
        GeneralComponent.get(context).inject(this)
    }

    companion object :ApplicationContextSingletonHolder<DependencyHolder>(::DependencyHolder)
}
