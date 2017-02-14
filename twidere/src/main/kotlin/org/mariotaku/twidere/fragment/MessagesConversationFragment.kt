package org.mariotaku.twidere.fragment

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_messages_conversation.*
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.empty
import org.mariotaku.pickncrop.library.MediaPickerActivity
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PICK_MEDIA
import org.mariotaku.twidere.activity.ThemedMediaPickerActivity
import org.mariotaku.twidere.adapter.MediaPreviewAdapter
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_CONVERSATION_ID
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.loader.ObjectCursorLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.task.compose.AbsAddMediaTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.PreviewGridItemDecoration
import java.util.concurrent.atomic.AtomicReference

class MessagesConversationFragment : BaseFragment(), LoaderManager.LoaderCallbacks<List<ParcelableMessage>?> {
    private lateinit var conversationAdapter: MessagesConversationAdapter
    private lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    private val accountKey: UserKey get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)

    private val conversationId: String get() = arguments.getString(EXTRA_CONVERSATION_ID)

    private val account: AccountDetails? by lazy {
        AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        conversationAdapter = MessagesConversationAdapter(context)
        conversationAdapter.listener = object : MessagesConversationAdapter.Listener {
            override fun onMediaClick(position: Int, media: ParcelableMedia, accountKey: UserKey?) {
                val message = conversationAdapter.getMessage(position) ?: return
                IntentUtils.openMediaDirectly(context = context, accountKey = accountKey,
                        media = message.media, current = media,
                        newDocument = preferences[newDocumentApiKey], message = message)
            }
        }
        mediaPreviewAdapter = MediaPreviewAdapter(context)

        recyclerView.adapter = conversationAdapter
        recyclerView.layoutManager = FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)

        attachedMediaPreview.layoutManager = FixedLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        attachedMediaPreview.adapter = mediaPreviewAdapter
        attachedMediaPreview.addItemDecoration(PreviewGridItemDecoration(resources.getDimensionPixelSize(R.dimen.element_spacing_small)))

        sendMessage.setOnClickListener {
            performSendMessage()
        }
        addMedia.setOnClickListener {
            openMediaPicker()
        }

        updateMediaPreview()

        loaderManager.initLoader(0, null, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_MEDIA -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val mediaUris = MediaPickerActivity.getMediaUris(data)
                    TaskStarter.execute(AddMediaTask(this, mediaUris, true))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation, container, false)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableMessage>?> {
        return ConversationLoader(context, accountKey, conversationId)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableMessage>?>) {
        conversationAdapter.setData(null, null)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessage>?>, data: List<ParcelableMessage>?) {
        val conversationLoader = loader as? ConversationLoader
        val conversation = conversationLoader?.conversation
        conversationAdapter.setData(conversation, data)
    }

    private fun performSendMessage() {
        val conversation = conversationAdapter.conversation ?: return
        if (editText.empty && conversationAdapter.itemCount == 0) {
            editText.error = getString(R.string.hint_error_message_no_content)
            return
        }
        val text = editText.text.toString()
        val message = ParcelableNewMessage().apply {
            this.account = this@MessagesConversationFragment.account
            this.media = mediaPreviewAdapter.asList().toTypedArray()
            this.conversation_id = conversation.id
            this.recipient_ids = conversation.participants?.map {
                it.key.id
            }?.toTypedArray()
            this.text = text
        }
        LengthyOperationsService.sendMessageAsync(context, message)
        editText.text = null

        // Clear media, those media will be deleted after sent
        mediaPreviewAdapter.clear()
        updateMediaPreview()
    }

    private fun openMediaPicker() {
        val intent = ThemedMediaPickerActivity.withThemed(context)
                .pickSources(arrayOf(MediaPickerActivity.SOURCE_CAMERA,
                        MediaPickerActivity.SOURCE_CAMCORDER,
                        MediaPickerActivity.SOURCE_GALLERY,
                        MediaPickerActivity.SOURCE_CLIPBOARD))
                .containsVideo(true)
                .allowMultiple(false)
                .build()
        startActivityForResult(intent, REQUEST_PICK_MEDIA)
    }

    private fun attachMedia(media: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.addAll(media)
        updateMediaPreview()
    }

    private fun updateMediaPreview() {
        attachedMediaPreview.visibility = if (mediaPreviewAdapter.itemCount > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setProgressVisible(visible: Boolean) {

    }

    internal class AddMediaTask(
            fragment: MessagesConversationFragment,
            sources: Array<Uri>,
            deleteSrc: Boolean
    ) : AbsAddMediaTask<MessagesConversationFragment>(fragment.context, sources, deleteSrc) {

        init {
            callback = fragment
        }

        override fun afterExecute(callback: MessagesConversationFragment?, result: List<ParcelableMediaUpdate>?) {
            if (callback == null || result == null) return
            callback.setProgressVisible(false)
            callback.attachMedia(result)
        }

        override fun beforeExecute() {
            val fragment = callback ?: return
            fragment.setProgressVisible(true)
        }

    }

    internal class ConversationLoader(
            context: Context,
            val accountKey: UserKey,
            val conversationId: String
    ) : ObjectCursorLoader<ParcelableMessage>(context, ParcelableMessageCursorIndices::class.java) {

        private val atomicConversation = AtomicReference<ParcelableMessageConversation?>()
        val conversation: ParcelableMessageConversation? get() = atomicConversation.get()

        init {
            uri = Messages.CONTENT_URI
            projection = Messages.COLUMNS
            selection = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
            selectionArgs = arrayOf(accountKey.toString(), conversationId)
            sortOrder = OrderBy(Messages.SORT_ID, false).sql
        }

        override fun onLoadInBackground(): MutableList<ParcelableMessage> {
            atomicConversation.set(DataStoreUtils.findMessageConversation(context, accountKey, conversationId))
            return super.onLoadInBackground()
        }
    }

}

