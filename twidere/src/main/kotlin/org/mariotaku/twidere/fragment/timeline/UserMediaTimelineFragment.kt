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

import android.net.Uri
import android.os.Bundle
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.data.fetcher.UserMediaTimelineFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetUserMediaTimelineTask

class UserMediaTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int = FilterScope.USER_TIMELINE

    override val contentUri: Uri = Statuses.UserTimeline.CONTENT_URI

    override val timelineStyle: Int = TimelineStyle.GALLERY

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_media_timeline)
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val userKey = arguments.getParcelable<UserKey>(IntentConstants.EXTRA_USER_KEY) ?: return false
        val userScreenName = arguments.getString(IntentConstants.EXTRA_SCREEN_NAME) ?: return false
        val task = GetUserMediaTimelineTask(context)
        task.params = UserRelatedContentRefreshParam(userKey, userScreenName, param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher(): StatusesFetcher {
        return UserMediaTimelineFetcher(arguments.getParcelable(IntentConstants.EXTRA_USER_KEY),
                arguments.getString(IntentConstants.EXTRA_SCREEN_NAME))
    }

}