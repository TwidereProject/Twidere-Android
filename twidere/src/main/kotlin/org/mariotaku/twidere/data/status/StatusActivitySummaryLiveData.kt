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

package org.mariotaku.twidere.data.status

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import org.mariotaku.ktextension.addAllTo
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.data.ComputableExceptionLiveData
import org.mariotaku.twidere.exception.APINotSupportedException
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.model.api.key
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.updateStatusInfo

class StatusActivitySummaryLiveData(val context: Context) : ComputableExceptionLiveData<StatusActivitySummaryLiveData.StatusActivity>(false) {

    var accountKey: UserKey? = null
    var statusId: String? = null

    override fun compute(): StatusActivity {
        val context = this.context
        val accountKey = this.accountKey ?: throw RequiredFieldNotFoundException("account_key")
        val statusId = this.statusId ?: throw RequiredFieldNotFoundException("status_id")
        val account = AccountManager.get(context).getDetailsOrThrow(accountKey, true)
        if (AccountType.TWITTER != account.type) {
            throw APINotSupportedException()
        }
        val twitter = account.newMicroBlogInstance(context, Twitter::class.java)
        val activitySummary = if (account.isOfficial(context)) {
            twitter.getActivitySummaryOfficial(statusId, account)
        } else {
            twitter.getActivitySummary(statusId, account)
        }
        val countValues = ContentValues()
        countValues.put(TwidereDataStore.Statuses.REPLY_COUNT, activitySummary.replyCount)
        countValues.put(TwidereDataStore.Statuses.FAVORITE_COUNT, activitySummary.favoriteCount)
        countValues.put(TwidereDataStore.Statuses.RETWEET_COUNT, activitySummary.retweetCount)

        val cr = context.contentResolver
        val statusWhere = Expression.and(
                Expression.equalsArgs(TwidereDataStore.Statuses.ACCOUNT_KEY),
                Expression.or(
                        Expression.equalsArgs(TwidereDataStore.Statuses.ID),
                        Expression.equalsArgs(TwidereDataStore.Statuses.RETWEET_ID)))
        val statusWhereArgs = arrayOf(accountKey.toString(), statusId, statusId)
        cr.update(TwidereDataStore.Statuses.HomeTimeline.CONTENT_URI, countValues, statusWhere.sql, statusWhereArgs)
        cr.updateStatusInfo(DataStoreUtils.STATUSES_ACTIVITIES_URIS, TwidereDataStore.Statuses.COLUMNS,
                accountKey, statusId, ParcelableStatus::class.java) { item ->
            item.favorite_count = activitySummary.favoriteCount
            item.reply_count = activitySummary.replyCount
            item.retweet_count = activitySummary.retweetCount
            return@updateStatusInfo item
        }

        return activitySummary
    }

    private fun Twitter.getActivitySummary(statusId: String, account: AccountDetails): StatusActivity {
        val relatedUsers = getRetweets(statusId, Paging().count(10))
                .filterNot { DataStoreUtils.isFilteringUser(context, it.user.key) }
                .distinctBy { it.user.id }
                .map { it.user.toParcelable(account) }
        val result = StatusActivity(statusId, relatedUsers)

        val status = showStatus(statusId)
        result.favoriteCount = status.favoriteCount
        result.retweetCount = status.retweetCount
        result.replyCount = status.descendentReplyCount
        return result
    }

    private fun Twitter.getActivitySummaryOfficial(statusId: String, account: AccountDetails): StatusActivity {
        val summary = getStatusActivitySummary(statusId)
        val relatedIds = mutableSetOf<String>()
        summary.favoriters?.iDs?.let {
            it.slice(0 until (10.coerceAtMost(it.size)))
        }?.addAllTo(relatedIds)
        summary.retweeters?.iDs?.let {
            it.slice(0 until (10.coerceAtMost(it.size)))
        }?.addAllTo(relatedIds)

        val relatedUsers = lookupUsers(relatedIds.toTypedArray())
                .filterNot { DataStoreUtils.isFilteringUser(context, it.key) }
                .map { it.toParcelable(account) }
        val result = StatusActivity(statusId, relatedUsers)

        result.favoriteCount = summary.favoritersCount
        result.retweetCount = summary.retweetersCount
        result.replyCount = summary.descendentReplyCount
        return result
    }

    data class StatusActivity(
            var statusId: String,
            var relatedUsers: List<ParcelableUser>,
            var favoriteCount: Long = 0,
            var replyCount: Long = -1,
            var retweetCount: Long = 0
    ) {

        fun isStatus(status: ParcelableStatus): Boolean {
            return statusId == status.retweet_id ?: status.id
        }
    }

}