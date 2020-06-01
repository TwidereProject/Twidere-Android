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
import org.mariotaku.twidere.fragment.ParcelableStatusesFragment
import org.mariotaku.twidere.loader.statuses.TweetSearchLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.Utils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
open class StatusesSearchFragment : ParcelableStatusesFragment() {

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val context = context ?: return null
            val arguments = arguments ?: return null
            val accountKey = Utils.getAccountKey(context, arguments)
            val query = arguments.getString(EXTRA_QUERY)
            val local = arguments.getBoolean(EXTRA_LOCAL)
            val result = ArrayList<String>()
            result.add(AUTHORITY_SEARCH_TWEETS)
            result.add("account=$accountKey")
            result.add("query=$query")
            if (local) {
                result.add("local")
            }
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            val arguments = arguments ?: return null
            val tabPosition = arguments.getInt(EXTRA_TAB_POSITION, -1)
            val sb = StringBuilder("search_")
            if (tabPosition < 0) return null
            val query = arguments.getString(EXTRA_QUERY) ?: return null
            val encodedQuery: String
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8").replace("[^\\w\\d]".toRegex(), "_")
            } catch (e: UnsupportedEncodingException) {
                return null
            }

            sb.append(encodedQuery)
            return sb.toString()
        }

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        refreshing = true
        val accountKey = Utils.getAccountKey(context, args)
        val query = requireArguments().getString(EXTRA_QUERY)
        val local = requireArguments().getBoolean(EXTRA_LOCAL, false)
        val tabPosition = requireArguments().getInt(EXTRA_TAB_POSITION, -1)
        val makeGap = args.getBoolean(EXTRA_MAKE_GAP, true)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return TweetSearchLoader(requireActivity(), accountKey, query, adapterData, savedStatusesFileArgs,
                tabPosition, fromUser, makeGap, local, loadingMore)
    }

}
