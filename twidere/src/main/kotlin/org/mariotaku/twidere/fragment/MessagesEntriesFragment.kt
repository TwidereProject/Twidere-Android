package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import com.squareup.otto.Subscribe
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toStringArray
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesEntriesAdapter
import org.mariotaku.twidere.adapter.MessagesEntriesAdapter.MessageConversationClickListener
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.fragment.iface.IFloatingActionButtonFragment
import org.mariotaku.twidere.fragment.iface.IFloatingActionButtonFragment.ActionInfo
import org.mariotaku.twidere.loader.ObjectCursorLoader
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversationCursorIndices
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.GetMessagesTaskEvent
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.GetMessagesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/28.
 */
class MessagesEntriesFragment : AbsContentListRecyclerViewFragment<MessagesEntriesAdapter>(),
        LoaderCallbacks<List<ParcelableMessageConversation>?>, MessageConversationClickListener,
        IFloatingActionButtonFragment {

    private val accountKeys: Array<UserKey> by lazy {
        Utils.getAccountKeys(context, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context)
    }

    private val errorInfoKey: String = ErrorInfoStore.KEY_DIRECT_MESSAGES

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.listener = this
        adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
        loaderManager.initLoader(0, null, this)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableMessageConversation>?> {
        val loader = ObjectCursorLoader(context, ParcelableMessageConversationCursorIndices::class.java)
        loader.uri = Conversations.CONTENT_URI
        loader.selection = Expression.inArgs(Conversations.ACCOUNT_KEY, accountKeys.size).sql
        loader.selectionArgs = accountKeys.toStringArray()
        loader.projection = Conversations.COLUMNS
        loader.sortOrder = OrderBy(Conversations.SORT_ID, false).sql
        return loader
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableMessageConversation>?>?) {
        adapter.conversations = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessageConversation>?>?, data: List<ParcelableMessageConversation>?) {
        adapter.conversations = data
        adapter.drawAccountColors = accountKeys.size > 1
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        showContentOrError()
    }

    override fun onCreateAdapter(context: Context): MessagesEntriesAdapter {
        return MessagesEntriesAdapter(context)
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        twitterWrapper.getMessagesAsync(object : GetMessagesTask.RefreshNewTaskParam(context) {
            override val accountKeys: Array<UserKey> = this@MessagesEntriesFragment.accountKeys
        })
        return true
    }

    override fun onLoadMoreContents(position: Long) {
        if (position != ILoadMoreSupportAdapter.END) {
            return
        }
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.END)
        twitterWrapper.getMessagesAsync(object : GetMessagesTask.LoadMoreEntriesTaskParam(context) {
            override val accountKeys: Array<UserKey> = this@MessagesEntriesFragment.accountKeys
        })
    }

    override fun onConversationClick(position: Int) {
        val conversation = adapter.getConversation(position) ?: return
        IntentUtils.openMessageConversation(context, conversation.account_key, conversation.id)
    }

    override fun onProfileImageClick(position: Int) {
        val conversation = adapter.getConversation(position) ?: return
        val user = conversation.user ?: return
        IntentUtils.openUserProfile(context, user, preferences[newDocumentApiKey])
    }

    override fun getActionInfo(tag: String): ActionInfo? {
        return ActionInfo(R.drawable.ic_action_add, getString(R.string.new_direct_message))
    }

    override fun onActionClick(tag: String) {
        val accountKey = accountKeys.singleOrNull() ?: run {

            return
        }
        startActivity(IntentUtils.newMessageConversation(accountKey))
    }

    @Subscribe
    fun onGetMessagesTaskEvent(event: GetMessagesTaskEvent) {
        if (!event.running) {
            refreshing = false
        }
    }

    private fun showContentOrError() {
        val accountKeys = this.accountKeys
        if (adapter.itemCount > 0) {
            showContent()
        } else if (accountKeys.isNotEmpty()) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context,
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
