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
import android.view.MenuItem;

import org.mariotaku.twidere.loader.support.IDsUsersLoader;
import org.mariotaku.twidere.loader.support.IncomingFriendshipsLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

public class IncomingFriendshipsFragment extends CursorSupportUsersListFragment {

	@Override
	public IDsUsersLoader newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
		return new IncomingFriendshipsLoader(context, accountId, getNextCursor(), getData());
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ACCEPT: {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				final ParcelableUser user = getSelectedUser();
				if (twitter == null || user == null) return false;
				twitter.acceptFriendshipAsync(user.account_id, user.id);
				break;
			}
			case MENU_DENY: {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				final ParcelableUser user = getSelectedUser();
				if (twitter == null || user == null) return false;
				twitter.denyFriendshipAsync(user.account_id, user.id);
				break;
			}
		}
		return super.onMenuItemClick(item);
	}

	@Override
	protected UserMenuDialogFragment createMenuDialog() {
		return new IncomingFriendshipsMenuDialogFragment();
	}

}
