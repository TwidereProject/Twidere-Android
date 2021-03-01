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

package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.app.hasRunningLoadersSafe
import androidx.loader.content.Loader
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.adapter.StaggeredGridParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_FROM_USER
import org.mariotaku.twidere.extension.reachingEnd
import org.mariotaku.twidere.extension.reachingStart
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.loader.statuses.AbsRequestStatusesLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/5.
 */
abstract class AbsMediaStatusesFragment : AbsContentRecyclerViewFragment<StaggeredGridParcelableStatusesAdapter,
        StaggeredGridLayoutManager>(), LoaderCallbacks<List<ParcelableStatus>?>, DrawerCallback,
        IStatusViewHolder.StatusClickListener {

    final override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return LoaderManager.getInstance(this).hasRunningLoadersSafe()
        }
        set(value) {
            super.refreshing = value
        }

    final override val reachingEnd: Boolean
        get() = layoutManager.reachingEnd

    final override val reachingStart: Boolean
        get() = layoutManager.reachingStart

    protected open val loaderId: Int
        get() = tabId.toInt().coerceIn(0..Int.MAX_VALUE)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.statusClickListener = this
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        LoaderManager.getInstance(this).initLoader(loaderId, loaderArgs, this)
        showProgress()
    }

    final override fun onCreateLayoutManager(context: Context): StaggeredGridLayoutManager {
        return StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    final override fun scrollToPositionWithOffset(position: Int, offset: Int) {
        layoutManager.scrollToPositionWithOffset(position, offset)
    }

    final override fun onCreateAdapter(context: Context, requestManager: RequestManager): StaggeredGridParcelableStatusesAdapter {
        return StaggeredGridParcelableStatusesAdapter(context, requestManager)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableStatus>?> {
        val fromUser = args?.getBoolean(EXTRA_FROM_USER)
        args?.remove(EXTRA_FROM_USER)
        return onCreateStatusesLoader(requireActivity(), args!!, fromUser!!)
    }

    final override fun onLoadFinished(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        val changed = adapter.setData(data)
        if ((loader as IExtendedLoader).fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData(loader, data, changed)) {
                ILoadMoreSupportAdapter.END
            } else {
                ILoadMoreSupportAdapter.NONE
            }
        }
        loader.fromUser = false
        refreshing = false
        showContent()
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
    }

    final override fun onLoaderReset(loader: Loader<List<ParcelableStatus>?>) {
        adapter.setData(null)
    }

    final override fun onLoadMoreContents(position: Long) {
        // Only supports load from end
        if (ILoadMoreSupportAdapter.END != position) return
        super.onLoadMoreContents(position)
        // Get last raw status
        val startIdx = adapter.statusStartIndex
        if (startIdx < 0) return
        val statusCount = adapter.getStatusCount(true)
        if (statusCount <= 0) return
        val status = adapter.getStatus(startIdx + statusCount - 1, true)
        val maxId = status.id
        getStatuses(maxId, null)
    }

    final override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        context?.let {
            IntentUtils.openStatus(it, status, null)
        }
    }


    final override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        context?.let {
            IntentUtils.openStatus(it, status.account_key, status.quoted_id)
        }
    }

    protected open fun hasMoreData(loader: Loader<List<ParcelableStatus>?>,
                                   data: List<ParcelableStatus>?, changed: Boolean): Boolean {
        if (loader !is AbsRequestStatusesLoader) return false
        val pagination = loader.pagination as? SinceMaxPagination
        val maxId = pagination?.maxId?.takeIf(String::isNotEmpty)
        val sinceId = pagination?.sinceId?.takeIf(String::isNotEmpty)
        if (sinceId == null && maxId != null) {
            if (data != null && data.isNotEmpty()) {
                return changed
            }
        }
        return true
    }

    protected abstract fun onCreateStatusesLoader(context: Context, args: Bundle,
            fromUser: Boolean): Loader<List<ParcelableStatus>?>

    protected abstract fun getStatuses(maxId: String?, sinceId: String?): Int

}
