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

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.core.content.ContextCompat
import androidx.loader.content.Loader
import androidx.core.widget.TextViewCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.FixedLinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.*
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import kotlinx.android.synthetic.main.fragment_messages_conversation.*
import kotlinx.android.synthetic.main.fragment_messages_conversation.view.*
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.*
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.pickncrop.library.MediaPickerActivity
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PICK_MEDIA
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.ThemedMediaPickerActivity
import org.mariotaku.twidere.adapter.MediaPreviewAdapter
import org.mariotaku.twidere.adapter.MessagesConversationAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.fragment.AbsContentListRecyclerViewFragment
import org.mariotaku.twidere.fragment.EditAltTextDialogFragment
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.loader.ObjectCursorLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.event.GetMessagesTaskEvent
import org.mariotaku.twidere.model.event.SendMessageTaskEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.task.compose.AbsAddMediaTask
import org.mariotaku.twidere.task.compose.AbsDeleteMediaTask
import org.mariotaku.twidere.task.twitter.message.DestroyMessageTask
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.task.twitter.message.MarkMessageReadTask
import org.mariotaku.twidere.util.ClipboardUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.PreviewGridItemDecoration
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.compose.MediaPreviewViewHolder
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

class MessagesConversationFragment : AbsContentListRecyclerViewFragment<MessagesConversationAdapter>(),
        IToolBarSupportFragment, LoaderManager.LoaderCallbacks<List<ParcelableMessage>?>,
        EditAltTextDialogFragment.EditAltTextCallback {
    private lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    private val accountKey: UserKey get() = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!

    private val conversationId: String get() = arguments?.getString(EXTRA_CONVERSATION_ID)!!

    private val account: AccountDetails? by lazy {
        AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
    }

    private val loadMoreTaskTag: String
        get() = "loadMore:$accountKey:$conversationId"

    // Layout manager reversed, so treat start as end
    override val reachingEnd: Boolean
        get() = super.reachingStart

    // Layout manager reversed, so treat end as start
    override val reachingStart: Boolean
        get() = super.reachingEnd

    override val controlBarHeight: Int
        get() = toolbar.height

    override var controlBarOffset: Float = 1f

    override val toolbar: Toolbar
        get() = conversationContainer.toolbar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val context = context ?: return
        val account = this.account ?: run {
            activity?.finish()
            return
        }
        adapter.listener = object : MessagesConversationAdapter.Listener {
            override fun onMediaClick(position: Int, media: ParcelableMedia, accountKey: UserKey?) {
                val message = adapter.getMessage(position)
                IntentUtils.openMediaDirectly(context = context, accountKey = accountKey,
                        media = message.media, current = media,
                        newDocument = preferences[newDocumentApiKey], message = message)
            }

            override fun onMessageLongClick(position: Int, holder: RecyclerView.ViewHolder): Boolean {
                return recyclerView.showContextMenuForChild(holder.itemView)
            }
        }
        mediaPreviewAdapter = MediaPreviewAdapter(context, requestManager)

        mediaPreviewAdapter.listener = object : MediaPreviewAdapter.Listener {
            override fun onRemoveClick(position: Int, holder: MediaPreviewViewHolder) {
                val task = DeleteMediaTask(this@MessagesConversationFragment,
                        arrayOf(mediaPreviewAdapter.getItem(position)))
                TaskStarter.execute(task)
            }

            override fun onEditClick(position: Int, holder: MediaPreviewViewHolder) {
                attachedMediaPreview.showContextMenuForChild(holder.itemView)
            }
        }
        attachedMediaPreview.layoutManager = FixedLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        attachedMediaPreview.adapter = mediaPreviewAdapter
        attachedMediaPreview.addItemDecoration(PreviewGridItemDecoration(resources.getDimensionPixelSize(R.dimen.element_spacing_small)))

        registerForContextMenu(recyclerView)
        registerForContextMenu(attachedMediaPreview)

        sendMessage.setOnClickListener {
            performSendMessage()
        }
        addMedia.setOnClickListener {
            openMediaPicker()
        }
        conversationTitleContainer.setOnClickListener {
            val intent = IntentUtils.messageConversationInfo(accountKey, conversationId)
            startActivityForResult(intent, REQUEST_MANAGE_CONVERSATION_INFO)
        }

        val activity = this.activity
        if (activity is AppCompatActivity) {
            activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        val theme = Chameleon.getOverrideTheme(context, activity)
        conversationTitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        conversationSubtitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        conversationAvatar.style = preferences[profileImageStyleKey]

        setupEditText()

        // No refresh for this fragment
        refreshEnabled = false
        adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.NONE

        if (account.type == AccountType.TWITTER) {
            addMedia.visibility = View.VISIBLE
        } else {
            addMedia.visibility = View.GONE
        }

        if (savedInstanceState != null) {
            val list = savedInstanceState.getParcelableArrayList<ParcelableMediaUpdate>(EXTRA_MEDIA)
            if (list != null) {
                mediaPreviewAdapter.addAll(list)
            }
        }

        updateMediaPreview()

        LoaderManager.getInstance(this).initLoader(0, null, this)
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_MEDIA, ArrayList(mediaPreviewAdapter.asList()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_MEDIA -> {
                when (resultCode) {
                    Activity.RESULT_OK -> if (data != null) {
                        val mediaUris = MediaPickerActivity.getMediaUris(data)
                        val types = data.getBundleExtra(MediaPickerActivity.EXTRA_EXTRAS)?.getIntArray(EXTRA_TYPES)
                        TaskStarter.execute(AddMediaTask(this, mediaUris, types,
                            copySrc = false,
                            deleteSrc = false
                        ))
                    }
                    RESULT_SEARCH_GIF -> {
                        val provider = gifShareProvider ?: return
                        startActivityForResult(provider.createGifSelectorIntent(), REQUEST_ADD_GIF)
                    }
                }

            }
            REQUEST_MANAGE_CONVERSATION_INFO -> {
                if (resultCode == MessageConversationInfoFragment.RESULT_CLOSE) {
                    activity?.finish()
                }
            }
            REQUEST_ADD_GIF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val intent = context?.let {
                        ThemedMediaPickerActivity.withThemed(it)
                                .getMedia(data.data!!)
                                .extras(Bundle { this[EXTRA_TYPES] = intArrayOf(ParcelableMedia.Type.ANIMATED_GIF) })
                                .build()
                    }
                    startActivityForResult(intent, REQUEST_PICK_MEDIA)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation, container, false)
    }

    override fun setupWindow(activity: FragmentActivity): Boolean {
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_messages_conversation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        }
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableMessage>?> {
        return ConversationLoader(requireContext(), accountKey, conversationId)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessage>?>, data: List<ParcelableMessage>?) {
        val conversationLoader = loader as? ConversationLoader
        val conversation = conversationLoader?.conversation
        adapter.setData(conversation, data)
        adapter.displaySenderProfile = conversation?.conversation_type == ConversationType.GROUP
        if (conversation?.conversation_extras_type == ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL) {
            adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.START
        } else {
            adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.NONE
        }
        showContent()

        if (conversation != null && !conversation.is_temp) {
            markRead()
        }
        updateConversationStatus()
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableMessage>?>) {
        adapter.setData(null, null)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): MessagesConversationAdapter {
        return MessagesConversationAdapter(context, this.requestManager)
    }

    override fun onCreateLayoutManager(context: Context): LinearLayoutManager {
        return FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
    }

    override fun onCreateItemDecoration(context: Context, recyclerView: RecyclerView,
                                        layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        return null
    }

    override fun onLoadMoreContents(position: Long) {
        if (ILoadMoreSupportAdapter.START !in position) return
        val context = context ?: return
        val message = adapter.getMessage(adapter.messageRange.last)
        setLoadMoreIndicatorPosition(position)
        val param = GetMessagesTask.LoadMoreMessageTaskParam(context, accountKey, conversationId,
                message.id)
        param.taskTag = loadMoreTaskTag
        twitterWrapper.getMessagesAsync(param)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (menuInfo !is ExtendedRecyclerView.ContextMenuInfo) return
        val context = context ?: return
        val activity = activity ?: return
        when (menuInfo.recyclerViewId) {
            R.id.recyclerView -> {
                val message = adapter.getMessage(menuInfo.position)
                val conversation = adapter.conversation
                menu.setHeaderTitle(message.getSummaryText(context, userColorNameManager, conversation,
                        preferences[nameFirstKey]))
                activity.menuInflater.inflate(R.menu.menu_conversation_message_item, menu)
            }
            R.id.attachedMediaPreview -> {
                menu.setHeaderTitle(R.string.edit_media)
                activity.menuInflater.inflate(R.menu.menu_attached_media_edit, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: run {
            return super.onContextItemSelected(item)
        }
        val context = context ?: return super.onContextItemSelected(item)
        when (menuInfo.recyclerViewId) {
            R.id.recyclerView -> {
                val message = adapter.getMessage(menuInfo.position)
                when (item.itemId) {
                    R.id.copy -> {
                        ClipboardUtils.setText(context, message.text_unescaped)
                    }
                    R.id.delete -> {
                        val task = DestroyMessageTask(context, message.account_key,
                                message.conversation_id, message.id)
                        TaskStarter.execute(task)
                    }
                }
                return true
            }
            R.id.attachedMediaPreview -> {
                when (item.itemId) {
                    R.id.edit_description -> {
                        val position = menuInfo.position
                        val altText = mediaPreviewAdapter.getItem(position).alt_text
                        executeAfterFragmentResumed { fragment ->
                            EditAltTextDialogFragment.show(fragment.childFragmentManager, position,
                                    altText)
                        }
                    }
                }
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onSetAltText(position: Int, altText: String?) {
        mediaPreviewAdapter.setAltText(position, altText)
    }

    override fun onApplySystemWindowInsets(insets: Rect) {
        view?.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }

    @Subscribe
    fun onGetMessagesTaskEvent(event: GetMessagesTaskEvent) {
        if (!event.running && event.taskTag == loadMoreTaskTag) {
            setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
        }
    }

    @Subscribe
    fun onSendMessageTaskEvent(event: SendMessageTaskEvent) {
        if (!event.success || event.accountKey != accountKey || event.conversationId != conversationId) {
            return
        }
        val arguments = arguments ?: return
        val activity = activity ?: return
        val newConversationId = event.newConversationId ?: return
        arguments[EXTRA_CONVERSATION_ID] = newConversationId
        if (activity is LinkHandlerActivity) {
            activity.intent = IntentUtils.messageConversation(accountKey, newConversationId)
        }
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    private fun performSendMessage() {
        val context = context ?: return
        val conversation = adapter.conversation ?: return
        val conversationAccount = this.account ?: return
        if (conversation.readOnly) return
        if (editText.empty && mediaPreviewAdapter.itemCount == 0) {
            editText.error = getString(R.string.hint_error_message_no_content)
            return
        }
        if (conversationAccount.type == AccountType.TWITTER) {
            if (mediaPreviewAdapter.itemCount > defaultFeatures.twitterDirectMessageMediaLimit) {
                editText.error = getString(R.string.error_message_media_message_too_many)
                return
            } else {
                editText.error = null
            }
        } else if (mediaPreviewAdapter.itemCount > 0) {
            editText.error = getString(R.string.error_message_media_message_attachment_not_supported)
            return
        }
        val text = editText.text.toString()
        val message = ParcelableNewMessage().apply {
            this.account = conversationAccount
            this.media = mediaPreviewAdapter.asList().toTypedArray()
            this.conversation_id = conversation.id
            this.recipient_ids = conversation.participants?.filter {
                it.key != accountKey
            }?.map {
                it.key.id
            }?.toTypedArray()
            this.text = text
            this.is_temp_conversation = conversation.is_temp
        }
        LengthyOperationsService.sendMessageAsync(context, message)
        editText.text = null

        // Clear media, those media will be deleted after sent
        mediaPreviewAdapter.clear()
        updateMediaPreview()
    }

    private fun openMediaPicker() {
        val context = context ?: return
        val builder = ThemedMediaPickerActivity.withThemed(context)
        builder.pickSources(arrayOf(MediaPickerActivity.SOURCE_CAMERA,
                MediaPickerActivity.SOURCE_CAMCORDER,
                MediaPickerActivity.SOURCE_GALLERY,
                MediaPickerActivity.SOURCE_CLIPBOARD))
        if (gifShareProvider != null) {
            builder.addEntry(getString(R.string.action_add_gif), "gif", RESULT_SEARCH_GIF)
        }
        builder.containsVideo(true)
        builder.allowMultiple(false)
        val intent = builder.build()
        startActivityForResult(intent, REQUEST_PICK_MEDIA)
    }

    private fun attachMedia(media: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.addAll(media)
        updateMediaPreview()
    }

    private fun removeMedia(media: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.removeAll(media)
        updateMediaPreview()
    }

    private fun updateMediaPreview() {
        attachedMediaPreview.visibility = if (mediaPreviewAdapter.itemCount > 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
        editText.error = null
    }

    private fun setProgressVisible(visible: Boolean) {

    }

    private fun markRead() {
        val context = context ?: return
        TaskStarter.execute(MarkMessageReadTask(context, accountKey, conversationId))
    }

    private fun updateConversationStatus() {
        val context = context ?: return
        val activity = activity ?: return
        if (isDetached || activity.isFinishing) return
        val conversation = adapter.conversation ?: return
        val title = conversation.getTitle(context, userColorNameManager,
                preferences[nameFirstKey]).first
        val subtitle = conversation.getSubtitle(context)
        activity.title = title
        val readOnly = conversation.readOnly
        addMedia.isEnabled = !readOnly
        sendMessage.isEnabled = !readOnly
        editText.isEnabled = !readOnly

        conversationTitle.spannable = title
        if (subtitle != null) {
            conversationSubtitle.visibility = View.VISIBLE
            conversationSubtitle.spannable = subtitle
        } else {
            conversationSubtitle.visibility = View.GONE
        }


        val stateIcon = if (conversation.notificationDisabled) {
            ContextCompat.getDrawable(context, R.drawable.ic_message_type_speaker_muted)?.apply {
                mutate().setColorFilter(conversationTitle.currentTextColor, PorterDuff.Mode.SRC_ATOP)
            }
        } else {
            null
        }
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(conversationTitle, null,
                null, stateIcon, null)

        requestManager.loadProfileImage(context, conversation, preferences[profileImageStyleKey])
                .into(conversationAvatar)
    }

    private fun setupEditText() {
        editText.imageInputListener = { contentInfo ->
            val type = if (contentInfo.description.mimeTypeCount > 0) {
                AbsAddMediaTask.inferMediaType(contentInfo.description.getMimeType(0))
            } else {
                ParcelableMedia.Type.IMAGE
            }
            val task = AddMediaTask(this, arrayOf(contentInfo.contentUri), intArrayOf(type),
                copySrc = true,
                deleteSrc = false
            )
            task.callback = {
                contentInfo.releasePermission()
            }
            TaskStarter.execute(task)
        }
    }

    internal class AddMediaTask(
            fragment: MessagesConversationFragment,
            sources: Array<Uri>,
            types: IntArray?,
            copySrc: Boolean,
            deleteSrc: Boolean
    ) : AbsAddMediaTask<((List<ParcelableMediaUpdate>?) -> Unit)?>(fragment.requireContext(), sources, types, copySrc, deleteSrc) {

        private val fragmentRef = WeakReference(fragment)

        override fun afterExecute(callback: ((List<ParcelableMediaUpdate>?) -> Unit)?, result: List<ParcelableMediaUpdate>?) {
            callback?.invoke(result)
            val fragment = fragmentRef.get()
            if (fragment != null && result != null) {
                fragment.setProgressVisible(false)
                fragment.attachMedia(result)
            }
        }

        override fun beforeExecute() {
            val fragment = fragmentRef.get() ?: return
            fragment.setProgressVisible(true)
        }

    }

    internal class DeleteMediaTask(
            fragment: MessagesConversationFragment,
            val media: Array<ParcelableMediaUpdate>
    ) : AbsDeleteMediaTask<MessagesConversationFragment>(fragment.requireContext(),
            media.mapToArray { Uri.parse(it.uri) }) {

        init {
            callback = fragment
        }

        override fun afterExecute(callback: MessagesConversationFragment?, results: BooleanArray) {
            if (callback == null) return
            callback.setProgressVisible(false)
            callback.removeMedia(media.toList())
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
    ) : ObjectCursorLoader<ParcelableMessage>(context, ParcelableMessage::class.java) {

        private val atomicConversation = AtomicReference<ParcelableMessageConversation?>()
        val conversation: ParcelableMessageConversation? get() = atomicConversation.get()

        init {
            uri = Messages.CONTENT_URI
            projection = Messages.COLUMNS
            selection = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
            selectionArgs = arrayOf(accountKey.toString(), conversationId)
            sortOrder = OrderBy(Messages.SORT_ID, false).sql
            isUseCache = false
        }

        override fun onLoadInBackground(): MutableList<ParcelableMessage>? {
            atomicConversation.set(DataStoreUtils.findMessageConversation(context, accountKey, conversationId))
            return super.onLoadInBackground()
        }
    }

    companion object {
        private const val REQUEST_MANAGE_CONVERSATION_INFO = 101
        private const val REQUEST_ADD_GIF = 102
        private const val RESULT_SEARCH_GIF = 11

        private const val EXTRA_TYPES = "types"
    }

}

