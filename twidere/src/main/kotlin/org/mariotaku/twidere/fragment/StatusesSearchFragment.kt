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
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.content.Loader
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.loader.TweetSearchLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.Utils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
open class StatusesSearchFragment : ParcelableStatusesFragment() {

    @TimelineType
    override val timelineType: String = TimelineType.SEARCH

    override fun onCreateStatusesLoader(context: Context,
                                        args: Bundle,
                                        fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        refreshing = true
        val accountKey = Utils.getAccountKey(context, args)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val page = args.getInt(EXTRA_PAGE, -1)
        val query = args.getString(EXTRA_QUERY)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val makeGap = args.getBoolean(EXTRA_MAKE_GAP, true)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return TweetSearchLoader(activity, accountKey, query, sinceId, maxId, page,
                adapterData, savedStatusesFileArgs, tabPosition, fromUser, makeGap,
                loadingMore)
    }

    override fun fitSystemWindows(insets: Rect) {
        super.fitSystemWindows(insets)
    }

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val args = arguments!!
            val accountKey = Utils.getAccountKey(context, args)!!
            val query = args.getString(EXTRA_QUERY)!!
            val result = ArrayList<String>()
            result.add(AUTHORITY_SEARCH_TWEETS)
            result.add("account=$accountKey")
            result.add("query=$query")
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            val args = arguments!!
            val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
            val sb = StringBuilder("search_")
            if (tabPosition < 0) return null
            val query = args.getString(EXTRA_QUERY) ?: return null
            val encodedQuery: String
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8").replace("[^\\w\\d]".toRegex(), "_")
            } catch (e: UnsupportedEncodingException) {
                return null
            }

            sb.append(encodedQuery)
            return sb.toString()
        }

}
