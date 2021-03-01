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
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.loader.app.LoaderManager
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.activity.QuickSearchBarActivity
import org.mariotaku.twidere.adapter.TrendsAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.constant.localTrendsWoeIdKey
import org.mariotaku.twidere.fragment.iface.IFloatingActionButtonFragment
import org.mariotaku.twidere.fragment.iface.IFloatingActionButtonFragment.ActionInfo
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.TrendsRefreshedEvent
import org.mariotaku.twidere.model.tab.extra.TrendsTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends
import org.mariotaku.twidere.util.IntentUtils.openTweetSearch
import org.mariotaku.twidere.util.Utils

class TrendsSuggestionsFragment : AbsContentListViewFragment<TrendsAdapter>(), LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, IFloatingActionButtonFragment {

    private val tabExtras: TrendsTabExtras?
        get() = arguments?.getParcelable(EXTRA_EXTRAS)

    private val accountKey: UserKey? get() {
        return context?.let { Utils.getAccountKeys(it, arguments)?.firstOrNull() }
                ?: context?.let { Utils.getDefaultAccountKey(it) }
    }

    private val woeId: Int get() {
        val id = tabExtras?.woeId ?: 0
        return if (id > 0) id else preferences[localTrendsWoeIdKey]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView.onItemClickListener = this
        LoaderManager.getInstance(this).initLoader(0, null, this)
        showProgress()
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): TrendsAdapter {
        return TrendsAdapter(requireActivity())
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val uri = CachedTrends.Local.CONTENT_URI
        val loaderWhere = Expression.and(Expression.equalsArgs(CachedTrends.ACCOUNT_KEY),
                Expression.equalsArgs(CachedTrends.WOEID)).sql
        val loaderWhereArgs = arrayOf(accountKey?.toString().orEmpty(), woeId.toString())
        return CursorLoader(requireActivity(), uri, CachedTrends.COLUMNS, loaderWhere, loaderWhereArgs, CachedTrends.TREND_ORDER)
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        if (multiSelectManager.isActive) return
        val trend: String = (if (view is ListView) {
            adapter.getItem(position - view.headerViewsCount)
        } else {
            adapter.getItem(position)

        })
            ?: return
        activity?.let { openTweetSearch(it, accountKey, trend) }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.swapCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        adapter.swapCursor(cursor)
        if (adapter.isEmpty) {
            showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
        } else {
            showContent()
        }
    }

    override fun onRefresh() {
        if (refreshing) return
        val accountKey = this.accountKey ?: return
        twitterWrapper.getLocalTrendsAsync(accountKey, woeId)
    }

    override var refreshing: Boolean
        get() = false
        set(value) {
            super.refreshing = value
        }

    override fun onStart() {
        super.onStart()
        LoaderManager.getInstance(this).restartLoader(0, null, this)
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

    override fun getActionInfo(tag: String): ActionInfo? {
        when (tag) {
            "home" -> {
                return ActionInfo(R.drawable.ic_action_search, getString(R.string.action_search))
            }
        }
        return null
    }

    override fun onActionClick(tag: String): Boolean {
        val intent = Intent(activity, QuickSearchBarActivity::class.java)
        intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        startActivity(intent)
        return true
    }
}
