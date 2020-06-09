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
import androidx.loader.content.Loader
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.ParcelableStatusesFragment
import org.mariotaku.twidere.loader.statuses.UserListTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.util.Utils
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
class UserListTimelineFragment : ParcelableStatusesFragment() {

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val arguments = arguments ?: return null
            val context = context ?: return null
            val accountKey = Utils.getAccountKey(context, arguments)
            val listId = arguments.getString(EXTRA_LIST_ID)
            val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            val listName = arguments.getString(EXTRA_LIST_NAME)
            val result = ArrayList<String>()
            result.add(TwidereConstants.AUTHORITY_USER_LIST_TIMELINE)
            result.add("account=$accountKey")
            when {
                listId != null -> {
                    result.add("list_id=$listId")
                }
                listName != null -> {
                    if (userKey != null) {
                        result.add("user_id=$userKey")
                    } else if (screenName != null) {
                        result.add("screen_name=$screenName")
                    }
                    return null
                }
                else -> {
                    return null
                }
            }
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            val arguments = arguments ?: return null
            val tabPosition = arguments.getInt(EXTRA_TAB_POSITION, -1)
            val sb = StringBuilder("user_list_")
            if (tabPosition < 0) return null
            val listId = arguments.getString(EXTRA_LIST_ID)
            val listName = arguments.getString(EXTRA_LIST_NAME)
            when {
                listId != null -> {
                    sb.append(listId)
                }
                listName != null -> {
                    val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
                    val screenName = arguments.getString(EXTRA_SCREEN_NAME)
                    when {
                        userKey != null -> {
                            sb.append(userKey)
                        }
                        screenName != null -> {
                            sb.append(screenName)
                        }
                        else -> {
                            return null
                        }
                    }
                    sb.append('_')
                    sb.append(listName)
                }
                else -> {
                    return null
                }
            }
            return sb.toString()
        }

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        refreshing = true
        val extras = arguments?.getParcelable<HomeTabExtras>(EXTRA_EXTRAS)
        val accountKey = Utils.getAccountKey(context, args)
        val listId = args.getString(EXTRA_LIST_ID)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val listName = args.getString(EXTRA_LIST_NAME)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return UserListTimelineLoader(requireActivity(), accountKey, listId, userKey, screenName, listName,
                adapterData, savedStatusesFileArgs, tabPosition, fromUser, loadingMore, extras)
    }

}
