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

package org.mariotaku.twidere.constant


object TableIds {
    const val HOME_TIMELINE = 11
    const val PUBLIC_TIMELINE = 12
    const val NETWORK_PUBLIC_TIMELINE = 13

    const val FAVORITES = 21
    const val USER_TIMELINE = 22
    const val USER_MEDIA_TIMELINE = 23
    const val LIST_TIMELINE = 24
    const val GROUP_TIMELINE = 25
    const val SEARCH_TIMELINE = 26
    const val MEDIA_SEARCH_TIMELINE = 27

    val RANGE_CUSTOM_TIMELINE = FAVORITES..MEDIA_SEARCH_TIMELINE

    const val ACTIVITIES_ABOUT_ME = 51
    const val ACTIVITIES_BY_FRIENDS = 52
    const val MESSAGES = 71
    const val MESSAGES_CONVERSATIONS = 74
    const val FILTERED_USERS = 81
    const val FILTERED_KEYWORDS = 82
    const val FILTERED_SOURCES = 83
    const val FILTERED_LINKS = 84
    const val FILTERS_SUBSCRIPTIONS = 89
    const val TRENDS_LOCAL = 91
    const val SAVED_SEARCHES = 92
    const val SEARCH_HISTORY = 93
    const val DRAFTS = 101
    const val TABS = 102
    const val CACHED_USERS = 111
    const val CACHED_STATUSES = 112
    const val CACHED_HASHTAGS = 113
    const val CACHED_RELATIONSHIPS = 114

    const val VIRTUAL_PERMISSIONS = 1004
    const val VIRTUAL_CACHED_USERS_WITH_RELATIONSHIP = 1021
    const val VIRTUAL_CACHED_USERS_WITH_SCORE = 1022
    const val VIRTUAL_DRAFTS_UNSENT = 1031
    const val VIRTUAL_DRAFTS_NOTIFICATIONS = 1032
    const val VIRTUAL_SUGGESTIONS_AUTO_COMPLETE = 1041
    const val VIRTUAL_SUGGESTIONS_SEARCH = 1042

    const val VIRTUAL_NULL = 2000
    const val VIRTUAL_EMPTY = 2001
    const val VIRTUAL_DATABASE_PREPARE = 2003

    const val VIRTUAL_RAW_QUERY = 3000
}
