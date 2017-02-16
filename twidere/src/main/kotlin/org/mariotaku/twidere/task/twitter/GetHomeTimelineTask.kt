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
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.twitter.GetStatusesTask
import org.mariotaku.twidere.util.ErrorInfoStore

/**
 * Created by mariotaku on 16/2/11.
 */
class GetHomeTimelineTask(context: Context) : GetStatusesTask(context) {

    @Throws(MicroBlogException::class)
    override fun getStatuses(twitter: MicroBlog, paging: Paging): ResponseList<Status> {
        return twitter.getHomeTimeline(paging)
    }

    override val contentUri: Uri
        get() = Statuses.CONTENT_URI

    @TimelineType
    override val timelineType: String
        get() = TimelineType.HOME

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_HOME_TIMELINE

}
