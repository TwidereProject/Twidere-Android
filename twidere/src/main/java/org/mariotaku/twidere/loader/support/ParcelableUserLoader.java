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

import static org.mariotaku.twidere.util.ContentValuesCreator.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.io.IOException;

public final class ParcelableUserLoader extends AsyncTaskLoader<SingleResponse<ParcelableUser>> implements Constants {

	private final ContentResolver resolver;
	private final boolean omit_intent_extra, load_from_cache;
	private final Bundle extras;
	private final long account_id, user_id;
	private final String screen_name;

	public ParcelableUserLoader(final Context context, final long account_id, final long user_id,
			final String screen_name, final Bundle extras, final boolean omit_intent_extra,
			final boolean load_from_cache) {
		super(context);
		resolver = context.getContentResolver();
		this.omit_intent_extra = omit_intent_extra;
		this.load_from_cache = load_from_cache;
		this.extras = extras;
		this.account_id = account_id;
		this.user_id = user_id;
		this.screen_name = screen_name;
	}

	@Override
	public SingleResponse<ParcelableUser> loadInBackground() {
		if (!omit_intent_extra && extras != null) {
			final ParcelableUser user = extras.getParcelable(EXTRA_USER);
			if (user != null) {
				final ContentValues values = ParcelableUser.makeCachedUserContentValues(user);
				resolver.delete(CachedUsers.CONTENT_URI, CachedUsers.USER_ID + " = " + user.id, null);
				resolver.insert(CachedUsers.CONTENT_URI, values);
				return new SingleResponse<ParcelableUser>(user, null);
			}
		}
		final Twitter twitter = getTwitterInstance(getContext(), account_id, true);
		if (twitter == null) return new SingleResponse<ParcelableUser>(null, null);
		if (load_from_cache) {
			final String where = CachedUsers.USER_ID + " = " + user_id + " OR " + CachedUsers.SCREEN_NAME + " = '"
					+ screen_name + "'";
			final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where, null, null);
			final int count = cur.getCount();
			try {
				if (count > 0) {
					cur.moveToFirst();
					return new SingleResponse<ParcelableUser>(new ParcelableUser(cur, account_id), null);
				}
			} finally {
				cur.close();
			}
		}
		try {
			final User user = tryShowUser(twitter, user_id, screen_name);
			if (user == null) return new SingleResponse<ParcelableUser>(null, null);
			final ContentValues values = makeCachedUserContentValues(user);
			final String where = CachedUsers.USER_ID + " = " + user.getId() + " OR " + CachedUsers.SCREEN_NAME + " = ?";
			resolver.delete(CachedUsers.CONTENT_URI, where, new String[] { user.getScreenName() });
			resolver.insert(CachedUsers.CONTENT_URI, values);
			return new SingleResponse<ParcelableUser>(new ParcelableUser(user, account_id), null);
		} catch (final TwitterException e) {
			return new SingleResponse<ParcelableUser>(null, e);
		}
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	private static User showUser(final Twitter twitter, final long id, final String screenName) throws TwitterException {
		if (id != -1)
			return twitter.showUser(id);
		else if (screenName != null) return twitter.showUser(screenName);
		return null;
	}

	private static User showUserAlternative(final Twitter twitter, final long id, final String screenName)
			throws TwitterException {
		final String searchScreenName;
		if (screenName != null) {
			searchScreenName = screenName;
		} else if (id != -1) {
			searchScreenName = twitter.showFriendship(twitter.getId(), id).getTargetUserScreenName();
		} else
			return null;
		for (final User user : twitter.searchUsers(searchScreenName, 1)) {
			if (user.getId() == id || searchScreenName.equals(user.getScreenName())) return user;
		}
		return null;
	}

	private static User tryShowUser(final Twitter twitter, final long id, final String screenName)
			throws TwitterException {
		try {
			final User user = showUser(twitter, id, screenName);
			if (user != null) return user;
		} catch (final TwitterException e) {
			if (!(e.getCause() instanceof IOException))
				return showUserAlternative(twitter, id, screenName);
			else
				throw e;
		}
		return null;
	}

}
