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

package org.mariotaku.twidere.fragment.users

import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.fragment.DeleteUserListMembersDialogFragment
import org.mariotaku.twidere.fragment.ParcelableUsersFragment
import org.mariotaku.twidere.fragment.UserListFragment
import org.mariotaku.twidere.loader.users.AbsRequestUsersLoader
import org.mariotaku.twidere.loader.users.UserListMembersLoader
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListMembersChangedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.UserViewHolder
import java.util.*

class UserListMembersFragment : ParcelableUsersFragment() {

    val userList: ParcelableUserList?
        get() {
            val parent = parentFragment
            if (parent is UserListFragment) {
                return parent.userList
            }
            return null
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerForContextMenu(recyclerView)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_USER_LIST, userList)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            AbsRequestUsersLoader {
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val listId = args.getString(EXTRA_LIST_ID)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val listName = args.getString(EXTRA_LIST_NAME)
        return UserListMembersLoader(context, accountKey, listId, userKey, screenName, listName,
                adapter.getData(), fromUser)
    }

    override fun onUserLongClick(holder: UserViewHolder, position: Int): Boolean {
        return recyclerView.showContextMenuForChild(holder.itemView)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val arguments = arguments ?: return
        val accountKey = arguments.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
        if (accountKey == null || accountKey != userKey) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val user = adapter.getUser(contextMenuInfo!!.position) ?: return
        inflater.inflate(R.menu.action_user_list_member, menu)
        menu.setHeaderTitle(userColorNameManager.getDisplayName(user, preferences[nameFirstKey]))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val userList = userList ?: return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val user = adapter.getUser(contextMenuInfo.position) ?: return false
        when (item.itemId) {
            R.id.delete_from_list -> {
                parentFragmentManager.let { DeleteUserListMembersDialogFragment.show(it, userList, user) }
                return true
            }
        }
        return false
    }

    @Subscribe
    fun onUserListMembersChanged(event: UserListMembersChangedEvent) {
        val userList = event.userList
        val arguments = arguments ?: return
        val accountKey = arguments.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY) ?: return
        val listId = arguments.getString(EXTRA_LIST_ID)
        val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = arguments.getString(EXTRA_SCREEN_NAME)
        val listName = arguments.getString(EXTRA_LIST_NAME)
        if (!ParcelableUserListUtils.check(userList, accountKey, listId, userKey, screenName, listName)) {
            return
        }
        when (event.action) {
            UserListMembersChangedEvent.Action.ADDED -> {
                val newUsers = Arrays.asList(*event.users)
                val users = adapter.getData() ?: return
                if (users is MutableList) {
                    users.removeAll(newUsers)
                    users.addAll(0, newUsers)
                }
                users.forEachIndexed { idx, user -> user.position = idx.toLong() }
                adapter.notifyDataSetChanged()
            }
            UserListMembersChangedEvent.Action.REMOVED -> {
                val removedUsers = Arrays.asList(*event.users)
                val users = adapter.getData() ?: return
                if (users is MutableList) {
                    users.removeAll(removedUsers)
                }
                users.forEachIndexed { idx, user -> user.position = idx.toLong() }
                adapter.notifyDataSetChanged()
            }
        }
    }
}
