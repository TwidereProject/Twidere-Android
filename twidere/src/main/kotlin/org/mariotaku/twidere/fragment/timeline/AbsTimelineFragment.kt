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

import android.accounts.AccountManager
import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.LayoutManager
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.data.fetcher.StatusesFetcher
import org.mariotaku.twidere.data.source.CursorObjectLivePagedListProvider
import org.mariotaku.twidere.data.status.StatusesLivePagedListProvider
import org.mariotaku.twidere.extension.adapter.removeStatuses
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.fragment.AbsContentRecyclerViewFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.status.FavoriteConfirmDialogFragment
import org.mariotaku.twidere.fragment.status.RetweetQuoteDialogFragment
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.model.ObjectId
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.analyzer.Share
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.GetStatusesTaskEvent
import org.mariotaku.twidere.model.event.StatusDestroyedEvent
import org.mariotaku.twidere.model.event.StatusRetweetedEvent
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.refresh.BaseContentRefreshParam
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetStatusesTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.TimelineFilterHeaderViewHolder
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

    protected open val timelineFilter: TimelineFilter? = null

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
        registerForContextMenu(recyclerView)
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
            TimelineStyle.PLAIN -> createStatusesListItemDecoration(context, recyclerView, adapter)
            TimelineStyle.GALLERY -> createStatusesListGalleryDecoration(context, recyclerView)
            else -> super.onCreateItemDecoration(context, recyclerView, layoutManager)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val status = adapter.getStatus(contextMenuInfo!!.position)
        inflater.inflate(R.menu.action_status, menu)
        MenuUtils.setupForStatus(context, menu, preferences, twitterWrapper, userColorNameManager,
                status)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val status = adapter.getStatus(contextMenuInfo.position)
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
                values.put(Statuses.IS_GAP, 1)
                val where = Expression.equals(Statuses._ID, status._id).sql
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


    fun reloadAll() {
        showProgress()
        adapter.statuses = null
        statuses.removeObservers(this)
        statuses = createLiveData()
        statuses.observe(this, Observer { onDataLoaded(it) })
    }


    protected open fun onDataLoaded(data: PagedList<ParcelableStatus>?) {
        val firstVisiblePosition = layoutManager.firstVisibleItemPosition
        adapter.statuses = data
        adapter.timelineFilter = timelineFilter
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
                recyclerView.smoothScrollToPosition(0)
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

    protected open fun onStatusRetweetedEvent(event: StatusRetweetedEvent) {
        replaceStatusStates(event.status)
    }

    protected open fun onStatusDestroyedEvent(event: StatusDestroyedEvent) {
        if (!isStandalone) return
        adapter.removeStatuses { it != null && it.id == event.status.id }
    }

    protected open fun onTimelineFilterClick() {

    }


    private fun createLiveData(): LiveData<PagedList<ParcelableStatus>?> {
        return if (isStandalone) onCreateStandaloneLiveData() else onCreateDatabaseLiveData()
    }

    private fun onCreateStandaloneLiveData(): LiveData<PagedList<ParcelableStatus>?> {
        val accountKey = accountKeys.singleOrNull()!!
        val provider = StatusesLivePagedListProvider(context.applicationContext,
                onCreateStatusesFetcher(), accountKey, timelineFilter)
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
                Statuses.COLUMNS, Expression.and(*expressions.toTypedArray()).sql,
                expressionArgs.toTypedArray(), Statuses.DEFAULT_SORT_ORDER, ParcelableStatus::class.java)
        return provider.create(null, PagedList.Config.Builder()
                .setPageSize(50).setEnablePlaceholders(false).build())
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

    private fun replaceStatusStates(status: ParcelableStatus) {
        val statuses = adapter.statuses?.snapshot() ?: return
        val lm = layoutManager
        val range = lm.firstVisibleItemPosition..lm.lastVisibleItemPosition
        statuses.forEachIndexed { index, item ->
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

    private inner class StatusClickHandler : IStatusViewHolder.StatusClickListener {
        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
            val status = getFullStatus(position) ?: return
            IntentUtils.openStatus(activity, status, null)
        }

        override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
            val status = adapter.getStatus(position)
            System.identityHashCode(status)
            return false
        }

        override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
            val status = getFullStatus(position) ?: return
            handleActionClick(this@AbsTimelineFragment, id, status,
                    holder as StatusViewHolder)
        }

        override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
            val status = getFullStatus(position) ?: return false
            return handleActionLongClick(this@AbsTimelineFragment, status,
                    adapter.getItemId(position), id)
        }

        override fun onFilterClick(holder: TimelineFilterHeaderViewHolder) {
            onTimelineFilterClick()
        }

        override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
            val status = adapter.getStatus(statusPosition)
            IntentUtils.openMedia(activity, status, current, preferences[newDocumentApiKey],
                    preferences[displaySensitiveContentsKey])
        }

        override fun onQuotedMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
            val status = adapter.getStatus(statusPosition)
            val quotedMedia = status.quoted_media ?: return
            IntentUtils.openMedia(activity, status.account_key, status.is_possibly_sensitive, status,
                    current, quotedMedia, preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey])
        }

        override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
            val status = adapter.getStatus(position)
            val quotedId = status.quoted_id ?: return
            IntentUtils.openStatus(activity, status.account_key, quotedId)
        }

        override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
            val status = adapter.getStatus(position)
            val intent = IntentUtils.userProfile(status.account_key, status.user_key,
                    status.user_screen_name, status.extras?.user_statusnet_profile_url)
            IntentUtils.applyNewDocument(intent, preferences[newDocumentApiKey])
            startActivity(intent)
        }

        override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
            if (activity == null) return
            val view = layoutManager.findViewByPosition(position) ?: return
            recyclerView.showContextMenuForChild(view)
        }

        override fun onGapClick(holder: GapViewHolder, position: Int) {
            val status = adapter.getStatus(position)
            DebugLog.v(msg = "Load activity gap $status")
            adapter.addGapLoadingId(ObjectId(status.account_key, status.id))
            val accountKeys = arrayOf(status.account_key)
            val pagination = arrayOf(SinceMaxPagination.maxId(status.id, status.sort_id))
            getStatuses(BaseContentRefreshParam(accountKeys, pagination))
        }
    }


    class DefaultOnLikedListener(
            private val twitter: AsyncTwitterWrapper,
            private val status: ParcelableStatus,
            private val accountKey: UserKey? = null
    ) : LikeAnimationDrawable.OnLikedListener {

        override fun onLiked(): Boolean {
            if (status.is_favorite) return false
            twitter.createFavoriteAsync(accountKey ?: status.account_key, status)
            return true
        }
    }

    companion object {


        const val REQUEST_FAVORITE_SELECT_ACCOUNT = 101
        const val REQUEST_RETWEET_SELECT_ACCOUNT = 102


        val statusColumnsLite = Statuses.COLUMNS - arrayOf(Statuses.MENTIONS_JSON,
                Statuses.CARD, Statuses.FILTER_FLAGS, Statuses.FILTER_USERS, Statuses.FILTER_LINKS,
                Statuses.FILTER_SOURCES, Statuses.FILTER_NAMES, Statuses.FILTER_TEXTS,
                Statuses.FILTER_DESCRIPTIONS)

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

        fun handleActionClick(fragment: BaseFragment, id: Int, status: ParcelableStatus,
                holder: StatusViewHolder) {
            when (id) {
                R.id.reply -> {
                    val intent = Intent(INTENT_ACTION_REPLY)
                    intent.`package` = fragment.context.packageName
                    intent.putExtra(EXTRA_STATUS, status)
                    fragment.startActivity(intent)
                }
                R.id.retweet -> {
                    fragment.executeAfterFragmentResumed { f ->
                        RetweetQuoteDialogFragment.show(f.childFragmentManager,
                                status.account_key, status.id, status)
                    }
                }
                R.id.favorite -> {
                    when {
                        fragment.preferences[favoriteConfirmationKey] -> fragment.executeAfterFragmentResumed {
                            FavoriteConfirmDialogFragment.show(it.childFragmentManager,
                                    status.account_key, status.id, status)
                        }
                        status.is_favorite -> fragment.twitterWrapper.destroyFavoriteAsync(status.account_key, status.id)
                        else -> holder.playLikeAnimation(DefaultOnLikedListener(fragment.twitterWrapper, status))
                    }
                }
            }
        }

        fun handleActionLongClick(fragment: Fragment, status: ParcelableStatus, itemId: Long, id: Int): Boolean {
            when (id) {
                R.id.favorite -> {
                    val intent = selectAccountIntent(fragment.context, status, itemId)
                    fragment.startActivityForResult(intent, REQUEST_FAVORITE_SELECT_ACCOUNT)
                    return true
                }
                R.id.retweet -> {
                    val intent = selectAccountIntent(fragment.context, status, itemId, false)
                    fragment.startActivityForResult(intent, REQUEST_RETWEET_SELECT_ACCOUNT)
                    return true
                }
            }
            return false
        }

        fun handleActionActivityResult(fragment: BaseFragment, requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQUEST_FAVORITE_SELECT_ACCOUNT -> {
                    if (resultCode != Activity.RESULT_OK || data == null) return
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    val extras = data.getBundleExtra(EXTRA_EXTRAS)
                    val status = extras.getParcelable<ParcelableStatus>(EXTRA_STATUS)
                    if (fragment.preferences[favoriteConfirmationKey]) {
                        fragment.executeAfterFragmentResumed {
                            FavoriteConfirmDialogFragment.show(it.childFragmentManager,
                                    accountKey, status.id, status)
                        }
                    } else {
                        fragment.twitterWrapper.createFavoriteAsync(accountKey, status)
                    }
                }
                REQUEST_RETWEET_SELECT_ACCOUNT -> {
                    if (resultCode != Activity.RESULT_OK || data == null) return
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    val extras = data.getBundleExtra(EXTRA_EXTRAS)
                    val status = extras.getParcelable<ParcelableStatus>(EXTRA_STATUS)
                    if (status.account_key.host != accountKey.host) {
                        val composeIntent = Intent(fragment.context, ComposeActivity::class.java)
                        composeIntent.putExtra(Intent.EXTRA_TEXT, "${status.text_plain} ${LinkCreator.getStatusWebLink(status)}")
                        composeIntent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                        composeIntent.putExtra(EXTRA_SELECTION, 0)
                        fragment.startActivity(composeIntent)
                    } else fragment.executeAfterFragmentResumed {
                        RetweetQuoteDialogFragment.show(it.childFragmentManager, accountKey,
                                status.id, status)
                    }
                }
            }
        }


        fun handleKeyboardShortcutAction(fragment: BaseFragment, action: String,
                status: ParcelableStatus, position: Int): Boolean {
            when (action) {
                ACTION_STATUS_REPLY -> {
                    val intent = Intent(INTENT_ACTION_REPLY)
                    intent.putExtra(EXTRA_STATUS, status)
                    fragment.startActivity(intent)
                    return true
                }
                ACTION_STATUS_RETWEET -> {
                    fragment.executeAfterFragmentResumed {
                        RetweetQuoteDialogFragment.show(it.childFragmentManager,
                                status.account_key, status.id, status)
                    }
                    return true
                }
                ACTION_STATUS_FAVORITE -> {
                    if (fragment.preferences[favoriteConfirmationKey]) {
                        fragment.executeAfterFragmentResumed {
                            FavoriteConfirmDialogFragment.show(it.childFragmentManager,
                                    status.account_key, status.id, status)
                        }
                    } else if (status.is_favorite) {
                        fragment.twitterWrapper.destroyFavoriteAsync(status.account_key, status.id)
                    } else {
                        val holder = fragment.recyclerView.findViewHolderForLayoutPosition(position) as StatusViewHolder
                        holder.playLikeAnimation(DefaultOnLikedListener(fragment.twitterWrapper, status))
                    }
                    return true
                }
            }
            return false
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

        fun createStatusesListGalleryDecoration(context: Context, recyclerView: RecyclerView): RecyclerView.ItemDecoration {
            val itemDecoration = ExtendedDividerItemDecoration(context, (recyclerView.layoutManager as LinearLayoutManager).orientation)
            itemDecoration.setDecorationEndOffset(1)
            return itemDecoration
        }

        fun selectAccountIntent(context: Context, status: ParcelableStatus, itemId: Long,
                sameHostOnly: Boolean = true): Intent {
            val intent = Intent(context, AccountSelectorActivity::class.java)
            intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
            if (sameHostOnly) {
                intent.putExtra(EXTRA_ACCOUNT_HOST, status.account_key.host)
            }
            intent.putExtra(EXTRA_SINGLE_SELECTION, true)
            intent.putExtra(EXTRA_EXTRAS, Bundle {
                this[EXTRA_STATUS] = status
                this[EXTRA_ID] = itemId
            })
            return intent
        }


    }
}
