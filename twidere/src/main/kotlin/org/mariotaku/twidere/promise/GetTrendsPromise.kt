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

package org.mariotaku.twidere.promise

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.support.v4.util.ArraySet
import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.sqliteqb.library.Expression.and
import org.mariotaku.sqliteqb.library.Expression.equalsArgs
import org.mariotaku.twidere.annotation.AccountType.FANFOU
import org.mariotaku.twidere.dagger.component.PromisesComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.extension.getDetailsOrThrow
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ParcelableTrend
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.TrendsRefreshedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.content.ContentResolverUtils.bulkInsert
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import java.util.*
import javax.inject.Inject

/**
 * Get local trends
 * Created by mariotaku on 16/2/24.
 */
class GetTrendsPromise private constructor(private val application: Context) {
    @Inject
    lateinit var bus: Bus

    init {
        PromisesComponent.get(application).inject(this)
    }

    fun local(accountKey: UserKey, woeId: Int): Promise<Boolean, Exception> = task {
        val details = AccountManager.get(application).getDetailsOrThrow(accountKey, true)
        when (details.type) {
            FANFOU -> {
                val twitter = details.newMicroBlogInstance(application, cls = MicroBlog::class.java)
                return@task twitter.fanfouTrends
            }
            else -> {
                val twitter = details.newMicroBlogInstance(application, cls = MicroBlog::class.java)
                return@task twitter.getLocationTrends(woeId).first()
            }
        }
    }.then { trends ->
        val cr = application.contentResolver
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
        bulkInsert(cr, CachedTrends.Local.CONTENT_URI, allTrends.map(creator::create))
        ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, false,
                hashtags, null, null)
        bulkInsert(cr, CachedHashtags.CONTENT_URI, hashtags.map {
            val values = ContentValues()
            values.put(CachedHashtags.NAME, it)
            return@map values
        })
        return@then true
    }.successUi {
        bus.post(TrendsRefreshedEvent())
    }

    companion object : ApplicationContextSingletonHolder<GetTrendsPromise>(::GetTrendsPromise)
}