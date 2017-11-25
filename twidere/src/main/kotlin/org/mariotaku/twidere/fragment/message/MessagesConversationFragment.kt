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
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import kotlinx.android.synthetic.main.fragment_messages_conversation.*
import kotlinx.android.synthetic.main.fragment_messages_conversation.view.*
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
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
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_MEDIA
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.*
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
import org.mariotaku.twidere.promise.MessagePromises
import org.mariotaku.twidere.provider.TwidereDataStore.Messages
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.task.twitter.message.GetMessagesTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.holder.compose.MediaPreviewViewHolder
import java.util.concurrent.atomic.AtomicReference

class MessagesConversationFragment : AbsContentListRecyclerViewFragment<MessagesConversationAdapter>(),
        IToolBarSupportFragment, LoaderManager.LoaderCallbacks<List<ParcelableMessage>?>,
        EditAltTextDialogFragment.EditAltTextCallback {
    private lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    private inline val accountKey: UserKey get() = arguments!!.accountKey!!

    private val conversationId: String get() = arguments!!.conversationId!!

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
        get() = fragmentToolbar.height

    override var controlBarOffset: Float = 1f

    override val fragmentToolbar: Toolbar
        get() = conversationContainer.toolbar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        linkHandlerTitle = getString(R.string.title_direct_messages)
        val account = this.account ?: run {
            activity?.finish()
            return
        }
        val activity = this.activity!!
        adapter.listener = object : MessagesConversationAdapter.Listener {
            override fun onMediaClick(position: Int, media: ParcelableMedia, accountKey: UserKey?) {
                val message = adapter.getMessage(position)
                IntentUtils.openMediaDirectly(context = activity, accountKey = accountKey,
                        media = message.media, current = media,
                        newDocument = preferences[newDocumentApiKey], message = message)
            }

            override fun onMessageLongClick(position: Int, holder: RecyclerView.ViewHolder): Boolean {
                return recyclerView.showContextMenuForChild(holder.itemView)
            }
        }
        mediaPreviewAdapter = MediaPreviewAdapter(activity, requestManager)

        mediaPreviewAdapter.listener = object : MediaPreviewAdapter.Listener {
            override fun onRemoveClick(position: Int, holder: MediaPreviewViewHolder) {
                deleteMedia(mediaPreviewAdapter.getItem(position))
            }

            override fun onEditClick(position: Int, holder: MediaPreviewViewHolder) {
                attachedMediaPreview.showContextMenuForChild(holder.itemView)
            }
        }
        attachedMediaPreview.layoutManager = FixedLinearLayoutManager(activity,
                LinearLayoutManager.HORIZONTAL, false)
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

        if (activity is AppCompatActivity) {
            activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        val theme = Chameleon.getOverrideTheme(activity, activity)
        conversationTitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        conversationSubtitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        conversationAvatar.style = preferences[profileImageStyleKey]

        setupEditText()

        // No refresh for this fragment
        refreshEnabled = false
        adapter.loadMoreSupportedPosition = LoadMorePosition.NONE

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

        loaderManager.initLoader(0, null, this)
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
                        performObtainMedia(mediaUris, types, false, false)
                    }
                    RESULT_SEARCH_GIF -> {
                        startActivityForResult(gifShareProvider.createGifSelectorIntent(), REQUEST_ADD_GIF)
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
                    val intent = ThemedMediaPickerActivity.withThemed(context!!)
                            .getMedia(data.data)
                            .extras(Bundle { this[EXTRA_TYPES] = intArrayOf(ParcelableMedia.Type.ANIMATED_GIF) })
                            .build()
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
        return ConversationLoader(context!!, accountKey, conversationId)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableMessage>?>, data: List<ParcelableMessage>?) {
        val conversationLoader = loader as? ConversationLoader
        val conversation = conversationLoader?.conversation
        adapter.setData(conversation, data)
        adapter.displaySenderProfile = conversation?.conversation_type == ConversationType.GROUP
        if (conversation?.conversation_extras_type == ParcelableMessageConversation.ExtrasType.TWITTER_OFFICIAL) {
            adapter.loadMoreSupportedPosition = LoadMorePosition.START
        } else {
            adapter.loadMoreSupportedPosition = LoadMorePosition.NONE
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

    override fun onLoadMoreContents(position: Int) {
        if (LoadMorePosition.START !in position) return
        val message = adapter.getMessage(adapter.messageRange.endInclusive)
        setLoadMoreIndicatorPosition(position)
        val task = GetMessagesTask(context!!)
        task.params = GetMessagesTask.LoadMoreMessagesParam(context!!, accountKey, conversationId,
                message.id).apply {
            taskTag = loadMoreTaskTag
        }
        task.promise()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        if (menuInfo !is ExtendedRecyclerView.ContextMenuInfo) return
        when (menuInfo.recyclerViewId) {
            R.id.recyclerView -> {
                val message = adapter.getMessage(menuInfo.position)
                val conversation = adapter.conversation
                menu.setHeaderTitle(message.getSummaryText(context!!, userColorNameManager, conversation,
                        preferences[nameFirstKey]))
                activity!!.menuInflater.inflate(R.menu.menu_conversation_message_item, menu)
            }
            R.id.attachedMediaPreview -> {
                menu.setHeaderTitle(R.string.edit_media)
                activity!!.menuInflater.inflate(R.menu.menu_attached_media_edit, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: run {
            return super.onContextItemSelected(item)
        }
        when (menuInfo.recyclerViewId) {
            R.id.recyclerView -> {
                val message = adapter.getMessage(menuInfo.position)
                when (item.itemId) {
                    R.id.copy -> {
                        ClipboardUtils.setText(context!!, message.text_unescaped)
                    }
                    R.id.delete -> {
                        // TODO show progress
                        MessagePromises.getInstance(context!!).destroyMessage(message.account_key, message.conversation_id,
                                message.id)
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
            setLoadMoreIndicatorPosition(LoadMorePosition.NONE)
        }
    }

    @Subscribe
    fun onSendMessageTaskEvent(event: SendMessageTaskEvent) {
        if (!event.success || event.accountKey != accountKey || event.conversationId != conversationId) {
            return
        }
        val newConversationId = event.newConversationId ?: return
        arguments?.conversationId = newConversationId
        val activity = activity
        if (activity is LinkHandlerActivity) {
            activity.intent = IntentUtils.messageConversation(accountKey, newConversationId)
        }
        loaderManager.restartLoader(0, null, this)
    }

    private fun performSendMessage() {
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
        LengthyOperationsService.sendMessageAsync(context!!, message)
        editText.text = null

        // Clear media, those media will be deleted after sent
        mediaPreviewAdapter.clear()
        updateMediaPreview()
    }

    private fun openMediaPicker() {
        val builder = ThemedMediaPickerActivity.withThemed(context!!)
        builder.pickSources(arrayOf(MediaPickerActivity.SOURCE_CAMERA,
                MediaPickerActivity.SOURCE_CAMCORDER,
                MediaPickerActivity.SOURCE_GALLERY,
                MediaPickerActivity.SOURCE_CLIPBOARD))
        if (gifShareProvider.supported) {
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
        // TODO: Promise progress
        MessagePromises.getInstance(context!!).markRead(accountKey, conversationId)
    }

    private fun updateConversationStatus() {
        val activity = this.activity ?: return
        if (isDetached || activity.isFinishing) return
        val conversation = adapter.conversation ?: return
        val title = conversation.getTitle(activity, userColorNameManager,
                preferences[nameFirstKey]).first
        val subtitle = conversation.getSubtitle(activity)
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
            ContextCompat.getDrawable(activity, R.drawable.ic_message_type_speaker_muted)?.apply {
                mutate()
                setColorFilter(conversationTitle.currentTextColor, PorterDuff.Mode.SRC_ATOP)
            }
        } else {
            null
        }
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(conversationTitle, null,
                null, stateIcon, null)

        requestManager.loadProfileImage(activity, conversation, preferences[profileImageStyleKey])
                .into(conversationAvatar)
    }

    private fun setupEditText() {
        editText.imageInputListener = { contentInfo ->
            val weakThis by weak(this)
            val weakContentInfo by weak(contentInfo)
            promiseOnUi {
                weakThis?.setProgressVisible(true)
            } and context!!.obtainMedia(arrayOf(contentInfo.contentUri), intArrayOf(contentInfo.inferredMediaType),
                    true, false).successUi { media ->
                weakThis?.attachMedia(media)
            }.alwaysUi {
                weakThis?.setProgressVisible(false)
                weakContentInfo?.releasePermission()
            }
        }
    }

    private fun performObtainMedia(sources: Array<Uri>, types: IntArray?, copySrc: Boolean, deleteSrc: Boolean) {
        val weakThis by weak(this)
        promiseOnUi {
            weakThis?.setProgressVisible(true)
        } and context!!.obtainMedia(sources, types, copySrc, deleteSrc).successUi { media ->
            weakThis?.attachMedia(media)
        }.alwaysUi {
            weakThis?.setProgressVisible(false)
        }
    }

    private fun deleteMedia(vararg media: ParcelableMediaUpdate) {
        val weakThis by weak(this)
        promiseOnUi {
            weakThis?.setProgressVisible(true)
        }.then {
            val context = weakThis?.context ?: throw InterruptedException()
            media.forEach {
                Utils.deleteMedia(context, Uri.parse(it.uri))
            }
        }.alwaysUi {
            weakThis?.setProgressVisible(false)
            weakThis?.removeMedia(media.toList())
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

        override fun onLoadInBackground(): List<ParcelableMessage>? {
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

