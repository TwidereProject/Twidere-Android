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
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
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
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.fragment.status.FavoriteConfirmDialogFragment
import org.mariotaku.twidere.fragment.status.RetweetQuoteDialogFragment
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.analyzer.Share
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.glide.PauseRecyclerViewOnScrollListener
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/5.
 */
abstract class AbsStatusesFragment : AbsContentListRecyclerViewFragment<ParcelableStatusesAdapter>(),
        LoaderCallbacks<List<ParcelableStatus>?>, IStatusViewHolder.StatusClickListener,
        KeyboardShortcutCallback {

    private lateinit var statusesBusCallback: Any
    private lateinit var navigationHelper: RecyclerViewNavigationHelper
    private var pauseOnScrollListener: OnScrollListener? = null
    var loaderInitialized: Boolean = false
        private set

    private val onScrollListener = object : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = layoutManager
                saveReadPosition(layoutManager.findFirstVisibleItemPosition())
            }
        }
    }

    protected abstract val accountKeys: Array<UserKey>

    protected var adapterData: List<ParcelableStatus>?
        get() = adapter.getData()
        set(data) {
            adapter.setData(data)
        }

    @ReadPositionTag
    protected open val readPositionTag: String?
        get() = null

    protected open val timelineSyncTag: String?
        get() = null

    protected open val readPositionTagWithArguments: String?
        get() = readPositionTag

    protected open val useSortIdAsReadPosition: Boolean = true

    /**
     * Used for 'restore position' feature
     */
    protected open val currentReadPositionTag: String?
        get() {
            val positionTag = readPositionTagWithArguments ?: readPositionTag ?: return null
            return if (tabId < 0) null else "${positionTag}_${tabId}_current"
        }

    override val extraContentPadding: Rect
        get() {
            val paddingVertical = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
            return Rect(0, paddingVertical, 0, paddingVertical)
        }

    val shouldInitLoader: Boolean
        get() = (parentFragment as? StatusesFragmentDelegate)?.shouldInitLoader ?: true


    protected open val enableTimelineFilter: Boolean = false

    protected open val timelineFilter: TimelineFilter? = null

    protected open val loaderId: Int
        get() = tabId.toInt().coerceIn(0..Int.MAX_VALUE)

    // Fragment life cycles
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusesBusCallback = createMessageBusCallback()
        scrollListener.reversed = preferences[readFromBottomKey]
        adapter.statusClickListener = this
        registerForContextMenu(recyclerView)
        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter, this)
        pauseOnScrollListener = PauseRecyclerViewOnScrollListener(
            pauseOnScroll = false, pauseOnFling = false,
            requestManager = requestManager
        )

        if (shouldInitLoader) {
            initLoaderIfNeeded()
        }
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        recyclerView.addOnScrollListener(onScrollListener)
        pauseOnScrollListener?.let { recyclerView.addOnScrollListener(it) }
        bus.register(statusesBusCallback)
    }

    override fun onStop() {
        bus.unregister(statusesBusCallback)
        pauseOnScrollListener?.let { recyclerView.removeOnScrollListener(it) }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_FAVORITE_SELECT_ACCOUNT, REQUEST_RETWEET_SELECT_ACCOUNT -> {
                handleActionActivityResult(this, requestCode, resultCode, data)
            }
        }
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
            val status = adapter.getStatus(position)
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                activity?.let {
                    IntentUtils.openStatus(it, status, null)
                }
                return true
            }
            if (action == null) {
                action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
            }
            if (action == null) return false
            return handleKeyboardShortcutAction(this, action, status, position)
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

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableStatus>?> {
        val fromUser = args?.getBoolean(EXTRA_FROM_USER)
        args?.remove(EXTRA_FROM_USER)
        return onCreateStatusesLoader(requireActivity(), args!!, fromUser!!)
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
        val currentReadPositionTag = currentReadPositionTag
        val readFromBottom = preferences[readFromBottomKey]
        val firstLoad = adapterData.isNullOrEmpty()

        var lastReadId: Long = -1
        var lastReadViewTop = 0
        var loadMore = false
        var wasAtTop = false
        // 1. Save current read position if not first load
        if (!firstLoad) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            wasAtTop = firstVisibleItemPosition == 0
            // Get display range of statuses
            val statusRange = rangeOfSize(adapter.statusStartIndex, adapter.getStatusCount(raw = false))
            val lastReadPosition = if (loadMore || readFromBottom) {
                lastVisibleItemPosition
            } else {
                firstVisibleItemPosition
            }.coerceInOr(statusRange, -1)
            lastReadId = when {
                lastReadPosition < 0 -> {
                    -1
                }
                useSortIdAsReadPosition -> {
                    adapter.getStatusSortId(lastReadPosition, false)
                }
                else -> {
                    adapter.getStatusPositionKey(lastReadPosition)
                }
            }
            lastReadViewTop = layoutManager.findViewByPosition(lastReadPosition)?.top ?: 0
            loadMore = statusRange.last in 0..lastVisibleItemPosition
        } else if (rememberPosition) {
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

            lastReadViewTop = 0
        }
        // 2. Change adapter data
        adapterData = data
        adapter.timelineFilter = timelineFilter

        refreshEnabled = true

        var restorePosition = -1

        if (loader !is IExtendedLoader || loader.fromUser) {
            if (hasMoreData(loader, data)) {
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
        val status = adapter.getStatus(position)
        DebugLog.v(msg = "Load activity gap $status")
        adapter.addGapLoadingId(ObjectId(status.account_key, status.id))
        val accountKeys = arrayOf(status.account_key)
        val pagination = arrayOf(SinceMaxPagination.maxId(status.id, status.sort_id))
        getStatuses(BaseRefreshTaskParam(accountKeys, pagination))
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia,
            statusPosition: Int) {
        val status = adapter.getStatus(statusPosition)
        activity?.let {
            IntentUtils.openMedia(it, status, current, preferences[newDocumentApiKey],
                    preferences[displaySensitiveContentsKey])
        }
    }

    override fun onQuotedMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia,
            statusPosition: Int) {
        val status = adapter.getStatus(statusPosition)
        val quotedMedia = status.quoted_media ?: return
        activity?.let {
            IntentUtils.openMedia(it, status.account_key, status.is_possibly_sensitive, status,
                    current, quotedMedia, preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey])
        }
    }

    override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
        val status = getFullStatus(position) ?: return
        handleActionClick(this@AbsStatusesFragment, id, status, holder as StatusViewHolder)
    }

    override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
        val status = getFullStatus(position) ?: return false
        return handleActionLongClick(this, status, adapter.getItemId(position), id)
    }

    override fun onCreateItemDecoration(context: Context, recyclerView: RecyclerView, layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
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

    protected fun saveReadPosition() {
        saveReadPosition(layoutManager.findFirstVisibleItemPosition())
    }

    protected open fun onHasMoreDataChanged(hasMoreData: Boolean) {
    }

    override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = getFullStatus(position) ?: return
        activity?.let {
            IntentUtils.openStatus(it, status, null)
        }
    }

    override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        val quotedId = status.quoted_id ?: return
        activity?.let {
            IntentUtils.openStatus(it, status.account_key, quotedId)
        }
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
        val status = adapter.getStatus(position)
        val intent = IntentUtils.userProfile(status.account_key, status.user_key,
                status.user_screen_name, status.extras?.user_statusnet_profile_url)
        IntentUtils.applyNewDocument(intent, preferences[newDocumentApiKey])
        startActivity(intent)
    }

    override fun onLinkClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        val url = status.extras?.entities_url?.firstOrNull()
        OnLinkClickHandler.openLink(requireContext(), preferences, Uri.parse(url))
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
        LoaderManager.getInstance(this).initLoader(loaderId, loaderArgs, this)
        loaderInitialized = true
    }

    protected open fun createMessageBusCallback(): Any {
        return StatusesBusCallback()
    }

    @CallSuper
    protected open fun saveReadPosition(position: Int) {
        if (host == null) return
        if (position == RecyclerView.NO_POSITION || adapter.getStatusCount(false) <= 0) return
        val status = adapter.getStatus(position.coerceIn(rangeOfSize(adapter.statusStartIndex,
                adapter.getStatusCount(false))))
        val readPosition = if (useSortIdAsReadPosition) {
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
        timelineSyncTag?.let { syncTag ->
            timelineSyncManager?.setPosition(positionTag, syncTag, status.position_key)
        }
        currentReadPositionTag?.let {
            readStateManager.setPosition(it, readPosition, true)
        }
    }

    protected open fun getFullStatus(position: Int): ParcelableStatus? {
        return adapter.getStatus(position)
    }

    protected abstract fun hasMoreData(loader: Loader<List<ParcelableStatus>?>,
                                       data: List<ParcelableStatus>?): Boolean

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
        context?.let {
            MenuUtils.setupForStatus(it, menu, preferences, twitterWrapper, userColorNameManager,
                status)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val status = adapter.getStatus(contextMenuInfo.position)
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
                if (this !is CursorStatusesFragment) return true
                val resolver = context?.contentResolver
                val values = ContentValues()
                values.put(Statuses.IS_GAP, 1)
                val where = Expression.equals(Statuses._ID, status._id).sql
                resolver?.update(contentUri, values, where, null)
                return true
            }
            else -> return MenuUtils.handleStatusClick(requireActivity(), this, parentFragmentManager,
                    preferences, userColorNameManager, twitterWrapper, status, item)
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

        const val REQUEST_FAVORITE_SELECT_ACCOUNT = 101
        const val REQUEST_RETWEET_SELECT_ACCOUNT = 102

        fun handleActionClick(fragment: BaseFragment, id: Int, status: ParcelableStatus,
                holder: StatusViewHolder) {
            when (id) {
                R.id.reply -> {
                    val intent = Intent(INTENT_ACTION_REPLY)
                    intent.`package` = fragment.context?.packageName
                    intent.putExtra(EXTRA_STATUS, status)
                    fragment.startActivity(intent)
                }
                R.id.retweet -> {
                    fragment.executeAfterFragmentResumed { fragment ->
                        RetweetQuoteDialogFragment.show(fragment.childFragmentManager,
                                status.account_key, status.id, status)
                    }
                }
                R.id.favorite -> {
                    when {
                        fragment.preferences[favoriteConfirmationKey] -> {
                            fragment.executeAfterFragmentResumed {
                                FavoriteConfirmDialogFragment.show(it.childFragmentManager,
                                    status.account_key, status.id, status)
                            }
                        }
                        status.is_favorite -> {
                            fragment.twitterWrapper.destroyFavoriteAsync(status.account_key, status.id)
                        }
                        else -> {
                            holder.playLikeAnimation(DefaultOnLikedListener(fragment.twitterWrapper, status))
                        }
                    }
                }
            }
        }

        fun handleActionLongClick(fragment: Fragment, status: ParcelableStatus, itemId: Long, id: Int): Boolean {
            when (id) {
                R.id.favorite -> {
                    val intent = fragment.context?.let { selectAccountIntent(it, status, itemId) }
                    fragment.startActivityForResult(intent, REQUEST_FAVORITE_SELECT_ACCOUNT)
                    return true
                }
                R.id.retweet -> {
                    val intent = fragment.context?.let { selectAccountIntent(it, status, itemId, false) }
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
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)!!
                    val extras = data.getBundleExtra(EXTRA_EXTRAS)!!
                    val status = extras.getParcelable<ParcelableStatus>(EXTRA_STATUS)!!
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
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)!!
                    val extras = data.getBundleExtra(EXTRA_EXTRAS)!!
                    val status = extras.getParcelable<ParcelableStatus>(EXTRA_STATUS)!!
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
                    when {
                        fragment.preferences[favoriteConfirmationKey] -> {
                            fragment.executeAfterFragmentResumed {
                                FavoriteConfirmDialogFragment.show(it.childFragmentManager,
                                    status.account_key, status.id, status)
                            }
                        }
                        status.is_favorite -> {
                            fragment.twitterWrapper.destroyFavoriteAsync(status.account_key, status.id)
                        }
                        else -> {
                            val holder = fragment.recyclerView.findViewHolderForLayoutPosition(position) as StatusViewHolder
                            holder.playLikeAnimation(DefaultOnLikedListener(fragment.twitterWrapper, status))
                        }
                    }
                    return true
                }
            }
            return false
        }

    }
}
