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
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.Loader
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.loader.GroupTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.Utils
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
class GroupTimelineFragment : ParcelableStatusesFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_group_timeline, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.compose -> {
                val args = arguments
                val accountKey = Utils.getAccountKey(context, args)
                val groupName = args.getString(EXTRA_GROUP_NAME)
                if (groupName != null) {
                    val intent = Intent(activity, ComposeActivity::class.java)
                    intent.action = INTENT_ACTION_COMPOSE
                    intent.putExtra(Intent.EXTRA_TEXT, "!$groupName ")
                    intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                    startActivity(intent)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateStatusesLoader(context: Context,
                                        args: Bundle,
                                        fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        refreshing = true
        val accountKey = Utils.getAccountKey(context, args)
        val groupId = args.getString(EXTRA_GROUP_ID)
        val groupName = args.getString(EXTRA_GROUP_NAME)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return GroupTimelineLoader(activity, accountKey, groupId, groupName, sinceId,
                maxId, adapterData, savedStatusesFileArgs, tabPosition, fromUser, loadingMore)
    }

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val args = arguments!!
            val accountKey = Utils.getAccountKey(context, args)!!
            val groupId = args.getString(EXTRA_GROUP_ID)
            val groupName = args.getString(EXTRA_GROUP_NAME)
            val result = ArrayList<String>()
            result.add(AUTHORITY_GROUP_TIMELINE)
            result.add("account=$accountKey")
            if (groupId != null) {
                result.add("group_id=$groupId")
            } else if (groupName != null) {
                result.add("group_name=$groupName")
            } else {
                return null
            }
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            val args = arguments!!
            val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
            val sb = StringBuilder("group_")
            if (tabPosition < 0) return null
            val groupId = args.getString(EXTRA_GROUP_ID)
            val groupName = args.getString(EXTRA_GROUP_NAME)
            if (groupId != null) {
                sb.append(groupId)
            } else if (groupName != null) {
                sb.append(groupName)
            }
            return sb.toString()
        }

    override val timelineType: String
        @TimelineType
        get() = TimelineType.OTHER

}
