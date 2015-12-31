/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.OrderBy;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.view.ProfileImageView;

import javax.inject.Inject;


public class UserAutoCompleteAdapter extends SimpleCursorAdapter implements Constants {

    private static final String[] FROM = new String[0];
    private static final int[] TO = new int[0];

    @Inject
    MediaLoaderWrapper mProfileImageLoader;
    @Inject
    SharedPreferencesWrapper mPreferences;
    @Inject
    UserColorNameManager mUserColorNameManager;

    private final boolean mDisplayProfileImage;

    private int mIdIdx, mNameIdx, mScreenNameIdx, mProfileImageIdx;
    private long mAccountId;
    private char mToken;

    public UserAutoCompleteAdapter(final Context context) {
        super(context, R.layout.list_item_auto_complete, null, FROM, TO, 0);
        GeneralComponentHelper.build(context).inject(this);
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        final ProfileImageView icon = (ProfileImageView) view.findViewById(android.R.id.icon);

        // Clear images in order to prevent images in recycled view shown.
        icon.setImageDrawable(null);

        text1.setText(mUserColorNameManager.getUserNickname(cursor.getLong(mIdIdx), cursor.getString(mNameIdx)));
        text2.setText('@' + cursor.getString(mScreenNameIdx));
        if (mDisplayProfileImage) {
            final String profileImageUrl = cursor.getString(mProfileImageIdx);
            mProfileImageLoader.displayProfileImage(icon, profileImageUrl);
        } else {
            mProfileImageLoader.cancelDisplayTask(icon);
        }

        icon.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
        super.bindView(view, context, cursor);
    }

    public void closeCursor() {
        final Cursor cursor = swapCursor(null);
        if (cursor == null) return;
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public CharSequence convertToString(final Cursor cursor) {
        return cursor.getString(mScreenNameIdx);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) return null;
        final FilterQueryProvider filter = getFilterQueryProvider();
        if (filter != null) return filter.runQuery(constraint);
        final String query = constraint.toString();
        final String queryEscaped = query.replace("_", "^_");
        final long[] nicknameIds = Utils.getMatchedNicknameIds(query, mUserColorNameManager);
        final Expression usersSelection = Expression.or(
                Expression.likeRaw(new Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                Expression.likeRaw(new Columns.Column(CachedUsers.NAME), "?||'%'", "^"),
                Expression.in(new Columns.Column(CachedUsers.USER_ID), new RawItemArray(nicknameIds)));
        final String[] selectionArgs = new String[]{queryEscaped, queryEscaped};
        final String[] order = {CachedUsers.LAST_SEEN, CachedUsers.SCORE, CachedUsers.SCREEN_NAME,
                CachedUsers.NAME};
        final boolean[] ascending = {false, false, true, true};
        final OrderBy orderBy = new OrderBy(order, ascending);
        final Uri uri = Uri.withAppendedPath(CachedUsers.CONTENT_URI_WITH_SCORE, String.valueOf(mAccountId));
        return mContext.getContentResolver().query(uri, CachedUsers.COLUMNS, usersSelection.getSQL(),
                selectionArgs, orderBy.getSQL());
    }


    public void setAccountId(long accountId) {
        mAccountId = accountId;
    }

    @Override
    public Cursor swapCursor(final Cursor cursor) {
        if (cursor != null) {
            mIdIdx = cursor.getColumnIndex(CachedUsers.USER_ID);
            mNameIdx = cursor.getColumnIndex(CachedUsers.NAME);
            mScreenNameIdx = cursor.getColumnIndex(CachedUsers.SCREEN_NAME);
            mProfileImageIdx = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL);
        }
        return super.swapCursor(cursor);
    }


}
