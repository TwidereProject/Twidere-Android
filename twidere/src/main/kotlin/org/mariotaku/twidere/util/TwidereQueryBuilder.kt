/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util


import android.net.Uri
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.SQLFunctions
import org.mariotaku.sqliteqb.library.Table
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_NOTIFY_URI
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations

object TwidereQueryBuilder {

    fun rawQuery(rawQuery: String, notifyUri: Uri? = null): Uri {
        val builder = TwidereDataStore.CONTENT_URI_RAW_QUERY.buildUpon().appendPath(rawQuery)
        if (notifyUri != null) {
            builder.appendQueryParameter(QUERY_PARAM_NOTIFY_URI, notifyUri.toString())
        }
        return builder.build()
    }


    fun mapConversationsProjection(projection: String): Column = when (projection) {
        Conversations.UNREAD_COUNT -> Column(SQLFunctions.COUNT(
                "CASE WHEN ${Messages.TABLE_NAME}.${Messages.LOCAL_TIMESTAMP} > ${Conversations.TABLE_NAME}.${Conversations.LAST_READ_TIMESTAMP} THEN 1 ELSE NULL END"
        ), projection)
        else -> Column(Table(Conversations.TABLE_NAME), projection, projection)
    }

}
