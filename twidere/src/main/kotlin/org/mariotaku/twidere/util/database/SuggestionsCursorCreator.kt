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

import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.text.TextUtils
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.SQLiteDatabaseWrapper
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import java.util.regex.Pattern

/**
 * Created by mariotaku on 2017/2/16.
 */

object SuggestionsCursorCreator {

    private val PATTERN_SCREEN_NAME = Pattern.compile("(?i)[@\uFF20]?([a-z0-9_]{1,20})")

    private val historyProjectionMap = mapOf(
            Suggestions._ID to Columns.Column(SearchHistory._ID, Suggestions._ID).sql,
            Suggestions.TYPE to Columns.Column("'${Suggestions.Search.TYPE_SEARCH_HISTORY}'", Suggestions.TYPE).sql,
            Suggestions.TITLE to Columns.Column(SearchHistory.QUERY, Suggestions.TITLE).sql,
            Suggestions.SUMMARY to Columns.Column(SQLConstants.NULL, Suggestions.SUMMARY).sql,
            Suggestions.ICON to Columns.Column(SQLConstants.NULL, Suggestions.ICON).sql,
            Suggestions.EXTRA_ID to Columns.Column("0", Suggestions.EXTRA_ID).sql,
            Suggestions.EXTRA to Columns.Column(SQLConstants.NULL, Suggestions.EXTRA).sql,
            Suggestions.VALUE to Columns.Column(SearchHistory.QUERY, Suggestions.VALUE).sql
    )

    private val savedSearchesProjectionMap = mapOf(Suggestions._ID to Columns.Column(SavedSearches._ID, Suggestions._ID).sql,
            Suggestions.TYPE to Columns.Column("'${Suggestions.Search.TYPE_SAVED_SEARCH}'", Suggestions.TYPE).sql,
            Suggestions.TITLE to Columns.Column(SavedSearches.QUERY, Suggestions.TITLE).sql,
            Suggestions.SUMMARY to Columns.Column(SQLConstants.NULL, Suggestions.SUMMARY).sql,
            Suggestions.ICON to Columns.Column(SQLConstants.NULL, Suggestions.ICON).sql,
            Suggestions.EXTRA_ID to Columns.Column("0", Suggestions.EXTRA_ID).sql,
            Suggestions.EXTRA to Columns.Column(SQLConstants.NULL, Suggestions.EXTRA).sql,
            Suggestions.VALUE to Columns.Column(SavedSearches.QUERY, Suggestions.VALUE).sql
    )
    private val suggestionUsersProjectionMap = mapOf(Suggestions._ID to Columns.Column(CachedUsers._ID, Suggestions._ID).sql,
            Suggestions.TYPE to Columns.Column("'${Suggestions.Search.TYPE_USER}'", Suggestions.TYPE).sql,
            Suggestions.TITLE to Columns.Column(CachedUsers.NAME, Suggestions.TITLE).sql,
            Suggestions.SUMMARY to Columns.Column(CachedUsers.SCREEN_NAME, Suggestions.SUMMARY).sql,
            Suggestions.ICON to Columns.Column(CachedUsers.PROFILE_IMAGE_URL, Suggestions.ICON).sql,
            Suggestions.EXTRA_ID to Columns.Column(CachedUsers.USER_KEY, Suggestions.EXTRA_ID).sql,
            Suggestions.EXTRA to Columns.Column(SQLConstants.NULL, Suggestions.EXTRA).sql,
            Suggestions.VALUE to Columns.Column(CachedUsers.SCREEN_NAME, Suggestions.VALUE).sql
    )

    private val autoCompleteUsersProjectionMap = mapOf(Suggestions._ID to Columns.Column(CachedUsers._ID, Suggestions._ID).sql,
            Suggestions.TYPE to Columns.Column("'${Suggestions.AutoComplete.TYPE_USERS}'", Suggestions.TYPE).sql,
            Suggestions.TITLE to Columns.Column(CachedUsers.NAME, Suggestions.TITLE).sql,
            Suggestions.SUMMARY to Columns.Column(CachedUsers.SCREEN_NAME, Suggestions.SUMMARY).sql,
            Suggestions.ICON to Columns.Column(CachedUsers.PROFILE_IMAGE_URL, Suggestions.ICON).sql,
            Suggestions.EXTRA_ID to Columns.Column(CachedUsers.USER_KEY, Suggestions.EXTRA_ID).sql,
            Suggestions.EXTRA to Columns.Column(SQLConstants.NULL, Suggestions.EXTRA).sql,
            Suggestions.VALUE to Columns.Column(CachedUsers.SCREEN_NAME, Suggestions.VALUE).sql
    )

    private val hashtagsProjectionMap = mapOf(Suggestions._ID to Columns.Column(CachedHashtags._ID, Suggestions._ID).sql,
            Suggestions.TYPE to Columns.Column("'${Suggestions.AutoComplete.TYPE_HASHTAGS}'", Suggestions.TYPE).sql,
            Suggestions.TITLE to Columns.Column(CachedHashtags.NAME, Suggestions.TITLE).sql,
            Suggestions.SUMMARY to Columns.Column(SQLConstants.NULL, Suggestions.SUMMARY).sql,
            Suggestions.ICON to Columns.Column(SQLConstants.NULL, Suggestions.ICON).sql,
            Suggestions.EXTRA_ID to Columns.Column("0", Suggestions.EXTRA_ID).sql,
            Suggestions.EXTRA to Columns.Column(SQLConstants.NULL, Suggestions.EXTRA).sql,
            Suggestions.VALUE to Columns.Column(CachedHashtags.NAME, Suggestions.VALUE).sql
    )


    fun forSearch(db: SQLiteDatabaseWrapper, manager: UserColorNameManager,
            uri: Uri, projection: Array<String>?): Cursor? {
        val nonNullProjection = projection ?: Suggestions.COLUMNS
        val query = uri.getQueryParameter(QUERY_PARAM_QUERY) ?: return null
        val accountKey = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_KEY)?.let(UserKey::valueOf) ?: return null
        val emptyQuery = TextUtils.isEmpty(query)
        val cursors = mutableListOf(getHistoryCursor(db, nonNullProjection, query))
        if (emptyQuery) {
            cursors.add(getSavedSearchCursor(db, nonNullProjection, accountKey))
        } else {
            val queryTrimmed = query.replace("_", "^_").substringAfter("@")

            if (!hasName(db, queryTrimmed)) {
                val m = PATTERN_SCREEN_NAME.matcher(query)
                if (m.matches()) {
                    val screenName = m.group(1)
                    cursors.add(getScreenNameCursor(nonNullProjection, screenName))
                }
            }
            cursors.add(getUsersCursor(db, manager, accountKey, nonNullProjection, query, queryTrimmed))
        }
        return MergeCursor(cursors.toTypedArray())
    }


    fun forAutoComplete(db: SQLiteDatabaseWrapper, manager: UserColorNameManager,
            uri: Uri, projection: Array<String>?): Cursor? {
        val nonNullProjection = projection ?: Suggestions.COLUMNS
        val query = uri.getQueryParameter(QUERY_PARAM_QUERY) ?: return null
        val type = uri.getQueryParameter(QUERY_PARAM_TYPE) ?: return null
        val accountKey = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_KEY)?.let(UserKey::valueOf) ?: return null
        val queryEscaped = query.replace("_", "^_")
        when (type) {
            Suggestions.AutoComplete.TYPE_USERS -> {
                val nicknameKeys = Utils.getMatchedNicknameKeys(query, manager)
                val where = Expression.or(Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size),
                        Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                        Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^"))
                val whereArgs = nicknameKeys + queryEscaped + queryEscaped
                val orderBy = arrayOf(CachedUsers.SCORE, CachedUsers.LAST_SEEN, CachedUsers.SCREEN_NAME, CachedUsers.NAME)
                val ascending = booleanArrayOf(false, false, true, true)
                val mappedProjection = nonNullProjection.mapToArray { autoCompleteUsersProjectionMap[it]!! }
                val (sql, bindingArgs) = CachedUsersQueryBuilder.withScore(mappedProjection,
                        where.sql, whereArgs, OrderBy(orderBy, ascending).sql, accountKey, 0)
                return db.rawQuery(sql.sql, bindingArgs)
            }
            Suggestions.AutoComplete.TYPE_HASHTAGS -> {
                val where = Expression.likeRaw(Columns.Column(CachedHashtags.NAME), "?||'%'", "^")
                val whereArgs = arrayOf(queryEscaped)
                val mappedProjection = nonNullProjection.mapToArray { hashtagsProjectionMap[it] }
                return db.query(CachedHashtags.TABLE_NAME, mappedProjection, where.sql, whereArgs, null,
                        null, null)
            }
            else -> return null
        }
    }

    private fun getUsersCursor(db: SQLiteDatabaseWrapper, manager: UserColorNameManager, accountKey: UserKey,
            selection: Array<String>, query: String, queryTrimmed: String): Cursor {
        val nicknameKeys = Utils.getMatchedNicknameKeys(query, manager)
        val usersSelection = Expression.or(
                Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size),
                Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^")
        )
        val selectionArgs = nicknameKeys + queryTrimmed + queryTrimmed
        val order = arrayOf(CachedUsers.LAST_SEEN, CachedUsers.SCORE, CachedUsers.SCREEN_NAME, CachedUsers.NAME)
        val ascending = booleanArrayOf(false, false, true, true)
        val orderBy = OrderBy(order, ascending)
        val usersProjection = selection.mapToArray { suggestionUsersProjectionMap[it]!! }
        val usersQuery = CachedUsersQueryBuilder.withScore(usersProjection,
                usersSelection.sql, selectionArgs, orderBy.sql, accountKey, 0)
        return db.rawQuery(usersQuery.first.sql, usersQuery.second)
    }

    private fun getSavedSearchCursor(db: SQLiteDatabaseWrapper, projection: Array<String>, accountKey: UserKey): Cursor {
        val savedSearchesWhere = Expression.equalsArgs(SavedSearches.ACCOUNT_KEY)
        val whereArgs = arrayOf(accountKey.toString())
        val savedSearchesProjection = projection.mapToArray { savedSearchesProjectionMap[it] }
        return db.query(true, SavedSearches.TABLE_NAME, savedSearchesProjection, savedSearchesWhere.sql,
                whereArgs, null, null, SavedSearches.DEFAULT_SORT_ORDER, null)
    }

    private fun getHistoryCursor(db: SQLiteDatabaseWrapper, projection: Array<String>, query: String): Cursor {
        val queryEscaped = query.replace("_", "^_")
        val historySelection = Expression.likeRaw(Columns.Column(SearchHistory.QUERY), "?||'%'", "^")
        val historySelectionArgs = arrayOf(queryEscaped)
        val historyProjection = projection.mapToArray { historyProjectionMap[it] }
        val cursorLimit = if (TextUtils.isEmpty(query)) "3" else "2"
        return db.query(true, SearchHistory.TABLE_NAME, historyProjection, historySelection.sql,
                historySelectionArgs, null, null, SearchHistory.DEFAULT_SORT_ORDER, cursorLimit)
    }

    private fun getScreenNameCursor(projection: Array<String>, screenName: String): Cursor {
        fun mapSelectionToValue(column: String) = when (column) {
            Suggestions._ID -> "0"
            Suggestions.TYPE -> Suggestions.Search.TYPE_SCREEN_NAME
            Suggestions.TITLE -> screenName
            Suggestions.EXTRA_ID -> "0"
            Suggestions.VALUE -> screenName
            else -> null
        }

        val cursor = MatrixCursor(projection)
        cursor.addRow(projection.map(::mapSelectionToValue))
        return cursor
    }

    private fun hasName(db: SQLiteDatabaseWrapper, queryTrimmed: String): Boolean {
        val exactUserSelection = Expression.or(Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?", "^"))
        val exactUserCursor = db.query(CachedUsers.TABLE_NAME, arrayOf(SQLFunctions.COUNT()),
                exactUserSelection.sql, arrayOf(queryTrimmed), null, null, null, "1")
        try {
            return exactUserCursor.moveToPosition(0) && exactUserCursor.getInt(0) > 0
        } finally {
            exactUserCursor.close()
        }
    }
}
