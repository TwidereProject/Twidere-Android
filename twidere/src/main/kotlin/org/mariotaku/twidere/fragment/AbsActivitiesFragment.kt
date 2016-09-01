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

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.util.Log
import android.view.*
import com.squareup.otto.Subscribe
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaEvent
import edu.tsinghua.hotmobi.model.ScrollRecord
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_GAP
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_STATUS
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_STUB
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter.Companion.ITEM_VIEW_TYPE_TITLE_SUMMARY
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.fragment.AbsStatusesFragment.DefaultOnLikedListener
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.message.StatusListChangedEvent
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.model.util.getActivityStatus
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.imageloader.PauseRecyclerViewOnScrollListener
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.util.*

abstract class AbsActivitiesFragment protected constructor() : AbsContentListRecyclerViewFragment<ParcelableActivitiesAdapter>(), LoaderCallbacks<List<ParcelableActivity>>, ParcelableActivitiesAdapter.ActivityAdapterListener, KeyboardShortcutCallback {

    private val statusesBusCallback: Any
    private val hotMobiScrollTracker = object : OnScrollListener() {

        var records: MutableList<ScrollRecord>? = null
        private var firstVisibleTimestamp: Long = -1
        private var firstVisibleAccountId: UserKey? = null
        private var firstVisiblePosition = -1
        private var scrollState: Int = 0

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            val layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
            val pos = layoutManager.findFirstVisibleItemPosition()
            if (pos != firstVisiblePosition && pos >= 0) {
                //noinspection unchecked
                val adapter = recyclerView.adapter as ParcelableActivitiesAdapter
                val activity = adapter.getActivity(pos)
                if (activity != null) {
                    val timestamp = activity.timestamp
                    val accountKey = activity.account_key
                    if (timestamp != firstVisibleTimestamp || accountKey != firstVisibleAccountId) {
                        if (records == null) records = ArrayList<ScrollRecord>()
                        val time = System.currentTimeMillis()
                        records!!.add(ScrollRecord.create(timestamp.toString(), accountKey, time,
                                TimeZone.getDefault().getOffset(time).toLong(), scrollState))
                    }
                    firstVisibleTimestamp = timestamp
                    firstVisibleAccountId = accountKey
                }
            }
            firstVisiblePosition = pos
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            scrollState = newState
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (records != null) {
                    HotMobiLogger.getInstance(activity).logList(records, null, "scroll")
                }
                records = null
            }
        }
    }

    private val onScrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = layoutManager ?: return
                saveReadPosition(layoutManager.findFirstVisibleItemPosition())
            }
        }
    }

    private var navigationHelper: RecyclerViewNavigationHelper? = null
    private var pauseOnScrollListener: OnScrollListener? = null
    private var activeHotMobiScrollTracker: OnScrollListener? = null

    init {
        statusesBusCallback = createMessageBusCallback()
    }

    abstract fun getActivities(param: RefreshTaskParam): Boolean

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        var action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_REFRESH == action) {
            triggerRefresh()
            return true
        }
        val layoutManager = layoutManager
        if (recyclerView == null || layoutManager == null) return false
        val focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView,
                layoutManager.focusedChild)
        var position = RecyclerView.NO_POSITION
        if (focusedChild != null && focusedChild.parent === recyclerView) {
            position = recyclerView!!.getChildLayoutPosition(focusedChild)
        }
        if (position != RecyclerView.NO_POSITION) {
            val activity = adapter!!.getActivity(position) ?: return false
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                openActivity(activity)
                return true
            }
            val status = activity.getActivityStatus() ?: return false
            if (action == null) {
                action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
            }
            if (action == null) return false
            when (action) {
                ACTION_STATUS_REPLY -> {
                    val intent = Intent(IntentConstants.INTENT_ACTION_REPLY)
                    intent.putExtra(IntentConstants.EXTRA_STATUS, status)
                    startActivity(intent)
                    return true
                }
                ACTION_STATUS_RETWEET -> {
                    RetweetQuoteDialogFragment.show(fragmentManager, status)
                    return true
                }
                ACTION_STATUS_FAVORITE -> {
                    val twitter = twitterWrapper
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(status.account_key, status.id)
                    } else {
                        val holder = recyclerView.findViewHolderForLayoutPosition(position) as StatusViewHolder
                        holder.playLikeAnimation(DefaultOnLikedListener(twitter, status))
                    }
                    return true
                }
            }
        }
        return navigationHelper!!.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    private fun openActivity(activity: ParcelableActivity) {
        val status = activity.getActivityStatus()
        if (status != null) {
            IntentUtils.openStatus(context, status, null)
        }
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
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
        return navigationHelper!!.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int,
                                              event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper!!.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableActivity>> {
        val fromUser = args.getBoolean(IntentConstants.EXTRA_FROM_USER)
        args.remove(IntentConstants.EXTRA_FROM_USER)
        return onCreateActivitiesLoader(activity, args, fromUser)
    }

    protected fun saveReadPosition() {
        val layoutManager = layoutManager
        if (layoutManager != null) {
            saveReadPosition(layoutManager.findFirstVisibleItemPosition())
        }
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableActivity>>, data: List<ParcelableActivity>) {
        val adapter = adapter
        val rememberPosition = preferences.getBoolean(KEY_REMEMBER_POSITION, false)
        val readFromBottom = preferences.getBoolean(KEY_READ_FROM_BOTTOM, false)
        var lastReadId: Long
        val lastVisiblePos: Int
        val lastVisibleTop: Int
        val tag = currentReadPositionTag
        val layoutManager = layoutManager
        if (readFromBottom) {
            lastVisiblePos = layoutManager!!.findLastVisibleItemPosition()
        } else {
            lastVisiblePos = layoutManager!!.findFirstVisibleItemPosition()
        }
        if (lastVisiblePos != RecyclerView.NO_POSITION && lastVisiblePos < adapter!!.itemCount) {
            val activityStartIndex = adapter.activityStartIndex
            val activityEndIndex = activityStartIndex + adapter.activityCount
            val lastItemIndex = Math.min(activityEndIndex, lastVisiblePos)
            lastReadId = adapter.getTimestamp(lastItemIndex)
            val positionView = layoutManager.findViewByPosition(lastItemIndex)
            lastVisibleTop = if (positionView != null) positionView.top else 0
        } else if (rememberPosition && tag != null) {
            lastReadId = readStateManager.getPosition(tag)
            lastVisibleTop = 0
        } else {
            lastReadId = -1
            lastVisibleTop = 0
        }
        adapter!!.setData(data)
        val activityStartIndex = adapter.activityStartIndex
        // The last activity is activityEndExclusiveIndex - 1
        val activityEndExclusiveIndex = activityStartIndex + adapter.activityCount

        if (activityEndExclusiveIndex >= 0 && rememberPosition && tag != null) {
            val lastItemId = adapter.getTimestamp(activityEndExclusiveIndex)
            // Activity corresponds to last read timestamp was deleted, use last item timestamp
            // instead
            if (lastItemId > 0 && lastReadId < lastItemId) {
                lastReadId = lastItemId
            }
        }

        refreshEnabled = true
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData(data)) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
            var pos = -1
            for (i in activityStartIndex..activityEndExclusiveIndex - 1) {
                if (lastReadId != -1L && adapter.getTimestamp(i) <= lastReadId) {
                    pos = i
                    break
                }
            }
            if (pos != -1 && adapter.isActivity(pos) && (readFromBottom || lastVisiblePos != 0)) {
                if (layoutManager.height == 0) {
                    // RecyclerView has not currently laid out, ignore padding.
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop)
                } else {
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop - layoutManager.paddingTop)
                }
            }
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        onLoadingFinished()
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
        val activity = adapter?.getActivity(position) ?: return
        if (BuildConfig.DEBUG) {
            Log.v(TwidereConstants.LOGTAG, "Load activity gap $activity")
        }
        val accountIds = arrayOf(activity.account_key)
        val maxIds = arrayOf(activity.min_position)
        val maxSortIds = longArrayOf(activity.min_sort_position)
        getActivities(BaseRefreshTaskParam(accountIds, maxIds, null, maxSortIds, null))
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, position: Int) {
        val adapter = adapter ?: return
        val status = adapter.getActivity(position)?.getActivityStatus() ?: return
        IntentUtils.openMedia(activity, status, media, null, preferences.getBoolean(KEY_NEW_DOCUMENT_API))
        // BEGIN HotMobi
        val event = MediaEvent.create(activity, status, media, timelineType, adapter.mediaPreviewEnabled)
        HotMobiLogger.getInstance(activity).log(status.account_key, event)
        // END HotMobi
    }

    protected abstract val timelineType: String

    override fun onStatusActionClick(holder: IStatusViewHolder, id: Int, position: Int) {
        val status = getActivityStatus(position) ?: return
        val activity = activity
        when (id) {
            R.id.reply -> {
                val intent = Intent(IntentConstants.INTENT_ACTION_REPLY)
                intent.`package` = activity.packageName
                intent.putExtra(IntentConstants.EXTRA_STATUS, status)
                activity.startActivity(intent)
            }
            R.id.retweet -> {
                RetweetQuoteDialogFragment.show(fragmentManager, status)
            }
            R.id.favorite -> {
                if (status.is_favorite) {
                    twitterWrapper.destroyFavoriteAsync(status.account_key, status.id)
                } else {
                    holder.playLikeAnimation(DefaultOnLikedListener(twitterWrapper, status))
                }
            }
        }
    }

    override fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int) {
        val activity = adapter!!.getActivity(position) ?: return
        val list = ArrayList<Parcelable>()
        if (activity.target_object_statuses?.isNotEmpty() ?: false) {
            list.addAll(activity.target_object_statuses)
        } else if (activity.target_statuses?.isNotEmpty() ?: false) {
            list.addAll(activity.target_statuses)
        }
        list.addAll(ParcelableActivityUtils.getAfterFilteredSources(activity))
        IntentUtils.openItems(getActivity(), list)
    }

    override fun onStatusMenuClick(holder: IStatusViewHolder, menuView: View, position: Int) {
        if (activity == null) return
        val lm = layoutManager ?: return
        val view = lm.findViewByPosition(position) ?: return
        if (lm.getItemViewType(view) != ITEM_VIEW_TYPE_STATUS) {
            return
        }
        recyclerView.showContextMenuForChild(view)
    }

    override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = getActivityStatus(position) ?: return
        IntentUtils.openStatus(context, status, null)
    }

    private fun getActivityStatus(position: Int): ParcelableStatus? {
        return adapter?.getActivity(position)?.getActivityStatus()
    }

    override fun onStart() {
        super.onStart()
        recyclerView.addOnScrollListener(onScrollListener)
        recyclerView.addOnScrollListener(pauseOnScrollListener)
        val task = object : AbstractTask<Any?, Boolean, RecyclerView>() {
            public override fun doLongOperation(params: Any?): Boolean {
                val context = context ?: return false
                val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE)
                if (!prefs.getBoolean(KEY_USAGE_STATISTICS, false)) return false
                val logFile = HotMobiLogger.getLogFile(context, null, "scroll")
                return logFile.length() < 131072
            }

            public override fun afterExecute(recyclerView: RecyclerView?, result: Boolean?) {
                if (result!!) {
                    activeHotMobiScrollTracker = hotMobiScrollTracker
                    recyclerView!!.addOnScrollListener(activeHotMobiScrollTracker)
                }
            }
        }
        task.callback = recyclerView
        TaskStarter.execute(task)
        bus.register(statusesBusCallback)
    }

    override fun onStop() {
        bus.unregister(statusesBusCallback)
        if (activeHotMobiScrollTracker != null) {
            recyclerView.removeOnScrollListener(activeHotMobiScrollTracker)
        }
        activeHotMobiScrollTracker = null
        recyclerView.removeOnScrollListener(pauseOnScrollListener)
        recyclerView.removeOnScrollListener(onScrollListener)
        if (userVisibleHint) {
            saveReadPosition()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun scrollToStart(): Boolean {
        val result = super.scrollToStart()
        if (result) {
            saveReadPosition(0)
        }
        return result
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        scrollListener!!.reversed = preferences.getBoolean(KEY_READ_FROM_BOTTOM)
        val adapter = adapter
        val layoutManager = layoutManager
        adapter!!.setListener(this)
        registerForContextMenu(recyclerView)
        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager!!, adapter,
                this)
        pauseOnScrollListener = PauseRecyclerViewOnScrollListener(adapter.mediaLoader.imageLoader, false, true)

        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(IntentConstants.EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
        showProgress()
    }

    override val reachingEnd: Boolean
        get() {
            val lm = layoutManager
            val adapter = adapter
            val lastPosition = lm!!.findLastCompletelyVisibleItemPosition()
            val itemCount = adapter!!.itemCount
            var finalPos = itemCount - 1
            for (i in lastPosition + 1..itemCount - 1) {
                if (adapter.getItemViewType(i) != ParcelableActivitiesAdapter.ITEM_VIEW_TYPE_EMPTY) {
                    finalPos = i - 1
                    break
                }
            }
            return finalPos >= itemCount - 1
        }

    override val reachingStart: Boolean
        get() = super.reachingStart

    protected open fun createMessageBusCallback(): Any {
        return StatusesBusCallback()
    }

    protected abstract val accountKeys: Array<UserKey>

    protected val adapterData: List<ParcelableActivity>?
        get() {
            return adapter?.getData()
        }

    protected open val readPositionTag: String?
        @ReadPositionTag
        get() = null

    protected abstract fun hasMoreData(data: List<ParcelableActivity>?): Boolean

    protected abstract fun onCreateActivitiesLoader(context: Context, args: Bundle,
                                                    fromUser: Boolean): Loader<List<ParcelableActivity>>

    protected abstract fun onLoadingFinished()

    protected fun saveReadPosition(position: Int) {
        if (host == null) return
        if (position == RecyclerView.NO_POSITION) return
        val item = adapter!!.getActivity(position) ?: return
        var positionUpdated = false
        readPositionTag?.let {
            for (accountKey in accountKeys) {
                val tag = Utils.getReadPositionTagWithAccount(it, accountKey)
                if (readStateManager.setPosition(tag, item.timestamp)) {
                    positionUpdated = true
                }
            }
        }
        currentReadPositionTag?.let {
            readStateManager.setPosition(it, item.timestamp, true)
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
        val adapter = adapter
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val position = contextMenuInfo!!.position
        when (adapter!!.getItemViewType(position)) {
            ITEM_VIEW_TYPE_STATUS -> {
                val status = getActivityStatus(position) ?: return
                inflater.inflate(R.menu.action_status, menu)
                MenuUtils.setupForStatus(context, preferences, menu, status,
                        twitterWrapper)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (!userVisibleHint) return false
        val adapter = adapter
        val contextMenuInfo = item!!.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val position = contextMenuInfo.position

        when (adapter!!.getItemViewType(position)) {
            ITEM_VIEW_TYPE_STATUS -> {
                val status = getActivityStatus(position) ?: return false
                if (item.itemId == R.id.share) {
                    val shareIntent = Utils.createStatusShareIntent(activity, status)
                    val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
                    Utils.addCopyLinkIntent(context, chooser, LinkCreator.getStatusWebLink(status))
                    startActivity(chooser)
                    return true
                }
                return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                        userColorNameManager, twitterWrapper, status, item)
            }
        }
        return false
    }


    override fun createItemDecoration(context: Context, recyclerView: RecyclerView,
                                      layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val adapter = adapter!!
        val itemDecoration = object : DividerItemDecoration(context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation) {
            override fun isDividerEnabled(childPos: Int): Boolean {
                when (adapter.getItemViewType(childPos)) {
                    ITEM_VIEW_TYPE_STATUS, ITEM_VIEW_TYPE_TITLE_SUMMARY, ITEM_VIEW_TYPE_GAP,
                    ITEM_VIEW_TYPE_STUB -> {
                        return true
                    }
                    else -> {
                        return false
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
            adapter!!.notifyDataSetChanged()
        }

    }
}
