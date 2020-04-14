/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.database.Cursor
import androidx.cursoradapter.widget.SimpleCursorAdapter

import org.mariotaku.twidere.provider.TwidereDataStore

/**
 * Created by mariotaku on 15/4/29.
 */
class TrendsAdapter(context: Context) : SimpleCursorAdapter(context,
        android.R.layout.simple_list_item_1, null, arrayOf(TwidereDataStore.CachedTrends.NAME),
        intArrayOf(android.R.id.text1), 0) {
    private var nameIdx: Int = 0

    override fun getItem(position: Int): String? {
        val c = cursor
        if (c != null && !c.isClosed && c.moveToPosition(position))
            return c.getString(nameIdx)
        return null
    }

    override fun swapCursor(c: Cursor?): Cursor? {
        if (c != null) {
            nameIdx = c.getColumnIndex(TwidereDataStore.CachedTrends.NAME)
        }
        return super.swapCursor(c)
    }

}
