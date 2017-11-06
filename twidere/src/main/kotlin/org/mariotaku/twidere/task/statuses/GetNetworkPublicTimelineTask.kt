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
import org.mariotaku.twidere.data.fetcher.NetworkPublicTimelineFetcher
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.ErrorInfoStore

class GetNetworkPublicTimelineTask(context: Context) : GetStatusesTask<ContentRefreshParam>(context) {

    override val contentUri: Uri = Statuses.NetworkPublic.CONTENT_URI

    override val filterScopes: Int = FilterScope.PUBLIC_TIMELINE

    override val errorInfoKey: String = ErrorInfoStore.KEY_NETWORK_PUBLIC_TIMELINE

    override fun getStatusesFetcher(params: ContentRefreshParam?) = NetworkPublicTimelineFetcher()

}