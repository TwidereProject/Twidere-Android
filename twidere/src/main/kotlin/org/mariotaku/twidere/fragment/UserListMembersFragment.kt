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
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.loader.UserListMembersLoader
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.UserListMembersChangedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.UserViewHolder
import java.util.*

class UserListMembersFragment : CursorSupportUsersListFragment() {

    public override fun onCreateUsersLoader(context: Context,
                                            args: Bundle, fromUser: Boolean): CursorSupportUsersLoader {
        val accountId = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val listId = args.getString(EXTRA_LIST_ID)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val listName = args.getString(EXTRA_LIST_NAME)
        val loader = UserListMembersLoader(context, accountId, listId,
                userKey, screenName, listName, adapter!!.getData(), fromUser)
        loader.cursor = nextCursor
        loader.page = nextPage
        return loader
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerForContextMenu(recyclerView)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putParcelable(EXTRA_USER_LIST, userList)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onUserLongClick(holder: UserViewHolder, position: Int): Boolean {
        return recyclerView.showContextMenuForChild(holder.itemView)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val userList = userList ?: return
        val args = arguments
        val accountId = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        if (accountId == null || accountId != userKey) return
        val adapter = adapter
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        inflater.inflate(R.menu.action_user_list_member, menu)
        val user = adapter!!.getUser(contextMenuInfo!!.position)
        menu.setHeaderTitle(userColorNameManager.getDisplayName(user, preferences.getBoolean(KEY_NAME_FIRST)))
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (!userVisibleHint) return false
        val userList = userList ?: return false
        val contextMenuInfo = item!!.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val user = adapter!!.getUser(contextMenuInfo.position) ?: return false
        when (item.itemId) {
            R.id.delete_from_list -> {
                DeleteUserListMembersDialogFragment.show(fragmentManager, userList, user)
                return true
            }
        }
        return false
    }

    val userList: ParcelableUserList?
        get() {
            val parent = parentFragment
            if (parent is UserListFragment) {
                return parent.userList
            }
            return null
        }

    @Subscribe
    fun onUserListMembersChanged(event: UserListMembersChangedEvent) {
        val userList = event.userList
        val args = arguments
        val accountId = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val listId = args.getString(EXTRA_LIST_ID)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val listName = args.getString(EXTRA_LIST_NAME)
        if (!ParcelableUserListUtils.check(userList, accountId, listId, userKey, screenName, listName)) {
            return
        }
        when (event.action) {
            UserListMembersChangedEvent.Action.ADDED -> {
                val adapter = adapter
                val newUsers = Arrays.asList(*event.users)
                val users = adapter!!.getData() ?: return
                if (users is MutableList) {
                    users.removeAll(newUsers)
                    users.addAll(0, newUsers)
                }
                users.forEachIndexed { idx, user -> user.position = idx.toLong() }
                adapter.notifyDataSetChanged()
            }
            UserListMembersChangedEvent.Action.REMOVED -> {
                val adapter = adapter
                val removedUsers = Arrays.asList(*event.users)
                val users = adapter!!.getData() ?: return
                if (users is MutableList) {
                    users.removeAll(removedUsers)
                }
                users.forEachIndexed { idx, user -> user.position = idx.toLong() }
                adapter.notifyDataSetChanged()
            }
        }
    }
}
