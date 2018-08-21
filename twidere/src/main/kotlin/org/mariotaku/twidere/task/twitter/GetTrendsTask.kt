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
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Trends
import org.mariotaku.sqliteqb.library.Expression.and
import org.mariotaku.sqliteqb.library.Expression.equalsArgs
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.annotation.AccountType.FANFOU
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableTrend
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.TrendsRefreshedEvent
import org.mariotaku.twidere.model.util.AccountUtils.getAccountDetails
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.DebugLog.w
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert
import java.util.*

/**
 * Get local trends
 * Created by mariotaku on 16/2/24.
 */
class GetTrendsTask(
        context: Context
) : BaseAbstractTask<RefreshTaskParam, Unit, (Boolean) -> Unit>(context) {

    override fun doLongOperation(param: RefreshTaskParam) {
        val accountKeys = param.accountKeys.takeIf { it.isNotEmpty() }
        accountKeys?.map { accountKey ->
            var woeIds = arrayOf(param.extraId.toInt())
            if (param.extraId == (-1).toLong()) {
                val loaderWhere = equalsArgs(CachedTrends.ACCOUNT_KEY).sql + " GROUP BY " + CachedTrends.WOEID
                val loaderWhereArgs = arrayOf(accountKey.toString())
                val cursorResult = context.contentResolver.query(CachedTrends.Local.CONTENT_URI, CachedTrends.COLUMNS, loaderWhere, loaderWhereArgs, null)
                cursorResult.moveToFirst()
                while (cursorResult.moveToNext()) {
                    woeIds += cursorResult.getInt(0)
                }
                cursorResult.close()
            }

            val details = getAccountDetails(AccountManager.get(context), accountKey, true) ?: return
            val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)

            woeIds.map { woeId ->
                if (woeId > 0) {
                    try {
                        val trends = when (details.type) {
                            FANFOU -> twitter.fanfouTrends
                            else -> twitter.getLocationTrends(woeId).firstOrNull()
                        } ?: return
                        storeTrends(context.contentResolver, CachedTrends.Local.CONTENT_URI, trends, accountKey, woeId)
                    } catch (e: MicroBlogException) {
                        w(LOGTAG, tr = e)
                    }
                }
            }
        }
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, results: Unit?) {
        bus.post(TrendsRefreshedEvent())
    }

    private fun storeTrends(cr: ContentResolver, uri: Uri, trends: Trends, accountKey: UserKey, woeId: Int) {
        val hashtags = ArraySet<String>()
        val deleteWhere = and(equalsArgs(CachedTrends.ACCOUNT_KEY), equalsArgs(CachedTrends.WOEID)).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), woeId.toString())
        cr.delete(CachedTrends.Local.CONTENT_URI, deleteWhere, deleteWhereArgs)

        val allTrends = ArrayList<ParcelableTrend>()

        trends.trends.forEachIndexed { idx, trend ->
            val hashtag = trend.name.replaceFirst("#", "")
            hashtags.add(hashtag)
            allTrends.add(ParcelableTrend().apply {
                this.account_key = accountKey
                this.woe_id = woeId
                this.name = trend.name
                this.timestamp = System.currentTimeMillis()
                this.trend_order = idx
            })
        }
        val creator = ObjectCursor.valuesCreatorFrom(ParcelableTrend::class.java)
        bulkInsert(cr, uri, allTrends.map(creator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null, null)
        bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
    }
}
