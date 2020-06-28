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
import androidx.loader.app.hasRunningLoadersSafe
import androidx.loader.content.Loader
import android.text.TextUtils
import androidx.loader.app.LoaderManager
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.adapter.ListParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.getMaxId
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.loader.statuses.AbsRequestStatusesLoader
import org.mariotaku.twidere.model.BaseRefreshTaskParam
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.StatusDestroyedEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.event.StatusRetweetedEvent
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.util.Utils
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by mariotaku on 14/12/3.
 */
abstract class ParcelableStatusesFragment : AbsStatusesFragment() {

    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return LoaderManager.getInstance(this).hasRunningLoadersSafe()
        }
        set(value) {
            super.refreshing = value
        }

    protected open val savedStatusesFileArgs: Array<String>?
        get() = null


    override val accountKeys: Array<UserKey>
        get() = context?.let { Utils.getAccountKeys(it, arguments) } ?: emptyArray()

    private var lastId: String? = null
    private var nextPagination: Pagination? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            nextPagination = savedInstanceState.getParcelable(EXTRA_NEXT_PAGINATION)
        }
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_NEXT_PAGINATION, nextPagination)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableStatus>?> {
        val loader = super.onCreateLoader(id, args)
        if (loader is AbsRequestStatusesLoader) {
            loader.pagination = args?.getParcelable(EXTRA_PAGINATION)
        }
        return loader
    }

    override fun getStatuses(param: RefreshTaskParam): Boolean {
        if (!loaderInitialized) return false
        val args = Bundle(arguments)
        val maxId = param.getMaxId(0)
        if (maxId != null) {
            args.putBoolean(EXTRA_MAKE_GAP, false)
        }
        args.putBoolean(EXTRA_LOADING_MORE, param.isLoadingMore)
        args.putBoolean(EXTRA_FROM_USER, true)
        args.putParcelable(EXTRA_PAGINATION, param.pagination?.getOrNull(0))
        LoaderManager.getInstance(this).restartLoader(loaderId, args, this)
        return true
    }

    override fun hasMoreData(loader: Loader<List<ParcelableStatus>?>,
                             data: List<ParcelableStatus>?): Boolean {
        if (data == null || data.isEmpty()) return false
        if (loader is IPaginationLoader) {
            return loader.nextPagination != null
        }
        return true
    }

    override fun createMessageBusCallback(): Any {
        return ParcelableStatusesBusCallback()
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ListParcelableStatusesAdapter {
        return ListParcelableStatusesAdapter(context, this.requestManager)
    }

    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        refreshEnabled = true
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        if (adapter.itemCount > 0) {
            showContent()
        } else if (loader is AbsRequestStatusesLoader) {
            val e = loader.exception
            if (e != null) {
                context ?.let {
                    showError(R.drawable.ic_info_error_generic, e.getErrorMessage(it))
                }
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
        }
    }

    override fun onLoadMoreContents(position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START != 0L || refreshing) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        // Get last raw status
        val startIdx = adapter.statusStartIndex
        if (startIdx < 0) return
        val statusCount = adapter.getStatusCount(true)
        if (statusCount <= 0) return
        val status = adapter.getStatus(startIdx + statusCount - 1, true)
        val accountKeys = arrayOf(status.account_key)
        val pagination = arrayOf<Pagination?>(SinceMaxPagination.maxId(status.id, -1))
        val param = BaseRefreshTaskParam(accountKeys, pagination)
        param.isLoadingMore = true
        getStatuses(param)
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        val accountKeys = accountKeys
        val statusStartIndex = adapter.statusStartIndex
        if (statusStartIndex >= 0 && adapter.getStatusCount(true) > 0) {
            val firstStatus = adapter.getStatus(statusStartIndex, true)
            val pagination = Array(accountKeys.size) {
                if (firstStatus.account_key == accountKeys[it]) {
                    SinceMaxPagination.sinceId(firstStatus.id, -1)
                } else {
                    null
                }
            }
            getStatuses(BaseRefreshTaskParam(accountKeys, pagination))
        } else {
            getStatuses(BaseRefreshTaskParam(accountKeys, null))
        }
        return true
    }

    fun removeStatus(statusId: String) {
        val list = adapterData ?: return
        val dataToRemove = HashSet<ParcelableStatus>()
        for (element in list) {
            if (element.id == statusId || element.retweet_id == statusId) {
                dataToRemove.add(element)
            } else if (element.my_retweet_id == statusId) {
                element.my_retweet_id = null
                element.retweet_count = element.retweet_count - 1
            }
        }
        if (list is MutableList) {
            list.removeAll(dataToRemove)
        }
        adapterData = list
    }

    fun replaceStatusStates(status: ParcelableStatus?) {
        if (status == null) return
        val lm = layoutManager
        val rangeStart = max(adapter.statusStartIndex, lm.findFirstVisibleItemPosition())
        val rangeEnd = min(lm.findLastVisibleItemPosition(), adapter.statusStartIndex + adapter.getStatusCount(false) - 1)
        for (i in rangeStart..rangeEnd) {
            val item = adapter.getStatus(i, false)
            if (status == item) {
                item.favorite_count = status.favorite_count
                item.retweet_count = status.retweet_count
                item.reply_count = status.reply_count

                item.is_favorite = status.is_favorite
            }
        }
        adapter.notifyItemRangeChanged(rangeStart, rangeEnd)
    }

    private fun updateFavoritedStatus(status: ParcelableStatus) {
        replaceStatusStates(status)
    }

    private fun updateRetweetedStatuses(status: ParcelableStatus?) {
        val data = adapterData
        if (status?.retweet_id == null || data == null) return
        data.forEach { orig ->
            if (orig.account_key == status.account_key && TextUtils.equals(orig.id, status.retweet_id)) {
                orig.my_retweet_id = status.my_retweet_id
                orig.retweet_count = status.retweet_count
            }
        }
        adapterData = data
    }

    protected open fun notifyFavoriteTask(event: FavoriteTaskEvent) {
        if (event.isSucceeded) {
            updateFavoritedStatus(event.status!!)
        }
    }

    protected open fun notifyStatusDestroyed(event: StatusDestroyedEvent) {
        removeStatus(event.status.id)
    }

    protected open fun notifyStatusListChanged(event: StatusListChangedEvent) {
        adapter.notifyDataSetChanged()
    }

    protected open fun notifyStatusRetweeted(event: StatusRetweetedEvent) {
        updateRetweetedStatuses(event.status)
    }

    protected inner class ParcelableStatusesBusCallback {

        @Subscribe
        fun onFavoriteTaskEvent(event: FavoriteTaskEvent) {
            notifyFavoriteTask(event)
        }

        @Subscribe
        fun onStatusDestroyedEvent(event: StatusDestroyedEvent) {
            notifyStatusDestroyed(event)
        }

        @Subscribe
        fun onStatusListChangedEvent(event: StatusListChangedEvent) {
            notifyStatusListChanged(event)
        }

        @Subscribe
        fun onStatusRetweetedEvent(event: StatusRetweetedEvent) {
            notifyStatusRetweeted(event)
        }

    }

}
