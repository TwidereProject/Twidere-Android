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

package org.mariotaku.twidere.task.statuses

import android.content.Context
import android.net.Uri
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.data.fetcher.ListTimelineFetcher
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.model.refresh.ListTimelineContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.ErrorInfoStore

class GetListTimelineTask(context: Context) : GetStatusesTask<ListTimelineContentRefreshParam>(context) {

    override val contentUri: Uri
        get() = Statuses.ListTimeline.CONTENT_URI.withAppendedPath(params.tabId)

    override val filterScopes: Int = FilterScope.LIST_GROUP_TIMELINE

    override val errorInfoKey: String = ErrorInfoStore.KEY_LIST_GROUP_TIMELINE

    override fun getStatusesFetcher(params: ListTimelineContentRefreshParam?): StatusesFetcher {
        return ListTimelineFetcher(params?.id, params?.slug, params?.ownerId, params?.ownerScreenName)
    }

}