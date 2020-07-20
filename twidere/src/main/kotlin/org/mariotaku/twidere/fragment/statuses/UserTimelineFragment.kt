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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.loader.content.Loader
import androidx.appcompat.app.AlertDialog
import com.squareup.otto.Subscribe
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.constant.userTimelineFilterKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ParcelableStatusesFragment
import org.mariotaku.twidere.loader.statuses.UserTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.StatusPinEvent
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.timeline.UserTimelineFilter
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.TimelineFilterHeaderViewHolder
import java.util.*

/**
 * User timeline
 *
 * Created by mariotaku on 14/12/2.
 */
class UserTimelineFragment : ParcelableStatusesFragment() {

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val arguments = arguments ?: return null
            val context = context ?: return null
            val accountKey = Utils.getAccountKey(context, arguments)
            val userKey = arguments.getParcelable<UserKey?>(EXTRA_USER_KEY)
            val screenName = arguments.getString(EXTRA_SCREEN_NAME)
            val result = ArrayList<String>()
            result.add(AUTHORITY_USER_TIMELINE)
            result.add("account=$accountKey")
            when {
                userKey != null -> {
                    result.add("user_id=$userKey")
                }
                screenName != null -> {
                    result.add("screen_name=$screenName")
                }
                else -> {
                    return null
                }
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
            val arguments = arguments ?: return null
            if (arguments.getLong(EXTRA_TAB_ID, -1) < 0) return null
            val sb = StringBuilder("user_timeline_")

            val userKey = arguments.getParcelable<UserKey>(EXTRA_USER_KEY)
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
            return sb.toString()
        }

    override val enableTimelineFilter: Boolean
        get() = arguments?.getBoolean(EXTRA_ENABLE_TIMELINE_FILTER) ?: false

    override val timelineFilter: TimelineFilter?
        get() = if (enableTimelineFilter) preferences[userTimelineFilterKey] else null

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        refreshing = true
        val data = adapterData
        val accountKey = Utils.getAccountKey(context, args)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val profileUrl = args.getString(EXTRA_PROFILE_URL)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        val loadPinnedStatus = args.getBoolean(EXTRA_LOAD_PINNED_STATUS, false)
        return UserTimelineLoader(context, accountKey, userKey, screenName, profileUrl, data,
                savedStatusesFileArgs, tabPosition, fromUser, loadingMore, loadPinnedStatus,
                timelineFilter as? UserTimelineFilter)
    }

    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        loader as UserTimelineLoader
        if (loader.loadPinnedStatus) {
            adapter.pinnedStatuses = loader.pinnedStatuses
        }
        super.onStatusesLoaded(loader, data)
    }

    override fun onFilterClick(holder: TimelineFilterHeaderViewHolder) {
        val df = UserTimelineFilterDialogFragment()
        df.setTargetFragment(this, REQUEST_SET_TIMELINE_FILTER)
        parentFragmentManager.let { df.show(it, "set_timeline_filter") }
    }

    @Subscribe
    fun onStatusPinEvent(event: StatusPinEvent) {
        val userKey = arguments?.getParcelable<UserKey?>(EXTRA_USER_KEY) ?: return
        if (event.userKey != userKey) return
        triggerRefresh()
    }

    private fun reloadAllStatuses() {
        adapterData = null
        triggerRefresh()
        showProgress()
    }

    class UserTimelineFilterDialogFragment : BaseDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
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
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }

    companion object {
        const val EXTRA_ENABLE_TIMELINE_FILTER = "enable_timeline_filter"
        const val EXTRA_LOAD_PINNED_STATUS = "load_pinned_status"
        const val REQUEST_SET_TIMELINE_FILTER = 101
    }
}
