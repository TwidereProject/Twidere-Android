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

import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.loader.statuses.UserMentionsLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

class UserMentionsFragment : StatusesSearchFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.user_mentions)
    }

    override fun onCreateStatusesLoader(context: Context,
            args: Bundle,
            fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val makeGap = args.getBoolean(EXTRA_MAKE_GAP, true)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return UserMentionsLoader(activity, accountKey, screenName, adapterData,
                fromUser, makeGap, false, loadingMore)
    }

}
