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
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_SCREEN_NAME
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER_KEY
import org.mariotaku.twidere.data.fetcher.UserFavoritesFetcher
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.UserRelatedContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetUserFavoritesTask

class FavoritesTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int = FilterScope.HOME

    override val contentUri: Uri = Statuses.CONTENT_URI

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val userKey = arguments.getParcelable<UserKey>(EXTRA_USER_KEY) ?: return false
        val userScreenName = arguments.getString(EXTRA_SCREEN_NAME) ?: return false
        val task = GetUserFavoritesTask(context)
        task.params = UserRelatedContentRefreshParam(userKey, userScreenName, param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher(): UserFavoritesFetcher {
        return UserFavoritesFetcher(arguments.getParcelable<UserKey>(EXTRA_USER_KEY),
                arguments.getString(EXTRA_SCREEN_NAME))
    }
}