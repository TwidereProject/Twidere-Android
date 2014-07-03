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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.util.HtmlEscapeHelper;

public class SourceAutoCompleteAdapter extends SimpleCursorAdapter implements Constants {

	private static final String[] COLUMNS = new String[] { CachedStatuses._ID, CachedStatuses.SOURCE };
	private static final String[] FROM = new String[0];
	private static final int[] TO = new int[0];

	private final SQLiteDatabase mDatabase;

	private int mSourceIdx;

	public SourceAutoCompleteAdapter(final Context context) {
		super(context, android.R.layout.simple_list_item_1, null, FROM, TO, 0);
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mDatabase = app != null ? app.getSQLiteDatabase() : null;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		if (isCursorClosed()) return;
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		text1.setText(convertToString(cursor));
		super.bindView(view, context, cursor);
	}

	@Override
	public CharSequence convertToString(final Cursor cursor) {
		if (isCursorClosed() || mSourceIdx == -1) return null;
		return HtmlEscapeHelper.toPlainText(cursor.getString(mSourceIdx));
	}

	public boolean isCursorClosed() {
		final Cursor cursor = getCursor();
		return cursor == null || cursor.isClosed();
	}

	@Override
	public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
		final String constraint_escaped = constraint != null ? constraint.toString().replaceAll("_", "^_") : null;
		final String selection = constraint != null ? CachedStatuses.SOURCE + " LIKE '%\">'||?||'%</a>' ESCAPE '^'"
				: null;
		final String[] selectionArgs = constraint != null ? new String[] { constraint_escaped } : null;
		return mDatabase.query(true, CachedStatuses.TABLE_NAME, COLUMNS, selection, selectionArgs,
				CachedStatuses.SOURCE, null, null, null);
	}

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mSourceIdx = cursor.getColumnIndex(CachedStatuses.SOURCE);
		}
		return super.swapCursor(cursor);
	}

}
