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
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.TabAccountFlags
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_LIST
import org.mariotaku.twidere.fragment.statuses.UserListTimelineFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.model.tab.argument.UserListArguments
import org.mariotaku.twidere.model.tab.conf.UserListExtraConfiguration

/**
 * Created by mariotaku on 2016/11/27.
 */

class UserListTimelineTabConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.list_timeline)

    override val icon = DrawableHolder.Builtin.LIST

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_REQUIRED

    override val fragmentClass = UserListTimelineFragment::class.java

    override fun checkAccountAvailability(details: AccountDetails) = when (details.type) {
        AccountType.TWITTER -> true
        else -> false
    }

    override fun getExtraConfigurations(context: Context) = arrayOf(
            UserListExtraConfiguration(EXTRA_USER_LIST).headerTitle(R.string.title_user_list)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val arguments = tab.arguments as UserListArguments
        when (extraConf.key) {
            EXTRA_USER_LIST -> {
                val userList = (extraConf as UserListExtraConfiguration).value ?: return false
                arguments.listId = userList.id
            }
        }
        return true
    }
}
