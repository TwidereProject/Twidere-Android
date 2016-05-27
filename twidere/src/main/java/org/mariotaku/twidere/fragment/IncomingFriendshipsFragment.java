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

package org.mariotaku.twidere.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter;
import org.mariotaku.twidere.loader.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.IncomingFriendshipsLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.UserViewHolder;

public class IncomingFriendshipsFragment extends CursorSupportUsersListFragment implements
        IUsersAdapter.RequestClickListener {

    @Override
    public CursorSupportUsersLoader onCreateUsersLoader(final Context context, @NonNull final Bundle args,
                                                        final boolean fromUser) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final IncomingFriendshipsLoader loader = new IncomingFriendshipsLoader(context, accountKey,
                getData(), fromUser);
        loader.setCursor(getNextCursor());
        loader.setPage(getNextPage());
        return loader;
    }

    @NonNull
    @Override
    protected ParcelableUsersAdapter onCreateAdapter(Context context) {
        final ParcelableUsersAdapter adapter = super.onCreateAdapter(context);
        final Bundle args = getArguments();
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        if (accountKey == null) {
            adapter.setRequestClickListener(null);
        } else if (USER_TYPE_FANFOU_COM.equals(accountKey.getHost()) ||
                Utils.isOfficialCredentials(context, accountKey)) {
            adapter.setRequestClickListener(this);
        } else {
            adapter.setRequestClickListener(null);
        }
        return adapter;
    }

    @Override
    public void onAcceptClicked(UserViewHolder holder, int position) {
        final ParcelableUsersAdapter adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null) return;
        mTwitterWrapper.acceptFriendshipAsync(user.account_key, user.key);
    }

    @Override
    public void onDenyClicked(UserViewHolder holder, int position) {
        final ParcelableUsersAdapter adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null) return;
        mTwitterWrapper.denyFriendshipAsync(user.account_key, user.key);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    protected boolean shouldRemoveUser(int position, FriendshipTaskEvent event) {
        if (!event.isSucceeded()) return false;
        switch (event.getAction()) {
            case FriendshipTaskEvent.Action.BLOCK:
            case FriendshipTaskEvent.Action.ACCEPT:
            case FriendshipTaskEvent.Action.DENY: {
                return true;
            }
        }
        return false;
    }
}
