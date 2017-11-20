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
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.FixedAsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.CompoundButton
import android.widget.EditText
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.activity_home_content.view.*
import kotlinx.android.synthetic.main.fragment_messages_conversation_info.*
import kotlinx.android.synthetic.main.header_message_conversation_info.view.*
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.pickncrop.library.MediaPickerActivity
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.ThemedMediaPickerActivity
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.annotation.ProfileImageSize
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.view.calculateSpaceItemHeight
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.fragment.message.MessageConversationInfoFragment.ConversationInfoAdapter.Companion.VIEW_TYPE_BOTTOM_SPACE
import org.mariotaku.twidere.fragment.message.MessageConversationInfoFragment.ConversationInfoAdapter.Companion.VIEW_TYPE_HEADER
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.ParcelableMessageConversation.ExtrasType
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.promise.ConversationPromises
import org.mariotaku.twidere.promise.MessagePromises
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.promise.UpdateStatusPromise
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.view.holder.SimpleUserViewHolder

/**
 * Created by mariotaku on 2017/2/15.
 */

class MessageConversationInfoFragment : BaseFragment(), IToolBarSupportFragment,
        LoaderManager.LoaderCallbacks<ParcelableMessageConversation?> {

    private val accountKey: UserKey get() = arguments!!.accountKey!!
    private val conversationId: String get() = arguments!!.conversationId!!

    private lateinit var adapter: ConversationInfoAdapter
    private lateinit var itemDecoration: ConversationInfoDecoration

    override val controlBarHeight: Int get() = fragmentToolbar.measuredHeight
    override var controlBarOffset: Float = 0f

    override val fragmentToolbar: Toolbar
        get() = toolbarLayout.toolbar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        linkHandlerTitle = getString(R.string.title_direct_messages_conversation_info)

        val context = this.activity!!

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        val theme = Chameleon.getOverrideTheme(context, activity)

        adapter = ConversationInfoAdapter(context, requestManager)
        adapter.listener = object : ConversationInfoAdapter.Listener {
            override fun onUserClick(position: Int) {
                val user = adapter.getUser(position) ?: return
                startActivity(IntentUtils.userProfile(user))
            }

            override fun onAddUserClick(position: Int) {
                val conversation = adapter.conversation ?: return
                val intent = Intent(IntentConstants.INTENT_ACTION_SELECT_USER)
                intent.putExtra(EXTRA_ACCOUNT_KEY, conversation.account_key)
                intent.setClass(context, UserSelectorActivity::class.java)
                startActivityForResult(intent, REQUEST_CONVERSATION_ADD_USER)
            }

            override fun onDisableNotificationChanged(disabled: Boolean) {
                performSetNotificationDisabled(disabled)
            }

        }
        itemDecoration = ConversationInfoDecoration(adapter,
                resources.getDimensionPixelSize(R.dimen.element_spacing_large)
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LayoutManager(context)
        recyclerView.addItemDecoration(itemDecoration)


        val profileImageStyle = preferences[profileImageStyleKey]
        appBarIcon.style = profileImageStyle
        conversationAvatar.style = profileImageStyle

        toolbarLayout.setStatusBarScrimColor(theme.statusBarColor)
        coordinatorLayout.setStatusBarBackgroundColor(theme.statusBarColor)

        val avatarBackground = ChameleonUtils.getColorDependent(theme.colorToolbar)
        appBarIcon.setShapeBackground(avatarBackground)
        appBarTitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        appBarSubtitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        conversationAvatar.setShapeBackground(avatarBackground)
        conversationTitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        conversationSubtitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        editButton.setOnClickListener {
            executeAfterFragmentResumed { fragment ->
                val df = EditInfoDialogFragment()
                df.show(fragment.childFragmentManager, "edit_info")
            }
        }

        loaderManager.initLoader(0, null, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONVERSATION_ADD_USER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                    performAddParticipant(user)
                }
            }
            REQUEST_PICK_MEDIA -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = MediaPickerActivity.getMediaUris(data).firstOrNull() ?: return
                        performSetConversationAvatar(uri)
                    }
                    RESULT_CODE_REMOVE_CONVERSATION_AVATAR -> {
                        performSetConversationAvatar(null)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation_info, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_messages_conversation_info, menu)
    }

    override fun onApplySystemWindowInsets(insets: Rect) {
        // No-op
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.setItemAvailability(R.id.clear_messages, true)
        menu.setItemAvailability(R.id.leave_conversation, true)
        if (adapter.conversation?.conversation_extras_type == ExtrasType.TWITTER_OFFICIAL) {
            menu.setItemTitle(R.id.leave_conversation, R.string.action_leave_conversation)
        } else {
            menu.setItemTitle(R.id.leave_conversation, R.string.action_clear_messages_and_delete_conversation)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.leave_conversation -> {
                val df = DestroyConversationConfirmDialogFragment()
                df.show(childFragmentManager, "destroy_conversation_confirm")
                return true
            }
            R.id.clear_messages -> {
                val df = ClearMessagesConfirmDialogFragment()
                df.show(childFragmentManager, "clear_messages_confirm")
                return true
            }
        }
        return false
    }

    override fun setupWindow(activity: FragmentActivity): Boolean {
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ParcelableMessageConversation?> {
        return ConversationInfoLoader(context!!, accountKey, conversationId)
    }

    override fun onLoaderReset(loader: Loader<ParcelableMessageConversation?>?) {
    }

    override fun onLoadFinished(loader: Loader<ParcelableMessageConversation?>?, data: ParcelableMessageConversation?) {
        val context = this.context ?: return
        if (data == null) {
            activity?.finish()
            return
        }

        val name = data.getTitle(context, userColorNameManager, preferences[nameFirstKey]).first
        val summary = data.getSubtitle(context)

        @ImageShapeStyle val profileImageStyle: Int = preferences[profileImageStyleKey]
        requestManager.loadProfileImage(context, data, profileImageStyle).into(conversationAvatar)
        requestManager.loadProfileImage(context, data, profileImageStyle, 0f,
                0f, ProfileImageSize.REASONABLY_SMALL).into(appBarIcon)
        appBarTitle.spannable = name
        conversationTitle.spannable = name
        if (summary != null) {
            appBarSubtitle.visibility = View.VISIBLE
            conversationSubtitle.visibility = View.VISIBLE

            appBarSubtitle.spannable = summary
            conversationSubtitle.spannable = summary
        } else {
            appBarSubtitle.visibility = View.GONE
            conversationSubtitle.visibility = View.GONE
        }
        if (data.conversation_extras_type == ExtrasType.TWITTER_OFFICIAL
                && data.conversation_type == ConversationType.GROUP) {
            editButton.visibility = View.VISIBLE
            adapter.showButtonSpace = true
        } else {
            editButton.visibility = View.GONE
            adapter.showButtonSpace = false
        }

        adapter.conversation = data

        activity?.invalidateOptionsMenu()
    }

    private fun performDestroyConversation() {
        val weakThis by weak(this)
        showProgressDialog("leave_conversation_progress") and MessagePromises.getInstance(context!!).destroyConversation(accountKey, conversationId).successUi {
            val f = weakThis ?: return@successUi
            f.dismissProgressDialog("leave_conversation_progress")
            f.activity?.setResult(RESULT_CLOSE)
            f.activity?.finish()
        }
    }

    private fun performClearMessages() {
        val weakThis by weak(this)
        showProgressDialog("clear_messages_progress") and MessagePromises.getInstance(context!!).clearMessages(accountKey, conversationId).successUi { succeed ->
            val f = weakThis ?: return@successUi
            f.dismissDialogThen("clear_messages_progress") {
                if (succeed) {
                    activity?.finish()
                }
            }
        }
    }

    private fun performAddParticipant(user: ParcelableUser) {
        ProgressDialogFragment.show(childFragmentManager, "add_participant_progress")
        val weakThis by weak(this)
        ConversationPromises.getInstance(context!!).addParticipants(accountKey,
                conversationId, listOf(user)).alwaysUi {
            weakThis?.dismissDialogThen("add_participant_progress") {
                loaderManager.restartLoader(0, null, this)
            }
        }
    }

    private fun performSetNotificationDisabled(disabled: Boolean) {
        val weakThis by weak(this)
        showProgressDialog("set_notifications_disabled_progress") and ConversationPromises.getInstance(context!!).setNotificationDisabled(accountKey, conversationId, disabled).alwaysUi {
            weakThis?.dismissProgressDialog("set_notifications_disabled_progress")
        }
    }


    private fun openEditAction(type: String) {
        when (type) {
            "name" -> {
                executeAfterFragmentResumed { fragment ->
                    val df = EditNameDialogFragment()
                    df.show(fragment.childFragmentManager, "edit_name")
                }
            }
            "avatar" -> {
                val intent = ThemedMediaPickerActivity.withThemed(context!!)
                        .allowMultiple(false)
                        .aspectRatio(1, 1)
                        .containsVideo(false)
                        .addEntry(getString(R.string.action_remove_conversation_avatar),
                                "remove_avatar", RESULT_CODE_REMOVE_CONVERSATION_AVATAR)
                        .build()
                startActivityForResult(intent, REQUEST_PICK_MEDIA)
            }
        }
    }

    private fun performSetConversationName(name: String) {
        val conversationId = this.conversationId
        performUpdateInfo("set_name_progress", updateAction = updateAction@ { fragment, account, microBlog ->
            val context = fragment.context!!
            when (account.type) {
                AccountType.TWITTER -> {
                    if (account.isOfficial(context)) {
                        return@updateAction microBlog.updateDmConversationName(conversationId, name).isSuccessful
                    }
                }
            }
            throw UnsupportedOperationException()
        }, successAction = {
            put(Conversations.CONVERSATION_NAME, name)
        })
    }

    private fun performSetConversationAvatar(uri: Uri?) {
        val conversationId = this.conversationId
        performUpdateInfo("set_avatar_progress", updateAction = updateAction@ { fragment, account, microBlog ->
            val context = fragment.context!!
            when (account.type) {
                AccountType.TWITTER -> {
                    if (account.isOfficial(context)) {
                        val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                        if (uri == null) {
                            val result = microBlog.updateDmConversationAvatar(conversationId, null)
                            if (result.isSuccessful) {
                                val dmResponse = microBlog.getDmConversation(conversationId, null).conversationTimeline
                                return@updateAction dmResponse.conversations[conversationId]?.avatarImageHttps
                            }
                            throw MicroBlogException("Error ${result.responseCode}")
                        }
                        var deleteAlways: List<UpdateStatusPromise.MediaDeletionItem>? = null
                        try {
                            val media = arrayOf(ParcelableMediaUpdate().apply {
                                this.uri = uri.toString()
                                this.delete_always = true
                            })
                            val uploadResult = UpdateStatusPromise.uploadMicroBlogMediaShared(context,
                                    upload, account, media, null, null, true, null)
                            deleteAlways = uploadResult.deleteAlways
                            val avatarId = uploadResult.ids.first()
                            val result = microBlog.updateDmConversationAvatar(conversationId, avatarId)
                            if (result.isSuccessful) {
                                uploadResult.deleteOnSuccess.forEach { it.delete(context) }
                                val dmResponse = microBlog.getDmConversation(conversationId, null).conversationTimeline
                                return@updateAction dmResponse.conversations[conversationId]?.avatarImageHttps
                            }
                            throw MicroBlogException("Error ${result.responseCode}")
                        } catch (e: UpdateStatusPromise.UploadException) {
                            e.deleteAlways?.forEach {
                                it.delete(context)
                            }
                            throw e
                        } finally {
                            deleteAlways?.forEach { it.delete(context) }
                        }
                    }
                }
            }
            throw UnsupportedOperationException()
        }, successAction = { u ->
            put(Conversations.CONVERSATION_AVATAR, u)
        })
    }

    private inline fun <T> performUpdateInfo(
            tag: String,
            crossinline updateAction: (MessageConversationInfoFragment, AccountDetails, MicroBlog) -> T,
            crossinline successAction: ContentValues.(T) -> Unit
    ) {
        ProgressDialogFragment.show(childFragmentManager, tag)
        val weakThis by weak(this)
        val accountKey = this.accountKey
        val conversationId = this.conversationId
        task {
            val fragment = weakThis ?: throw InterruptedException()
            val context = fragment.context ?: throw InterruptedException()
            val account = AccountUtils.getAccountDetails(AccountManager.get(context),
                    accountKey, true) ?: throw MicroBlogException("No account")
            val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
            return@task updateAction(fragment, account, microBlog)
        }.then { result ->
            val fragment = weakThis ?: throw InterruptedException()
            val context = fragment.context ?: throw InterruptedException()
            val values = ContentValues().apply { successAction(result) }
            val where = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val whereArgs = arrayOf(accountKey.toString(), conversationId)
            context.contentResolver.update(Conversations.CONTENT_URI, values, where,
                    whereArgs)
        }.alwaysUi {
            val fragment = weakThis ?: return@alwaysUi
            fragment.dismissDialogThen(tag) {
                loaderManager.restartLoader(0, null, this)
            }
        }
    }

    private inline fun dismissDialogThen(tag: String, crossinline action: MessageConversationInfoFragment.() -> Unit) {
        executeAfterFragmentResumed { fragment ->
            val df = fragment.childFragmentManager.findFragmentByTag(tag) as? DialogFragment
            df?.dismiss()
            action(fragment as MessageConversationInfoFragment)
        }
    }

    internal class ConversationInfoLoader(
            context: Context,
            val accountKey: UserKey,
            val conversationId: String
    ) : FixedAsyncTaskLoader<ParcelableMessageConversation?>(context) {

        override fun loadInBackground(): ParcelableMessageConversation? {
            val where = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val whereArgs = arrayOf(accountKey.toString(), conversationId)
            context.contentResolver.queryReference(Conversations.CONTENT_URI, Conversations.COLUMNS, where,
                    whereArgs, null)?.use { (cur) ->
                if (cur.moveToFirst()) {
                    val indices = ObjectCursor.indicesFrom(cur, ParcelableMessageConversation::class.java)
                    return indices.newObject(cur)
                }
            }
            return null
        }

        override fun onStartLoading() {
            forceLoad()
        }

    }

    class ConversationInfoAdapter(
            context: Context,
            requestManager: RequestManager
    ) : BaseRecyclerViewAdapter<RecyclerView.ViewHolder>(context, requestManager),
            IItemCountsAdapter {
        private val inflater = LayoutInflater.from(context)

        override val itemCounts: ItemCounts = ItemCounts(5)

        var listener: Listener? = null

        var conversation: ParcelableMessageConversation? = null
            set(value) {
                field = value
                updateItemCounts()
                notifyDataSetChanged()
            }

        var showButtonSpace: Boolean = false
            set(value) {
                field = value
                updateItemCounts()
                notifyDataSetChanged()
            }


        init {
            setHasStableIds(true)
        }

        override fun getItemCount(): Int {
            return itemCounts.itemCount
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                VIEW_TYPE_HEADER -> {
                    (holder as HeaderViewHolder).display(this.conversation!!)
                }
                VIEW_TYPE_USER -> {
                    val participantIdx = position - itemCounts.getItemStartPosition(ITEM_INDEX_ITEM)
                    val user = getUser(position)!!
                    (holder as UserViewHolder).display(user, participantIdx == 0)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                VIEW_TYPE_TOP_SPACE -> {
                    val view = inflater.inflate(R.layout.header_message_conversation_info_button_space, parent, false)
                    return SpaceViewHolder(view)
                }
                VIEW_TYPE_HEADER -> {
                    val view = inflater.inflate(HeaderViewHolder.layoutResource, parent, false)
                    return HeaderViewHolder(view, this)
                }
                VIEW_TYPE_USER -> {
                    val view = inflater.inflate(R.layout.list_item_conversation_info_user, parent, false)
                    return UserViewHolder(view, this)
                }
                VIEW_TYPE_ADD_USER -> {
                    val view = inflater.inflate(R.layout.list_item_conversation_info_add_user, parent, false)
                    return AddUserViewHolder(view, this)
                }
                VIEW_TYPE_BOTTOM_SPACE -> {
                    val view = inflater.inflate(R.layout.list_item_conversation_info_space, parent, false)
                    return SpaceViewHolder(view)
                }
            }
            throw UnsupportedOperationException()
        }

        override fun getItemViewType(position: Int): Int {
            val countIndex = itemCounts.getItemCountIndex(position)
            when (countIndex) {
                ITEM_INDEX_TOP_SPACE -> return VIEW_TYPE_TOP_SPACE
                ITEM_INDEX_HEADER -> return VIEW_TYPE_HEADER
                ITEM_INDEX_ITEM -> return VIEW_TYPE_USER
                ITEM_INDEX_ADD_USER -> return VIEW_TYPE_ADD_USER
                ITEM_INDEX_SPACE -> return VIEW_TYPE_BOTTOM_SPACE
                else -> throw UnsupportedCountIndexException(countIndex, position)
            }
        }

        override fun getItemId(position: Int): Long {
            val countIndex = itemCounts.getItemCountIndex(position)
            when (countIndex) {
                ITEM_INDEX_ITEM -> {
                    val user = getUser(position)!!
                    return (countIndex.toLong() shl 32) or user.hashCode().toLong()
                }
                else -> {
                    return (countIndex.toLong() shl 32) or getItemViewType(position).toLong()
                }
            }
        }

        fun getUser(position: Int): ParcelableUser? {
            val itemPos = position - itemCounts.getItemStartPosition(ITEM_INDEX_ITEM)
            return conversation?.participants?.getOrNull(itemPos)
        }

        private fun updateItemCounts() {
            val conversation = this.conversation ?: run {
                itemCounts.clear()
                return
            }
            val participantsSize = conversation.participants.size
            itemCounts[ITEM_INDEX_TOP_SPACE] = if (showButtonSpace) 1 else 0
            itemCounts[ITEM_INDEX_HEADER] = 1
            itemCounts[ITEM_INDEX_ITEM] = participantsSize
            when (conversation.conversation_type) {
                ConversationType.GROUP -> {
                    if (participantsSize < defaultFeatures.getDirectMessageMaxParticipants(conversation.conversation_extras_type)) {
                        itemCounts[ITEM_INDEX_ADD_USER] = 1
                    } else {
                        itemCounts[ITEM_INDEX_ADD_USER] = 0
                    }
                }
                else -> {
                    itemCounts[ITEM_INDEX_ADD_USER] = 0
                }
            }

            itemCounts[ITEM_INDEX_SPACE] = 1
        }

        interface Listener {
            fun onUserClick(position: Int) {}
            fun onAddUserClick(position: Int) {}
            fun onDisableNotificationChanged(disabled: Boolean) {}

        }

        companion object {
            internal const val ITEM_INDEX_TOP_SPACE = 0
            internal const val ITEM_INDEX_HEADER = 1
            internal const val ITEM_INDEX_ITEM = 2
            internal const val ITEM_INDEX_ADD_USER = 3
            internal const val ITEM_INDEX_SPACE = 4

            internal const val VIEW_TYPE_TOP_SPACE = 0
            internal const val VIEW_TYPE_HEADER = 1
            internal const val VIEW_TYPE_USER = 2
            internal const val VIEW_TYPE_ADD_USER = 3
            internal const val VIEW_TYPE_BOTTOM_SPACE = 4

        }

    }

    internal class SpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    internal class AddUserViewHolder(itemView: View, adapter: ConversationInfoAdapter) : RecyclerView.ViewHolder(itemView) {

        private val itemContent = itemView.findViewById<View>(R.id.itemContent)

        init {
            itemContent.setOnClickListener {
                adapter.listener?.onAddUserClick(layoutPosition)
            }
        }

    }

    internal class UserViewHolder(
            itemView: View,
            adapter: ConversationInfoAdapter
    ) : SimpleUserViewHolder<ConversationInfoAdapter>(itemView, adapter) {

        private val headerIcon = itemView.findViewById<View>(R.id.headerIcon)
        private val itemContent = itemView.findViewById<View>(R.id.itemContent)

        init {
            itemContent.setOnClickListener {
                adapter.listener?.onUserClick(layoutPosition)
            }
        }

        fun display(user: ParcelableUser, displayHeaderIcon: Boolean) {
            super.displayUser(user)
            headerIcon.visibility = if (displayHeaderIcon) View.VISIBLE else View.INVISIBLE
        }

    }

    internal class HeaderViewHolder(itemView: View, adapter: ConversationInfoAdapter) : RecyclerView.ViewHolder(itemView) {

        private val muteSwitch = itemView.muteNotifications

        private val listener = CompoundButton.OnCheckedChangeListener { _, checked ->
            adapter.listener?.onDisableNotificationChanged(checked)
        }

        fun display(conversation: ParcelableMessageConversation) {
            muteSwitch.setOnCheckedChangeListener(null)
            muteSwitch.isChecked = conversation.notificationDisabled
            muteSwitch.setOnCheckedChangeListener(listener)
        }

        companion object {
            const val layoutResource = R.layout.header_message_conversation_info
        }

    }

    internal class LayoutManager(
            context: Context
    ) : FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {

        override fun getDecoratedMeasuredHeight(child: View): Int {
            if (getItemViewType(child) == VIEW_TYPE_BOTTOM_SPACE) {
                val height = calculateSpaceItemHeight(child, VIEW_TYPE_BOTTOM_SPACE, VIEW_TYPE_HEADER)
                if (height >= 0) {
                    return height
                }
            }
            return super.getDecoratedMeasuredHeight(child)
        }

    }

    class EditInfoDialogFragment : BaseDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val actions = arrayOf(Action(getString(R.string.action_edit_conversation_name), "name"),
                    Action(getString(R.string.action_edit_conversation_avatar), "avatar"))
            val builder = AlertDialog.Builder(context!!)
            builder.setItems(actions.mapToArray(Action::title)) { _, which ->
                val action = actions[which]
                (parentFragment as MessageConversationInfoFragment).openEditAction(action.type)
            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

        data class Action(val title: String, val type: String)

    }

    class EditNameDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context!!)
            builder.setView(R.layout.dialog_edit_conversation_name)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                val editName = (dialog as Dialog).findViewById<EditText>(R.id.editName)
                (parentFragment as MessageConversationInfoFragment).performSetConversationName(editName.text.toString())
            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }
    }


    class DestroyConversationConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage(R.string.message_destroy_conversation_confirm)
            builder.setPositiveButton(R.string.action_leave_conversation) { _, _ ->
                (parentFragment as MessageConversationInfoFragment).performDestroyConversation()
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }

    class ClearMessagesConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context!!)
            builder.setMessage(R.string.message_clear_messages_confirm)
            builder.setPositiveButton(R.string.action_clear_messages) { _, _ ->
                (parentFragment as MessageConversationInfoFragment).performClearMessages()
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }

    internal class ConversationInfoDecoration(
            val adapter: ConversationInfoAdapter,
            val typeSpacing: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildLayoutPosition(view)
            if (position < 0) return
            val itemCounts = adapter.itemCounts
            val countIndex = itemCounts.getItemCountIndex(position)
            when (countIndex) {
                ConversationInfoAdapter.ITEM_INDEX_TOP_SPACE,
                ConversationInfoAdapter.ITEM_INDEX_SPACE,
                ConversationInfoAdapter.ITEM_INDEX_ADD_USER -> {
                    outRect.setEmpty()
                }
                else -> {
                    // Previous item is space or first item
                    if (position == 0 || itemCounts.getItemCountIndex(position - 1)
                            == ConversationInfoAdapter.ITEM_INDEX_TOP_SPACE) {
                        outRect.setEmpty()
                    } else if (itemCounts.getItemStartPosition(countIndex) == position) {
                        outRect.set(0, typeSpacing, 0, 0)
                    } else {
                        outRect.setEmpty()
                    }
                }
            }
        }
    }

    companion object {
        const val RESULT_CLOSE = 101
        const val REQUEST_CONVERSATION_ADD_USER = 101
        const val REQUEST_PICK_MEDIA = 102
        const val RESULT_CODE_REMOVE_CONVERSATION_AVATAR = 10
    }

}
