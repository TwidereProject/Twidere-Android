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

package org.mariotaku.twidere.util

import android.content.ContentValues
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.twitter.model.SavedSearch
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches

object ContentValuesCreator {

    fun createFilteredUser(status: ParcelableStatus): ContentValues {
        val values = ContentValues()
        values.put(Filters.Users.USER_KEY, status.user_key.toString())
        values.put(Filters.Users.NAME, status.user_name)
        values.put(Filters.Users.SCREEN_NAME, status.user_screen_name)
        return values
    }

    fun createFilteredUser(user: ParcelableUser): ContentValues {
        val values = ContentValues()
        values.put(Filters.Users.USER_KEY, user.key.toString())
        values.put(Filters.Users.NAME, user.name)
        values.put(Filters.Users.SCREEN_NAME, user.screen_name)
        return values
    }

    fun createFilteredUser(user: ParcelableUserMention): ContentValues {
        val values = ContentValues()
        values.put(Filters.Users.USER_KEY, user.key.toString())
        values.put(Filters.Users.NAME, user.name)
        values.put(Filters.Users.SCREEN_NAME, user.screen_name)
        return values
    }

    fun createSavedSearch(savedSearch: SavedSearch, accountKey: UserKey): ContentValues {
        val values = ContentValues()
        values.put(SavedSearches.ACCOUNT_KEY, accountKey.toString())
        values.put(SavedSearches.SEARCH_ID, savedSearch.id)
        values.put(SavedSearches.CREATED_AT, savedSearch.createdAt.time)
        values.put(SavedSearches.NAME, savedSearch.name)
        values.put(SavedSearches.QUERY, savedSearch.query)
        return values
    }

    fun createSavedSearches(savedSearches: List<SavedSearch>, accountKey: UserKey): Array<ContentValues> {
        return savedSearches.mapToArray { createSavedSearch(it, accountKey) }
    }

    fun createStatus(orig: Status, accountKey: UserKey, accountType: String,
            profileImageSize: String): ContentValues {
        return ObjectCursor.valuesCreatorFrom(ParcelableStatus::class.java)
                .create(orig.toParcelable(accountKey, accountType, profileImageSize))
    }

}
