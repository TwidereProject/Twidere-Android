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
import org.mariotaku.microblog.library.twitter.model.SavedSearch
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.model.util.getActivityStatus
import org.mariotaku.twidere.provider.TwidereDataStore.*
import java.io.IOException
import java.util.*

object ContentValuesCreator {

    @Throws(IOException::class)
    fun createCachedUser(user: User): ContentValues {
        val values = ContentValues()
        ParcelableUserValuesCreator.writeTo(ParcelableUserUtils.fromUser(user, null), values)
        return values
    }

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

    fun createMessageDraft(accountKey: UserKey, recipientId: String, text: String, imageUri: String?): ContentValues {
        val values = ContentValues()
        values.put(Drafts.ACTION_TYPE, Draft.Action.SEND_DIRECT_MESSAGE)
        values.put(Drafts.TEXT, text)
        values.put(Drafts.ACCOUNT_KEYS, accountKey.toString())
        values.put(Drafts.TIMESTAMP, System.currentTimeMillis())
        if (imageUri != null) {
            val mediaArray = arrayOf(ParcelableMediaUpdate(imageUri, 0))
            values.put(Drafts.MEDIA, JsonSerializer.serialize(Arrays.asList(*mediaArray),
                    ParcelableMediaUpdate::class.java))
        }
        val extra = SendDirectMessageActionExtras()
        extra.recipientId = recipientId
        values.put(Drafts.ACTION_EXTRAS, JsonSerializer.serialize(extra))
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
        return savedSearches.map { createSavedSearch(it, accountKey) }.toTypedArray()
    }

    @Throws(IOException::class)
    fun createStatus(orig: Status, accountKey: UserKey): ContentValues {
        return ParcelableStatusValuesCreator.create(ParcelableStatusUtils.fromStatus(orig,
                accountKey, false))
    }

    @Throws(IOException::class)
    fun createActivity(activity: ParcelableActivity,
                       details: AccountDetails,
                       manager: UserColorNameManager): ContentValues {
        val values = ContentValues()
        val status = activity.getActivityStatus()

        activity.account_color = details.color

        if (status != null) {
            ParcelableStatusUtils.updateExtraInformation(status, details)

            activity.status_id = status.id
            activity.status_retweet_id = status.retweet_id
            activity.status_my_retweet_id = status.my_retweet_id

            if (status.is_retweet) {
                activity.status_retweeted_by_user_key = status.retweeted_by_user_key
            } else if (status.is_quote) {
                activity.status_quote_spans = status.quoted_spans
                activity.status_quote_text_plain = status.quoted_text_plain
                activity.status_quote_source = status.quoted_source
                activity.status_quoted_user_key = status.quoted_user_key
            }
            activity.status_user_key = status.user_key
            activity.status_user_following = status.user_is_following
            activity.status_spans = status.spans
            activity.status_text_plain = status.text_plain
            activity.status_source = status.source
        }
        ParcelableActivityValuesCreator.writeTo(activity, values)
        return values
    }


}
