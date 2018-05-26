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

package org.mariotaku.twidere.fragment.status

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.app.hasRunningLoadersSafe
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.android.synthetic.main.layout_content_fragment_common.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.twitter.TranslationResult
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.ColorPickerDialogActivity
import org.mariotaku.twidere.adapter.StatusDetailsAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.*
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.data.status.StatusActivitySummaryLiveData
import org.mariotaku.twidere.data.status.StatusLiveData
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.data.observe
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.originalId
import org.mariotaku.twidere.extension.model.quoted
import org.mariotaku.twidere.extension.view.calculateSpaceItemHeight
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.timeline.AbsTimelineFragment
import org.mariotaku.twidere.loader.statuses.ConversationLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.event.FavoriteTaskEvent
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.singleton.BusSingleton
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.task.AbsAccountRequestTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.ContentScrollHandler.ContentListSupport
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.RecyclerViewScrollHandler.RecyclerViewCallback
import org.mariotaku.twidere.view.CardMediaContainer.OnMediaClickListener
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder.StatusClickListener
import java.lang.ref.WeakReference

/**
 * Displays status details
 * Created by mariotaku on 14/12/5.
 */
class StatusFragment : BaseFragment(), OnMediaClickListener, StatusClickListener,
        KeyboardShortcutCallback, ContentListSupport<StatusDetailsAdapter> {
    private var mItemDecoration: ExtendedDividerItemDecoration? = null

    override lateinit var adapter: StatusDetailsAdapter

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var navigationHelper: RecyclerViewNavigationHelper
    private lateinit var scrollListener: RecyclerViewScrollHandler<StatusDetailsAdapter>

    private var loadTranslationTask: LoadTranslationTask? = null
    // Data fields
    private var conversationLoaderInitialized: Boolean = false

    private var hasMoreConversation = true

    // Listeners
    private val conversationsLoaderCallback = object : LoaderCallbacks<List<ParcelableStatus>> {
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableStatus>> {
            val adapter = this@StatusFragment.adapter
            adapter.isRepliesLoading = true
            adapter.isConversationsLoading = true
            adapter.updateItemDecoration()
            val status: ParcelableStatus = args!!.getParcelable(EXTRA_STATUS)
            val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
            return ConversationLoader(activity!!, status, adapter.data, true, loadingMore).apply {
                pagination = args.toPagination()
            }
        }

        override fun onLoadFinished(loader: Loader<List<ParcelableStatus>>, data: List<ParcelableStatus>?) {
            val adapter = this@StatusFragment.adapter
            adapter.updateItemDecoration()
            val conversationLoader = loader as ConversationLoader
            var supportedPositions = 0
            if (data != null && !data.isEmpty()) {
                val sinceSortId = (conversationLoader.pagination as? SinceMaxPagination)?.sinceSortId
                        ?: -1
                if (sinceSortId < data[data.size - 1].sort_id) {
                    supportedPositions = supportedPositions or LoadMorePosition.END
                }
                if (data[0].in_reply_to_status_id != null) {
                    supportedPositions = supportedPositions or LoadMorePosition.START
                }
            } else {
                supportedPositions = supportedPositions or LoadMorePosition.END
                val status = status
                if (status?.in_reply_to_status_id != null) {
                    supportedPositions = supportedPositions or LoadMorePosition.START
                }
            }
            adapter.loadMoreSupportedPosition = supportedPositions
            setConversation(data)
            adapter.isConversationsLoading = false
            adapter.isRepliesLoading = false
        }

        override fun onLoaderReset(loader: Loader<List<ParcelableStatus>>) {

        }
    }

    private lateinit var statusActivitySummaryLiveData: StatusActivitySummaryLiveData
    private lateinit var statusLiveData: StatusLiveData

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SET_COLOR -> {
                val status = adapter.status ?: return
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) return
                    val color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT)
                    UserColorNameManager.get(context!!).setUserColor(status.user_key, color)
                } else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
                    UserColorNameManager.get(context!!).clearUserColor(status.user_key)
                }
                statusLiveData.load()
            }
            AbsTimelineFragment.REQUEST_OPEN_SELECT_ACCOUNT,
            AbsTimelineFragment.REQUEST_FAVORITE_SELECT_ACCOUNT,
            AbsTimelineFragment.REQUEST_RETWEET_SELECT_ACCOUNT -> {
                AbsTimelineFragment.handleActionActivityResult(this, requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        linkHandlerTitle = getString(R.string.title_status)
        Utils.setNdefPushMessageCallback(activity!!, CreateNdefMessageCallback cb@{
            val status = status ?: return@cb null
            val link = LinkCreator.getStatusWebLink(status) ?: return@cb null
            return@cb NdefMessage(arrayOf(NdefRecord.createUri(link)))
        })
        adapter = StatusDetailsAdapter(this)
        layoutManager = StatusListLinearLayoutManager(context!!, recyclerView)
        mItemDecoration = StatusDividerItemDecoration(context!!, adapter, layoutManager.orientation)
        recyclerView.addItemDecoration(mItemDecoration)
        layoutManager.recycleChildrenOnDetach = true
        recyclerView.layoutManager = layoutManager
        recyclerView.clipToPadding = false
        adapter.statusClickListener = this
        recyclerView.adapter = adapter
        registerForContextMenu(recyclerView)

        scrollListener = RecyclerViewScrollHandler(this, RecyclerViewCallback(recyclerView))
        scrollListener.touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager,
                adapter, null)

        setState(STATE_LOADING)

        statusActivitySummaryLiveData = StatusActivitySummaryLiveData(context!!)
        val fragmentArgs = arguments!!
        val accountKey = fragmentArgs.accountKey
        val statusId = fragmentArgs.statusId
        statusLiveData = StatusLiveData(activity!!, false, fragmentArgs, accountKey, statusId)
        statusActivitySummaryLiveData.observe(this, success = { data ->
            adapter.updateItemDecoration()
            adapter.statusActivity = data
        }, fail = {
            DebugLog.w(tr = it)
        })
        statusLiveData.observe(this, success = { (account, status) ->
            val readPosition = saveReadPosition()
            if (adapter.setStatus(status, account)) {
                adapter.loadMoreSupportedPosition = LoadMorePosition.BOTH
                adapter.data = null
                loadConversation(status, null, null)
                loadActivity(status)

                val position = adapter.getFirstPositionOfItem(StatusDetailsAdapter.ITEM_IDX_STATUS)
                if (position != RecyclerView.NO_POSITION) {
                    layoutManager.scrollToPositionWithOffset(position, 0)
                }
            } else if (readPosition != null) {
                restoreReadPosition(readPosition)
            }
            setState(STATE_LOADED)
            activity?.invalidateOptionsMenu()
        }, fail = {
            adapter.loadMoreSupportedPosition = LoadMorePosition.NONE
            setState(STATE_ERROR)
            val errorInfo = StatusCodeMessageUtils.getErrorInfo(context!!, it)
            errorText.spannable = errorInfo.message
            errorIcon.setImageResource(errorInfo.icon)
            activity?.invalidateOptionsMenu()
        })

        statusLiveData.load()
    }

    override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
        val status = adapter.getStatus(statusPosition)
        val preferences = PreferencesSingleton.get(context!!)
        IntentUtils.openMedia(activity!!, status, current, preferences[newDocumentApiKey],
                preferences[displaySensitiveContentsKey])
    }

    override fun onQuotedMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
        val status = adapter.getStatus(statusPosition)
        val quotedMedia = status.quoted?.media ?: return
        val preferences = PreferencesSingleton.get(context!!)
        IntentUtils.openMedia(activity!!, status.account_key, status.is_possibly_sensitive, status,
                current, quotedMedia, preferences[newDocumentApiKey],
                preferences[displaySensitiveContentsKey])
    }

    override fun onGapClick(holder: GapViewHolder, position: Int) {

    }

    override fun onItemActionClick(holder: ViewHolder, id: Int, position: Int) {
        val status = adapter.getStatus(position)
        AbsTimelineFragment.handleActionClick(this, id, status, holder as IStatusViewHolder)
    }

    override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
        val status = adapter.getStatus(position)
        return AbsTimelineFragment.handleActionLongClick(this, status, adapter.getItemId(position), id)
    }

    override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        IntentUtils.openStatus(activity!!, status)
    }

    override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        val quotedId = status.quoted?.id ?: return
        IntentUtils.openStatus(activity!!, status.account_key, quotedId)
    }

    override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
        return false
    }

    override fun onItemMenuClick(holder: ViewHolder, menuView: View, position: Int) {
        if (activity == null) return
        val view = layoutManager.findViewByPosition(position) ?: return
        recyclerView.showContextMenuForChild(view, menuView)
    }

    override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
        val status = adapter.getStatus(position)
        IntentUtils.openUserProfile(activity!!, status.account_key, status.user_key,
                status.user_screen_name, status.user_profile_image_url,
                PreferencesSingleton.get(context!!)[newDocumentApiKey], null)
    }

    override fun onMediaClick(view: View, current: ParcelableMedia, accountKey: UserKey?, id: Long) {
        val status = adapter.status ?: return
        if ((view.parent as View).id == R.id.quotedMediaPreview && status.attachment?.quoted?.media != null) {
            IntentUtils.openMediaDirectly(activity!!, accountKey, status.attachment?.quoted?.media!!, current,
                    newDocument = PreferencesSingleton.get(context!!)[newDocumentApiKey], status = status)
        } else if (status.attachment?.media != null) {
            IntentUtils.openMediaDirectly(activity!!, accountKey, status.attachment?.media!!, current,
                    newDocument = PreferencesSingleton.get(context!!)[newDocumentApiKey], status = status)
        }
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler,
            keyCode: Int, event: KeyEvent,
            metaState: Int): Boolean {
        if (!KeyboardShortcutsHandler.isValidForHotkey(keyCode, event)) return false
        val focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView, layoutManager.focusedChild)
        val position: Int
        if (focusedChild != null && focusedChild.parent === recyclerView) {
            position = recyclerView.getChildLayoutPosition(focusedChild)
        } else {
            return false
        }
        if (position == -1) return false
        val status = adapter.getStatus(position)
        val action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
                ?: return false
        return AbsTimelineFragment.handleKeyboardShortcutAction(this, action, status, position)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState)
                ?: return false
        when (action) {
            ACTION_STATUS_REPLY, ACTION_STATUS_RETWEET, ACTION_STATUS_FAVORITE -> return true
        }
        return navigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler,
            keyCode: Int, repeatCount: Int,
            event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper.handleKeyboardShortcutRepeat(handler, keyCode,
                repeatCount, event, metaState)
    }

    override val refreshing: Boolean
        get() = loaderManager.hasRunningLoadersSafe()

    override fun onLoadMoreContents(@LoadMorePosition position: Int) {
        if (!hasMoreConversation) return
        if (LoadMorePosition.START in position) {
            val start = adapter.getIndexStart(StatusDetailsAdapter.ITEM_IDX_CONVERSATION)
            val first = adapter.getStatus(start)
            if (first.in_reply_to_status_id == null) return
            loadConversation(status, null, first.id)
        } else if (LoadMorePosition.END in position) {
            val start = adapter.getIndexStart(StatusDetailsAdapter.ITEM_IDX_CONVERSATION)
            val last = adapter.getStatus(start + adapter.getStatusCount() - 1)
            loadConversation(status, last.id, null)
        }
        adapter.loadMoreIndicatorPosition = position
    }

    override fun setControlVisible(visible: Boolean) {
        // No-op
    }

    override fun onApplySystemWindowInsets(insets: Rect) {
        recyclerView.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }

    override val reachingEnd: Boolean
        get() {
            val lm = layoutManager
            var itemPos = lm.findLastCompletelyVisibleItemPosition()
            if (itemPos == RecyclerView.NO_POSITION) {
                // No completely visible item, find visible item instead
                itemPos = lm.findLastVisibleItemPosition()
            }
            return itemPos >= lm.itemCount - 1
        }

    override val reachingStart: Boolean
        get() {
            val lm = layoutManager
            var itemPos = lm.findFirstCompletelyVisibleItemPosition()
            if (itemPos == RecyclerView.NO_POSITION) {
                // No completely visible item, find visible item instead
                itemPos = lm.findFirstVisibleItemPosition()
            }
            return itemPos <= 1
        }

    private val status: ParcelableStatus?
        get() = adapter.status

    private fun loadConversation(status: ParcelableStatus?, sinceId: String?, maxId: String?) {
        if (status == null || activity == null) return
        val args = Bundle {
            this[EXTRA_ACCOUNT_KEY] = status.account_key
            this[EXTRA_STATUS_ID] = status.originalId
            this[EXTRA_SINCE_ID] = sinceId
            this[EXTRA_MAX_ID] = maxId
            this[EXTRA_STATUS] = status
        }
        if (conversationLoaderInitialized) {
            loaderManager.restartLoader(LOADER_ID_STATUS_CONVERSATIONS, args, conversationsLoaderCallback)
            return
        }
        loaderManager.initLoader(LOADER_ID_STATUS_CONVERSATIONS, args, conversationsLoaderCallback)
        conversationLoaderInitialized = true
    }


    private fun loadActivity(status: ParcelableStatus?) {
        if (status == null || host == null || isDetached) return
        statusActivitySummaryLiveData.accountKey = status.account_key
        statusActivitySummaryLiveData.statusId = status.originalId
        statusActivitySummaryLiveData.load()
    }

    internal fun loadTranslation(status: ParcelableStatus?) {
        if (status == null) return
        if (loadTranslationTask?.isFinished == true) return
        loadTranslationTask = run {
            val task = LoadTranslationTask(this, status)
            task.promise()
            return@run task
        }
    }

    internal fun reloadTranslation() {
        loadTranslationTask = null
        loadTranslation(adapter.status)
    }

    private fun setConversation(data: List<ParcelableStatus>?) {
        val readPosition = saveReadPosition()
        adapter.data = data
        hasMoreConversation = data != null
        restoreReadPosition(readPosition)
    }

    private fun displayTranslation(translation: TranslationResult) {
        adapter.translationResult = translation
    }

    private fun saveReadPosition(): ReadPosition? {
        val lm = layoutManager
        val adapter = this.adapter
        val position = lm.findFirstVisibleItemPosition()
        if (position == RecyclerView.NO_POSITION) return null
        val itemType = adapter.getItemType(position)
        var itemId = adapter.getItemId(position)
        val positionView: View?
        if (itemType == StatusDetailsAdapter.ITEM_IDX_CONVERSATION_LOAD_MORE) {
            // Should be next item
            positionView = lm.findViewByPosition(position + 1)
            itemId = adapter.getItemId(position + 1)
        } else {
            positionView = lm.findViewByPosition(position)
        }
        return ReadPosition(itemId, positionView?.top ?: 0)
    }

    private fun restoreReadPosition(position: ReadPosition?) {
        val adapter = this.adapter
        if (position == null) return
        val adapterPosition = adapter.findPositionByItemId(position.statusId)
        if (adapterPosition < 0) return
        layoutManager.scrollToPositionWithOffset(adapterPosition, position.offsetTop)
    }

    private fun setState(state: Int) {
        statusContent.visibility = if (state == STATE_LOADED) View.VISIBLE else View.GONE
        progressContainer.visibility = if (state == STATE_LOADING) View.VISIBLE else View.GONE
        errorContainer.visibility = if (state == STATE_ERROR) View.VISIBLE else View.GONE
    }

    override fun onStart() {
        super.onStart()
        BusSingleton.register(this)
        recyclerView.addOnScrollListener(scrollListener)
        recyclerView.setOnTouchListener(scrollListener.touchListener)
    }

    override fun onStop() {
        recyclerView.setOnTouchListener(null)
        recyclerView.removeOnScrollListener(scrollListener)
        BusSingleton.unregister(this)
        super.onStop()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint) return
        val contextMenuInfo = menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: return
        val status = adapter.getStatus(contextMenuInfo.position)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.action_status, menu)
        MenuUtils.setupForStatus(context!!, menu, PreferencesSingleton.get(context!!), UserColorNameManager.get(context!!), status)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item.menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: return false
        val status = adapter.getStatus(contextMenuInfo.position)
        if (item.itemId == R.id.share) {
            val shareIntent = Utils.createStatusShareIntent(activity!!, status)
            val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))

            startActivity(chooser)
            return true
        }
        return MenuUtils.handleStatusClick(activity!!, this, fragmentManager!!,
                PreferencesSingleton.get(context!!), UserColorNameManager.get(context!!), status, item)
    }

    @Subscribe
    fun notifyStatusListChanged(event: StatusListChangedEvent) {
        adapter.notifyDataSetChanged()
    }

    @Subscribe
    fun notifyFavoriteTask(event: FavoriteTaskEvent) {
        if (!event.isSucceeded) return
        val status = adapter.findStatusById(event.accountKey, event.statusId)
        if (status != null) {
            when (event.action) {
                FavoriteTaskEvent.Action.CREATE -> {
                    status.is_favorite = true
                }
                FavoriteTaskEvent.Action.DESTROY -> {
                    status.is_favorite = false
                }
            }
        }
    }

    internal fun onUserClick(user: ParcelableUser) {
        IntentUtils.openUserProfile(context!!, user, true, null)
    }

    internal fun openTranslationDestinationChooser() {
        val account = adapter.statusAccount ?: return
        val weakThis by weak(this)
        (showProgressDialog("get_language_settings") and task {
            val context = weakThis?.context ?: throw InterruptedException()
            val microBlog = account.newMicroBlogInstance(context, Twitter::class.java)
            return@task Pair(microBlog.accountSettings.language,
                    microBlog.languages.map { TranslationDestinationDialogFragment.DisplayLanguage(it.name, it.code) })
        }).successUi { (_, settings) ->
            val (accountLanguage, languages) = settings
            val fragment = weakThis ?: return@successUi
            val df = TranslationDestinationDialogFragment.create(languages, accountLanguage)
            df.setTargetFragment(fragment, 0)
            df.show(fragment.fragmentManager, "translation_destination_settings")
        }.alwaysUi {
            val fragment = weakThis ?: return@alwaysUi
            fragment.dismissProgressDialog("get_language_settings")
        }
    }

    class LoadSensitiveImageConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val f = parentFragment
                    if (f is StatusFragment) {
                        val adapter = f.adapter
                        adapter.isDetailMediaExpanded = true
                    }
                }
            }

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(android.R.string.dialog_alert_title)
            builder.setMessage(R.string.sensitive_content_warning)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }
    }

    internal class LoadTranslationTask(fragment: StatusFragment, val status: ParcelableStatus) :
            AbsAccountRequestTask<Any?, TranslationResult, Any?>(fragment.context!!, status.account_key) {

        private val weakFragment = WeakReference(fragment)

        override fun onExecute(account: AccountDetails, params: Any?): TranslationResult {
            val twitter = account.newMicroBlogInstance(context, Twitter::class.java)
            val prefDest = PreferencesSingleton.get(context).getString(KEY_TRANSLATION_DESTINATION, null)
            val dest: String
            if (TextUtils.isEmpty(prefDest)) {
                dest = twitter.accountSettings.language
                val editor = PreferencesSingleton.get(context).edit()
                editor.putString(KEY_TRANSLATION_DESTINATION, dest)
                editor.apply()
            } else {
                dest = prefDest
            }
            return twitter.showTranslation(status.originalId, dest)
        }

        override fun onSucceed(callback: Any?, result: TranslationResult) {
            val fragment = weakFragment.get() ?: return
            fragment.displayTranslation(result)
        }

        override fun onException(callback: Any?, exception: MicroBlogException) {
            Toast.makeText(context, exception.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
    }

    data class ReadPosition(var statusId: Long, var offsetTop: Int)

    private class StatusListLinearLayoutManager(context: Context, private val recyclerView: RecyclerView) : FixedLinearLayoutManager(context) {
        private var spaceHeight: Int = 0

        init {
            orientation = LinearLayoutManager.VERTICAL
        }

        override fun getDecoratedMeasuredHeight(child: View): Int {
            if (getItemViewType(child) == StatusDetailsAdapter.VIEW_TYPE_SPACE) {
                val height = calculateSpaceItemHeight(child, StatusDetailsAdapter.VIEW_TYPE_SPACE,
                        StatusDetailsAdapter.VIEW_TYPE_DETAIL_STATUS)
                if (height >= 0) {
                    return height
                }
            }
            return super.getDecoratedMeasuredHeight(child)
        }

        override fun setOrientation(orientation: Int) {
            if (orientation != LinearLayoutManager.VERTICAL)
                throw IllegalArgumentException("Only VERTICAL orientation supported")
            super.setOrientation(orientation)
        }


        override fun computeVerticalScrollExtent(state: RecyclerView.State?): Int {
            val firstPosition = findFirstVisibleItemPosition()
            val lastPosition = Math.min(validScrollItemCount - 1, findLastVisibleItemPosition())
            if (firstPosition < 0 || lastPosition < 0) return 0
            val childCount = lastPosition - firstPosition + 1
            if (childCount > 0) {
                if (isSmoothScrollbarEnabled) {
                    var extent = childCount * 100
                    var view = findViewByPosition(firstPosition) ?: return 0
                    val top = view.top
                    var height = view.height
                    if (height > 0) {
                        extent += top * 100 / height
                    }

                    view = findViewByPosition(lastPosition) ?: return 0
                    val bottom = view.bottom
                    height = view.height
                    if (height > 0) {
                        extent -= (bottom - getHeight()) * 100 / height
                    }
                    return extent
                } else {
                    return 1
                }
            }
            return 0
        }

        override fun computeVerticalScrollOffset(state: RecyclerView.State?): Int {
            val firstPosition = findFirstVisibleItemPosition()
            val lastPosition = Math.min(validScrollItemCount - 1, findLastVisibleItemPosition())
            if (firstPosition < 0 || lastPosition < 0) return 0
            val childCount = lastPosition - firstPosition + 1
            val skippedCount = skippedScrollItemCount
            if (firstPosition >= skippedCount && childCount > 0) {
                if (isSmoothScrollbarEnabled) {
                    val view = findViewByPosition(firstPosition) ?: return 0
                    val top = view.top
                    val height = view.height
                    if (height > 0) {
                        return Math.max((firstPosition - skippedCount) * 100 - top * 100 / height, 0)
                    }
                } else {
                    val count = validScrollItemCount
                    val index = when {
                        firstPosition == 0 -> 0
                        firstPosition + childCount == count -> count
                        else -> firstPosition + childCount / 2
                    }
                    return (firstPosition + childCount * (index / count.toFloat())).toInt()
                }
            }
            return 0
        }

        override fun computeVerticalScrollRange(state: RecyclerView.State?): Int {
            return if (isSmoothScrollbarEnabled) {
                Math.max(validScrollItemCount * 100, 0)
            } else {
                validScrollItemCount
            }
        }

        private val skippedScrollItemCount: Int
            get() {
                val adapter = recyclerView.adapter as StatusDetailsAdapter
                var skipped = 0
                if (!adapter.isConversationsLoading) {
                    skipped += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_CONVERSATION_LOAD_MORE)
                }
                return skipped
            }

        private val validScrollItemCount: Int
            get() {
                val adapter = recyclerView.adapter as StatusDetailsAdapter
                var count = 0
                if (adapter.isConversationsLoading) {
                    count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_CONVERSATION_LOAD_MORE)
                }
                count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_CONVERSATION_ERROR)
                count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_CONVERSATION)
                count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_STATUS)
                count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_REPLY)
                count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_REPLY_ERROR)
                if (adapter.isRepliesLoading) {
                    count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_REPLY_LOAD_MORE)
                }
                val spaceHeight = calculateSpaceHeight()
                if (spaceHeight > 0) {
                    count += adapter.getTypeCount(StatusDetailsAdapter.ITEM_IDX_SPACE)
                }
                return count
            }

        private fun calculateSpaceHeight(): Int {
            val space = findViewByPosition(itemCount - 1) ?: return spaceHeight
            spaceHeight = getDecoratedMeasuredHeight(space)
            return spaceHeight
        }


    }

    private class StatusDividerItemDecoration(
            context: Context,
            private val statusAdapter: StatusDetailsAdapter,
            orientation: Int
    ) : ExtendedDividerItemDecoration(context, orientation) {

        override fun isDividerEnabled(childPos: Int): Boolean {
            if (childPos >= statusAdapter.itemCount || childPos < 0) return false
            val itemType = statusAdapter.getItemType(childPos)
            when (itemType) {
                StatusDetailsAdapter.ITEM_IDX_REPLY_LOAD_MORE, StatusDetailsAdapter.ITEM_IDX_REPLY_ERROR,
                StatusDetailsAdapter.ITEM_IDX_SPACE -> return false
            }
            return true
        }

    }

    companion object {

        // Constants
        private val LOADER_ID_DETAIL_STATUS = 1
        private val LOADER_ID_STATUS_CONVERSATIONS = 2
        private val STATE_LOADED = 1
        private val STATE_LOADING = 2
        private val STATE_ERROR = 3

        fun Bundle.toPagination(): Pagination {
            val maxId = getString(EXTRA_MAX_ID)
            val sinceId = getString(EXTRA_SINCE_ID)
            val maxSortId = getLong(EXTRA_MAX_SORT_ID)
            val sinceSortId = getLong(EXTRA_SINCE_SORT_ID)
            return SinceMaxPagination().apply {
                this.maxId = maxId
                this.sinceId = sinceId
                this.maxSortId = maxSortId
                this.sinceSortId = sinceSortId
            }
        }

    }
}