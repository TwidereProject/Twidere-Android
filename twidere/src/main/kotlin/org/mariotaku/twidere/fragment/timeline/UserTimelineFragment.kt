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

import android.arch.paging.NullPaddedList
import android.arch.paging.rawList
import android.net.Uri
import android.os.Bundle
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.userTimelineFilterKey
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.data.fetcher.UserTimelineFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetUserTimelineTask

class UserTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int = FilterScope.USER_TIMELINE

    override val contentUri: Uri = Statuses.UserTimeline.CONTENT_URI

    override val timelineFilter: TimelineFilter?
        get() = if (arguments.getBoolean(EXTRA_ENABLE_TIMELINE_FILTER)) preferences[userTimelineFilterKey] else null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_statuses)
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val userKey = arguments.getParcelable<UserKey>(IntentConstants.EXTRA_USER_KEY) ?: return false
        val userScreenName = arguments.getString(IntentConstants.EXTRA_SCREEN_NAME) ?: return false
        val task = GetUserTimelineTask(context)
        task.params = UserRelatedContentRefreshParam(userKey, userScreenName, param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher(): StatusesFetcher {
        return UserTimelineFetcher(arguments.getParcelable(IntentConstants.EXTRA_USER_KEY),
                arguments.getString(IntentConstants.EXTRA_SCREEN_NAME))
    }

    override fun onFavoriteTaskEvent(event: FavoriteTaskEvent) {
        if (!event.isSucceeded) return
        val statuses = adapter.statuses as? NullPaddedList ?: return
        // FIXME: Dirty hack, may break in future versions
        if (statuses.rawList.removeAll { it != null && it.id == event.statusId }) {
            adapter.notifyDataSetChanged()
        }
    }


    companion object {
        const val EXTRA_ENABLE_TIMELINE_FILTER = "enable_timeline_filter"
        const val EXTRA_LOAD_PINNED_STATUS = "load_pinned_status"
        const val REQUEST_SET_TIMELINE_FILTER = 101
    }
}