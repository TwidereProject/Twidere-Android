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

package org.mariotaku.twidere.loader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.mariotaku.abstask.library.TaskStarter;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.Constants;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.fragment.UserFragment;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserCursorIndices;
import org.mariotaku.twidere.model.ParcelableUserValuesCreator;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.task.UpdateAccountInfoTask;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

import static org.mariotaku.twidere.util.ContentValuesCreator.createCachedUser;

public final class ParcelableUserLoader extends AsyncTaskLoader<SingleResponse<ParcelableUser>> implements Constants {

    private final boolean mOmitIntentExtra, mLoadFromCache;
    private final Bundle mExtras;
    private final UserKey mAccountKey;
    private final UserKey mUserKey;
    private final String mScreenName;

    @Inject
    UserColorNameManager mUserColorNameManager;

    public ParcelableUserLoader(final Context context, final UserKey accountKey,
                                final UserKey userKey, final String screenName,
                                final Bundle extras, final boolean omitIntentExtra,
                                final boolean loadFromCache) {
        super(context);
        GeneralComponentHelper.build(context).inject(this);
        this.mOmitIntentExtra = omitIntentExtra;
        this.mLoadFromCache = loadFromCache;
        this.mExtras = extras;
        this.mAccountKey = accountKey;
        this.mUserKey = userKey;
        this.mScreenName = screenName;
    }

    @Override
    public SingleResponse<ParcelableUser> loadInBackground() {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final UserKey accountKey = mAccountKey;
        ParcelableCredentials credentials = null;
        for (ParcelableCredentials cred : ParcelableCredentialsUtils.getCredentialses(context)) {
            if (cred.account_key.equals(accountKey)) {
                credentials = cred;
                break;
            } else if (cred.account_user != null && cred.account_user.account_key.equals(accountKey)) {
                credentials = cred;
                break;
            }
        }
        if (credentials == null) return SingleResponse.getInstance();
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableUser user = mExtras.getParcelable(EXTRA_USER);
            if (user != null) {
                final ContentValues values = ParcelableUserValuesCreator.create(user);
                resolver.insert(CachedUsers.CONTENT_URI, values);
                ParcelableUserUtils.updateExtraInformation(user, credentials, mUserColorNameManager);
                return SingleResponse.getInstance(user);
            }
        }
        final MicroBlog twitter = MicroBlogAPIFactory.getTwitterInstance(context, credentials, true, true);
        if (twitter == null) return SingleResponse.getInstance();
        if (mLoadFromCache) {
            final Expression where;
            final String[] whereArgs;
            if (mUserKey != null) {
                where = Expression.equalsArgs(CachedUsers.USER_KEY);
                whereArgs = new String[]{mUserKey.toString()};
            } else if (mScreenName != null) {
                final String host = mAccountKey.getHost();
                if (host != null) {
                    where = Expression.and(
                            Expression.likeRaw(new Columns.Column(CachedUsers.USER_KEY), "'%@'||?"),
                            Expression.equalsArgs(CachedUsers.SCREEN_NAME)
                    );
                    whereArgs = new String[]{host, mScreenName};
                } else {
                    where = Expression.equalsArgs(CachedUsers.SCREEN_NAME);
                    whereArgs = new String[]{mScreenName};
                }
            } else {
                return SingleResponse.getInstance();
            }
            final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS,
                    where.getSQL(), whereArgs, null);
            if (cur != null) {
                try {
                    cur.moveToFirst();
                    final ParcelableUserCursorIndices indices = new ParcelableUserCursorIndices(cur);
                    while (!cur.isAfterLast()) {
                        final ParcelableUser user = indices.newObject(cur);
                        if (TextUtils.equals(UserKeyUtils.getUserHost(user), user.key.getHost())) {
                            user.account_key = accountKey;
                            user.account_color = credentials.color;
                            return SingleResponse.getInstance(user);
                        }
                        cur.moveToNext();
                    }
                } finally {
                    cur.close();
                }
            }
        }
        try {
            final User twitterUser;
            if (mExtras != null && UserFragment.Referral.SELF_PROFILE.equals(mExtras.getString(EXTRA_REFERRAL))) {
                twitterUser = twitter.verifyCredentials();
            } else {
                String profileUrl = null;
                if (mExtras != null) {
                    profileUrl = mExtras.getString(EXTRA_PROFILE_URL);
                }
                if (MicroBlogAPIFactory.isStatusNetCredentials(credentials) && mUserKey != null &&
                        profileUrl != null && !TextUtils.equals(credentials.account_key.getHost(),
                        mUserKey.getHost())) {
                    twitterUser = twitter.showExternalProfile(profileUrl);
                } else {
                    final String id = mUserKey != null ? mUserKey.getId() : null;
                    twitterUser = TwitterWrapper.tryShowUser(twitter, id, mScreenName,
                            credentials.account_type);
                }
            }
            final ContentValues cachedUserValues = createCachedUser(twitterUser);
            resolver.insert(CachedUsers.CONTENT_URI, cachedUserValues);
            final ParcelableUser user = ParcelableUserUtils.fromUser(twitterUser, accountKey);
            ParcelableUserUtils.updateExtraInformation(user, credentials, mUserColorNameManager);
            final SingleResponse<ParcelableUser> response = SingleResponse.getInstance(user);
            response.getExtras().putParcelable(EXTRA_ACCOUNT, credentials);
            return response;
        } catch (final MicroBlogException e) {
            Log.w(LOGTAG, e);
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableUser user = mExtras.getParcelable(EXTRA_USER);
            if (user != null) {
                deliverResult(SingleResponse.getInstance(user));
            }
        }
        forceLoad();
    }

    @Override
    public void deliverResult(SingleResponse<ParcelableUser> data) {
        super.deliverResult(data);
        if (data.hasData()) {
            final ParcelableUser user = data.getData();
            if (user.is_cache) return;
            final ParcelableAccount account = data.getExtras().getParcelable(EXTRA_ACCOUNT);
            if (account != null) {
                final UpdateAccountInfoTask task = new UpdateAccountInfoTask(getContext());
                task.setParams(Pair.create(account, user));
                TaskStarter.execute(task);
            }
        }
    }
}
