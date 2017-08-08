/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter.iface

import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.view.holder.GroupViewHolder

/**
 * Created by mariotaku on 15/4/16.
 */
interface IGroupsAdapter<in Data> : IContentAdapter {

    val groupsCount: Int

    val nameFirst: Boolean

    val showAccountsColor: Boolean

    val groupAdapterListener: GroupAdapterListener?

    fun getGroup(position: Int): ParcelableGroup?

    fun getGroupId(position: Int): String?

    fun setData(data: Data?)

    interface GroupAdapterListener {

        fun onGroupClick(holder: GroupViewHolder, position: Int)

        fun onGroupLongClick(holder: GroupViewHolder, position: Int): Boolean

    }
}
