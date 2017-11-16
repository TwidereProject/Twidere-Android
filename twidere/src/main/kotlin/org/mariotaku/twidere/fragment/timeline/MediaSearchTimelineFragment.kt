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
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.data.fetcher.MediaSearchTimelineFetcher
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.extension.local
import org.mariotaku.twidere.extension.query
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.SearchTimelineContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetMediaSearchTimelineTask

class MediaSearchTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int
        get() = FilterScope.SEARCH_RESULTS
    override val contentUri: Uri
        get() = Statuses.MediaSearchTimeline.CONTENT_URI.withAppendedPath(tabId)

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val task = GetMediaSearchTimelineTask(context!!)
        task.params = SearchTimelineContentRefreshParam(arguments!!.query, arguments!!.local, param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher(): StatusesFetcher {
        return MediaSearchTimelineFetcher(arguments!!.query)
    }

}
