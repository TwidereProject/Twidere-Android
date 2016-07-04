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
import android.support.v4.content.Loader
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.UserListsLoader
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.UserListDestroyedEvent
import org.mariotaku.twidere.util.MenuUtils
import org.mariotaku.twidere.util.Utils

class UserListsFragment : ParcelableUserListsFragment() {

    public override fun onCreateUserListsLoader(context: Context,
                                                args: Bundle, fromUser: Boolean): Loader<List<ParcelableUserList>> {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        return UserListsLoader(activity, accountKey, userKey, screenName, true, data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_user_lists_owned, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.new_user_list -> {
                val f = CreateUserListDialogFragment()
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
                f.arguments = args
                f.show(fragmentManager, null)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val item = menu!!.findItem(R.id.new_user_list)
        val accountId = accountKey
        if (accountId == null || item == null) return
        if (accountId == userId) {
            MenuUtils.setItemAvailability(menu, R.id.new_user_list, true)
        } else {
            MenuUtils.setItemAvailability(menu, R.id.new_user_list, Utils.isMyAccount(activity, screenName))
        }
    }

    private val screenName: String
        get() = arguments.getString(EXTRA_SCREEN_NAME)

    private val userId: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_USER_KEY)

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
        val adapter = adapter
        //        final int listsIdx = adapter.findItemPosition(id);
        //        if (listsIdx >= 0) {
        //            adapter.removeAt(listsIdx);
        //        }
    }

}
