/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.os.Bundle

import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter

class ListsFragment : AbsToolbarTabPagesFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun addTabs(adapter: SupportTabsAdapter) {
        adapter.add(cls = UserListsOwnershipsFragment::class.java, args = arguments,
                name = getString(R.string.title_user_list_ownerships))
        adapter.add(cls = UserListSubscriptionsFragment::class.java, args = arguments,
                name = getString(R.string.title_user_list_subscriptions))
        adapter.add(cls = UserListMembershipsFragment::class.java, args = arguments,
                name = getString(R.string.title_user_list_memberships))
    }
}
