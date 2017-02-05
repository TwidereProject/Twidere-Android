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
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.*
import com.squareup.otto.Subscribe
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaEvent
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.coerceInOr
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.ktextension.rangeOfSize
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.readFromBottomKey
import org.mariotaku.twidere.constant.rememberPositionKey
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.analyzer.Share
import org.mariotaku.twidere.model.message.StatusListChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.imageloader.PauseRecyclerViewOnScrollListener
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/5.
 */
abstract class AbsStatusesFragment protected constructor() :
        AbsContentListRecyclerViewFragment<ParcelableStatusesAdapter>(),
        LoaderCallbacks<List<ParcelableStatus>?>, IStatusViewHolder.StatusClickListener,
        KeyboardShortcutCallback {

    private lateinit var statusesBusCallback: Any
    private lateinit var navigationHelper: RecyclerViewNavigationHelper
    private var pauseOnScrollListener: OnScrollListener? = null
    var loaderInitialized: Boolean = false
        private set

    private val onScrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = layoutManager
                saveReadPosition(layoutManager.findFirstVisibleItemPosition())
            }
        }
    }

    protected abstract val timelineType: String

    protected abstract val accountKeys: Array<UserKey>

    protected var adapterData: List<ParcelableStatus>?
        get() = adapter.getData()
        set(data) {
            adapter.setData(data)
        }

    @ReadPositionTag
    protected open val readPositionTag: String?
        get() = null

    protected open val readPositionTagWithArguments: String?
        get() = readPositionTag

    protected open val useSortIdAsReadPosition: Boolean = true

    /**
     * Used for 'restore position' feature
     */
    protected open val currentReadPositionTag: String?
        get() = if (readPositionTag == null || tabId < 0) null else "${readPositionTag}_${tabId}_current"

    override val extraContentPadding: Rect
        get() {
            val paddingVertical = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
            return Rect(0, paddingVertical, 0, paddingVertical)
        }

    val shouldInitLoader: Boolean
        get() = (parentFragment as? StatusesFragmentDelegate)?.shouldInitLoader ?: true


    // Fragment life cycles
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusesBusCallback = createMessageBusCallback()
        scrollListener.reversed = preferences[readFromBottomKey]
        adapter.statusClickListener = this
        registerForContextMenu(recyclerView)
        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter, this)
        pauseOnScrollListener = PauseRecyclerViewOnScrollListener(adapter.mediaLoader.imageLoader, false, true)

        if (shouldInitLoader) {
            initLoaderIfNeeded()
        }
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        recyclerView.addOnScrollListener(onScrollListener)
        recyclerView.addOnScrollListener(pauseOnScrollListener)
        bus.register(statusesBusCallback)
    }

    override fun onStop() {
        bus.unregister(statusesBusCallback)
        recyclerView.removeOnScrollListener(pauseOnScrollListener)
        recyclerView.removeOnScrollListener(onScrollListener)
        if (userVisibleHint) {
            saveReadPosition()
        }
        super.onStop()
    }

    override fun onDestroy() {
        adapter.statusClickListener = null
        super.onDestroy()
    }

    abstract fun getStatuses(param: RefreshTaskParam): Boolean

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        var action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_REFRESH == action) {
            triggerRefresh()
            return true
        }
        val focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView,
                layoutManager.focusedChild)
        var position = -1
        if (focusedChild != null && focusedChild.parent === recyclerView) {
            position = recyclerView.getChildLayoutPosition(focusedChild)
        }
        if (position != -1) {
            val status = adapter.getStatus(position) ?: return false
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                IntentUtils.openStatus(activity, status, null)
                return true
            }
            if (action == null) {
                action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
            }
            if (action == null) return false
            when (action) {
                ACTION_STATUS_REPLY -> {
                    val intent = Intent(INTENT_ACTION_REPLY)
                    intent.putExtra(EXTRA_STATUS, status)
                    startActivity(intent)
                    return true
                }
                ACTION_STATUS_RETWEET -> {
                    executeAfterFragmentResumed {
                        RetweetQuoteDialogFragment.show(fragmentManager, status)
                    }
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
        return navigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
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
        return navigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int,
                                              event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableStatus>?> {
        val fromUser = args.getBoolean(EXTRA_FROM_USER)
        args.remove(EXTRA_FROM_USER)
        return onCreateStatusesLoader(activity, args, fromUser)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (userVisibleHint && !isVisibleToUser && host != null) {
            saveReadPosition()
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    /**
     * Statuses loaded, update adapter data & restore load position
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
    override fun onLoadFinished(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        val rememberPosition = preferences[rememberPositionKey]
        val readPositionTag = currentReadPositionTag
        val readFromBottom = preferences[readFromBottomKey]
        val firstLoad = adapterData.isNullOrEmpty()

        var lastReadId: Long = -1
        var lastReadViewTop: Int = 0
        var loadMore = false
        var wasAtTop = false
        // 1. Save current read position if not first load
        if (!firstLoad) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            wasAtTop = firstVisibleItemPosition == 0
            val statusRange = rangeOfSize(adapter.statusStartIndex, adapter.statusCount - 1)
            val lastReadPosition = if (loadMore || readFromBottom) {
                lastVisibleItemPosition
            } else {
                firstVisibleItemPosition
            }.coerceInOr(statusRange, -1)
            lastReadId = if (useSortIdAsReadPosition) {
                adapter.getStatusSortId(lastReadPosition)
            } else {
                adapter.getStatusPositionKey(lastReadPosition)
            }
            lastReadViewTop = layoutManager.findViewByPosition(lastReadPosition)?.top ?: 0
            loadMore = statusRange.endInclusive >= 0 && lastVisibleItemPosition >= statusRange.endInclusive
        } else if (rememberPosition && readPositionTag != null) {
            lastReadId = readStateManager.getPosition(readPositionTag)
            lastReadViewTop = 0
        }
        // 2. Change adapter data
        adapterData = data

        refreshEnabled = true

        var restorePosition = -1

        if (loader !is IExtendedLoader || loader.fromUser) {
            if (hasMoreData(data)) {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
                onHasMoreDataChanged(true)
            } else {
                adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.NONE
                onHasMoreDataChanged(false)
            }
            restorePosition = if (useSortIdAsReadPosition) {
                adapter.findPositionBySortId(lastReadId)
            } else {
                adapter.findPositionByPositionKey(lastReadId)
            }
        } else {
            onHasMoreDataChanged(false)
        }
        if (loadMore) {
            restorePosition += 1
            restorePosition.coerceInOr(0 until layoutManager.itemCount, -1)
        }
        if (restorePosition != -1 && adapter.isStatus(restorePosition) && (loadMore || !wasAtTop
                || readFromBottom || (rememberPosition && firstLoad))) {
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
        onStatusesLoaded(loader, data)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableStatus>?>) {
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
    }


    override fun onGapClick(holder: GapViewHolder, position: Int) {
        val adapter = this.adapter
        val status = adapter.getStatus(position) ?: return
        DebugLog.v(TwidereConstants.LOGTAG, "Load activity gap " + status)
        adapter.addGapLoadingId(ObjectId(status.account_key, status.id))
        val accountIds = arrayOf(status.account_key)
        val maxIds = arrayOf<String?>(status.id)
        val maxSortIds = longArrayOf(status.sort_id)
        getStatuses(BaseRefreshTaskParam(accountIds, maxIds, null, maxSortIds, null))
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int) {
        val status = adapter.getStatus(statusPosition) ?: return
        IntentUtils.openMedia(activity, status, media, preferences[newDocumentApiKey],
                preferences[displaySensitiveContentsKey])
        // BEGIN HotMobi
        val event = MediaEvent.create(activity, status, media, timelineType,
                adapter.mediaPreviewEnabled)
        HotMobiLogger.getInstance(activity).log(status.account_key, event)
        // END HotMobi
    }

    override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
        val status = adapter.getStatus(position) ?: return
        handleStatusActionClick(context, fragmentManager, twitterWrapper, holder as StatusViewHolder, status, id)
    }

    override fun createItemDecoration(context: Context, recyclerView: RecyclerView, layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val itemDecoration = DividerItemDecoration(context, (recyclerView.layoutManager as LinearLayoutManager).orientation)
        val res = context.resources
        if (adapter.profileImageEnabled) {
            val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 2 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
            itemDecoration.setPadding { position, rect ->
                val itemViewType = adapter.getItemViewType(position)
                var nextItemIsStatus = false
                if (position < adapter.itemCount - 1) {
                    nextItemIsStatus = adapter.getItemViewType(position + 1) == ParcelableStatusesAdapter.ITEM_VIEW_TYPE_STATUS
                }
                if (nextItemIsStatus && itemViewType == ParcelableStatusesAdapter.ITEM_VIEW_TYPE_STATUS) {
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

    protected fun saveReadPosition() {
        saveReadPosition(layoutManager.findFirstVisibleItemPosition())
    }

    protected open fun onHasMoreDataChanged(hasMoreData: Boolean) {
    }

    override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position) ?: return
        IntentUtils.openStatus(activity, status, null)
    }

    override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position) ?: return
        val quotedId = status.quoted_id ?: return
        IntentUtils.openStatus(activity, status.account_key, quotedId)
    }

    override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
        //TODO handle long click event
        return true
    }

    override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
        if (activity == null) return
        val view = layoutManager.findViewByPosition(position) ?: return
        recyclerView.showContextMenuForChild(view)
    }

    override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)!!
        val intent = IntentUtils.userProfile(status.account_key, status.user_key,
                status.user_screen_name, Referral.TIMELINE_STATUS,
                status.extras.user_statusnet_profile_url)
        IntentUtils.applyNewDocument(intent, preferences[newDocumentApiKey])
        startActivity(intent)
    }

    override fun scrollToStart(): Boolean {
        val result = super.scrollToStart()
        if (result) {
            saveReadPosition(0)
        }
        return result
    }

    fun initLoaderIfNeeded() {
        if (isDetached || host == null || loaderInitialized) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
        loaderInitialized = true
    }

    protected open fun createMessageBusCallback(): Any {
        return StatusesBusCallback()
    }


    protected fun saveReadPosition(position: Int) {
        if (host == null) return
        if (position == RecyclerView.NO_POSITION) return
        val status = adapter.getStatus(position) ?: return
        val positionKey = if (status.position_key > 0) status.position_key else status.timestamp
        readPositionTagWithArguments?.let {
            accountKeys.map { accountKey -> Utils.getReadPositionTagWithAccount(it, accountKey) }
                    .forEach { readStateManager.setPosition(it, positionKey) }
        }
        currentReadPositionTag?.let {
            readStateManager.setPosition(it, positionKey, true)
        }
    }

    protected abstract fun hasMoreData(data: List<ParcelableStatus>?): Boolean

    protected abstract fun onCreateStatusesLoader(context: Context, args: Bundle,
                                                  fromUser: Boolean): Loader<List<ParcelableStatus>?>

    protected abstract fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?)

    // Context Menu functions

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val status = adapter.getStatus(contextMenuInfo!!.position)
        inflater.inflate(R.menu.action_status, menu)
        MenuUtils.setupForStatus(context, preferences, menu, status!!, twitterWrapper,
                userColorNameManager)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val status = adapter.getStatus(contextMenuInfo.position) ?: return false
        if (item.itemId == R.id.share) {
            val shareIntent = Utils.createStatusShareIntent(activity, status)
            val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
            startActivity(chooser)

            val am = AccountManager.get(context)
            val accountType = AccountUtils.findByAccountKey(am, status.account_key)?.getAccountType(am)
            Analyzer.log(Share.status(accountType, status))
            return true
        }
        return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                userColorNameManager, twitterWrapper, status, item)
    }

    class DefaultOnLikedListener(
            private val twitter: AsyncTwitterWrapper,
            private val status: ParcelableStatus
    ) : LikeAnimationDrawable.OnLikedListener {

        override fun onLiked(): Boolean {
            if (status.is_favorite) return false
            twitter.createFavoriteAsync(status.account_key, status.id)
            return true
        }
    }

    protected inner class StatusesBusCallback {

        @Subscribe
        fun notifyStatusListChanged(event: StatusListChangedEvent) {
            adapter.notifyDataSetChanged()
        }

    }

    interface StatusesFragmentDelegate {
        val shouldInitLoader: Boolean
    }

    companion object {

        fun handleStatusActionClick(context: Context, fm: FragmentManager,
                                    twitter: AsyncTwitterWrapper?, holder: StatusViewHolder,
                                    status: ParcelableStatus?, id: Int) {

            if (status == null) return
            when (id) {
                R.id.reply -> {
                    val intent = Intent(INTENT_ACTION_REPLY)
                    intent.`package` = context.packageName
                    intent.putExtra(EXTRA_STATUS, status)
                    context.startActivity(intent)
                }
                R.id.retweet -> {
                    RetweetQuoteDialogFragment.show(fm, status)
                }
                R.id.favorite -> {
                    if (twitter == null) return
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(status.account_key, status.id)
                    } else {
                        holder.playLikeAnimation(DefaultOnLikedListener(twitter,
                                status))
                    }
                }
            }
        }
    }
}
