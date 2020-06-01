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
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.loader.statuses.UserMentionsLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.Utils
import java.util.*

class UserMentionsFragment : StatusesSearchFragment() {

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val arguments = arguments ?: return null
            val context = context ?: return null
            val accountKey = Utils.getAccountKey(context, arguments)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            val result = ArrayList<String>()
            result.add(AUTHORITY_USER_MENTIONS)
            result.add("account=$accountKey")
            result.add("screen_name=$screenName")
            return result.toTypedArray()
        }

    override fun onCreateStatusesLoader(context: Context,
            args: Bundle,
            fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        val screenName = args.getString(EXTRA_SCREEN_NAME)!!
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val makeGap = args.getBoolean(EXTRA_MAKE_GAP, true)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return UserMentionsLoader(requireActivity(), accountKey, screenName, adapterData,
                savedStatusesFileArgs, tabPosition, fromUser, makeGap, false, loadingMore)
    }

}
