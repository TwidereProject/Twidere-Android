/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.UserListTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.Utils
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
class UserListTimelineFragment : ParcelableStatusesFragment() {

    override val timelineType: String
        @TimelineType
        get() = TimelineType.OTHER

    override fun onCreateStatusesLoader(context: Context,
                                        args: Bundle,
                                        fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        refreshing = true
        val listId = args.getString(EXTRA_LIST_ID)
        val accountKey = Utils.getAccountKey(context, args)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val listName = args.getString(EXTRA_LIST_NAME)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return UserListTimelineLoader(activity, accountKey, listId, userKey, screenName,
                listName, sinceId, maxId, adapterData, savedStatusesFileArgs, tabPosition,
                fromUser, loadingMore)
    }

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val args = arguments!!
            val accountKey = Utils.getAccountKey(context, args)!!
            val listId = args.getString(EXTRA_LIST_ID)
            val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
            val screenName = args.getString(EXTRA_SCREEN_NAME)
            val listName = args.getString(EXTRA_LIST_NAME)
            val result = ArrayList<String>()
            result.add(TwidereConstants.AUTHORITY_USER_LIST_TIMELINE)
            result.add("account=$accountKey")
            if (listId != null) {
                result.add("list_id=$listId")
            } else if (listName != null) {
                if (userKey != null) {
                    result.add("user_id=$userKey")
                } else if (screenName != null) {
                    result.add("screen_name=$screenName")
                }
                return null
            } else {
                return null
            }
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            val args = arguments!!
            val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
            val sb = StringBuilder("user_list_")
            if (tabPosition < 0) return null
            val listId = args.getString(EXTRA_LIST_ID)
            val listName = args.getString(EXTRA_LIST_NAME)
            if (listId != null) {
                sb.append(listId)
            } else if (listName != null) {
                val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
                val screenName = args.getString(EXTRA_SCREEN_NAME)
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

}
