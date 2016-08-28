/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.util.SimpleArrayMap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.sqliteqb.library.ArgsArray
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.NOTIFICATION_ID_DIRECT_MESSAGES
import org.mariotaku.twidere.activity.HomeActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity
import org.mariotaku.twidere.adapter.MessageEntriesAdapter
import org.mariotaku.twidere.adapter.MessageEntriesAdapter.DirectMessageEntry
import org.mariotaku.twidere.adapter.MessageEntriesAdapter.MessageEntriesAdapterListener
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.ACTION_NAVIGATION_REFRESH
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import org.mariotaku.twidere.model.BaseRefreshTaskParam
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.GetMessagesTaskEvent
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver
import java.util.*

class DirectMessagesFragment : AbsContentListRecyclerViewFragment<MessageEntriesAdapter>(), LoaderCallbacks<Cursor>, MessageEntriesAdapterListener, KeyboardShortcutCallback {

    // Listeners
    private val reloadContentObserver = SupportFragmentReloadCursorObserver(
            this, 0, this)

    private var mRemoveUnreadCountsTask: RemoveUnreadCountsTask? = null
    private var mNavigationHelper: RecyclerViewNavigationHelper? = null

    // Data fields
    val unreadCountsToRemove = SimpleArrayMap<UserKey, MutableSet<String>>()
    private val mReadPositions = Collections.synchronizedSet(HashSet<Int>())
    private var mFirstVisibleItem: Int = 0

    override fun onCreateAdapter(context: Context): MessageEntriesAdapter {
        return MessageEntriesAdapter(context)
    }

    fun onLoadMoreContents(@IndicatorPosition position: Int) {
        // Only supports load from end, so remove START flag
        val pos = position and ILoadMoreSupportAdapter.START.inv().toInt()
        if (pos == 0) return
        loadMoreMessages()
    }

    override fun setControlVisible(visible: Boolean) {
        val activity = activity
        if (activity is IControlBarActivity) {
            activity.setControlBarVisibleAnimate(visible)
        }
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler,
                                              keyCode: Int, repeatCount: Int,
                                              event: KeyEvent, metaState: Int): Boolean {
        return mNavigationHelper!!.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler,
                                              keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_REFRESH == action) {
            triggerRefresh()
            return true
        }
        return false
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        return ACTION_NAVIGATION_REFRESH == action || mNavigationHelper!!.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val uri = DirectMessages.ConversationEntries.CONTENT_URI
        val accountIds = accountKeys
        val selection = Expression.`in`(Column(Statuses.ACCOUNT_KEY),
                ArgsArray(accountIds.size)).sql
        val selectionArgs = TwidereArrayUtils.toStringArray(accountIds, 0, accountIds.size)
        return CursorLoader(activity, uri, null, selection, selectionArgs, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (activity == null) return
        val isEmpty = cursor != null && cursor.count == 0
        mFirstVisibleItem = -1
        val adapter = adapter
        adapter!!.setCursor(cursor)
        adapter.loadMoreIndicatorPosition = ILoadMoreSupportAdapter.NONE
        adapter.loadMoreSupportedPosition = if (hasMoreData(cursor)) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
        val accountIds = accountKeys
        adapter.setShowAccountsColor(accountIds.size > 1)
        refreshEnabled = true

        if (accountIds.size > 0) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context,
                    errorInfoStore.get(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountIds[0]))
            if (isEmpty && errorInfo != null) {
                showEmpty(errorInfo.icon, errorInfo.message)
            } else {
                showContent()
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.no_account_selected))
        }
    }

    protected fun hasMoreData(cursor: Cursor?): Boolean {
        return cursor != null && cursor.count != 0
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        val adapter = adapter
        adapter!!.setCursor(null)
    }

    override fun onEntryClick(position: Int, entry: DirectMessageEntry) {
        IntentUtils.openMessageConversation(activity, entry.account_key, entry.conversation_id)
    }

    override fun onUserClick(position: Int, entry: DirectMessageEntry) {
        IntentUtils.openUserProfile(activity, entry.account_key,
                UserKey.valueOf(entry.conversation_id), entry.screen_name, null,
                preferences.getBoolean(KEY_NEW_DOCUMENT_API), null)
    }

    @Subscribe
    fun onGetMessagesTaskChanged(event: GetMessagesTaskEvent) {
        if (event.uri == Inbox.CONTENT_URI && !event.running) {
            refreshing = false
            setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
            refreshEnabled = true
        }
    }

    override fun scrollToStart(): Boolean {
        val result = super.scrollToStart()
        if (result) {
            val twitter = twitterWrapper
            val tabPosition = tabPosition
            if (tabPosition >= 0) {
                twitter.clearUnreadCountAsync(tabPosition)
            }
        }
        return result
    }

    override fun triggerRefresh(): Boolean {
        AsyncTaskUtils.executeTask(object : AsyncTask<Any, Any, RefreshTaskParam>() {

            override fun doInBackground(vararg params: Any): RefreshTaskParam? {
                val context = context ?: return null
                val accountIds = accountKeys
                val ids = DataStoreUtils.getNewestMessageIds(context,
                        Inbox.CONTENT_URI, accountIds)
                return BaseRefreshTaskParam(accountIds, ids, null)
            }

            override fun onPostExecute(result: RefreshTaskParam?) {
                val twitter = twitterWrapper
                if (result == null) return
                twitter.getReceivedDirectMessagesAsync(result)
                twitter.getSentDirectMessagesAsync(BaseRefreshTaskParam(result.accountKeys, null, null))
            }

        })
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_direct_messages, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(activity is LinkHandlerActivity)
        val adapter = adapter!!
        mNavigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager!!, adapter, this)

        adapter.listener = this

        loaderManager.initLoader(0, null, this)
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        contentResolver.registerContentObserver(Accounts.CONTENT_URI, true, reloadContentObserver)
        bus.register(this)
        val adapter = adapter
        adapter!!.updateReadState()
    }

    override fun onStop() {
        bus.unregister(this)
        contentResolver.unregisterContentObserver(reloadContentObserver)
        super.onStop()
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.compose -> {
                openNewMessageConversation()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun openNewMessageConversation() {
        val accountIds = accountKeys
        if (accountIds.size == 1) {
            IntentUtils.openMessageConversation(activity, accountIds[0], null)
        } else {
            IntentUtils.openMessageConversation(activity, null, null)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val context = context
        if (isVisibleToUser && context != null) {
            for (accountKey in accountKeys) {
                val tag = "messages_" + accountKey
                notificationManager.cancel(tag, NOTIFICATION_ID_DIRECT_MESSAGES)
            }
        }
    }

    override fun createItemDecoration(context: Context, recyclerView: RecyclerView, layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val itemDecoration = DividerItemDecoration(context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation)
        val res = context.resources
        val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 3 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
        itemDecoration.setPadding(decorPaddingLeft, 0, 0, 0)
        itemDecoration.setDecorationEndOffset(1)
        return itemDecoration
    }

    protected val accountKeys: Array<UserKey>
        get() {
            val args = arguments
            val accountKeys = Utils.getAccountKeys(context, args)
            if (accountKeys != null) {
                return accountKeys
            }
            val activity = activity
            if (activity is HomeActivity) {
                return activity.activatedAccountKeys
            }
            return DataStoreUtils.getActivatedAccountKeys(getActivity())
        }

    private fun addReadPosition(firstVisibleItem: Int) {
        if (mFirstVisibleItem != firstVisibleItem) {
            mReadPositions.add(firstVisibleItem)
        }
        mFirstVisibleItem = firstVisibleItem
    }

    private fun addUnreadCountsToRemove(accountId: UserKey, id: String) {
        if (unreadCountsToRemove.indexOfKey(accountId) < 0) {
            val counts = HashSet<String>()
            counts.add(id)
            unreadCountsToRemove.put(accountId, counts)
        } else {
            val counts = unreadCountsToRemove.get(accountId)
            counts.add(id)
        }
    }

    private fun loadMoreMessages() {
        if (refreshing) return
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.END)
        refreshEnabled = false
        AsyncTaskUtils.executeTask(object : AsyncTask<Any, Any, Array<RefreshTaskParam>>() {

            override fun doInBackground(vararg params: Any): Array<RefreshTaskParam>? {
                val context = context ?: return null
                val accountKeys = accountKeys
                return arrayOf(
                        BaseRefreshTaskParam(accountKeys, DataStoreUtils.getOldestMessageIds(context,
                                DirectMessages.Inbox.CONTENT_URI, accountKeys), null),
                        BaseRefreshTaskParam(accountKeys, DataStoreUtils.getOldestMessageIds(context,
                                DirectMessages.Outbox.CONTENT_URI, accountKeys), null)
                )
            }

            override fun onPostExecute(result: Array<RefreshTaskParam>?) {
                if (result == null) return
                val twitter = twitterWrapper
                twitter.getReceivedDirectMessagesAsync(result[0])
                twitter.getSentDirectMessagesAsync(result[1])
            }

        })
    }

    private fun removeUnreadCounts() {
        if (mRemoveUnreadCountsTask != null && mRemoveUnreadCountsTask!!.status == AsyncTask.Status.RUNNING)
            return
        mRemoveUnreadCountsTask = RemoveUnreadCountsTask(mReadPositions, this)
        AsyncTaskUtils.executeTask<RemoveUnreadCountsTask, Any>(mRemoveUnreadCountsTask)
    }

    internal class RemoveUnreadCountsTask(readPositions: Set<Int>, private val fragment: DirectMessagesFragment) : AsyncTask<Any, Any, Any>() {
        private val readPositions: Set<Int>
        private val adapter: MessageEntriesAdapter

        init {
            this.readPositions = Collections.synchronizedSet(HashSet(readPositions))
            adapter = fragment.adapter!!
        }

        override fun doInBackground(vararg params: Any): Any? {
            for (pos in readPositions) {
                val entry = adapter.getEntry(pos)
                val id = entry!!.conversation_id
                val accountKey = entry.account_key
                fragment.addUnreadCountsToRemove(accountKey, id)
            }
            return null
        }

        override fun onPostExecute(result: Any) {
            fragment.twitterWrapper.removeUnreadCountsAsync(fragment.tabPosition,
                    fragment.unreadCountsToRemove)
        }

    }

}
