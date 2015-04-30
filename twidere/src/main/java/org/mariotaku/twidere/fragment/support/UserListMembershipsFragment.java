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
import android.support.v4.content.Loader;

import org.mariotaku.twidere.loader.support.UserListMembershipsLoader;
import org.mariotaku.twidere.model.ParcelableUserList;

import java.util.List;

public class UserListMembershipsFragment extends ParcelableUserListsFragment {

    @Override
    public Loader<List<ParcelableUserList>> onCreateUserListsLoader(final Context context,
                                                                    final Bundle args, final boolean fromUser) {
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final long cursor = args.getLong(EXTRA_NEXT_CURSOR, -1);
        return new UserListMembershipsLoader(getActivity(), accountId, userId, screenName, cursor, getData());
    }

}
