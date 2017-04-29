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
import android.support.v4.util.ArraySet
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.InternalActivityCreator
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.extension.api.batchGetRelationships
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.fragment.InteractionsTimelineFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.sync.TimelineSyncManager

/**
 * Created by mariotaku on 16/2/11.
 */
class GetActivitiesAboutMeTask(context: Context) : GetActivitiesTask(context) {

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_INTERACTIONS

    override val contentUri: Uri
        get() = Activities.AboutMe.CONTENT_URI


    private val profileImageSize = context.getString(R.string.profile_image_size)

    @Throws(MicroBlogException::class)
    override fun getActivities(account: AccountDetails, paging: Paging): List<ParcelableActivity> {
        when (account.type) {
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                val notifications = mastodon.getNotifications(paging)
                val allUsers = notifications.flatMap {
                    val user = it.account
                    val statusUser = it.status?.account
                    return@flatMap when {
                        user != null && statusUser != null -> listOf(user, statusUser)
                        user != null -> listOf(user)
                        statusUser != null -> listOf(statusUser)
                        else -> emptyList()
                    }
                }
                val userIds = allUsers.mapTo(ArraySet<String>()) { it.id }
                val relationships = mastodon.batchGetRelationships(userIds)
                val activities = notifications.map {
                    it.toParcelable(account, relationships)
                }
                return activities
            }
            AccountType.TWITTER -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                if (account.isOfficial(context)) {
                    return microBlog.getActivitiesAboutMe(paging).map {
                        it.toParcelable(account, profileImageSize = profileImageSize)
                    }
                }
                return microBlog.getMentionsTimeline(paging).map {
                    InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                            profileImageSize = profileImageSize)
                }
            }
            AccountType.FANFOU -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getMentions(paging).map {
                    InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                            profileImageSize = profileImageSize)
                }
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                return microBlog.getMentionsTimeline(paging).map {
                    InternalActivityCreator.status(it, account.key.id).toParcelable(account,
                            profileImageSize = profileImageSize)
                }
            }
        }
    }

    override fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>) {
        val tag = InteractionsTimelineFragment.getTimelineSyncTag(accountKeys)
        manager.fetchSingle(ReadPositionTag.ACTIVITIES_ABOUT_ME, tag)
    }
}
