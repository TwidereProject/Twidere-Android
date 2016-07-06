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

package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.loader.RetweetsOfMeLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.Utils
import java.util.*

class RetweetsOfMeFragment : ParcelableStatusesFragment() {

    override fun onCreateStatusesLoader(context: Context,
                                        args: Bundle,
                                        fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        val accountKey = Utils.getAccountKey(context, args)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return RetweetsOfMeLoader(context, accountKey, sinceId, maxId, adapterData,
                savedStatusesFileArgs, tabPosition, fromUser, loadingMore)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //        final IStatusesListAdapter<List<ParcelableStatus>> adapter = getAdapter();
        //        adapter.setIndicateMyStatusDisabled(false);
        //        adapter.setFiltersEnabled(true);
        //        adapter.setIgnoredFilterFields(true, false, false, false, false);
    }

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val args = arguments!!
            val accountKey = Utils.getAccountKey(context, args)!!
            val result = ArrayList<String>()
            result.add(AUTHORITY_RETWEETS_OF_ME)
            result.add("account=$accountKey")
            return result.toTypedArray()
        }

    override val timelineType: String
        get() = TimelineType.INTERACTIONS
}
