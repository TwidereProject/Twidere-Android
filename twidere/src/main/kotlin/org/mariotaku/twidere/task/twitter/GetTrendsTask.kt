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

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.support.v4.util.ArraySet
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Trends
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableTrend
import org.mariotaku.twidere.model.ParcelableTrendValuesCreator
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.TrendsRefreshedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*

/**
 * Created by mariotaku on 16/2/24.
 */
class GetTrendsTask(
        context: android.content.Context,
        private val accountKey: org.mariotaku.twidere.model.UserKey,
        private val woeId: Int
) : org.mariotaku.twidere.task.BaseAbstractTask<Any?, Unit, Any?>(context) {

    override fun doLongOperation(param: Any?) {
        val details = org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails(android.accounts.AccountManager.get(context), accountKey, true) ?: return
        val twitter = details.newMicroBlogInstance(context, cls = org.mariotaku.microblog.library.MicroBlog::class.java)
        try {
            val trends = when {
                details.type == org.mariotaku.twidere.annotation.AccountType.FANFOU -> twitter.fanfouTrends
                else -> twitter.getLocationTrends(woeId).firstOrNull()
            } ?: return
            storeTrends(context.contentResolver, org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends.Local.CONTENT_URI, trends)
        } catch (e: org.mariotaku.microblog.library.MicroBlogException) {
            org.mariotaku.twidere.util.DebugLog.w(LOGTAG, tr = e)
        }
    }

    override fun afterExecute(handler: Any?, result: Unit) {
        bus.post(org.mariotaku.twidere.model.event.TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: android.content.ContentResolver, uri: android.net.Uri, trends: org.mariotaku.microblog.library.twitter.model.Trends) {
        val hashtags = android.support.v4.util.ArraySet<String>()
        val deleteWhere = org.mariotaku.sqliteqb.library.Expression.and(org.mariotaku.sqliteqb.library.Expression.equalsArgs(org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends.ACCOUNT_KEY),
                org.mariotaku.sqliteqb.library.Expression.equalsArgs(org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)

        val allTrends = java.util.ArrayList<org.mariotaku.twidere.model.ParcelableTrend>()

        trends.trends.forEachIndexed { idx, trend ->
            val hashtag = trend.name.replaceFirst("#", "")
            hashtags.add(hashtag)
            allTrends.add(org.mariotaku.twidere.model.ParcelableTrend().apply {
                this.account_key = accountKey
                this.woe_id = woeId
                this.name = trend.name
                this.timestamp = System.currentTimeMillis()
                this.trend_order = idx
            })
        }
        org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert(cr, uri, allTrends.map(org.mariotaku.twidere.model.ParcelableTrendValuesCreator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null, null)
        ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
    }
}
