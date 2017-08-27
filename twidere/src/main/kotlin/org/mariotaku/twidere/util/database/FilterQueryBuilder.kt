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

package org.mariotaku.twidere.util.database

import android.content.ContentResolver
import org.mariotaku.twidere.extension.rawQuery
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import java.util.*

/**
 * Created by mariotaku on 2017/2/16.
 */

object FilterQueryBuilder {

    fun isFiltered(cr: ContentResolver, activity: ParcelableStatus): Boolean {
        return isFiltered(cr, activity.user_key, activity.text_plain,
                activity.quoted_text_plain, activity.spans, activity.quoted_spans,
                activity.source, activity.quoted_source, activity.retweeted_by_user_key,
                activity.quoted_user_key)
    }

    fun isFiltered(cr: ContentResolver, userKey: UserKey?, textPlain: String?, quotedTextPlain: String?,
            spans: Array<SpanItem>?, quotedSpans: Array<SpanItem>?, source: String?, quotedSource: String?,
            retweetedByKey: UserKey?, quotedUserKey: UserKey?): Boolean {
        val query = FilterQueryBuilder.isFilteredQuery(userKey,
                textPlain, quotedTextPlain, spans, quotedSpans, source, quotedSource, retweetedByKey,
                quotedUserKey, true)
        val cur = cr.rawQuery(query.first, query.second) ?: return false
        @Suppress("ConvertTryFinallyToUseCall")
        try {
            return cur.moveToFirst() && cur.getInt(0) != 0
        } finally {
            cur.close()
        }
    }

    fun isFilteredQuery(userKey: UserKey?, textPlain: String?, quotedTextPlain: String?,
            spans: Array<SpanItem>?, quotedSpans: Array<SpanItem>?, source: String?, quotedSource: String?,
            retweetedByKey: UserKey?, quotedUserKey: UserKey?, filterRts: Boolean): Pair<String, Array<String>> {
        val builder = StringBuilder()
        val selectionArgs = ArrayList<String>()
        builder.append("SELECT ")
        if (textPlain != null) {
            selectionArgs.add(textPlain)
            addTextPlainStatement(builder)
        }
        if (quotedTextPlain != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            selectionArgs.add(quotedTextPlain)
            addTextPlainStatement(builder)
        }
        if (spans != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            addSpansStatement(spans, builder, selectionArgs)
        }
        if (quotedSpans != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            addSpansStatement(quotedSpans, builder, selectionArgs)
        }
        if (userKey != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            selectionArgs.add(userKey.toString())
            createUserKeyStatement(builder)
        }
        if (retweetedByKey != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            selectionArgs.add(retweetedByKey.toString())
            createUserKeyStatement(builder)
        }
        if (quotedUserKey != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            selectionArgs.add(quotedUserKey.toString())
            createUserKeyStatement(builder)
        }
        if (source != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            selectionArgs.add(source)
            appendSourceStatement(builder)
        }
        if (quotedSource != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ")
            }
            selectionArgs.add(quotedSource)
            appendSourceStatement(builder)
        }
        return Pair(builder.toString(), selectionArgs.toTypedArray())
    }


    private fun createUserKeyStatement(builder: StringBuilder) {
        builder.append("(SELECT ")
                .append("?")
                .append(" IN (SELECT ")
                .append(Filters.Users.USER_KEY)
                .append(" FROM ")
                .append(Filters.Users.TABLE_NAME)
                .append("))")
    }

    private fun appendSourceStatement(builder: StringBuilder) {
        builder.append("(SELECT 1 IN (SELECT ? LIKE '%>'||")
                .append(Filters.Sources.TABLE_NAME).append(".")
                .append(Filters.VALUE).append("||'</a>%' FROM ")
                .append(Filters.Sources.TABLE_NAME)
                .append("))")
    }

    private fun addTextPlainStatement(builder: StringBuilder) {
        builder.append("(SELECT 1 IN (SELECT ? LIKE '%'||")
                .append(Filters.Keywords.TABLE_NAME)
                .append(".")
                .append(Filters.VALUE)
                .append("||'%' FROM ")
                .append(Filters.Keywords.TABLE_NAME)
                .append("))")
    }

    private fun addSpansStatement(spans: Array<SpanItem>, builder: StringBuilder, selectionArgs: MutableList<String>) {
        val spansFlat = StringBuilder()
        for (span in spans) {
            spansFlat.append(span.link)
            spansFlat.append(' ')
        }
        selectionArgs.add(spansFlat.toString())
        builder.append("(SELECT 1 IN (SELECT ? LIKE '%'||")
                .append(Filters.Links.TABLE_NAME)
                .append(".")
                .append(Filters.VALUE)
                .append("||'%' FROM ")
                .append(Filters.Links.TABLE_NAME)
                .append("))")
    }

}
