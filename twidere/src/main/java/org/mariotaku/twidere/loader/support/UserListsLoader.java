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

package org.mariotaku.twidere.loader.support;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableUserList;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import java.util.List;

public class UserListsLoader extends BaseUserListsLoader {

	public static final String LOGTAG = UserListsLoader.class.getSimpleName();

	private final long mUserId;
	private final String mScreenName;

	public UserListsLoader(final Context context, final long accountId, final long userId, final String screenName,
			final List<ParcelableUserList> data) {
		super(context, accountId, 0, data);
		mUserId = userId;
		mScreenName = screenName;
	}

	@Override
	public ResponseList<UserList> getUserLists(final Twitter twitter) throws TwitterException {
		if (twitter == null) return null;
		if (mUserId > 0)
			return twitter.getUserLists(mUserId);
		else if (mScreenName != null) return twitter.getUserLists(mScreenName);
		return null;
	}

	@Override
	protected boolean isFollowing(final UserList list) {
		return true;
	}
}
