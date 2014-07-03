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

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableUser;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.Collections;
import java.util.List;

public abstract class Twitter4JUsersLoader extends ParcelableUsersLoader {

	private final long mAccountId;

	private final Context mContext;

	public Twitter4JUsersLoader(final Context context, final long account_id, final List<ParcelableUser> data) {
		super(context, data);
		mContext = context;
		mAccountId = account_id;
	}

	@Override
	public List<ParcelableUser> loadInBackground() {
		final List<ParcelableUser> data = getData();
		final List<User> users;
		try {
			users = getUsers(getTwitterInstance(mContext, mAccountId, true));
			if (users == null) return data;
		} catch (final TwitterException e) {
			e.printStackTrace();
			return data;
		}
		int pos = data.size();
		for (final User user : users) {
			if (hasId(user.getId())) {
				continue;
			}
			data.add(new ParcelableUser(user, mAccountId, pos));
			pos++;
		}
		Collections.sort(data);
		return data;
	}

	protected abstract List<User> getUsers(Twitter twitter) throws TwitterException;
}
