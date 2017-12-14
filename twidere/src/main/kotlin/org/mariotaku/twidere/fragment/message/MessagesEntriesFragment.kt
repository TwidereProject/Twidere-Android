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

package org.mariotaku.twidere.fragment.message

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toStringArray
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_ACCOUNT_KEYS
import org.mariotaku.twidere.TwidereConstants.REQUEST_SELECT_ACCOUNT
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.adapter.MessagesEntriesAdapter
import org.mariotaku.twidere.adapter.MessagesEntriesAdapter.MessageConversationClickListener
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_TYPES
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.data.CursorObjectLivePagedListProvider
import org.mariotaku.twidere.extension.accountKey
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.model.getTitle
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.extension.promise
import org.mariotaku.twidere.fragment.AbsContentListRecyclerViewFragment
import org.mariotaku.twidere.fragment.iface.IFloatingActionButtonFragment
import org.mariotaku.twidere.fragment.iface.IFloatingActionButtonFragment.ActionInfo
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.GetMessagesTaskEvent
import org.mariotaku.twidere.promise.MessagePromises
import org.mariotaku.twidere.provider.TwidereDataProvider
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.ExtendedRecyclerView

/**
 * Created by mariotaku on 16/3/28.
 */
class MessagesEntriesFragment : AbsContentListRecyclerViewFragment<MessagesEntriesAdapter>(),
        MessageConversationClickListener, IFloatingActionButtonFragment {

    private var entries: LiveData<PagedList<ParcelableMessageConversation>?>? = null
        private set(value) {
            field?.removeObservers(this)
            field = value
        }

    private val accountKeys: Array<UserKey> by lazy {
        Utils.getAccountKeys(context!!, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context!!)
    }

    private val errorInfoKey: String = ErrorInfoStore.KEY_DIRECT_MESSAGES

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_direct_messages)
        adapter.listener = this
        adapter.loadMoreSupportedPosition = LoadMorePosition.END
        registerForContextMenu(recyclerView)

        setupLiveData()
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode != Activity.RESULT_OK) return
                val accountKey = data!!.extras!!.accountKey!!
                startActivity(IntentUtils.newMessageConversation(accountKey))
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): MessagesEntriesAdapter {
        return MessagesEntriesAdapter(context, this.requestManager)
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        val task = GetMessagesTask(context!!)
        task.params = object : GetMessagesTask.RefreshNewParam(context!!) {
            override val accountKeys: Array<UserKey> = this@MessagesEntriesFragment.accountKeys
        }
        task.promise()
        return true
    }

    override fun onLoadMoreContents(position: Int) {
        if (position != LoadMorePosition.END) {
            return
        }
        setLoadMoreIndicatorPosition(LoadMorePosition.END)
        val task = GetMessagesTask(context!!)
        task.params = object : GetMessagesTask.LoadMoreEntriesParam(context!!) {
            override val accountKeys: Array<UserKey> = this@MessagesEntriesFragment.accountKeys
        }
        task.promise()
    }

    override fun onConversationClick(position: Int) {
        val conversation = adapter.getConversation(position)
        IntentUtils.openMessageConversation(context!!, conversation.account_key, conversation.id)
    }

    override fun onConversationLongClick(position: Int): Boolean {
        val view = recyclerView.layoutManager.findViewByPosition(position) ?: return false
        recyclerView.showContextMenuForChild(view)
        return true
    }

    override fun onProfileImageClick(position: Int) {
        val conversation = adapter.getConversation(position)
        val user = conversation.user ?: return
        IntentUtils.openUserProfile(context!!, user, preferences[newDocumentApiKey])
    }

    override fun getActionInfo(tag: String): ActionInfo? {
        return ActionInfo(R.drawable.ic_action_add, getString(R.string.new_direct_message))
    }

    override fun onActionClick(tag: String): Boolean {
        val accountKey = accountKeys.singleOrNull() ?: run {
            val selectIntent = Intent(context, AccountSelectorActivity::class.java)
            selectIntent.putExtra(EXTRA_ACCOUNT_KEYS, accountKeys)
            selectIntent.putExtra(EXTRA_ACCOUNT_TYPES, arrayOf(AccountType.TWITTER,
                    AccountType.FANFOU))
            startActivityForResult(selectIntent, REQUEST_SELECT_ACCOUNT)
            return true
        }
        startActivity(IntentUtils.newMessageConversation(accountKey))
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val info = menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: return
        val conversation = adapter.getConversation(info.position)
        val inflater = MenuInflater(context)
        inflater.inflate(R.menu.context_message_entry, menu)
        menu.setHeaderTitle(conversation.getTitle(context!!, userColorNameManager,
                preferences[nameFirstKey]).first)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (!userVisibleHint) return false
        val menuInfo = item.menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: return false
        when (item.itemId) {
            R.id.mark_read -> {
                val conversation = adapter.getConversation(menuInfo.position)
                // TODO: Promise progress
                MessagePromises.getInstance(context!!).markRead(conversation.account_key, conversation.id)
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    @Subscribe
    fun onGetMessagesTaskEvent(event: GetMessagesTaskEvent) {
        if (!event.running) {
            refreshing = false
        }
    }

    private fun setupLiveData() {
        entries = createLiveData()
        entries?.observe(this, Observer { onDataLoaded(it) })
    }

    private fun createLiveData(): LiveData<PagedList<ParcelableMessageConversation>?> {
        val projection = (Conversations.COLUMNS + Conversations.UNREAD_COUNT).map {
            TwidereQueryBuilder.mapConversationsProjection(it)
        }.toTypedArray()
        val qb = SQLQueryBuilder.select(Columns(*projection))
        qb.from(Table(Conversations.TABLE_NAME))
        qb.join(Join(false, Join.Operation.LEFT_OUTER, Table(Messages.TABLE_NAME),
                Expression.and(
                        Expression.equals(
                                Column(Table(Conversations.TABLE_NAME), Conversations.CONVERSATION_ID),
                                Column(Table(Messages.TABLE_NAME), Messages.CONVERSATION_ID)
                        ),
                        Expression.equals(
                                Column(Table(Conversations.TABLE_NAME), Conversations.ACCOUNT_KEY),
                                Column(Table(Messages.TABLE_NAME), Messages.ACCOUNT_KEY))
                )
        ))
        qb.where(Expression.inArgs(Column(Table(Conversations.TABLE_NAME), Conversations.ACCOUNT_KEY), accountKeys.size))
        qb.groupBy(Column(Table(Messages.TABLE_NAME), Messages.CONVERSATION_ID))
        qb.orderBy(OrderBy(arrayOf(Conversations.LOCAL_TIMESTAMP, Conversations.SORT_ID), booleanArrayOf(false, false)))
        qb.limit(RawSQLLang(TwidereDataProvider.PLACEHOLDER_LIMIT))
        val provider = CursorObjectLivePagedListProvider(context!!.contentResolver,
                TwidereQueryBuilder.rawQuery(qb.buildSQL(), Conversations.CONTENT_URI),
                selectionArgs = accountKeys.toStringArray(),
                cls = ParcelableMessageConversation::class.java)
        return provider.create(null, PagedList.Config.Builder()
                .setPageSize(50).setEnablePlaceholders(false).build())
    }

    private fun onDataLoaded(data: PagedList<ParcelableMessageConversation>?) {
        adapter.conversations = data
        adapter.drawAccountColors = accountKeys.size > 1
        setLoadMoreIndicatorPosition(LoadMorePosition.NONE)
        showContentOrError()
    }

    private fun showContentOrError() {
        val accountKeys = this.accountKeys
        if (adapter.itemCount > 0) {
            showContent()
        } else if (accountKeys.isNotEmpty()) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context!!,
                    errorInfoStore[errorInfoKey, accountKeys[0]])
            if (errorInfo != null) {
                showEmpty(errorInfo.icon, errorInfo.message)
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.message_toast_no_account_selected))
        }
    }

}
