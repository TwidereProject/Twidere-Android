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
import android.util.Log;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserCursorIndices;
import org.mariotaku.twidere.model.ParcelableUserValuesCreator;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;

import static org.mariotaku.twidere.util.ContentValuesCreator.createCachedUser;
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
        int accountColor = DataStoreUtils.getAccountColor(context, mAccountId);
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableUser user = mExtras.getParcelable(EXTRA_USER);
            if (user != null) {
                final ContentValues values = ParcelableUserValuesCreator.create(user);
                resolver.insert(CachedUsers.CONTENT_URI, values);
                user.account_color = accountColor;
                return SingleResponse.getInstance(user);
            }
        }
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, mAccountId, true);
        if (twitter == null) return SingleResponse.getInstance();
        if (mLoadFromCache) {
            final Expression where;
            final String[] whereArgs;
            if (mUserId > 0) {
                where = Expression.equals(CachedUsers.USER_ID, mUserId);
                whereArgs = null;
            } else {
                where = Expression.equalsArgs(CachedUsers.SCREEN_NAME);
                whereArgs = new String[]{mScreenName};
            }
            final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS,
                    where.getSQL(), whereArgs, null);
            if (cur != null) {
                try {
                    if (cur.moveToFirst()) {
                        final ParcelableUserCursorIndices indices = new ParcelableUserCursorIndices(cur);
                        final ParcelableUser user = indices.newObject(cur);
                        user.account_id = mAccountId;
                        user.account_color = accountColor;
                        return SingleResponse.getInstance(user);
                    }
                } finally {
                    cur.close();
                }
            }
        }
        try {
            final User twitterUser = TwitterWrapper.tryShowUser(twitter, mUserId, mScreenName);
            final ContentValues cachedUserValues = createCachedUser(twitterUser);
            final long userId = twitterUser.getId();
            resolver.insert(CachedUsers.CONTENT_URI, cachedUserValues);
            final ParcelableUser user = ParcelableUserUtils.fromUser(twitterUser, mAccountId);
            if (isMyAccount(context, userId)) {
                final ContentValues accountValues = new ContentValues();
                accountValues.put(Accounts.NAME, user.name);
                accountValues.put(Accounts.SCREEN_NAME, user.screen_name);
                accountValues.put(Accounts.PROFILE_IMAGE_URL, user.profile_image_url);
                accountValues.put(Accounts.PROFILE_BANNER_URL, user.profile_banner_url);
                accountValues.put(Accounts.ACCOUNT_USER, JsonSerializer.serialize(user,
                        ParcelableUser.class));
                accountValues.put(Accounts.ACCOUNT_HOST, ParcelableUserUtils.getUserHost(user));
                final String accountWhere = Expression.equals(Accounts.ACCOUNT_ID, userId).getSQL();
                resolver.update(Accounts.CONTENT_URI, accountValues, accountWhere, null);
            }
            user.account_color = accountColor;
            return SingleResponse.getInstance(user);
        } catch (final TwitterException e) {
            Log.w(LOGTAG, e);
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
