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
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.Utils
import java.io.IOException

/**
 * Created by mariotaku on 16/2/11.
 */
class GetHomeTimelineTask(context: Context) : GetStatusesTask(context) {

    override val contentUri: Uri
        get() = Statuses.CONTENT_URI

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_HOME_TIMELINE

    @Throws(MicroBlogException::class)
    override fun getStatuses(twitter: MicroBlog, paging: Paging): ResponseList<Status> {
        return twitter.getHomeTimeline(paging)
    }

    override fun setLocalReadPosition(accountKey: UserKey, details: AccountDetails, twitter: MicroBlog) {
        val syncManager = timelineSyncManagerFactory.get() ?: return
        try {
            val tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.HOME_TIMELINE, accountKey)
            syncManager.blockingGetPosition(ReadPositionTag.HOME_TIMELINE, tag)
        }catch (e: IOException) {

        }
    }
}
