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
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.app.hasRunningLoadersSafe
import androidx.loader.content.Loader
import android.view.View
import android.widget.AdapterView
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.SavedSearch
import org.mariotaku.twidere.adapter.SavedSearchesAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.loader.SavedSearchesLoader
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.SavedSearchDestroyedEvent
import org.mariotaku.twidere.util.IntentUtils.openTweetSearch
import java.util.*

class SavedSearchesListFragment : AbsContentListViewFragment<SavedSearchesAdapter>(),
        LoaderCallbacks<ResponseList<SavedSearch>?>, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    override var refreshing: Boolean
        get() = LoaderManager.getInstance(this).hasRunningLoadersSafe()
        set(value) {
            super.refreshing = value
        }

    val accountKey: UserKey
        get() = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this
        LoaderManager.getInstance(this).initLoader(0, null, this)
        showProgress()
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): SavedSearchesAdapter {
        return SavedSearchesAdapter(activity)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ResponseList<SavedSearch>?> {
        return SavedSearchesLoader(requireActivity(), accountKey)
    }

    override fun onItemLongClick(view: AdapterView<*>, child: View, position: Int, id: Long): Boolean {
        val item = adapter.findItem(id) ?: return false
        parentFragmentManager.let { DestroySavedSearchDialogFragment.show(it, accountKey, item.id, item.name) }
        return true
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val item = adapter.findItem(id) ?: return
        activity?.let { openTweetSearch(it, accountKey, item.query) }
    }

    override fun onLoaderReset(loader: Loader<ResponseList<SavedSearch>?>) {
        adapter.setData(null)
    }

    override fun onLoadFinished(loader: Loader<ResponseList<SavedSearch>?>, data: ResponseList<SavedSearch>?) {
        if (data != null) {
            Collections.sort(data, POSITION_COMPARATOR)
        }
        adapter.setData(data)
        showContent()
        refreshing = false
    }

    override fun onRefresh() {
        if (refreshing) return
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    @Subscribe
    fun onSavedSearchDestroyed(event: SavedSearchDestroyedEvent) {
        val adapter = adapter
        adapter.removeItem(event.accountKey, event.searchId)
    }

    companion object {

        private val POSITION_COMPARATOR = Comparator<SavedSearch> { object1, object2 -> object1.position - object2.position }
    }
}
