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
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.util.TwitterWrapper;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.mariotaku.twidere.util.ContentValuesCreator.createCachedUser;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyAccount;

public final class ParcelableUserLoader extends AsyncTaskLoader<SingleResponse<ParcelableUser>> implements Constants {

    private final boolean mOmitIntentExtra, mLoadFromCache;
    private final Bundle mExtras;
    private final long mAccountId, mUserId;
    private final String mScreenName;

    public ParcelableUserLoader(final Context context, final long accountId, final long userId,
                                final String screenName, final Bundle extras, final boolean omitIntentExtra,
                                final boolean loadFromCache) {
        super(context);
        this.mOmitIntentExtra = omitIntentExtra;
        this.mLoadFromCache = loadFromCache;
        this.mExtras = extras;
        this.mAccountId = accountId;
        this.mUserId = userId;
        this.mScreenName = screenName;
    }

    @Override
    public SingleResponse<ParcelableUser> loadInBackground() {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableUser user = mExtras.getParcelable(EXTRA_USER);
            if (user != null) {
                final ContentValues values = ParcelableUser.makeCachedUserContentValues(user);
                resolver.delete(CachedUsers.CONTENT_URI, CachedUsers.USER_ID + " = " + user.id, null);
                resolver.insert(CachedUsers.CONTENT_URI, values);
                return SingleResponse.getInstance(user);
            }
        }
        final Twitter twitter = getTwitterInstance(context, mAccountId, true);
        if (twitter == null) return SingleResponse.getInstance();
        if (mLoadFromCache) {
            final String where = CachedUsers.USER_ID + " = " + mUserId + " OR " + CachedUsers.SCREEN_NAME + " = '"
                    + mScreenName + "'";
            final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where, null, null);
            final int count = cur.getCount();
            try {
                if (count > 0) {
                    cur.moveToFirst();
                    return SingleResponse.getInstance(new ParcelableUser(cur, mAccountId));
                }
            } finally {
                cur.close();
            }
        }
        try {
            final User user = TwitterWrapper.tryShowUser(twitter, mUserId, mScreenName);
            final ContentValues cachedUserValues = createCachedUser(user);
            final long userId = user.getId();
            final String cachedUserWhere = Expression.equals(CachedUsers.USER_ID, userId).getSQL();
            resolver.delete(CachedUsers.CONTENT_URI, cachedUserWhere, null);
            resolver.insert(CachedUsers.CONTENT_URI, cachedUserValues);
            final ParcelableUser result = new ParcelableUser(user, mAccountId);
            if (isMyAccount(context, userId)) {
                final ContentValues accountValues = new ContentValues();
                accountValues.put(Accounts.NAME, result.name);
                accountValues.put(Accounts.SCREEN_NAME, result.screen_name);
                accountValues.put(Accounts.PROFILE_IMAGE_URL, result.profile_image_url);
                accountValues.put(Accounts.PROFILE_BANNER_URL, result.profile_banner_url);
                final String accountWhere = Expression.equals(Accounts.ACCOUNT_ID, userId).getSQL();
                resolver.update(Accounts.CONTENT_URI, accountValues, accountWhere, null);
            }
            return SingleResponse.getInstance(result);
        } catch (final TwitterException e) {
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
