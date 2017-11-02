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
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_SCREEN_NAME
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_KEY
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.data.fetcher.UserMentionsTimelineFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetUserMentionsTimelineTask

class UserMentionsTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int
        get() = FilterScope.SEARCH_RESULTS
    override val contentUri: Uri
        get() = Statuses.UserMentions.CONTENT_URI.withAppendedPath(tabId)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.user_mentions)
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val task = GetUserMentionsTimelineTask(context)
        task.params = UserRelatedContentRefreshParam(arguments.getParcelable(EXTRA_USER_KEY),
                arguments.getString(EXTRA_SCREEN_NAME), param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher(): StatusesFetcher {
        return UserMentionsTimelineFetcher(arguments.getParcelable(EXTRA_USER_KEY),
                arguments.getString(EXTRA_SCREEN_NAME))
    }

}