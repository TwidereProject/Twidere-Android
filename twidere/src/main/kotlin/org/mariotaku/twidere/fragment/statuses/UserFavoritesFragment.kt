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

import android.os.Bundle
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_USER_KEY
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.fragment.CursorStatusesFragment
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.refresh.RefreshTaskParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.ErrorInfoStore

/**
 * Created by mariotaku on 14/12/2.
 */
class UserFavoritesFragment : CursorStatusesFragment() {

    override val errorInfoKey = ErrorInfoStore.KEY_PUBLIC_TIMELINE

    override val contentUri = Statuses.Favorites.CONTENT_URI.withAppendedPath(tabId)

    override val notificationType = 0

    override val isFilterEnabled = true

    override val readPositionTag = ReadPositionTag.PUBLIC_TIMELINE

    override val timelineSyncTag: String?
        get() = PublicTimelineFragment.getTimelineSyncTag(accountKeys)

    override val filterScopes: Int
        get() = FilterScope.PUBLIC_TIMELINE

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = if (preferences[iWantMyStarsBackKey]) {
            getString(R.string.title_favorites)
        } else {
            getString(R.string.title_likes)
        }
    }

    override fun updateRefreshState() {
        val twitter = twitterWrapper
        refreshing = twitter.isStatusTimelineRefreshing(contentUri)
    }


    override fun getStatuses(param: RefreshTaskParam): Boolean {
//        val task = GetUserFavoritesTask(context)
//        task.params = param
//        TaskStarter.execute(task)
        return true
    }

     fun notifyFavoriteTask(event: FavoriteTaskEvent) {
        if (event.action == FavoriteTaskEvent.Action.DESTROY && event.isSucceeded) {
            event.status?.let { status ->
                val args = arguments!!
                val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
                if (status.account_key == userKey) {
//                    removeStatus(event.statusId)
                    triggerRefresh()
                    return
                }
            }
        }
//        super.notifyFavoriteTask(event)
    }

}
