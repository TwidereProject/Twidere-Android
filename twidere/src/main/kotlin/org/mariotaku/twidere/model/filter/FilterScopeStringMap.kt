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

package org.mariotaku.twidere.model.filter

import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.annotation.FilterScope

/**
 * Created by mariotaku on 2017/9/16.
 */
object FilterScopeStringMap {

    private val mappings: Array<Mapping>

    init {
        mappings = arrayOf(
                Mapping(FilterScope.ALL, "all"),
                Mapping(FilterScope.DEFAULT, "default"),
                Mapping(FilterScope.TARGET_NAME, "target_name"),
                Mapping(FilterScope.TARGET_DESCRIPTION, "target_description"),
                Mapping(FilterScope.TARGET_TEXT, "target_text"),
                Mapping(FilterScope.HOME, "home"),
                Mapping(FilterScope.INTERACTIONS, "interactions"),
                Mapping(FilterScope.MESSAGES, "messages"),
                Mapping(FilterScope.SEARCH_RESULTS, "search_results"),
                Mapping(FilterScope.LIST_GROUP_TIMELINE, "list_group_timeline"),
                Mapping(FilterScope.FAVORITES, "favorites"),
                Mapping(FilterScope.USER_TIMELINE, "user_timeline"),
                Mapping(FilterScope.PUBLIC_TIMELINE, "public_timeline")
        )
    }

    fun toString(scope: Int): String {
        val result = StringBuilder()
        var tmp = scope
        while (tmp != 0) {
            val mapping = mappings.firstOrNull { (v, _) -> v in tmp } ?: break
            if (result.isNotEmpty()) {
                result.append('|')
            }
            result.append(mapping.name)
            tmp = tmp and mapping.scope.inv()
        }
        return result.toString()
    }

    fun fromString(str: String): Int {
        return str.split('|').mapNotNull { seg ->
            mappings.firstOrNull { (_, n) -> n == seg }
        }.fold(0) { acc, mapping -> acc or mapping.scope }
    }

    private data class Mapping(val scope: Int, val name: String)
}