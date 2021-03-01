/*
 * 				Twidere - Twitter client for Android
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

import android.content.Context
import android.os.Bundle
import androidx.loader.content.Loader
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.userlists.UserListOwnershipsLoader
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListDestroyedEvent
import org.mariotaku.twidere.util.Utils

class UserListsOwnershipsFragment : ParcelableUserListsFragment() {

    private val screenName: String?
        get() = arguments?.getString(EXTRA_SCREEN_NAME)

    private val userKey: UserKey?
        get() = arguments?.getParcelable<UserKey?>(EXTRA_USER_KEY)

    override fun onCreateUserListsLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableUserList>> {
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        return UserListOwnershipsLoader(requireActivity(), accountKey, userKey, screenName, data).apply {
            pagination = args.getParcelable(EXTRA_PAGINATION)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_user_lists_owned, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_user_list -> {
                val f = CreateUserListDialogFragment()
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
                f.arguments = args
                parentFragmentManager.let { f.show(it, null) }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val accountKey = this.accountKey ?: return
        val screenName = this.screenName
        if (accountKey == userKey) {
            menu.setItemAvailability(R.id.new_user_list, true)
        } else {
            menu.setItemAvailability(R.id.new_user_list, screenName != null &&
                    Utils.isMyAccount(requireActivity(), screenName))
        }
    }


    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    @Subscribe
    fun onUserListDestroyed(event: UserListDestroyedEvent) {
        removeUserList(event.userList.id)
    }

    private fun removeUserList(id: String) {
        //        final int listsIdx = adapter.findItemPosition(id);
        //        if (listsIdx >= 0) {
        //            adapter.removeAt(listsIdx);
        //        }
    }

}
