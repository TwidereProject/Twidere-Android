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
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.*
import com.bumptech.glide.Glide
import com.squareup.otto.Subscribe
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaEvent
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.analyzer.Share
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.glide.PauseRecyclerViewOnScrollListener
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
        pauseOnScrollListener = PauseRecyclerViewOnScrollListener(false, false, Glide.with(this))

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
            // Get display range of statuses
            val statusRange = rangeOfSize(adapter.statusStartIndex, adapter.getStatusCount(raw = false))
            val lastReadPosition = if (loadMore || readFromBottom) {
                lastVisibleItemPosition
            } else {
                firstVisibleItemPosition
            }.coerceInOr(statusRange, -1)
            lastReadId = if (useSortIdAsReadPosition) {
                adapter.getStatusSortId(lastReadPosition, false)
            } else {
                adapter.getStatusPositionKey(lastReadPosition)
            }
            lastReadViewTop = layoutManager.findViewByPosition(lastReadPosition)?.top ?: 0
            loadMore = statusRange.endInclusive in 0..lastVisibleItemPosition
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
        val status = adapter.getStatus(position)
        DebugLog.v(msg = "Load activity gap $status")
        adapter.addGapLoadingId(ObjectId(status.account_key, status.id))
        val accountIds = arrayOf(status.account_key)
        val maxIds = arrayOf<String?>(status.id)
        val maxSortIds = longArrayOf(status.sort_id)
        getStatuses(BaseRefreshTaskParam(accountKeys = accountIds, maxIds = maxIds, sinceIds = null,
                maxSortIds = maxSortIds, sinceSortIds = null))
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia,
            statusPosition: Int) {
        val status = adapter.getStatus(statusPosition)
        IntentUtils.openMedia(activity, status, current, preferences[newDocumentApiKey],
                preferences[displaySensitiveContentsKey])
        // BEGIN HotMobi
        val event = MediaEvent.create(activity, status, current, timelineType,
                adapter.mediaPreviewEnabled)
        HotMobiLogger.getInstance(activity).log(status.account_key, event)
        // END HotMobi
    }

    override fun onQuotedMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia,
            statusPosition: Int) {
        val status = adapter.getStatus(statusPosition)
        val quotedMedia = status.quoted_media ?: return
        IntentUtils.openMedia(activity, status.account_key, status.is_possibly_sensitive, status,
                current, quotedMedia, preferences[newDocumentApiKey], preferences[displaySensitiveContentsKey])
        // BEGIN HotMobi
        val event = MediaEvent.create(activity, status, current, timelineType,
                adapter.mediaPreviewEnabled)
        HotMobiLogger.getInstance(activity).log(status.account_key, event)
        // END HotMobi
    }

    override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
        val status = adapter.getStatus(position)
        handleActionClick(holder as StatusViewHolder, status, id)
    }

    override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
        val status = adapter.getStatus(position)
        return handleActionLongClick(this, status, adapter.getItemId(position), id)
    }

    override fun createItemDecoration(context: Context, recyclerView: RecyclerView, layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
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
        val status = adapter.getStatus(position)
        IntentUtils.openStatus(activity, status, null)
    }

    override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
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
        val status = adapter.getStatus(position)
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
        if (position == RecyclerView.NO_POSITION || adapter.getStatusCount(false) <= 0) return
        val status = adapter.getStatus(position)
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
        MenuUtils.setupForStatus(context, preferences, menu, status, twitterWrapper,
                userColorNameManager)
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
                if (this !is CursorStatusesFragment) return true
                val resolver = context.contentResolver
                val values = ContentValues()
                values.put(Statuses.IS_GAP, 1)
                val where = Expression.equalsArgs(Statuses._ID).sql
                val whereArgs = arrayOf(status._id.toString())
                resolver.update(contentUri, values, where, whereArgs)
                return true
            }
            else -> return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                    userColorNameManager, twitterWrapper, status, item)
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

        fun BaseFragment.handleActionClick(holder: StatusViewHolder, status: ParcelableStatus, id: Int) {
            when (id) {
                R.id.reply -> {
                    val intent = Intent(INTENT_ACTION_REPLY)
                    intent.`package` = context.packageName
                    intent.putExtra(EXTRA_STATUS, status)
                    startActivity(intent)
                }
                R.id.retweet -> {
                    executeAfterFragmentResumed { fragment ->
                        RetweetQuoteDialogFragment.show(fragment.childFragmentManager, status)
                    }
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

        fun handleActionLongClick(fragment: Fragment, status: ParcelableStatus, itemId: Long, id: Int): Boolean {
            when (id) {
                R.id.favorite -> {
                    val intent = selectAccountIntent(fragment.context, status, itemId)
                    fragment.startActivityForResult(intent, REQUEST_FAVORITE_SELECT_ACCOUNT)
                    return true
                }
                R.id.retweet -> {
                    val intent = selectAccountIntent(fragment.context, status, itemId)
                    fragment.startActivityForResult(intent, REQUEST_RETWEET_SELECT_ACCOUNT)
                    return true
                }
            }
            return false
        }

        fun handleActionActivityResult(fragment: BaseFragment, requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                AbsStatusesFragment.REQUEST_FAVORITE_SELECT_ACCOUNT -> {
                    if (resultCode != Activity.RESULT_OK || data == null) return
                    val accountKey = data.getParcelableExtra<UserKey>(IntentConstants.EXTRA_ACCOUNT_KEY)
                    val extras = data.getBundleExtra(IntentConstants.EXTRA_EXTRAS)
                    val status = extras.getParcelable<ParcelableStatus>(IntentConstants.EXTRA_STATUS)
                    fragment.twitterWrapper.createFavoriteAsync(accountKey, status)
                }
                AbsStatusesFragment.REQUEST_RETWEET_SELECT_ACCOUNT -> {
                    if (resultCode != Activity.RESULT_OK || data == null) return
                    val accountKey = data.getParcelableExtra<UserKey>(IntentConstants.EXTRA_ACCOUNT_KEY)
                    val extras = data.getBundleExtra(IntentConstants.EXTRA_EXTRAS)
                    val status = extras.getParcelable<ParcelableStatus>(IntentConstants.EXTRA_STATUS)
                    RetweetQuoteDialogFragment.show(fragment.childFragmentManager, status, accountKey)
                }
            }
        }

        fun selectAccountIntent(context: Context, status: ParcelableStatus, itemId: Long): Intent {
            val intent = Intent(context, AccountSelectorActivity::class.java)
            intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
            intent.putExtra(EXTRA_ACCOUNT_HOST, status.account_key.host)
            intent.putExtra(EXTRA_SINGLE_SELECTION, true)
            intent.putExtra(EXTRA_EXTRAS, Bundle {
                this[EXTRA_STATUS] = status
                this[EXTRA_ID] = itemId
            })
            return intent
        }

    }
}
