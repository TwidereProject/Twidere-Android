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
import android.arch.paging.PagedList
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.LayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import android.widget.Toast
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.addAllTo
import org.mariotaku.ktextension.addTo
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toNulls
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.data.source.CursorObjectLivePagedListProvider
import org.mariotaku.twidere.data.status.StatusesLivePagedListProvider
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.fragment.AbsContentRecyclerViewFragment
import org.mariotaku.twidere.fragment.AbsStatusesFragment
import org.mariotaku.twidere.fragment.CursorStatusesFragment
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.GetStatusesTaskEvent
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetStatusesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

abstract class AbsTimelineFragment : AbsContentRecyclerViewFragment<ParcelableStatusesAdapter, LayoutManager>() {

    override val reachingStart: Boolean
        get() = listView.firstVisiblePosition <= 0

    override val reachingEnd: Boolean
        get() = listView.lastVisiblePosition >= listView.count - 1

    @TimelineStyle
    protected open val timelineStyle: Int
        get() = TimelineStyle.PLAIN

    protected open val isStandalone: Boolean
        get() = tabId <= 0

    protected open val filtersEnabled: Boolean
        get() = true

    @FilterScope
    protected abstract val filterScope: Int

    /**
     * Content Uri for in-database data source
     */
    protected abstract val contentUri: Uri

    protected lateinit var statuses: LiveData<PagedList<ParcelableStatus>?>
        private set

    protected val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: if (isStandalone) {
            emptyArray()
        } else {
            DataStoreUtils.getActivatedAccountKeys(context)
        }

    private val busEventHandler = BusEventHandler()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.statusClickListener = StatusClickHandler()
        statuses = createLiveData()

        statuses.observe(this, Observer { onDataLoaded(it) })
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        bus.register(busEventHandler)
    }

    override fun onStop() {
        bus.unregister(busEventHandler)
        super.onStop()
    }

    override fun onCreateLayoutManager(context: Context): LayoutManager = when (timelineStyle) {
        TimelineStyle.STAGGERED -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        else -> FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableStatusesAdapter {
        return ParcelableStatusesAdapter(context, requestManager, timelineStyle)
    }

    override fun onCreateItemDecoration(context: Context, recyclerView: RecyclerView,
            layoutManager: LayoutManager): RecyclerView.ItemDecoration? {
        return when (timelineStyle) {
            TimelineStyle.PLAIN -> {
                createStatusesListItemDecoration(context, recyclerView, adapter)
            }
            else -> {
                super.onCreateItemDecoration(context, recyclerView, layoutManager)
            }
        }
    }

    override fun triggerRefresh(): Boolean {
        if (isStandalone) {
            return false
        }
        return getStatuses(object : ContentRefreshParam {
            override val accountKeys: Array<UserKey> by lazy {
                this@AbsTimelineFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val sinceIds = DataStoreUtils.getNewestStatusIds(context, contentUri, keys)
                val sinceSortIds = DataStoreUtils.getNewestStatusSortIds(context, contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.sinceId(sinceIds[idx], sinceSortIds[idx])
                }
            }

            override val shouldAbort: Boolean
                get() = context == null

            override val hasMaxIds: Boolean
                get() = false
        })
    }

    override fun onLoadMoreContents(position: Long) {
        if (isStandalone) return
        if (position != ILoadMoreSupportAdapter.END) return
        val started = getStatuses(object : ContentRefreshParam {
            override val accountKeys by lazy {
                this@AbsTimelineFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val maxIds = DataStoreUtils.getOldestStatusIds(context, contentUri, keys)
                val maxSortIds = DataStoreUtils.getOldestStatusSortIds(context, contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.maxId(maxIds[idx], maxSortIds[idx])
                }
            }

        })
        if (started) {
            adapter.loadMoreIndicatorPosition = position
        }
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

    protected open fun onDataLoaded(data: PagedList<ParcelableStatus>?) {
        adapter.statuses = data
        when {
//            data is ExceptionResponseList -> {
//                showEmpty(R.drawable.ic_info_error_generic, data.exception.toString())
//            }
            data == null || data.isEmpty() -> {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
            else -> {
                showContent()
            }
        }
    }

    protected abstract fun getStatuses(param: ContentRefreshParam): Boolean

    protected abstract fun onCreateStatusesFetcher(): StatusesFetcher

    protected open fun getExtraSelection(): Pair<Expression, Array<String>?>? {
        return null
    }

    protected open fun getMaxLoadItemLimit(forAccount: UserKey): Int {
        return 200
    }

    protected open fun onFavoriteTaskEvent(event: FavoriteTaskEvent) {
        val status = event.status
        if (event.isSucceeded && status != null) {
            replaceStatusStates(status)
        }
    }


    private fun createLiveData(): LiveData<PagedList<ParcelableStatus>?> {
        return if (isStandalone) onCreateStandaloneLiveData() else onCreateDatabaseLiveData()
    }

    private fun onCreateStandaloneLiveData(): LiveData<PagedList<ParcelableStatus>?> {
        val accountKey = accountKeys.singleOrNull()!!
        val provider = StatusesLivePagedListProvider(context.applicationContext,
                onCreateStatusesFetcher(), accountKey)
        val maxLoadLimit = getMaxLoadItemLimit(accountKey)
        val loadLimit = preferences[loadItemLimitKey]
        return provider.create(null, PagedList.Config.Builder()
                .setPageSize(loadLimit.coerceAtMost(maxLoadLimit))
                .setInitialLoadSizeHint(loadLimit.coerceAtMost(maxLoadLimit))
                .build())
    }

    private fun onCreateDatabaseLiveData(): LiveData<PagedList<ParcelableStatus>?> {
        val table = DataStoreUtils.getTableNameByUri(contentUri)!!
        val accountKeys = accountKeys
        val expressions = mutableListOf(Expression.inArgs(Statuses.ACCOUNT_KEY, accountKeys.size))
        val expressionArgs = mutableListOf(*accountKeys.mapToArray(UserKey::toString))
        if (filtersEnabled) {
            expressions.add(DataStoreUtils.buildStatusFilterWhereClause(preferences, table,
                    null, filterScope))
        }
        val extraSelection = getExtraSelection()
        if (extraSelection != null) {
            extraSelection.first.addTo(expressions)
            extraSelection.second?.addAllTo(expressionArgs)
        }
        val provider = CursorObjectLivePagedListProvider(context.contentResolver, contentUri,
                CursorStatusesFragment.statusColumnsLite, Expression.and(*expressions.toTypedArray()).sql,
                expressionArgs.toTypedArray(), Statuses.DEFAULT_SORT_ORDER, ParcelableStatus::class.java)
        return provider.create(null, 20)
    }

    private fun getFullStatus(position: Int): ParcelableStatus? {
        if (isStandalone) {
            return adapter.getStatus(position, false)
        }
        val _id = adapter.getRowId(position)
        val where = Expression.equals(Statuses._ID, _id).sql
        return context.contentResolver.queryOne(contentUri, Statuses.COLUMNS, where, null, null,
                ParcelableStatus::class.java)
    }

    fun replaceStatusStates(status: ParcelableStatus) {
        val statuses = adapter.statuses?.snapshot() ?: return
        val lm = layoutManager
        val range = lm.firstVisibleItemPosition..lm.lastVisibleItemPosition
        statuses.forEachIndexed { index, item ->
            if (item?.id != status.id) return@forEachIndexed
            item.favorite_count = status.favorite_count
            item.retweet_count = status.retweet_count
            item.reply_count = status.reply_count

            item.is_favorite = status.is_favorite
            if (index in range) {
                adapter.notifyItemRangeChanged(index, 1)
            }
        }
    }

    private inner class BusEventHandler {

        @Subscribe
        fun notifyGetStatusesTaskChanged(event: GetStatusesTaskEvent) {
            if (event.uri != contentUri) return
            refreshing = event.running
            if (!event.running) {
                setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
                refreshEnabled = true
                // TODO: showContentOrError()

                val exception = event.exception
                if (exception is GetStatusesTask.GetTimelineException && userVisibleHint) {
                    Toast.makeText(context, exception.getToastMessage(context), Toast.LENGTH_SHORT).show()
                }
            }
        }

        @Subscribe
        fun notifyFavoriteTask(event: FavoriteTaskEvent) {
            onFavoriteTaskEvent(event)
        }

    }

    private inner class StatusClickHandler : IStatusViewHolder.StatusClickListener {
        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
            val status = getFullStatus(position) ?: return
            IntentUtils.openStatus(activity, status, null)
        }

        override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
            val status = getFullStatus(position) ?: return
            AbsStatusesFragment.handleActionClick(this@AbsTimelineFragment, id, status,
                    holder as StatusViewHolder)
        }

        override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
            val status = getFullStatus(position) ?: return false
            return AbsStatusesFragment.handleActionLongClick(this@AbsTimelineFragment, status,
                    adapter.getItemId(position), id)
        }
    }

    companion object {

        private val LayoutManager.firstVisibleItemPosition: Int
            get() = when (this) {
                is LinearLayoutManager -> findFirstVisibleItemPosition()
                is StaggeredGridLayoutManager -> findFirstVisibleItemPositions(null).firstOrNull() ?: -1
                else -> throw UnsupportedOperationException()
            }

        private val LayoutManager.lastVisibleItemPosition: Int
            get() = when (this) {
                is LinearLayoutManager -> findLastVisibleItemPosition()
                is StaggeredGridLayoutManager -> findLastVisibleItemPositions(null).lastOrNull() ?: -1
                else -> throw UnsupportedOperationException()
            }

        fun createStatusesListItemDecoration(context: Context, recyclerView: RecyclerView,
                adapter: IContentAdapter): RecyclerView.ItemDecoration {
            adapter as RecyclerView.Adapter<*>
            val itemDecoration = ExtendedDividerItemDecoration(context, (recyclerView.layoutManager as LinearLayoutManager).orientation)
            val res = context.resources
            if (adapter.profileImageEnabled) {
                val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 2 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
                itemDecoration.setPadding { position, rect ->
                    val itemViewType = adapter.getItemViewType(position)
                    var nextItemIsStatus = false
                    if (position < adapter.itemCount - 1) {
                        nextItemIsStatus = adapter.getItemViewType(position + 1) == ParcelableStatusesAdapter.VIEW_TYPE_STATUS
                    }
                    if (nextItemIsStatus && itemViewType == ParcelableStatusesAdapter.VIEW_TYPE_STATUS) {
                        rect.left = decorPaddingLeft
                    } else {
                        rect.left = 0
                    }
                    true
                }
            }
            itemDecoration.setDecorationEndOffset(1)
            return itemDecoration
        }
    }
}
