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
import android.os.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.data.predicate.ParcelableActivityProcessor
import org.mariotaku.twidere.data.processor.DataSourceItemProcessor
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.promise
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.task.twitter.GetActivitiesAboutMeTask

class InteractionsActivitiesFragment : AbsActivitiesFragment() {

    override val filterScope: Int = FilterScope.INTERACTIONS

    override val contentUri: Uri = Activities.AboutMe.CONTENT_URI

    override val readPositionTag: String? = ReadPositionTag.ACTIVITIES_ABOUT_ME

    override val isStandalone: Boolean = false
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_interactions)
    }

    override fun getActivities(param: ContentRefreshParam): Boolean {
        val task = GetActivitiesAboutMeTask(context!!)
        task.params = param
        task.promise()
        return true
    }

    override fun onCreateCursorObjectProcessor(): DataSourceItemProcessor<ParcelableActivity> {
        val extras: InteractionsTabExtras? = arguments!!.getParcelable(EXTRA_EXTRAS)
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
