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

package org.mariotaku.twidere.task.twitter

import android.content.Context
import android.net.Uri
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.data.fetcher.ActivitiesFetcher
import org.mariotaku.twidere.data.fetcher.activities.ActivitiesAboutMeFetcher
import org.mariotaku.twidere.fragment.activities.InteractionsActivitiesFragment
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.sync.TimelineSyncManager

class GetActivitiesAboutMeTask(context: Context) : GetActivitiesTask(context) {

    override val errorInfoKey: String = ErrorInfoStore.KEY_INTERACTIONS
    override val filterScopes: Int = FilterScope.INTERACTIONS
    override val contentUri: Uri = Activities.AboutMe.CONTENT_URI

    override fun getActivitiesFetcher(params: ContentRefreshParam?): ActivitiesFetcher {
        return ActivitiesAboutMeFetcher()
    }

    override fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>) {
        val tag = InteractionsActivitiesFragment.getTimelineSyncTag(accountKeys)
        manager.fetchSingle(ReadPositionTag.ACTIVITIES_ABOUT_ME, tag)
    }
}
