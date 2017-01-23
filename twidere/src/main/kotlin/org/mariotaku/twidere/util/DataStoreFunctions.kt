package org.mariotaku.twidere.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.useCursor
import org.mariotaku.pickncrop.library.PNCUtils
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.twidere.constant.filterPossibilitySensitiveStatusesKey
import org.mariotaku.twidere.constant.filterUnavailableQuoteStatusesKey
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.ParcelableStatus.FilterFlags
import org.mariotaku.twidere.provider.TwidereDataStore.*

/**
 * Created by mariotaku on 2016/12/24.
 */

fun buildStatusFilterWhereClause(preferences: SharedPreferences,
                                 table: String,
                                 extraSelection: Expression?): Expression {
    val filteredUsersQuery = SQLQueryBuilder
            .select(Columns.Column(Table(Filters.Users.TABLE_NAME), Filters.Users.USER_KEY))
            .from(Tables(Filters.Users.TABLE_NAME))
            .build()
    val filteredUsersWhere = Expression.or(
            Expression.`in`(Columns.Column(Table(table), Statuses.USER_KEY), filteredUsersQuery),
            Expression.`in`(Columns.Column(Table(table), Statuses.RETWEETED_BY_USER_KEY), filteredUsersQuery),
            Expression.`in`(Columns.Column(Table(table), Statuses.QUOTED_USER_KEY), filteredUsersQuery)
    )
    val filteredIdsQueryBuilder = SQLQueryBuilder
            .select(Columns.Column(Table(table), Statuses._ID))
            .from(Tables(table))
            .where(filteredUsersWhere)
            .union()
            .select(Columns(Columns.Column(Table(table), Statuses._ID)))
            .from(Tables(table, Filters.Sources.TABLE_NAME))
            .where(Expression.or(
                    Expression.likeRaw(Columns.Column(Table(table), Statuses.SOURCE),
                            "'%>'||" + Filters.Sources.TABLE_NAME + "." + Filters.Sources.VALUE + "||'</a>%'"),
                    Expression.likeRaw(Columns.Column(Table(table), Statuses.QUOTED_SOURCE),
                            "'%>'||" + Filters.Sources.TABLE_NAME + "." + Filters.Sources.VALUE + "||'</a>%'")
            ))
            .union()
            .select(Columns(Columns.Column(Table(table), Statuses._ID)))
            .from(Tables(table, Filters.Keywords.TABLE_NAME))
            .where(Expression.or(
                    Expression.likeRaw(Columns.Column(Table(table), Statuses.TEXT_PLAIN),
                            "'%'||" + Filters.Keywords.TABLE_NAME + "." + Filters.Keywords.VALUE + "||'%'"),
                    Expression.likeRaw(Columns.Column(Table(table), Statuses.QUOTED_TEXT_PLAIN),
                            "'%'||" + Filters.Keywords.TABLE_NAME + "." + Filters.Keywords.VALUE + "||'%'")
            ))
            .union()
            .select(Columns(Columns.Column(Table(table), Statuses._ID)))
            .from(Tables(table, Filters.Links.TABLE_NAME))
            .where(Expression.or(
                    Expression.likeRaw(Columns.Column(Table(table), Statuses.SPANS),
                            "'%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%'"),
                    Expression.likeRaw(Columns.Column(Table(table), Statuses.QUOTED_SPANS),
                            "'%'||" + Filters.Links.TABLE_NAME + "." + Filters.Links.VALUE + "||'%'")
            ))
    var filterFlags: Long = 0
    if (preferences[filterUnavailableQuoteStatusesKey]) {
        filterFlags = filterFlags or FilterFlags.QUOTE_NOT_AVAILABLE
    }
    if (preferences[filterPossibilitySensitiveStatusesKey]) {
        filterFlags = filterFlags or FilterFlags.POSSIBILITY_SENSITIVE
    }

    val filterExpression = Expression.or(
            Expression.and(
                    Expression("(" + Statuses.FILTER_FLAGS + " & " + filterFlags + ") == 0"),
                    Expression.notIn(Columns.Column(Table(table), Statuses._ID), filteredIdsQueryBuilder.build())
            ),
            Expression.equals(Columns.Column(Table(table), Statuses.IS_GAP), 1)
    )
    if (extraSelection != null) {
        return Expression.and(filterExpression, extraSelection)
    }
    return filterExpression
}

@SuppressLint("Recycle")
fun deleteDrafts(context: Context, draftIds: LongArray): Int {
    val where = Expression.inArgs(Drafts._ID, draftIds.size).sql
    val whereArgs = draftIds.map(Long::toString).toTypedArray()

    context.contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, where, whereArgs,
            null).useCursor { cursor ->
        val indices = DraftCursorIndices(cursor)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val draft = indices.newObject(cursor)
            draft.media?.forEach { item ->
                try {
                    PNCUtils.deleteMedia(context, Uri.parse(item.uri))
                } catch (e: SecurityException) {
                    // Ignore
                }
            }
            cursor.moveToNext()
        }
    }
    return context.contentResolver.delete(Drafts.CONTENT_URI, where, whereArgs)
}
