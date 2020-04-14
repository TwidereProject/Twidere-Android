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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.cursoradapter.widget.SimpleCursorAdapter
import android.view.View
import android.widget.TextView

import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.provider.TwidereDataStore.CachedStatuses
import org.mariotaku.twidere.util.HtmlEscapeHelper

class SourceAutoCompleteAdapter(context: Context) : SimpleCursorAdapter(context,
        android.R.layout.simple_list_item_1, null, emptyArray<String>(), IntArray(0), 0) {

    private val database: SQLiteDatabase

    private var sourceIdx: Int = 0

    init {
        val app = TwidereApplication.getInstance(context)
        database = app.sqLiteDatabase
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        if (isCursorClosed) return
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        text1.text = convertToString(cursor)
        super.bindView(view, context, cursor)
    }

    override fun convertToString(cursor: Cursor?): CharSequence? {
        if (isCursorClosed || sourceIdx == -1) return null
        return HtmlEscapeHelper.toPlainText(cursor!!.getString(sourceIdx))
    }

    val isCursorClosed: Boolean
        get() {
            val cursor = cursor
            return cursor == null || cursor.isClosed
        }

    override fun runQueryOnBackgroundThread(constraint: CharSequence?): Cursor {
        val constraintEscaped = constraint?.toString()?.replace("_".toRegex(), "^_")
        val selection: String?
        val selectionArgs: Array<String>?
        if (constraintEscaped != null) {
            selection = "${CachedStatuses.SOURCE} LIKE '%\">'||?||'%</a>' ESCAPE '^'"
            selectionArgs = arrayOf(constraintEscaped)
        } else {
            selection = null
            selectionArgs = null
        }
        return database.query(true, CachedStatuses.TABLE_NAME, COLUMNS, selection, selectionArgs,
                CachedStatuses.SOURCE, null, null, null)
    }

    override fun swapCursor(cursor: Cursor?): Cursor? {
        if (cursor != null) {
            sourceIdx = cursor.getColumnIndex(CachedStatuses.SOURCE)
        }
        return super.swapCursor(cursor)
    }

    companion object {

        private val COLUMNS = arrayOf(CachedStatuses._ID, CachedStatuses.SOURCE)
    }

}
