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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.adapter.AbsUsersAdapter;
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter;
import org.mariotaku.twidere.loader.support.IDsUsersLoader;
import org.mariotaku.twidere.loader.support.IncomingFriendshipsLoader;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.message.FollowRequestTaskEvent;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.List;

public class IncomingFriendshipsFragment extends CursorSupportUsersListFragment implements IUsersAdapter.RequestClickListener {
    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public IDsUsersLoader onCreateUsersLoader(final Context context, @NonNull final Bundle args,
                                              final boolean fromUser) {
        final AccountKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final IncomingFriendshipsLoader loader = new IncomingFriendshipsLoader(context, accountKey,
                getData(), fromUser);
        loader.setCursor(getNextCursor());
        return loader;
    }

    @NonNull
    @Override
    protected ParcelableUsersAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableUsersAdapter adapter = super.onCreateAdapter(context, compact);
        adapter.setRequestClickListener(this);
        return adapter;
    }

    @Override
    public void onAcceptClicked(UserViewHolder holder, int position) {
        final AbsUsersAdapter<List<ParcelableUser>> adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null) return;
        mTwitterWrapper.acceptFriendshipAsync(new AccountKey(user.account_id, user.account_host),
                user.id);
    }

    @Override
    public void onDenyClicked(UserViewHolder holder, int position) {
        final AbsUsersAdapter<List<ParcelableUser>> adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null) return;
        mTwitterWrapper.denyFriendshipAsync(new AccountKey(user.account_id, user.account_host),
                user.id);
    }

    @Subscribe
    public void onFollowRequestTaskEvent(FollowRequestTaskEvent event) {
        final ParcelableUsersAdapter adapter = getAdapter();
        final int position = adapter.findPosition(event.getAccountKey(), event.getUserId());
        if (event.isFinished() && event.isSucceeded()) {
            adapter.removeUserAt(position);
        } else {
            adapter.notifyItemChanged(position);
        }
    }
}
