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

package org.mariotaku.twidere.model.tab.impl

import android.content.Context
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.TabAccountFlags
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.fragment.statuses.UserFavoritesFragment
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.model.tab.argument.UserArguments
import org.mariotaku.twidere.model.tab.conf.UserExtraConfiguration
import org.mariotaku.twidere.util.dagger.DependencyHolder

/**
 * Created by mariotaku on 2016/11/27.
 */

class FavoriteTimelineTabConfiguration : TabConfiguration() {

    override val name = FavoriteStringHolder

    override val icon = DrawableHolder.Builtin.FAVORITE

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_REQUIRED

    override val fragmentClass = UserFavoritesFragment::class.java

    override fun getExtraConfigurations(context: Context) = arrayOf(
            UserExtraConfiguration(EXTRA_USER).headerTitle(R.string.title_user)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: ExtraConfiguration): Boolean {
        val arguments = tab.arguments as UserArguments
        when (extraConf.key) {
            EXTRA_USER -> {
                val user = (extraConf as UserExtraConfiguration).value ?: return false
                arguments.setUserKey(user.key)
            }
        }
        return true
    }

    object FavoriteStringHolder : StringHolder() {

        override fun createString(context: Context): String {
            if (DependencyHolder.get(context).preferences[iWantMyStarsBackKey]) {
                return context.getString(R.string.title_favorites)
            }
            return context.getString(R.string.title_likes)
        }
    }
}
