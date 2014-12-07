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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.querybuilder.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.util.TwitterWrapper;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.mariotaku.twidere.util.ContentValuesCreator.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

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
                return SingleResponse.getInstance(user);
            }
        }
        final Twitter twitter = getTwitterInstance(getContext(), account_id, true);
        if (twitter == null) return SingleResponse.getInstance();
        if (load_from_cache) {
            final String where = CachedUsers.USER_ID + " = " + user_id + " OR " + CachedUsers.SCREEN_NAME + " = '"
                    + screen_name + "'";
            final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where, null, null);
            final int count = cur.getCount();
            try {
                if (count > 0) {
                    cur.moveToFirst();
                    return new SingleResponse<>(new ParcelableUser(cur, account_id), null);
                }
            } finally {
                cur.close();
            }
        }
        try {
            final User user = TwitterWrapper.tryShowUser(twitter, user_id, screen_name);
            if (user == null) return SingleResponse.getInstance();
            final ContentValues values = makeCachedUserContentValues(user);
            final String where = Expression.equals(CachedUsers.USER_ID, user.getId()).getSQL();
            resolver.delete(CachedUsers.CONTENT_URI, where, null);
            resolver.insert(CachedUsers.CONTENT_URI, values);
            return SingleResponse.getInstance(new ParcelableUser(user, account_id));
        } catch (final TwitterException e) {
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
