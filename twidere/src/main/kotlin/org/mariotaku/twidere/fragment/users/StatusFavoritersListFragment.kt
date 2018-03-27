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

package org.mariotaku.twidere.fragment.users

import android.content.Context
import android.os.Bundle
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.statusId
import org.mariotaku.twidere.fragment.AbsUsersFragment
import org.mariotaku.twidere.data.fetcher.users.StatusFavoritersFetcher
import org.mariotaku.twidere.singleton.PreferencesSingleton

class StatusFavoritersListFragment : AbsUsersFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = if (PreferencesSingleton.get(context!!)[iWantMyStarsBackKey]) {
            getString(R.string.title_users_favorited_this)
        } else {
            getString(R.string.title_users_liked_this)
        }
    }

    override fun onCreateUsersFetcher(): UsersFetcher {
        val args = arguments!!
        val statusId = args.statusId!!
        return StatusFavoritersFetcher(context!!, statusId)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableUsersAdapter {
        return super.onCreateAdapter(context, requestManager).apply {
            loadMoreSupportedPosition = LoadMorePosition.NONE
        }
    }

}
