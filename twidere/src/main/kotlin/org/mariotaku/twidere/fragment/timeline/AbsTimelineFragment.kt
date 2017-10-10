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

package org.mariotaku.twidere.fragment.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.LayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.data.ObjectCursorLiveData
import org.mariotaku.twidere.fragment.AbsContentRecyclerViewFragment
import org.mariotaku.twidere.fragment.CursorStatusesFragment
import org.mariotaku.twidere.model.ExceptionResponseList
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.BaseRefreshTaskParam
import org.mariotaku.twidere.model.refresh.RefreshTaskParam
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.Utils

abstract class AbsTimelineFragment<RefreshParam : RefreshTaskParam> :
        AbsContentRecyclerViewFragment<ParcelableStatusesAdapter, LayoutManager>() {

    override val reachingStart: Boolean
        get() = listView.firstVisiblePosition <= 0

    override val reachingEnd: Boolean
        get() = listView.lastVisiblePosition >= listView.count - 1

    @TimelineStyle
    protected open val timelineStyle: Int
        get() = TimelineStyle.PLAIN

    protected open val isLive: Boolean
        get() = tabId <= 0

    @FilterScope
    protected abstract val filterScope: Int

    /**
     * Content Uri for in-database data source
     */
    protected abstract val contentUri: Uri

    protected lateinit var statuses: LiveData<List<ParcelableStatus>?>
        private set

    protected val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: if (isLive) {
            emptyArray()
        } else {
            DataStoreUtils.getActivatedAccountKeys(context)
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statuses = createLiveData()

        statuses.observe(this, Observer { onDataLoaded(it) })
        showProgress()
    }

    override fun onCreateLayoutManager(context: Context): LayoutManager = when (timelineStyle) {
        TimelineStyle.STAGGERED -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        else -> FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableStatusesAdapter {
        return ParcelableStatusesAdapter(context, requestManager, timelineStyle)
    }

    override fun triggerRefresh(): Boolean {
        val param = onCreateRefreshParam(REFRESH_POSITION_START)
        return getStatuses(param)
    }

    override fun onLoadMoreContents(position: Long) {
        if (position != ILoadMoreSupportAdapter.END) return
        val param = onCreateRefreshParam(REFRESH_POSITION_END)
        getStatuses(param)
    }

    override fun scrollToPositionWithOffset(position: Int, offset: Int) {
        val layoutManager = this.layoutManager
        when (layoutManager) {
            is StaggeredGridLayoutManager -> {
                layoutManager.scrollToPositionWithOffset(position, offset)
            }
            is LinearLayoutManager -> {
                layoutManager.scrollToPositionWithOffset(position, offset)
            }
        }
    }

    protected open fun onDataLoaded(data: List<ParcelableStatus>?) {
        adapter.data = data
        when {
            data is ExceptionResponseList -> {
                showEmpty(R.drawable.ic_info_error_generic, data.exception.toString())
            }
            data == null || data.isEmpty() -> {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
            else -> {
                showContent()
            }
        }
    }

    protected abstract fun getStatuses(param: RefreshParam): Boolean

    protected abstract fun onCreateRefreshParam(position: Int): RefreshParam

    protected open fun onCreateRealTimeLiveData(): LiveData<List<ParcelableStatus>?> {
        throw UnsupportedOperationException()
    }

    private fun createLiveData(): LiveData<List<ParcelableStatus>?> {
        if (isLive) return onCreateRealTimeLiveData()
        return ObjectCursorLiveData(context.contentResolver, contentUri,
                CursorStatusesFragment.statusColumnsLite, cls = ParcelableStatus::class.java)
    }

    companion object {
        const val REFRESH_POSITION_START = -1
        const val REFRESH_POSITION_END = -2

        fun getBaseRefreshTaskParam(fragment: AbsTimelineFragment<*>, position: Int): BaseRefreshTaskParam {
            when (position) {
                REFRESH_POSITION_START -> {
                    return getRefreshBaseRefreshTaskParam(fragment)
                }
                REFRESH_POSITION_END -> {

                }
            }
            TODO()
        }

        private fun getRefreshBaseRefreshTaskParam(fragment: AbsTimelineFragment<*>): BaseRefreshTaskParam {
            val adapter = fragment.adapter
            val accountKeys = fragment.accountKeys
            val statusStartIndex = adapter.statusStartIndex
            if (statusStartIndex >= 0 && adapter.getStatusCount(true) > 0) {
                val firstStatus = adapter.getStatus(statusStartIndex, true)
                val pagination = Array(accountKeys.size) lambda@ {
                    if (firstStatus.account_key == accountKeys[it]) {
                        return@lambda SinceMaxPagination.sinceId(firstStatus.id, -1)
                    } else {
                        return@lambda null
                    }
                }
                return BaseRefreshTaskParam(accountKeys, pagination)
            } else {
                return BaseRefreshTaskParam(accountKeys, null)
            }
        }
    }
}
