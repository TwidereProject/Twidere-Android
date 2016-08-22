/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.twidere.adapter.TrendsAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_LOCAL_TRENDS_WOEID
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.TrendsRefreshedEvent
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.util.DataStoreUtils.getTableNameByUri
import org.mariotaku.twidere.util.IntentUtils.openTweetSearch
import org.mariotaku.twidere.util.Utils.getDefaultAccountKey

class TrendsSuggestionsFragment : AbsContentListViewFragment<TrendsAdapter>(), LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private var accountId: UserKey? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        accountId = getDefaultAccountKey(activity)
        listView.onItemClickListener = this
        loaderManager.initLoader(0, null, this)
        showProgress()
    }

    override fun onCreateAdapter(context: Context): TrendsAdapter {
        return TrendsAdapter(activity)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val uri = CachedTrends.Local.CONTENT_URI
        val table = getTableNameByUri(uri)
        val where: String?
        if (table != null) {
            val sqlSelectQuery = SQLQueryBuilder.select(Columns.Column(CachedTrends.TIMESTAMP))
                    .from(Table(table))
                    .orderBy(OrderBy(CachedTrends.TIMESTAMP, false))
                    .limit(1)
                    .build()
            where = Expression.equals(Columns.Column(CachedTrends.TIMESTAMP), sqlSelectQuery).sql
        } else {
            where = null
        }
        return CursorLoader(activity, uri, CachedTrends.COLUMNS, where, null, null)
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        if (multiSelectManager.isActive) return
        val trend: String?
        if (view is ListView) {
            trend = adapter!!.getItem(position - view.headerViewsCount)
        } else {
            trend = adapter!!.getItem(position)

        }
        if (trend == null) return
        openTweetSearch(activity, accountId, trend)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter!!.swapCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        adapter!!.swapCursor(cursor)
        showContent()
    }

    override fun onRefresh() {
        if (refreshing) return
        twitterWrapper.getLocalTrendsAsync(accountId, preferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1))
    }

    override var refreshing: Boolean
        get() = false
        set(value) {
            super.refreshing = value
        }

    override fun onStart() {
        super.onStart()
        loaderManager.restartLoader(0, null, this)
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    @Subscribe
    fun onTrendsRefreshedEvent(event: TrendsRefreshedEvent) {
        refreshing = false
    }

}
