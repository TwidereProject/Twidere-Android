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

package org.mariotaku.twidere.fragment.timeline

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.userTimelineFilterKey
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.data.fetcher.UserTimelineFetcher
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.timeline.UserTimelineFilter
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetUserTimelineTask

class UserTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int = FilterScope.USER_TIMELINE

    override val contentUri: Uri
        get() = Statuses.UserTimeline.CONTENT_URI.withAppendedPath(tabId)

    override val timelineFilter: TimelineFilter?
        get() = if (arguments.getBoolean(EXTRA_ENABLE_TIMELINE_FILTER)) preferences[userTimelineFilterKey] else null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_statuses)
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val task = GetUserTimelineTask(context)
        task.params = UserRelatedContentRefreshParam(arguments.getParcelable(EXTRA_USER_KEY),
                arguments.getString(EXTRA_SCREEN_NAME), param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher(): StatusesFetcher {
        return UserTimelineFetcher(arguments.getParcelable(EXTRA_USER_KEY),
                arguments.getString(EXTRA_SCREEN_NAME), arguments.getString(EXTRA_PROFILE_URL))
    }

    override fun onTimelineFilterClick() {
        val df = UserTimelineFilterDialogFragment()
        df.setTargetFragment(this, REQUEST_SET_TIMELINE_FILTER)
        df.show(fragmentManager, "set_timeline_filter")
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
                (targetFragment as UserTimelineFragment).reloadAll()
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