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

package org.mariotaku.twidere.fragment.activities

import android.net.Uri
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.data.CursorObjectLivePagedListProvider
import org.mariotaku.twidere.data.predicate.ParcelableActivityProcessor
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Activities

class InteractionsActivitiesFragment : AbsActivitiesFragment() {
    override val filterScope: Int = FilterScope.INTERACTIONS
    override val contentUri: Uri = Activities.AboutMe.CONTENT_URI

    override fun getActivities(param: ContentRefreshParam): Boolean {
        twitterWrapper.getActivitiesAboutMeAsync(param)
        return true
    }

    override fun onCreateCursorObjectProcessor(): CursorObjectLivePagedListProvider.CursorObjectProcessor<ParcelableActivity> {
        val extras: InteractionsTabExtras? = arguments.getParcelable(EXTRA_EXTRAS)
        var followingOnly = false
        var mentionsOnly = false
        if (extras != null) {
            followingOnly = extras.isMyFollowingOnly
            mentionsOnly = extras.isMentionsOnly
        }
        return ParcelableActivityProcessor(filterScope, followingOnly, mentionsOnly)
    }

    companion object {

        fun getTimelineSyncTag(accountKeys: Array<UserKey>): String {
            return "${ReadPositionTag.ACTIVITIES_ABOUT_ME}_${accountKeys.sorted().joinToString(",")}"
        }

    }
}
