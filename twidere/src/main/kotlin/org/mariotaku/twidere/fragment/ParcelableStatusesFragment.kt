/*
 * Twidere - Twitter client for Android
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
import android.support.v4.content.Loader
import android.text.TextUtils
import com.squareup.otto.Subscribe
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ListParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.MicroBlogAPIStatusesLoader
import org.mariotaku.twidere.model.BaseRefreshTaskParam
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.FavoriteTaskEvent
import org.mariotaku.twidere.model.message.StatusDestroyedEvent
import org.mariotaku.twidere.model.message.StatusListChangedEvent
import org.mariotaku.twidere.model.message.StatusRetweetedEvent
import org.mariotaku.twidere.util.Utils
import java.util.*

/**
 * Created by mariotaku on 14/12/3.
 */
abstract class ParcelableStatusesFragment : AbsStatusesFragment() {

    private var lastId: String? = null
    private var page = 1
    private var pageDelta: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(EXTRA_PAGE)
        }
    }

    fun deleteStatus(statusId: String) {
        val list = adapterData ?: return
        val dataToRemove = HashSet<ParcelableStatus>()
        for (i in 0 until list.size) {
            val status = list[i]
            if (TextUtils.equals(status.id, statusId) || TextUtils.equals(status.retweet_id, statusId)) {
                dataToRemove.add(status)
            } else if (TextUtils.equals(status.my_retweet_id, statusId)) {
                status.my_retweet_id = null
                status.retweet_count = status.retweet_count - 1
            }
        }
        if (list is MutableList) {
            list.removeAll(dataToRemove)
        }
        adapterData = list
    }

    override fun getStatuses(param: RefreshTaskParam): Boolean {
        if (!loaderInitialized) return false
        val args = Bundle(arguments)
        val maxIds = param.maxIds
        if (maxIds != null) {
            args.putString(EXTRA_MAX_ID, maxIds[0])
            args.putBoolean(EXTRA_MAKE_GAP, false)
        }
        val sinceIds = param.sinceIds
        if (sinceIds != null) {
            args.putString(EXTRA_SINCE_ID, sinceIds[0])
        }
        if (page > 0) {
            args.putInt(EXTRA_PAGE, page)
        }
        args.putBoolean(EXTRA_LOADING_MORE, param.isLoadingMore)
        args.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.restartLoader(0, args, this)
        return true
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun hasMoreData(data: List<ParcelableStatus>?): Boolean {
        if (data == null || data.isEmpty()) return false
        val tmpLastId = lastId
        lastId = data[data.size - 1].id
        return !TextUtils.equals(lastId, tmpLastId)
    }

    override val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: emptyArray()

    override fun createMessageBusCallback(): Any {
        return ParcelableStatusesBusCallback()
    }

    override fun onCreateAdapter(context: Context): ListParcelableStatusesAdapter {
        return ListParcelableStatusesAdapter(context)
    }

    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        refreshEnabled = true
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        val adapter = adapter
        if (adapter!!.itemCount > 0) {
            showContent()
        } else if (loader is MicroBlogAPIStatusesLoader) {
            val e = loader.exception
            if (e != null) {
                showError(R.drawable.ic_info_error_generic, Utils.getErrorMessage(context, e))
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
        }
    }

    override fun onLoadMoreContents(position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L || refreshing) return
        super.onLoadMoreContents(position.toLong())
        if (position == 0L) return
        val adapter = adapter
        // Load the last item
        val idx = adapter!!.statusStartIndex + adapter.rawStatusCount - 1
        if (idx < 0) return
        val status = adapter.getStatus(idx) ?: return
        val accountKeys = arrayOf(status.account_key)
        val maxIds = arrayOf<String?>(status.id)
        page += pageDelta
        val param = BaseRefreshTaskParam(accountKeys, maxIds, null)
        param.isLoadingMore = true
        getStatuses(param)
    }

    fun replaceStatusStates(status: ParcelableStatus?) {
        if (status == null) return
        val lm = layoutManager
        val adapter = adapter
        val rangeStart = Math.max(adapter!!.statusStartIndex, lm!!.findFirstVisibleItemPosition())
        val rangeEnd = Math.min(lm.findLastVisibleItemPosition(), adapter.statusStartIndex + adapter.statusCount - 1)
        for (i in rangeStart..rangeEnd) {
            val item = adapter.getStatus(i)
            if (status == item) {
                item.favorite_count = status.favorite_count
                item.retweet_count = status.retweet_count
                item.reply_count = status.reply_count

                item.is_favorite = status.is_favorite
            }
        }
        adapter.notifyItemRangeChanged(rangeStart, rangeEnd)
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        val adapter = adapter ?: return false
        val accountKeys = accountKeys
        if (adapter.statusCount > 0) {
            val firstStatus = adapter.getStatus(0)!!
            val sinceIds = Array(accountKeys.size) {
                return@Array if (firstStatus.account_key == accountKeys[it]) firstStatus.id else null
            }
            getStatuses(BaseRefreshTaskParam(accountKeys, null, sinceIds))
        } else {
            getStatuses(BaseRefreshTaskParam(accountKeys, null, null))
        }
        return true
    }

    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return loaderManager.hasRunningLoaders()
        }
        set(value) {
            super.refreshing = value
        }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt(EXTRA_PAGE, page)
    }

    protected open val savedStatusesFileArgs: Array<String>?
        get() = null

    override fun onHasMoreDataChanged(hasMoreData: Boolean) {
        pageDelta = if (hasMoreData) 1 else 0
    }

    private fun updateFavoritedStatus(status: ParcelableStatus) {
        val context = activity ?: return
        replaceStatusStates(status)
    }

    private fun updateRetweetedStatuses(status: ParcelableStatus?) {
        val data = adapterData
        if (status == null || status.retweet_id == null || data == null) return
        data.forEach { orig ->
            if (orig.account_key == status.account_key && TextUtils.equals(orig.id, status.retweet_id)) {
                orig.my_retweet_id = status.my_retweet_id
                orig.retweet_count = status.retweet_count
            }
        }
        adapterData = data
    }

    protected inner class ParcelableStatusesBusCallback {

        @Subscribe
        fun notifyFavoriteTask(event: FavoriteTaskEvent) {
            if (event.isSucceeded) {
                updateFavoritedStatus(event.status!!)
            }
        }

        @Subscribe
        fun notifyStatusDestroyed(event: StatusDestroyedEvent) {
            deleteStatus(event.status.id)
        }

        @Subscribe
        fun notifyStatusListChanged(event: StatusListChangedEvent) {
            adapter!!.notifyDataSetChanged()
        }

        @Subscribe
        fun notifyStatusRetweeted(event: StatusRetweetedEvent) {
            updateRetweetedStatuses(event.status)
        }

    }

}
