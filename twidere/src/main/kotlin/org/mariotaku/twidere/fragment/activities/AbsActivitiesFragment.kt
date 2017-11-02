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

package org.mariotaku.twidere.fragment.activities

import android.accounts.AccountManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.CallSuper
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_STATUS
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.readFromBottomKey
import org.mariotaku.twidere.data.CursorObjectLivePagedListProvider
import org.mariotaku.twidere.data.CursorObjectLivePagedListProvider.CursorObjectProcessor
import org.mariotaku.twidere.extension.model.activityStatus
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.extension.view.firstVisibleItemPosition
import org.mariotaku.twidere.extension.view.lastVisibleItemPosition
import org.mariotaku.twidere.fragment.AbsContentRecyclerViewFragment
import org.mariotaku.twidere.fragment.timeline.AbsTimelineFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.analyzer.Share
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.GetActivitiesTaskEvent
import org.mariotaku.twidere.model.event.StatusDestroyedEvent
import org.mariotaku.twidere.model.event.StatusRetweetedEvent
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.BaseContentRefreshParam
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.task.statuses.GetStatusesTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

abstract class AbsActivitiesFragment : AbsContentRecyclerViewFragment<ParcelableActivitiesAdapter, RecyclerView.LayoutManager>() {

    override val reachingStart: Boolean
        get() = recyclerView.layoutManager.firstVisibleItemPosition <= 0

    override val reachingEnd: Boolean
        get() = recyclerView.layoutManager.lastVisibleItemPosition >= recyclerView.layoutManager.itemCount - 1

    protected open val isStandalone: Boolean
        get() = tabId <= 0

    protected open val filtersEnabled: Boolean
        get() = true

    protected open val readPositionTag: String? = null

    protected open val readPositionTagWithArguments: String?
        get() = readPositionTag

    @FilterScope
    protected abstract val filterScope: Int

    /**
     * Content Uri for in-database data source
     */
    protected abstract val contentUri: Uri

    protected lateinit var activities: LiveData<PagedList<ParcelableActivity>?>
        private set

    protected val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: if (isStandalone) {
            emptyArray()
        } else {
            DataStoreUtils.getActivatedAccountKeys(context)
        }

    private val busEventHandler = BusEventHandler()
    private val scrollHandler = ScrollHandler()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerForContextMenu(recyclerView)
        adapter.activityClickListener = ActivityClickHandler()
        activities = createLiveData()
        activities.observe(this, Observer { onDataLoaded(it) })
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        recyclerView.addOnScrollListener(scrollHandler)
        bus.register(busEventHandler)
    }

    override fun onStop() {
        bus.unregister(busEventHandler)
        recyclerView.removeOnScrollListener(scrollHandler)
        if (userVisibleHint) {
            saveReadPosition(layoutManager.firstVisibleItemPosition)
        }
        super.onStop()
    }

    override fun onCreateLayoutManager(context: Context): RecyclerView.LayoutManager {
        return FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableActivitiesAdapter {
        return ParcelableActivitiesAdapter(context, requestManager)
    }

    override fun onCreateItemDecoration(context: Context, recyclerView: RecyclerView,
            layoutManager: RecyclerView.LayoutManager): RecyclerView.ItemDecoration? {
        return AbsTimelineFragment.createStatusesListItemDecoration(context, recyclerView, adapter)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val status = adapter.getActivity(contextMenuInfo.position).activityStatus ?: return
        inflater.inflate(R.menu.action_status, menu)
        MenuUtils.setupForStatus(context, menu, preferences, userColorNameManager, status)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val status = adapter.getActivity(contextMenuInfo.position).activityStatus ?: return false
        when (item.itemId) {
            R.id.share -> {
                val shareIntent = Utils.createStatusShareIntent(activity, status)
                val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
                startActivity(chooser)

                val am = AccountManager.get(context)
                val accountType = AccountUtils.findByAccountKey(am, status.account_key)?.getAccountType(am)
                Analyzer.log(Share.status(accountType, status))
                return true
            }
            R.id.make_gap -> {
                if (isStandalone) return true
                val resolver = context.contentResolver
                val values = ContentValues()
                values.put(Activities.IS_GAP, 1)
                val where = Expression.equals(Activities._ID, status._id).sql
                resolver.update(contentUri, values, where, null)
                return true
            }
            else -> return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                    preferences, userColorNameManager, twitterWrapper, status, item)
        }
    }


    override fun triggerRefresh(): Boolean {
        if (isStandalone) {
            return false
        }
        return getActivities(object : ContentRefreshParam {
            override val accountKeys: Array<UserKey> by lazy {
                this@AbsActivitiesFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val sinceIds = DataStoreUtils.getNewestStatusIds(context, contentUri, keys)
                val sinceSortIds = DataStoreUtils.getNewestStatusSortIds(context, contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.sinceId(sinceIds[idx], sinceSortIds[idx])
                }
            }

            override val tabId: Long
                get() = this@AbsActivitiesFragment.tabId

            override val shouldAbort: Boolean
                get() = context == null

            override val hasMaxIds: Boolean
                get() = false
        })
    }

    override fun onLoadMoreContents(position: Long) {
        if (isStandalone) return
        if (position != ILoadMoreSupportAdapter.END) return
        val started = getActivities(object : ContentRefreshParam {
            override val accountKeys by lazy {
                this@AbsActivitiesFragment.accountKeys
            }
            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val maxIds = DataStoreUtils.getOldestStatusIds(context, contentUri, keys)
                val maxSortIds = DataStoreUtils.getOldestStatusSortIds(context, contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.maxId(maxIds[idx], maxSortIds[idx])
                }
            }

            override val tabId: Long
                get() = this@AbsActivitiesFragment.tabId

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


    fun reloadAll() {
        showProgress()
        adapter.activities = null
        activities.removeObservers(this)
        activities = createLiveData()
        activities.observe(this, Observer { onDataLoaded(it) })
    }


    protected open fun onDataLoaded(data: PagedList<ParcelableActivity>?) {
        val firstVisiblePosition = layoutManager.firstVisibleItemPosition
        adapter.showAccountsColor = accountKeys.size > 1
        adapter.activities = data
        when {
            data == null || data.isEmpty() -> {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
            else -> {
                showContent()
            }
        }
        if (firstVisiblePosition == 0 && !preferences[readFromBottomKey]) {
            recyclerView.post {
                scrollToStart()
            }
        }
    }

    protected abstract fun getActivities(param: ContentRefreshParam): Boolean

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

    protected open fun onStatusRetweetedEvent(event: StatusRetweetedEvent) {
        replaceStatusStates(event.status)
    }

    protected open fun onStatusDestroyedEvent(event: StatusDestroyedEvent) {
        if (!isStandalone) return
    }

    @CallSuper
    protected open fun saveReadPosition(position: Int) {
        if (host == null) return
        if (position == RecyclerView.NO_POSITION || adapter.getActivityCount(false) <= 0) return
        val status = adapter.getActivity(position.coerceIn(rangeOfSize(adapter.activityStartIndex,
                adapter.getActivityCount(false))))
        val readPosition = if (isStandalone) {
            status.sort_id
        } else {
            status.position_key
        }
        val positionTag = readPositionTag ?: ReadPositionTag.CUSTOM_TIMELINE
        readPositionTagWithArguments?.let {
            accountKeys.forEach { accountKey ->
                val tag = Utils.getReadPositionTagWithAccount(it, accountKey)
                readStateManager.setPosition(tag, readPosition)
            }
        }
    }

    protected abstract fun onCreateCursorObjectProcessor(): CursorObjectProcessor<ParcelableActivity>

    private fun createLiveData(): LiveData<PagedList<ParcelableActivity>?> {
        return if (isStandalone) onCreateStandaloneLiveData() else onCreateDatabaseLiveData()
    }

    private fun onCreateStandaloneLiveData(): LiveData<PagedList<ParcelableActivity>?> {
        throw UnsupportedOperationException("Not yet needed")
    }

    private fun onCreateDatabaseLiveData(): LiveData<PagedList<ParcelableActivity>?> {
        val table = DataStoreUtils.getTableNameByUri(contentUri)!!
        val accountKeys = accountKeys
        val expressions = mutableListOf(Expression.inArgs(Activities.ACCOUNT_KEY, accountKeys.size))
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
                Activities.COLUMNS, Expression.and(*expressions.toTypedArray()).sql,
                expressionArgs.toTypedArray(), Activities.DEFAULT_SORT_ORDER,
                ParcelableActivity::class.java, onCreateCursorObjectProcessor())
        return provider.create(null, PagedList.Config.Builder()
                .setPageSize(50).setEnablePlaceholders(false).build())
    }

    private fun getFullActivity(position: Int): ParcelableActivity? {
        if (isStandalone) {
            return adapter.getActivity(position, false)
        }
        val _id = adapter.getRowId(position)
        val where = Expression.equals(Activities._ID, _id).sql
        return context.contentResolver.queryOne(contentUri, Activities.COLUMNS, where, null, null,
                ParcelableActivity::class.java)
    }

    private fun replaceStatusStates(status: ParcelableStatus) {
        val activities = adapter.activities?.snapshot() ?: return
        val lm = layoutManager
        val range = lm.firstVisibleItemPosition..lm.lastVisibleItemPosition
        activities.forEachIndexed { index, item ->
            if (item?.id != status.id) return@forEachIndexed
            item.favorite_count = status.favorite_count
            item.retweet_count = status.retweet_count
            item.reply_count = status.reply_count

            item.my_retweet_id = status.my_retweet_id
            item.is_favorite = status.is_favorite
            if (index in range) {
                adapter.notifyItemRangeChanged(index, 1)
            }
        }
    }

    private inner class BusEventHandler {


        @Subscribe
        fun notifyGetActivitiesTaskChanged(event: GetActivitiesTaskEvent) {
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
        fun notifyStatusDestroyed(event: StatusDestroyedEvent) {
            onStatusDestroyedEvent(event)
        }

        @Subscribe
        fun notifyFavoriteTask(event: FavoriteTaskEvent) {
            onFavoriteTaskEvent(event)
        }

        @Subscribe
        fun notifyRetweetTask(event: StatusRetweetedEvent) {
            onStatusRetweetedEvent(event)
        }

    }

    private inner class ActivityClickHandler : ParcelableActivitiesAdapter.ActivityAdapterListener {
        override fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int) {
            val activity = adapter.getActivity(position)
            val list = ArrayList<Parcelable>()
            if (activity.target_objects?.statuses.isNotNullOrEmpty()) {
                activity.target_objects?.statuses?.addAllTo(list)
            } else if (activity.targets?.statuses.isNotNullOrEmpty()) {
                activity.targets?.statuses?.addAllTo(list)
            }
            activity.sources?.addAllTo(list)
            IntentUtils.openItems(getActivity(), list)
        }

        override fun onStatusActionClick(holder: IStatusViewHolder, id: Int, position: Int) {
            val status = getActivityStatus(position) ?: return
            AbsTimelineFragment.handleActionClick(this@AbsActivitiesFragment, id, status,
                    holder as IStatusViewHolder)
        }

        override fun onStatusActionLongClick(holder: IStatusViewHolder, id: Int, position: Int): Boolean {
            val status = getActivityStatus(position) ?: return false
            return AbsTimelineFragment.handleActionLongClick(this@AbsActivitiesFragment,
                    status, adapter.getItemId(position), id)
        }

        override fun onStatusMenuClick(holder: IStatusViewHolder, menuView: View, position: Int) {
            if (activity == null) return
            val lm = layoutManager
            val view = lm.findViewByPosition(position) ?: return
            if (lm.getItemViewType(view) != ITEM_VIEW_TYPE_STATUS) {
                return
            }
            recyclerView.showContextMenuForChild(view)
        }

        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
            val status = getActivityStatus(position) ?: return
            IntentUtils.openStatus(activity, status, null)
        }

        override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
            val status = getActivityStatus(position)?.takeIf { it.quoted_id != null } ?: return
            IntentUtils.openStatus(context, status.account_key, status.quoted_id)
        }

        override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, position: Int) {
            val status = getActivityStatus(position) ?: return
            IntentUtils.openMedia(activity, status, media, preferences[newDocumentApiKey],
                    preferences[displaySensitiveContentsKey],
                    null)
        }

        override fun onGapClick(holder: GapViewHolder, position: Int) {
            val activity = adapter.getActivity(position)
            DebugLog.v(msg = "Load activity gap $activity")
            if (!AccountUtils.isOfficial(context, activity.account_key)) {
                // Skip if item is not a status
                if (activity.action !in Activity.Action.MENTION_ACTIONS) {
                    adapter.removeGapLoadingId(ObjectId(activity.account_key, activity.id))
                    adapter.notifyItemChanged(position)
                    return
                }
            }
            val accountKeys = arrayOf(activity.account_key)
            val pagination = arrayOf(SinceMaxPagination.maxId(activity.min_position,
                    activity.min_sort_position))
            getActivities(BaseContentRefreshParam(accountKeys, pagination).also {
                it.extraId = activity._id
            })
        }

        private fun getActivityStatus(position: Int): ParcelableStatus? {
            return adapter.getActivity(position).activityStatus
        }
    }

    private inner class ScrollHandler : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = layoutManager
                saveReadPosition(layoutManager.firstVisibleItemPosition)
            }
        }
    }

}