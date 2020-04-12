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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.fragment.ParcelableUsersFragment
import org.mariotaku.twidere.loader.users.AbsRequestUsersLoader
import org.mariotaku.twidere.loader.users.IncomingFriendshipsLoader
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.view.holder.UserViewHolder

class IncomingFriendshipsFragment : ParcelableUsersFragment(), IUsersAdapter.RequestClickListener {
    override val showFollow: Boolean = false

    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            AbsRequestUsersLoader {
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        return IncomingFriendshipsLoader(context, accountKey, adapter.getData(), fromUser)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableUsersAdapter {
        val adapter = super.onCreateAdapter(context, requestManager)
        val accountKey = arguments?.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY) ?: return adapter
        if (USER_TYPE_FANFOU_COM == accountKey.host) {
            adapter.requestClickListener = this
        } else if (AccountUtils.isOfficial(context, accountKey)) {
            adapter.requestClickListener = this
        }
        return adapter
    }

    override fun onAcceptClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        twitterWrapper.acceptFriendshipAsync(accountKey, user.key)
    }

    override fun onDenyClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        twitterWrapper.denyFriendshipAsync(accountKey, user.key)
    }

    @SuppressLint("SwitchIntDef")
    override fun shouldRemoveUser(position: Int, event: FriendshipTaskEvent): Boolean {
        if (!event.isSucceeded) return false
        when (event.action) {
            FriendshipTaskEvent.Action.BLOCK, FriendshipTaskEvent.Action.ACCEPT, FriendshipTaskEvent.Action.DENY -> {
                return true
            }
        }
        return false
    }
}
