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
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.extension.rawQueryReference
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Filters.*

object ContentFiltersUtils {

    fun isFiltered(cr: ContentResolver, status: ParcelableStatus, filterRts: Boolean,
            @FilterScope scope: Int, allowedKeywords: Array<String>? = null): Boolean {
        val query = isFilteredQuery(status.filter_users, status.filter_texts,
                status.filter_sources, status.filter_links, status.filter_names,
                status.filter_descriptions, filterRts, scope, allowedKeywords)
        return cr.rawQueryReference(query.first, query.second)?.use { (cur) ->
            cur.moveToFirst() && cur.getInt(0) != 0
        } ?: false
    }

    private fun isFilteredQuery(users: Array<UserKey>?, texts: String?, sources: Array<String>?,
            links: Array<String>?, names: Array<String>?, descriptions: String?,
            filterRts: Boolean, @FilterScope scope: Int, allowedKeywords: Array<String>? = null): Pair<String, Array<String>> {
        var numExpressions = 0
        val selectionArgs = mutableListOf<String>()
        val queryBuilder = StringBuilder("SELECT ")

        fun addExpression(ruleTable: String, ruleField: String, scopeField: String,
                @FilterScope expressionScope: Int, noScopeAsTrue: Boolean, matchType: Int,
                value: String, extraWhereAppend: ((StringBuilder, MutableList<String>, String) -> Unit)? = null) {
            if (numExpressions > 0) {
                queryBuilder.append(" OR ")
            }
            selectionArgs.add(value)
            queryBuilder.append("1 IN (SELECT ? LIKE ")
            when (matchType) {
                LINE_MATCH -> {
                    queryBuilder.append("'%\\'||")
                    queryBuilder.append(ruleField)
                    queryBuilder.append("||'\\%'")
                }
                LINE_CONTAINS -> {
                    queryBuilder.append("'\\%'||")
                    queryBuilder.append(ruleField)
                    queryBuilder.append("||'%\\'")
                }
                CONTAINS -> {
                    queryBuilder.append("'%'||")
                    queryBuilder.append(ruleField)
                    queryBuilder.append("||'%'")
                }
            }
            queryBuilder.append(" FROM ")
            queryBuilder.append(ruleTable)
            queryBuilder.append(" WHERE ")
            if (noScopeAsTrue) {
                queryBuilder.append("(")
                queryBuilder.append(scopeField)
                queryBuilder.append(" = 0 OR ")
            }
            queryBuilder.append(scopeField)
            queryBuilder.append(" & ")
            queryBuilder.append(expressionScope)
            queryBuilder.append(" != 0")
            if (noScopeAsTrue) {
                queryBuilder.append(")")
            }
            extraWhereAppend?.invoke(queryBuilder, selectionArgs, ruleField)
            queryBuilder.append(")")
            numExpressions += 1
        }

        fun allowKeywordsWhere(sb: StringBuilder, args: MutableList<String>, ruleField: String) {
            val allowed = allowedKeywords?.takeUnless(Array<*>::isEmpty) ?: return
            sb.append(" AND NOT (")
            allowed.forEachIndexed { i, s ->
                args.add(s)
                if (i != 0) {
                    sb.append(" OR ")
                }
                sb.append("? LIKE ")
                sb.append(ruleField)
            }
            sb.append(")")
        }

        if (users != null) {
            addExpression(Users.TABLE_NAME, Users.USER_KEY, Users.SCOPE, scope, true,
                    LINE_MATCH, users.joinToString("\n", "\\", "\\"))
        }
        if (sources != null) {
            addExpression(Sources.TABLE_NAME, Sources.VALUE, Sources.SCOPE, scope, true,
                    LINE_MATCH, sources.joinToString("\n", "\\", "\\"))
        }
        if (links != null) {
            addExpression(Links.TABLE_NAME, Links.VALUE, Links.SCOPE, scope, true,
                    LINE_CONTAINS, links.joinToString("\n", "\\", "\\"))
        }
        if (texts != null) {
            addExpression(Keywords.TABLE_NAME, Keywords.VALUE, Keywords.SCOPE,
                    FilterScope.TARGET_TEXT or scope, true, CONTAINS,
                    texts, ::allowKeywordsWhere)
        }
        if (names != null) {
            addExpression(Keywords.TABLE_NAME, Keywords.VALUE, Keywords.SCOPE,
                    FilterScope.TARGET_NAME or scope, false, LINE_CONTAINS,
                    names.joinToString("\n", "\\", "\\"), ::allowKeywordsWhere)
        }
        if (descriptions != null) {
            addExpression(Keywords.TABLE_NAME, Keywords.VALUE, Keywords.SCOPE,
                    FilterScope.TARGET_DESCRIPTION or scope, false, CONTAINS,
                    descriptions, ::allowKeywordsWhere)
        }
        if (numExpressions == 0)
            return Pair("SELECT 0", emptyArray())
        return Pair(queryBuilder.toString(), selectionArgs.toTypedArray())
    }

    private const val LINE_MATCH = 0
    private const val LINE_CONTAINS = 1
    private const val CONTAINS = 2

}
