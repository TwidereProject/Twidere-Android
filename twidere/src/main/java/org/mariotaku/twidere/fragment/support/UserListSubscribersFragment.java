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

import org.mariotaku.twidere.loader.support.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.support.UserListSubscribersLoader;

public class UserListSubscribersFragment extends CursorSupportUsersListFragment {

	@Override
	public CursorSupportUsersLoader onCreateUsersLoader(final Context context, final Bundle args, boolean fromUser) {
		if (args == null) return null;
		final long listId = args.getLong(EXTRA_LIST_ID, -1);
		final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final long userId = args.getLong(EXTRA_USER_ID, -1);
		final String screenName = args.getString(EXTRA_SCREEN_NAME);
		final String listName = args.getString(EXTRA_LIST_NAME);
		return new UserListSubscribersLoader(context, accountId, listId, userId, screenName, listName,
				getNextCursor(), getData(), fromUser);
	}

}
