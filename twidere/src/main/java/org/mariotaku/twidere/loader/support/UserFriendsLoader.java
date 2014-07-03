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

import org.mariotaku.twidere.model.ParcelableUser;

import twitter4j.CursorPaging;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.List;

public class UserFriendsLoader extends CursorSupportUsersLoader {

	private final long mUserId;
	private final String mScreenName;

	public UserFriendsLoader(final Context context, final long account_id, final long user_id,
			final String screen_name, final long max_id, final List<ParcelableUser> users_list) {
		super(context, account_id, max_id, users_list);
		mUserId = user_id;
		mScreenName = screen_name;
	}

	@Override
	protected PagableResponseList<User> getCursoredUsers(final Twitter twitter, final CursorPaging paging)
			throws TwitterException {
		if (twitter == null) return null;
		if (mUserId > 0)
			return twitter.getFriendsList(mUserId, paging);
		else if (mScreenName != null) return twitter.getFriendsList(mScreenName, paging);
		return null;
	}

}
