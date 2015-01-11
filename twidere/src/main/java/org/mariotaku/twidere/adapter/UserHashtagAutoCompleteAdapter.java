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

package org.mariotaku.twidere.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedValues;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.util.UserColorNameUtils.getUserNickname;

public class UserHashtagAutoCompleteAdapter extends SimpleCursorAdapter implements Constants {

    private static final String[] FROM = new String[0];
    private static final int[] TO = new int[0];

    private final ContentResolver mResolver;
    private final SQLiteDatabase mDatabase;
    private final ImageLoaderWrapper mProfileImageLoader;
    private final SharedPreferences mPreferences, mUserNicknamePreferences;

    private final EditText mEditText;

    private final boolean mDisplayProfileImage, mNicknameOnly;

    private int mProfileImageUrlIdx, mNameIdx, mScreenNameIdx, mUserIdIdx;
    private char mToken = '@';

    public UserHashtagAutoCompleteAdapter(final Context context) {
        this(context, null);
    }

    public UserHashtagAutoCompleteAdapter(final Context context, final EditText view) {
        super(context, R.layout.list_item_two_line_small, null, FROM, TO, 0);
        mEditText = view;
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mUserNicknamePreferences = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mResolver = context.getContentResolver();
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mProfileImageLoader = app != null ? app.getImageLoaderWrapper() : null;
        mDatabase = app != null ? app.getSQLiteDatabase() : null;
        mDisplayProfileImage = mPreferences != null && mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mNicknameOnly = mPreferences != null && mPreferences.getBoolean(KEY_NICKNAME_ONLY, false);
    }

    public UserHashtagAutoCompleteAdapter(final EditText view) {
        this(view.getContext(), view);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        if (isCursorClosed()) return;
        final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);

        // Clear images in prder to prevent images in recycled view shown.
        icon.setImageDrawable(null);

        if (mScreenNameIdx != -1 && mNameIdx != -1 && mUserIdIdx != -1) {
            final String nick = getUserNickname(context, cursor.getLong(mUserIdIdx));
            final String name = cursor.getString(mNameIdx);
            if (TextUtils.isEmpty(nick)) {
                text1.setText(name);
            } else {
                text1.setText(mNicknameOnly ? nick : context.getString(R.string.name_with_nickname, name, nick));
            }
            text2.setText("@" + cursor.getString(mScreenNameIdx));
        } else {
            text1.setText("#" + cursor.getString(mNameIdx));
            text2.setText(R.string.hashtag);
        }
        icon.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
        if (mProfileImageUrlIdx != -1) {
            if (mDisplayProfileImage && mProfileImageLoader != null) {
                final String profile_image_url_string = cursor.getString(mProfileImageUrlIdx);
                mProfileImageLoader.displayProfileImage(icon, profile_image_url_string);
            } else {
                icon.setImageResource(R.drawable.ic_profile_image_default);
            }
        } else {
            icon.setImageResource(R.drawable.ic_action_hashtag);
        }
        super.bindView(view, context, cursor);
    }

    public void closeCursor() {
        final Cursor cursor = getCursor();
        if (cursor == null) return;
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    public CharSequence convertToString(final Cursor cursor) {
        if (isCursorClosed()) return null;
        return cursor.getString(mScreenNameIdx != -1 ? mScreenNameIdx : mNameIdx);
    }

    public boolean isCursorClosed() {
        final Cursor cursor = getCursor();
        return cursor == null || cursor.isClosed();
    }

    @Override
    public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
        char token = mToken;
        if (mEditText != null && constraint != null) {
            final CharSequence text = mEditText.getText();
            token = text.charAt(mEditText.getSelectionEnd() - constraint.length() - 1);
        }
        if (isAtSymbol(token) == isAtSymbol(mToken)) {
            final FilterQueryProvider filter = getFilterQueryProvider();
            if (filter != null) return filter.runQuery(constraint);
        }
        mToken = token;
        final String constraintEscaped = constraint != null ? constraint.toString().replaceAll("_", "^_") : null;
        if (isAtSymbol(token)) {
            final Expression selection;
            final String[] selectionArgs;
            if (constraintEscaped != null) {
                final long[] nicknameIds = Utils.getMatchedNicknameIds(ParseUtils.parseString(constraint), mUserNicknamePreferences);
                selection = Expression.or(Expression.likeRaw(new Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                        Expression.likeRaw(new Column(CachedUsers.NAME), "?||'%'", "^"),
                        Expression.in(new Column(CachedUsers.USER_ID), new RawItemArray(nicknameIds)));
                selectionArgs = new String[]{constraintEscaped, constraintEscaped};
            } else {
                selection = null;
                selectionArgs = null;
            }
            final OrderBy orderBy = new OrderBy(CachedUsers.SCREEN_NAME, CachedUsers.NAME);
            return mResolver.query(CachedUsers.CONTENT_URI, CachedUsers.BASIC_COLUMNS,
                    selection != null ? selection.getSQL() : null, selectionArgs, orderBy.getSQL());
        } else {
            final String selection = constraintEscaped != null ? CachedHashtags.NAME + " LIKE ?||'%' ESCAPE '^'"
                    : null;
            final String[] selectionArgs = constraintEscaped != null ? new String[]{constraintEscaped} : null;
            return mDatabase.query(true, CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, selection, selectionArgs,
                    null, null, CachedHashtags.NAME, null);
        }
    }

    @Override
    public Cursor swapCursor(final Cursor cursor) {
        if (cursor != null) {
            mNameIdx = cursor.getColumnIndex(CachedValues.NAME);
            mScreenNameIdx = cursor.getColumnIndex(CachedUsers.SCREEN_NAME);
            mUserIdIdx = cursor.getColumnIndex(CachedUsers.USER_ID);
            mProfileImageUrlIdx = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL);
        }
        return super.swapCursor(cursor);
    }


    private static boolean isAtSymbol(final char character) {
        switch (character) {
            case '\uff20':
            case '@':
                return true;
        }
        return false;
    }

}
