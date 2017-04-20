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

import org.mariotaku.ktextension.mapToArray
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.sqliteqb.library.query.SQLSelectQuery
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 2017/2/17.
 */
object CachedUsersQueryBuilder {

    val scoreExpr = "${valueOrZero(CachedRelationships.FOLLOWING)} * 100 + " +
            "${valueOrZero(CachedRelationships.NOTIFICATIONS_ENABLED)} * 50 + " +
            "${valueOrZero(CachedRelationships.FOLLOWED_BY)} * 50 - " +
            "${valueOrZero(CachedRelationships.BLOCKING)} * 100 - " +
            "${valueOrZero(CachedRelationships.BLOCKED_BY)} * 100 - " +
            "${valueOrZero(CachedRelationships.MUTING)} * 100"

    fun withRelationship(projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?,
            accountKey: UserKey): Pair<SQLSelectQuery, Array<String>> {
        return withRelationship(Utils.getColumnsFromProjection(projection), selection,
                selectionArgs, sortOrder, accountKey)
    }

    fun withRelationship(select: Selectable,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?,
            accountKey: UserKey): Pair<SQLSelectQuery, Array<String>> {
        val qb = SQLSelectQuery.Builder()
        qb.select(select).from(Tables(CachedUsers.TABLE_NAME))
        val relationshipsUserKey = Columns.Column(Table(CachedRelationships.TABLE_NAME),
                CachedRelationships.USER_KEY)
        val usersUserKey = Columns.Column(Table(CachedUsers.TABLE_NAME),
                CachedRelationships.USER_KEY)
        val relationshipsAccountKey = Columns.Column(Table(CachedRelationships.TABLE_NAME),
                CachedRelationships.ACCOUNT_KEY)
        val on = Expression.and(
                Expression.equals(relationshipsUserKey, usersUserKey),
                Expression.equalsArgs(relationshipsAccountKey.sql)
        )
        qb.join(Join(false, Join.Operation.LEFT, Table(CachedRelationships.TABLE_NAME), on))
        val userTypeExpression: Expression
        val host = accountKey.host
        val accountKeyArgs: Array<String>
        if (host == null) {
            userTypeExpression = Expression.notLikeRaw(Columns.Column(Table(CachedUsers.TABLE_NAME),
                    CachedUsers.USER_KEY), "'%@%'")
            accountKeyArgs = arrayOf(accountKey.toString())
        } else {
            userTypeExpression = Expression.likeRaw(Columns.Column(Table(CachedUsers.TABLE_NAME),
                    CachedUsers.USER_KEY), "'%@'||?")
            accountKeyArgs = arrayOf(accountKey.toString(), host)
        }
        if (selection != null) {
            qb.where(Expression.and(userTypeExpression, Expression(selection)))
        } else {
            qb.where(userTypeExpression)
        }
        if (sortOrder != null) {
            qb.orderBy(OrderBy(sortOrder))
        }
        val mergedArgs = if (selectionArgs != null) {
            accountKeyArgs + selectionArgs
        } else {
            accountKeyArgs
        }
        return Pair<SQLSelectQuery, Array<String>>(qb.build(), mergedArgs)
    }

    fun withScore(projection: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            sortOrder: String?,
            accountKey: UserKey,
            limit: Int): Pair<SQLSelectQuery, Array<String>> {
        val qb = SQLSelectQuery.Builder()
        val select = Utils.getColumnsFromProjection(projection)
        val columns = CachedUsers.COLUMNS.mapToArray { column ->
            if (CachedUsers._ID == column || CachedUsers.USER_KEY == column) {
                return@mapToArray Columns.Column(Table(CachedUsers.TABLE_NAME), column, column)
            } else {
                return@mapToArray Columns.Column(column)
            }
        } + Columns.Column(scoreExpr, "score")
        qb.select(select)
        val pair = withRelationship(Columns(*columns), null, null, null, accountKey)
        qb.from(pair.first)
        val mergedArgs = if (selectionArgs != null) {
            pair.second + selectionArgs
        } else {
            pair.second
        }
        if (selection != null) {
            qb.where(Expression(selection))
        }
        if (sortOrder != null) {
            qb.orderBy(OrderBy(sortOrder))
        }
        if (limit > 0) {
            qb.limit(limit)
        }
        return Pair<SQLSelectQuery, Array<String>>(qb.build(), mergedArgs)
    }

    private fun valueOrZero(column: String): String = "CASE WHEN $column IS NULL THEN 0 ELSE $column END"

}
