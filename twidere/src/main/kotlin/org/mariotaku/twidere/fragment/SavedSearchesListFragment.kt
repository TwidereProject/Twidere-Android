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
import android.support.v4.app.hasRunningLoadersSafe
import android.view.View
import android.widget.AdapterView
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.microblog.library.model.microblog.SavedSearch
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SavedSearchesAdapter
import org.mariotaku.twidere.data.SavedSearchesLiveData
import org.mariotaku.twidere.extension.accountKey
import org.mariotaku.twidere.extension.data.observe
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.SavedSearchDestroyedEvent
import org.mariotaku.twidere.singleton.BusSingleton
import org.mariotaku.twidere.util.IntentUtils.openTweetSearch
import java.util.*

class SavedSearchesListFragment : AbsContentListViewFragment<SavedSearchesAdapter>(),
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    override var refreshing: Boolean
        get() = loaderManager.hasRunningLoadersSafe()
        set(value) {
            super.refreshing = value
        }

    val accountKey: UserKey
        get() = arguments!!.accountKey!!

    private lateinit var savedSearchesLiveData: SavedSearchesLiveData
    private val positionComparator = Comparator<SavedSearch> { object1, object2 -> object1.position - object2.position }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.saved_searches)

        savedSearchesLiveData = SavedSearchesLiveData(activity!!, accountKey)

        listView.onItemClickListener = this
        listView.onItemLongClickListener = this

        savedSearchesLiveData.observe(this, success = { data ->
            adapter.setData(data)
            showContent()
            refreshing = false
        }, fail = { ex ->
            showError(R.drawable.ic_info_error_generic, ex.getErrorMessage(context!!))
            refreshing = false
        })

        savedSearchesLiveData.load()

        showProgress()
    }

    override fun onStop() {
        BusSingleton.unregister(this)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        BusSingleton.register(this)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): SavedSearchesAdapter {
        return SavedSearchesAdapter(activity)
    }

    override fun onItemLongClick(view: AdapterView<*>, child: View, position: Int, id: Long): Boolean {
        val item = adapter.findItem(id) ?: return false
        DestroySavedSearchDialogFragment.show(fragmentManager!!, accountKey, item.id, item.name)
        return true
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val item = adapter.findItem(id) ?: return
        openTweetSearch(activity!!, accountKey, item.query)
    }

    override fun onRefresh() {
        if (refreshing) return
        savedSearchesLiveData.load()
    }

    @Subscribe
    fun onSavedSearchDestroyed(event: SavedSearchDestroyedEvent) {
        val adapter = adapter
        adapter.removeItem(event.accountKey, event.searchId)
    }

}
