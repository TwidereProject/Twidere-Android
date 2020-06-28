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
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.AbsMediaStatusesFragment
import org.mariotaku.twidere.loader.statuses.MediaTimelineLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 14/11/5.
 */
class UserMediaTimelineFragment : AbsMediaStatusesFragment() {

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableStatus>?> {
        val accountKey = args.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return MediaTimelineLoader(context, accountKey, userKey, screenName, adapter.getData(),
                null, tabPosition, fromUser, loadingMore)
    }


    override fun getStatuses(maxId: String?, sinceId: String?): Int {
        if (context == null) return -1
        val args = Bundle(arguments)
        args.putBoolean(EXTRA_MAKE_GAP, false)
        args.putString(EXTRA_MAX_ID, maxId)
        args.putString(EXTRA_SINCE_ID, sinceId)
        args.putBoolean(EXTRA_FROM_USER, true)
        LoaderManager.getInstance(this).restartLoader(loaderId, args, this)
        return 0
    }

}
