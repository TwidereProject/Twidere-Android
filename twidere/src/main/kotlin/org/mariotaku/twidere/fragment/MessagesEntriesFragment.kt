package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import org.mariotaku.kpreferences.get
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.MessagesEntriesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.model.user
import org.mariotaku.twidere.loader.ObjectCursorLoader
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversationCursorIndices
import org.mariotaku.twidere.model.SimpleRefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/28.
 */
class MessagesEntriesFragment : AbsContentListRecyclerViewFragment<MessagesEntriesAdapter>(),
        LoaderManager.LoaderCallbacks<List<ParcelableMessageConversation>?>, MessagesEntriesAdapter.MessageConversationClickListener {

    private val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context)

    private val errorInfoKey: String = ErrorInfoStore.KEY_DIRECT_MESSAGES

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.listener = this
        adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableMessageConversation>?> {
        val loader = ObjectCursorLoader(context, ParcelableMessageConversationCursorIndices::class.java)
        loader.uri = Conversations.CONTENT_URI
        loader.projection = Conversations.COLUMNS
        loader.sortOrder = OrderBy(Conversations.LOCAL_TIMESTAMP, false).sql
        return loader
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableMessageConversation>?>?) {
        adapter.conversations = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessageConversation>?>?, data: List<ParcelableMessageConversation>?) {
        adapter.conversations = data
        adapter.drawAccountColors = accountKeys.size > 1
        showContentOrError()
    }

    override fun onCreateAdapter(context: Context): MessagesEntriesAdapter {
        return MessagesEntriesAdapter(context)
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        twitterWrapper.getMessagesAsync(object : SimpleRefreshTaskParam() {
            override fun getAccountKeysWorker(): Array<UserKey> {
                return this@MessagesEntriesFragment.accountKeys
            }
        })
        return true
    }

    override fun onLoadMoreContents(position: Long) {
        super.onLoadMoreContents(position)
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
