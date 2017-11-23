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
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.data.fetcher.UserFavoritesFetcher
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.adapter.removeStatuses
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetUserFavoritesTask

class FavoritesTimelineFragment : AbsTimelineFragment() {

    override val filterScope: Int = FilterScope.HOME

    override val contentUri: Uri
        get() = Statuses.Favorites.CONTENT_URI.withAppendedPath(tabId)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = if (preferences[iWantMyStarsBackKey]) {
            getString(R.string.title_favorites)
        } else {
            getString(R.string.title_likes)
        }
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val task = GetUserFavoritesTask(context!!)
        task.params = UserRelatedContentRefreshParam(arguments!!.userKey, arguments!!.screenName,
                param)
        task.promise()
        return true
    }

    override fun onCreateStatusesFetcher(): UserFavoritesFetcher {
        return UserFavoritesFetcher(arguments!!.userKey, arguments!!.screenName)
    }

    override fun onFavoriteTaskEvent(event: FavoriteTaskEvent) {
        if (!isStandalone) return
        if (event.action != FavoriteTaskEvent.Action.DESTROY && !event.isSucceeded) return
        adapter.removeStatuses { it != null && it.id == event.statusId }
    }
}