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
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.fragment.HomeTimelineFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.ErrorInfoStore
import java.io.IOException

/**
 * Created by mariotaku on 16/2/11.
 */
class GetHomeTimelineTask(context: Context) : GetStatusesTask(context) {

    override val contentUri: Uri
        get() = Statuses.CONTENT_URI

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_HOME_TIMELINE

    private val profileImageSize = context.getString(R.string.profile_image_size)

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus> {
        when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                return mastodon.getHomeTimeline(paging).map {
                    it.toParcelable(account)
                }
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getHomeTimeline(paging).map {
                    it.toParcelable(account, profileImageSize)
                }
            }
        }
    }

    override fun syncFetchReadPosition(accountKeys: Array<UserKey>) {
        val manager = timelineSyncManagerFactory.get() ?: return
        val tag = HomeTimelineFragment.getTimelineSyncTag(accountKeys)
        try {
            manager.blockingGetPosition(ReadPositionTag.ACTIVITIES_ABOUT_ME, tag)
        } catch (e: IOException) {
            return
        }
    }
}
