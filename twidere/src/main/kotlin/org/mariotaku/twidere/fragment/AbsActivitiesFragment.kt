/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import android.view.*
import androidx.loader.app.LoaderManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_GAP
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_STATUS
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_STUB
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_TITLE_SUMMARY
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_FROM_USER
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.readFromBottomKey
import org.mariotaku.twidere.constant.rememberPositionKey
import org.mariotaku.twidere.extension.model.activityStatus
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.analyzer.Share
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.glide.PauseRecyclerViewOnScrollListener
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

abstract class AbsActivitiesFragment protected constructor() :
        AbsContentListRecyclerViewFragment<ParcelableActivitiesAdapter>(),
        LoaderCallbacks<List<ParcelableActivity>>, ParcelableActivitiesAdapter.ActivityAdapterListener,
        KeyboardShortcutCallback {

    private lateinit var activitiesBusCallback: Any
    private lateinit var navigationHelper: RecyclerViewNavigationHelper

    private lateinit var pauseOnScrollListener: OnScrollListener

    private val onScrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = layoutManager
                saveReadPosition(layoutManager.findFirstVisibleItemPosition())
            }
        }
    }

    protected open val loaderId: Int
        get() = tabId.toInt().coerceIn(0..Int.MAX_VALUE)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activitiesBusCallback = createMessageBusCallback()
        scrollListener.reversed = preferences[readFromBottomKey]
        adapter.setListener(this)
        registerForContextMenu(recyclerView)
        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this)
        pauseOnScrollListener = PauseRecyclerViewOnScrollListener(
            pauseOnScroll = false, pauseOnFling = false,
            requestManager = requestManager
        )

        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        LoaderManager.getInstance(this).initLoader(loaderId, loaderArgs, this)
        showProgress()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AbsStatusesFragment.REQUEST_FAVORITE_SELECT_ACCOUNT,
            AbsStatusesFragment.REQUEST_RETWEET_SELECT_ACCOUNT -> {
                AbsStatusesFragment.handleActionActivityResult(this, requestCode, resultCode, data)
            }
        }
    }

    abstract fun getActivities(param: RefreshTaskParam): Boolean

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        var action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_REFRESH == action) {
            triggerRefresh()
            return true
        }
        val focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView,
                layoutManager.focusedChild)
        var position = RecyclerView.NO_POSITION
        if (focusedChild != null && focusedChild.parent === recyclerView) {
            position = recyclerView.getChildLayoutPosition(focusedChild)
        }
        if (position != RecyclerView.NO_POSITION) {
            val activity = adapter.getActivity(position)
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                openActivity(activity)
                return true
            }
            val status = activity.activityStatus ?: return false
            if (action == null) {
                action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
            }
            if (action == null) return false
            return AbsStatusesFragment.handleKeyboardShortcutAction(this, action, status,
                    position)
        }
        return navigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    private fun openActivity(activity: ParcelableActivity) {
        val status = activity.activityStatus
        if (status != null) {
            context?.let {
                IntentUtils.openStatus(it, activity, null)
            }
        }
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int,
            event: KeyEvent, metaState: Int): Boolean {
        var action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_REFRESH == action) {
            return true
        }
        if (action == null) {
            action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
        }
        if (action == null) return false
        when (action) {
            ACTION_STATUS_REPLY, ACTION_STATUS_RETWEET, ACTION_STATUS_FAVORITE -> return true
        }
        return navigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
            repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableActivity>> {
        val fromUser = args?.getBoolean(EXTRA_FROM_USER)
        args?.remove(EXTRA_FROM_USER)
        return onCreateActivitiesLoader(requireActivity(), args!!, fromUser!!)
    }

    protected fun saveReadPosition() {
        saveReadPosition(layoutManager.findFirstVisibleItemPosition())
    }

    /**
     * Activities loaded, update adapter data & restore load position
     *
     * Steps:
     * 1. Save current read position if not first load (adapter data is not empty)
     *   1.1 If readFromBottom is true, save position on screen bottom
     * 2. Change adapter data
     * 3. Restore adapter data
     *   3.1 If lastVisible was last item, keep lastVisibleItem position (load more)
     *   3.2 Else, if readFromBottom is true:
     *     3.1.1 If position was first, keep lastVisibleItem position (pull refresh)
     *     3.1.2 Else, keep lastVisibleItem position
     *   3.2 If readFromBottom is false:
     *     3.2.1 If position was first, set new position to 0 (pull refresh)
     *     3.2.2 Else, keep firstVisibleItem position (gap clicked)
     */
    override fun onLoadFinished(loader: Loader<List<ParcelableActivity>>, data: List<ParcelableActivity>) {
        val rememberPosition = preferences[rememberPositionKey]
        val readPositionTag = currentReadPositionTag
        val readFromBottom = preferences[readFromBottomKey]
        val firstLoad = adapterData.isNullOrEmpty()

        var lastReadId = -1L
        var lastReadViewTop = 0
        var loadMore = false
        var wasAtTop = false

        // 1. Save current read position if not first load
        if (!firstLoad) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            wasAtTop = firstVisibleItemPosition == 0
            // Get display range of activities
            val activityRange = rangeOfSize(adapter.activityStartIndex, adapter.getActivityCount(raw = false))
            val lastReadPosition = if (loadMore || readFromBottom) {
                lastVisibleItemPosition
            } else {
                firstVisibleItemPosition
            }.coerceInOr(activityRange, -1)
            lastReadId = if (lastReadPosition < 0) {
                -1
            } else {
                adapter.getTimestamp(lastReadPosition)
            }
            lastReadViewTop = layoutManager.findViewByPosition(lastReadPosition)?.top ?: 0
            loadMore = activityRange.last in 0..lastVisibleItemPosition
        } else if (rememberPosition && readPositionTag != null) {
            val syncManager = timelineSyncManager
            val positionTag = this.readPositionTag
            val syncTag = this.timelineSyncTag
            val currentTag = this.currentReadPositionTag

            if (syncManager != null && positionTag != null && syncTag != null &&
                    syncPreferences.isSyncEnabled(SyncTaskRunner.SYNC_TYPE_TIMELINE_POSITIONS)) {
                lastReadId = syncManager.peekPosition(positionTag, syncTag)
            }
            if (lastReadId <= 0 && currentTag != null) {
                lastReadId = readStateManager.getPosition(currentTag)
            }
        }

        adapter.setData(data)

        refreshEnabled = true

        var restorePosition = -1

        if (loader !is IExtendedLoader || loader.fromUser) {
            if (hasMoreData(data)) {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
            } else {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.NONE
            }
            restorePosition = adapter.findPositionBySortTimestamp(lastReadId)
        }

        if (loadMore) {
            restorePosition += 1
            restorePosition.coerceInOr(0 until layoutManager.itemCount, -1)
        }
        if (restorePosition != -1 && adapter.isActivity(restorePosition) && (loadMore || !wasAtTop ||
                readFromBottom
                || (rememberPosition && firstLoad))) {
            if (layoutManager.height == 0) {
                // RecyclerView has not currently laid out, ignore padding.
                layoutManager.scrollToPositionWithOffset(restorePosition, lastReadViewTop)
            } else {
                layoutManager.scrollToPositionWithOffset(restorePosition, lastReadViewTop - layoutManager.paddingTop)
            }
        }


        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        onContentLoaded(loader, data)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableActivity>>) {
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (userVisibleHint && !isVisibleToUser && host != null) {
            saveReadPosition()
        }
        super.setUserVisibleHint(isVisibleToUser)
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
        getActivities(BaseRefreshTaskParam(accountKeys, pagination).also {
            it.extraId = activity._id
        })
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, position: Int) {
        val status = adapter.getActivity(position).activityStatus ?: return
        activity?.let {
            IntentUtils.openMedia(it, status, media, preferences[newDocumentApiKey],
                preferences[displaySensitiveContentsKey],
                null)
        }
    }

    override fun onStatusActionClick(holder: IStatusViewHolder, id: Int, position: Int) {
        val status = getActivityStatus(position) ?: return
        AbsStatusesFragment.handleActionClick(this, id, status, holder as StatusViewHolder)
    }

    override fun onStatusActionLongClick(holder: IStatusViewHolder, id: Int, position: Int): Boolean {
        val status = getActivityStatus(position) ?: return false
        return AbsStatusesFragment.handleActionLongClick(this, status, adapter.getItemId(position), id)
    }

    override fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int) {
        val activity = getFullActivity(position) ?: return
        val list = ArrayList<Parcelable>()
        if (activity.target_objects?.statuses.isNotNullOrEmpty()) {
            activity.target_objects?.statuses?.addAllTo(list)
        } else if (activity.targets?.statuses.isNotNullOrEmpty()) {
            activity.targets?.statuses?.addAllTo(list)
        }
        activity.sources?.addAllTo(list)
        getActivity()?.let { IntentUtils.openItems(it, list) }
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
        context?.let {
            IntentUtils.openStatus(it, status, null)
        }
    }

    override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = getActivityStatus(position)?.takeIf { it.quoted_id != null } ?: return
        context?.let { IntentUtils.openStatus(it, status.account_key, status.quoted_id) }
    }

    protected open fun getFullActivity(position: Int): ParcelableActivity? {
        return adapter.getActivity(position)
    }

    protected open fun getActivityStatus(position: Int): ParcelableStatus? {
        return getFullActivity(position)?.activityStatus
    }

    override fun onStart() {
        super.onStart()
        recyclerView.addOnScrollListener(onScrollListener)
        recyclerView.addOnScrollListener(pauseOnScrollListener)
        bus.register(activitiesBusCallback)
    }

    override fun onStop() {
        bus.unregister(activitiesBusCallback)
        recyclerView.removeOnScrollListener(pauseOnScrollListener)
        recyclerView.removeOnScrollListener(onScrollListener)
        if (userVisibleHint) {
            saveReadPosition()
        }
        super.onStop()
    }

    override fun scrollToStart(): Boolean {
        val result = super.scrollToStart()
        if (result) {
            saveReadPosition(0)
        }
        return result
    }

    override val reachingEnd: Boolean
        get() {
            val lm = layoutManager
            var lastPosition = lm.findLastCompletelyVisibleItemPosition()
            if (lastPosition == RecyclerView.NO_POSITION) {
                lastPosition = lm.findLastVisibleItemPosition()
            }
            val itemCount = adapter.itemCount
            var finalPos = itemCount - 1
            for (i in lastPosition + 1 until itemCount) {
                if (adapter.getItemViewType(i) != ParcelableActivitiesAdapter.ITEM_VIEW_TYPE_EMPTY) {
                    finalPos = i - 1
                    break
                }
            }
            return finalPos >= itemCount - 1
        }

    protected open fun createMessageBusCallback(): Any {
        return StatusesBusCallback()
    }

    protected abstract val accountKeys: Array<UserKey>

    protected val adapterData: List<ParcelableActivity>?
        get() {
            return adapter.getData()
        }

    protected open val readPositionTag: String?
        @ReadPositionTag
        get() = null

    protected open val timelineSyncTag: String?
        get() = null

    protected abstract fun hasMoreData(data: List<ParcelableActivity>?): Boolean

    protected abstract fun onCreateActivitiesLoader(context: Context, args: Bundle,
            fromUser: Boolean): Loader<List<ParcelableActivity>>

    protected abstract fun onContentLoaded(loader: Loader<List<ParcelableActivity>>, data: List<ParcelableActivity>?)

    @CallSuper
    protected open fun saveReadPosition(position: Int) {
        if (host == null) return
        if (position == RecyclerView.NO_POSITION || adapter.getActivityCount(false) <= 0) return
        val item = adapter.getActivity(position)
        var positionUpdated = false
        readPositionTag?.let { positionTag ->
            accountKeys.forEach { accountKey ->
                val tag = Utils.getReadPositionTagWithAccount(positionTag, accountKey)
                if (readStateManager.setPosition(tag, item.position_key)) {
                    positionUpdated = true
                }
            }
            timelineSyncTag?.let { syncTag ->
                timelineSyncManager?.setPosition(positionTag, syncTag, item.position_key)
            }
            currentReadPositionTag?.let { currentTag ->
                readStateManager.setPosition(currentTag, item.position_key, true)
            }
        }

        if (positionUpdated) {
            twitterWrapper.setActivitiesAboutMeUnreadAsync(accountKeys, item.timestamp)
        }
    }

    override val extraContentPadding: Rect
        get() {
            val paddingVertical = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
            return Rect(0, paddingVertical, 0, paddingVertical)
        }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val position = contextMenuInfo.position
        when (adapter.getItemViewType(position)) {
            ITEM_VIEW_TYPE_STATUS -> {
                val status = getActivityStatus(position) ?: return
                inflater.inflate(R.menu.action_status, menu)
                context?.let {
                    MenuUtils.setupForStatus(it, menu, preferences, twitterWrapper, userColorNameManager,
                            status)
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val position = contextMenuInfo.position
        when (adapter.getItemViewType(position)) {
            ITEM_VIEW_TYPE_STATUS -> {
                val status = getActivityStatus(position) ?: return false
                when (item.itemId) {
                    R.id.share -> {
                        val shareIntent = activity?.let { Utils.createStatusShareIntent(it, status) }
                        val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
                        startActivity(chooser)

                        val am = AccountManager.get(context)
                        val accountType = AccountUtils.findByAccountKey(am, status.account_key)?.getAccountType(am)
                        Analyzer.log(Share.status(accountType, status))
                        return true
                    }
                    R.id.make_gap -> {
                        if (this !is CursorActivitiesFragment) return true
                        val resolver = context?.contentResolver
                        val values = ContentValues()
                        values.put(Activities.IS_GAP, 1)
                        val _id = adapter.getActivity(position)._id
                        val where = Expression.equals(Activities._ID, _id).sql
                        resolver?.update(contentUri, values, where, null)
                        return true
                    }
                    else -> activity?.let {
                        parentFragmentManager.let { fragmentManager ->
                            MenuUtils.handleStatusClick(it, this, fragmentManager,
                                    preferences, userColorNameManager, twitterWrapper, status, item)
                        }
                    }
                }
            }
        }
        return false
    }


    override fun onCreateItemDecoration(context: Context, recyclerView: RecyclerView,
                                        layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val itemDecoration = object : ExtendedDividerItemDecoration(context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation) {
            override fun isDividerEnabled(childPos: Int): Boolean {
                if (childPos >= layoutManager.itemCount || childPos < 0) return false
                return when (adapter.getItemViewType(childPos)) {
                    ITEM_VIEW_TYPE_STATUS, ITEM_VIEW_TYPE_TITLE_SUMMARY, ITEM_VIEW_TYPE_GAP,
                    ITEM_VIEW_TYPE_STUB -> {
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
        val res = context.resources
        if (adapter.profileImageEnabled) {
            val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 2 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
            itemDecoration.setPadding { position, rect ->
                val itemViewType = adapter.getItemViewType(position)
                var nextItemIsStatus = false
                if (position < adapter.itemCount - 1) {
                    nextItemIsStatus = adapter.getItemViewType(position + 1) == ITEM_VIEW_TYPE_STATUS
                }
                if (nextItemIsStatus && itemViewType == ITEM_VIEW_TYPE_STATUS) {
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

    private val currentReadPositionTag: String?
        get() = "${readPositionTag}_${tabId}_current"

    protected inner class StatusesBusCallback {

        @Subscribe
        fun notifyStatusListChanged(event: StatusListChangedEvent) {
            adapter.notifyDataSetChanged()
        }

    }
}
