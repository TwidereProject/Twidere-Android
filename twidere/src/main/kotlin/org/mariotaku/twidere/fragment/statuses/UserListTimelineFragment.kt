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

package org.mariotaku.twidere.fragment.statuses

import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.fragment.ParcelableStatusesFragment
import org.mariotaku.twidere.loader.statuses.UserListTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 14/12/2.
 */
class UserListTimelineFragment : ParcelableStatusesFragment() {

    override val readPositionTagWithArguments: String?
        get() {
            val tabPosition = arguments.getInt(EXTRA_TAB_POSITION, -1)
            val sb = StringBuilder("user_list_")
            if (tabPosition < 0) return null
            val listId = arguments.getString(EXTRA_LIST_ID)
            val listName = arguments.getString(EXTRA_LIST_NAME)
            if (listId != null) {
                sb.append(listId)
            } else if (listName != null) {
                val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
                val screenName = arguments.getString(EXTRA_SCREEN_NAME)
                if (userKey != null) {
                    sb.append(userKey)
                } else if (screenName != null) {
                    sb.append(screenName)
                } else {
                    return null
                }
                sb.append('_')
                sb.append(listName)
            } else {
                return null
            }
            return sb.toString()
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.list_timeline)
    }

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        refreshing = true
        val accountKey = Utils.getAccountKey(context, args)
        val listId = args.getString(EXTRA_LIST_ID)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val listName = args.getString(EXTRA_LIST_NAME)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return UserListTimelineLoader(activity, accountKey, listId, userKey, screenName, listName,
                adapterData, fromUser, loadingMore)
    }

}
