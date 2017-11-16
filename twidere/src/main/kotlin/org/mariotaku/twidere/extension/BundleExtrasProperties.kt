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

package org.mariotaku.twidere.extension

import android.os.Bundle
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.model.*

inline var Bundle.name: String?
    get() = getString(EXTRA_NAME)
    set(value) = putString(EXTRA_NAME, value)

inline var Bundle.screenName: String?
    get() = getString(EXTRA_SCREEN_NAME)
    set(value) = putString(EXTRA_SCREEN_NAME, value)

inline var Bundle.query: String?
    get() = getString(EXTRA_QUERY)
    set(value) = putString(EXTRA_QUERY, value)

inline var Bundle.local: Boolean
    get() = getBoolean(EXTRA_LOCAL)
    set(value) = putBoolean(EXTRA_LOCAL, value)

inline var Bundle.accountKey: UserKey?
    get() = getParcelable(EXTRA_ACCOUNT_KEY)
    set(value) = putParcelable(EXTRA_ACCOUNT_KEY, value)

inline var Bundle.userKey: UserKey?
    get() = getParcelable(EXTRA_USER_KEY)
    set(value) = putParcelable(EXTRA_USER_KEY, value)

inline var Bundle.simpleLayout: Boolean
    get() = getBoolean(EXTRA_SIMPLE_LAYOUT)
    set(value) = putBoolean(EXTRA_SIMPLE_LAYOUT, value)

inline var Bundle.account: AccountDetails?
    get() = getParcelable(EXTRA_ACCOUNT)
    set(value) = putParcelable(EXTRA_ACCOUNT, value)

inline var Bundle.user: ParcelableUser?
    get() = getParcelable(EXTRA_USER)
    set(value) = putParcelable(EXTRA_USER, value)

inline var Bundle.userList: ParcelableUserList?
    get() = getParcelable(EXTRA_USER_LIST)
    set(value) = putParcelable(EXTRA_USER_LIST, value)

inline var Bundle.status: ParcelableStatus?
    get() = getParcelable(EXTRA_STATUS)
    set(value) = putParcelable(EXTRA_STATUS, value)

inline var Bundle.title: CharSequence?
    get() = getCharSequence(EXTRA_TITLE)
    set(value) = putCharSequence(EXTRA_TITLE, value)

inline var Bundle.text: CharSequence?
    get() = getCharSequence(EXTRA_TEXT)
    set(value) = putCharSequence(EXTRA_TEXT, value)

inline var Bundle.message: CharSequence?
    get() = getCharSequence(EXTRA_MESSAGE)
    set(value) = putCharSequence(EXTRA_MESSAGE, value)

inline var Bundle.position: Int
    get() = getInt(EXTRA_POSITION)
    set(value) = putInt(EXTRA_POSITION, value)

inline var Bundle.statusId: String?
    get() = getString(EXTRA_STATUS_ID)
    set(value) = putString(EXTRA_STATUS_ID, value)

inline var Bundle.listId: String?
    get() = getString(EXTRA_LIST_ID)
    set(value) = putString(EXTRA_LIST_ID, value)

inline var Bundle.listName: String?
    get() = getString(EXTRA_LIST_NAME)
    set(value) = putString(EXTRA_LIST_NAME, value)

inline var Bundle.groupId: String?
    get() = getString(EXTRA_GROUP_ID)
    set(value) = putString(EXTRA_GROUP_ID, value)

inline var Bundle.groupName: String?
    get() = getString(EXTRA_GROUP_NAME)
    set(value) = putString(EXTRA_GROUP_NAME, value)

inline var Bundle.conversationId: String?
    get() = getString(EXTRA_CONVERSATION_ID)
    set(value) = putString(EXTRA_CONVERSATION_ID, value)

inline var Bundle.stringId: String?
    get() = getString(EXTRA_ID)
    set(value) = putString(EXTRA_ID, value)

inline var Bundle.longId: Long
    get() = getLong(EXTRA_ID, -1)
    set(value) = putLong(EXTRA_ID, value)