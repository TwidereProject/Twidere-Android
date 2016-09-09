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
import edu.tsinghua.hotmobi.HotMobiLogger
import okhttp3.ConnectionPool
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.net.TwidereDns
import javax.inject.Inject

/**
 * Created by mariotaku on 15/12/31.
 */
class DependencyHolder internal constructor(context: Context) {
    @Inject
    lateinit var hotMobiLogger: HotMobiLogger
        internal set
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
    lateinit var dns: TwidereDns
        internal set
    @Inject
    lateinit var validator: TwidereValidator
        internal set
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
        internal set
    @Inject
    lateinit var connectionPoll: ConnectionPool
        internal set
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
        internal set

    init {
        GeneralComponentHelper.build(context).inject(this)
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
