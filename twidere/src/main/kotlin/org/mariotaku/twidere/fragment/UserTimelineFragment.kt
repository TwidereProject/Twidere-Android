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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.constant.userTimelineFilterKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.loader.UserTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.timeline.UserTimelineFilter
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.TimelineFilterHeaderViewHolder
import java.util.*

/**
 * Created by mariotaku on 14/12/2.
 */
class UserTimelineFragment : ParcelableStatusesFragment() {

    override val timelineType: String = TimelineType.USER

    val pinnedStatusIds: Array<String>?
        get() = (parentFragment as? UserTimelineFragmentDelegate)?.pinnedStatusIds

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val accountKey = Utils.getAccountKey(context, arguments)!!
            val userKey = arguments.getParcelable<UserKey>(EXTRA_USER_KEY)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            val result = ArrayList<String>()
            result.add(AUTHORITY_USER_TIMELINE)
            result.add("account=$accountKey")
            if (userKey != null) {
                result.add("user_id=$userKey")
            } else if (screenName != null) {
                result.add("screen_name=$screenName")
            } else {
                return null
            }
            (timelineFilter as? UserTimelineFilter)?.let {
                if (it.isIncludeReplies) {
                    result.add("include_replies")
                }
                if (it.isIncludeRetweets) {
                    result.add("include_retweets")
                }
            }
            return result.toTypedArray()
        }

    override val readPositionTagWithArguments: String?
        get() {
            if (arguments.getLong(EXTRA_TAB_ID, -1) < 0) return null
            val sb = StringBuilder("user_timeline_")

            val userKey = arguments.getParcelable<UserKey>(EXTRA_USER_KEY)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            if (userKey != null) {
                sb.append(userKey)
            } else if (screenName != null) {
                sb.append(screenName)
            } else {
                return null
            }
            return sb.toString()
        }

    override val enableTimelineFilter: Boolean
        get() = arguments.getBoolean(EXTRA_ENABLE_TIMELINE_FILTER)

    override val timelineFilter: TimelineFilter?
        get() = if (enableTimelineFilter) preferences[userTimelineFilterKey] else null

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        refreshing = true
        val data = adapterData
        val accountKey = Utils.getAccountKey(context, args)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val profileUrl = args.getString(EXTRA_PROFILE_URL)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        val pinnedIds = if (adapter.hasPinnedStatuses) null else pinnedStatusIds
        return UserTimelineLoader(context, accountKey, userKey, screenName, profileUrl, sinceId,
                maxId, data, savedStatusesFileArgs, tabPosition, fromUser, loadingMore, pinnedIds,
                timelineFilter as? UserTimelineFilter)
    }

    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        val timelineLoader = loader as? UserTimelineLoader
        if (!adapter.hasPinnedStatuses) {
            adapter.pinnedStatuses = timelineLoader?.pinnedStatuses
        }
        super.onStatusesLoaded(loader, data)
    }

    override fun onFilterClick(holder: TimelineFilterHeaderViewHolder) {
        val df = UserTimelineFilterDialogFragment()
        df.setTargetFragment(this, REQUEST_SET_TIMELINE_FILTER)
        df.show(childFragmentManager, "set_timeline_filter")
    }

    private fun reloadAllStatuses() {
        adapterData = null
        triggerRefresh()
        showProgress()
    }

    interface UserTimelineFragmentDelegate {
        val pinnedStatusIds: Array<String>?

    }

    class UserTimelineFilterDialogFragment : BaseDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            val values = resources.getStringArray(R.array.values_user_timeline_filter)
            val checkedItems = BooleanArray(values.size) {
                val filter = preferences[userTimelineFilterKey]
                when (values[it]) {
                    "replies" -> filter.isIncludeReplies
                    "retweets" -> filter.isIncludeRetweets
                    else -> false
                }
            }
            builder.setTitle(R.string.title_user_timeline_filter)
            builder.setMultiChoiceItems(R.array.entries_user_timeline_filter, checkedItems, null)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog as AlertDialog
                val listView = dialog.listView
                val filter = UserTimelineFilter().apply {
                    isIncludeRetweets = listView.isItemChecked(values.indexOf("retweets"))
                    isIncludeReplies = listView.isItemChecked(values.indexOf("replies"))
                }
                preferences.edit().apply {
                    this[userTimelineFilterKey] = filter
                }.apply()
                (targetFragment as UserTimelineFragment).reloadAllStatuses()
            }
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }

    }

    companion object {
        const val EXTRA_ENABLE_TIMELINE_FILTER = "enable_timeline_filter"
        const val REQUEST_SET_TIMELINE_FILTER = 101
    }
}
