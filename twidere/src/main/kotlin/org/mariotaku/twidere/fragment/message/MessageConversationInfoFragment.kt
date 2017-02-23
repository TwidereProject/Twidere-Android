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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import kotlinx.android.synthetic.main.activity_home_content.view.*
import kotlinx.android.synthetic.main.fragment_messages_conversation_info.*
import kotlinx.android.synthetic.main.header_message_conversation_info.view.*
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.*
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.useCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_CONVERSATION_ID
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.displayAvatarTo
import org.mariotaku.twidere.extension.model.getSubtitle
import org.mariotaku.twidere.extension.model.getTitle
import org.mariotaku.twidere.extension.model.notificationDisabled
import org.mariotaku.twidere.extension.view.calculateSpaceItemHeight
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.fragment.message.MessageConversationInfoFragment.ConversationInfoAdapter.Companion.VIEW_TYPE_HEADER
import org.mariotaku.twidere.fragment.message.MessageConversationInfoFragment.ConversationInfoAdapter.Companion.VIEW_TYPE_SPACE
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.twitter.message.DestroyConversationTask
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.view.holder.SimpleUserViewHolder
import java.lang.ref.WeakReference

/**
 * Created by mariotaku on 2017/2/15.
 */

class MessageConversationInfoFragment : BaseFragment(), IToolBarSupportFragment,
        LoaderManager.LoaderCallbacks<ParcelableMessageConversation?> {

    private val accountKey: UserKey get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)
    private val conversationId: String get() = arguments.getString(EXTRA_CONVERSATION_ID)

    private lateinit var adapter: ConversationInfoAdapter

    override val controlBarHeight: Int get() = toolbar.measuredHeight
    override var controlBarOffset: Float = 0f

    override val toolbar: Toolbar
        get() = toolbarLayout.toolbar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        val activity = this.activity

        if (activity is AppCompatActivity) {
            activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        val theme = Chameleon.getOverrideTheme(context, activity)

        adapter = ConversationInfoAdapter(context)
        adapter.listener = object : ConversationInfoAdapter.Listener {
            override fun onUserClick(position: Int) {
                val user = adapter.getUser(position) ?: return
                startActivity(IntentUtils.userProfile(user))
            }

        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LayoutManager(context)


        val profileImageStyle = preferences[profileImageStyleKey]
        appBarIcon.style = profileImageStyle
        conversationAvatar.style = profileImageStyle

        val avatarBackground = ChameleonUtils.getColorDependent(theme.colorToolbar)
        appBarIcon.setBackgroundColor(avatarBackground)
        appBarTitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        appBarSubtitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        conversationAvatar.setBackgroundColor(avatarBackground)
        conversationTitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        conversationSubtitle.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        loaderManager.initLoader(0, null, this)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation_info, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_messages_conversation_info, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.leave_conversation -> {
                val df = DestroyConversationConfirmDialogFragment()
                df.show(childFragmentManager, "destroy_conversation_confirm")
                return true
            }
            R.id.delete_messages -> {

            }
        }
        return false
    }

    override fun setupWindow(activity: FragmentActivity): Boolean {
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ParcelableMessageConversation?> {
        return ConversationInfoLoader(context, accountKey, conversationId)
    }

    override fun onLoaderReset(loader: Loader<ParcelableMessageConversation?>?) {
    }

    override fun onLoadFinished(loader: Loader<ParcelableMessageConversation?>?, data: ParcelableMessageConversation?) {
        if (data == null) {
            activity?.finish()
            return
        }
        adapter.conversation = data

        val name = data.getTitle(context, userColorNameManager, preferences[nameFirstKey]).first
        val summary = data.getSubtitle(context)

        data.displayAvatarTo(mediaLoader, conversationAvatar)
        data.displayAvatarTo(mediaLoader, appBarIcon)
        appBarTitle.text = name
        appBarSubtitle.text = summary
        conversationTitle.text = name
        conversationSubtitle.text = summary
    }

    private fun performDestroyConversation() {
        ProgressDialogFragment.show(childFragmentManager, "leave_conversation_progress")
        val weakThis = WeakReference(this)
        val task = DestroyConversationTask(context, accountKey, conversationId)
        task.callback = callback@ { succeed ->
            val f = weakThis.get() ?: return@callback
            f.executeAfterFragmentResumed { fragment ->
                val df = fragment.childFragmentManager.findFragmentByTag("leave_conversation_progress") as? DialogFragment
                df?.dismiss()
                if (succeed) {
                    activity?.setResult(RESULT_CLOSE)
                    activity?.finish()
                }
            }
        }
        TaskStarter.execute(task)
    }

    class ConversationInfoLoader(
            context: Context,
            val accountKey: UserKey,
            val conversationId: String) : AsyncTaskLoader<ParcelableMessageConversation?>(context) {
        override fun loadInBackground(): ParcelableMessageConversation? {
            val where = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val whereArgs = arrayOf(accountKey.toString(), conversationId)
            context.contentResolver.query(Conversations.CONTENT_URI, Conversations.COLUMNS, where,
                    whereArgs, null).useCursor { cur ->
                if (cur.moveToFirst()) {
                    return ParcelableMessageConversationCursorIndices.fromCursor(cur)
                }
            }
            return null
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }

    class ConversationInfoAdapter(context: Context) : BaseRecyclerViewAdapter<RecyclerView.ViewHolder>(context),
            IItemCountsAdapter {
        private val inflater = LayoutInflater.from(context)
        override val itemCounts: ItemCounts = ItemCounts(4)

        var listener: Listener? = null

        var conversation: ParcelableMessageConversation? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        init {
            setHasStableIds(true)
        }

        override fun getItemCount(): Int {
            val conversation = this.conversation ?: return 0
            itemCounts[ITEM_INDEX_HEADER] = 1
            itemCounts[ITEM_INDEX_ITEM] = conversation.participants.size
            itemCounts[ITEM_INDEX_ADD_USER] = 1
            itemCounts[ITEM_INDEX_SPACE] = 1
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
                VIEW_TYPE_HEADER -> {
                    val view = inflater.inflate(HeaderViewHolder.layoutResource, parent, false)
                    return HeaderViewHolder(view)
                }
                VIEW_TYPE_USER -> {
                    val view = inflater.inflate(R.layout.list_item_conversation_info_user, parent, false)
                    return UserViewHolder(view, this)
                }
                VIEW_TYPE_ADD_USER -> {
                    val view = inflater.inflate(R.layout.list_item_conversation_info_add_user, parent, false)
                    return AddUserViewHolder(view, this)
                }
                VIEW_TYPE_SPACE -> {
                    val view = inflater.inflate(R.layout.list_item_conversation_info_space, parent, false)
                    return SpaceViewHolder(view)
                }
            }
            throw UnsupportedOperationException()
        }

        override fun getItemViewType(position: Int): Int {
            when (itemCounts.getItemCountIndex(position)) {
                ITEM_INDEX_HEADER -> return VIEW_TYPE_HEADER
                ITEM_INDEX_ITEM -> return VIEW_TYPE_USER
                ITEM_INDEX_ADD_USER -> return VIEW_TYPE_ADD_USER
                ITEM_INDEX_SPACE -> return VIEW_TYPE_SPACE
            }
            throw UnsupportedOperationException()
        }

        override fun getItemId(position: Int): Long {
            when (itemCounts.getItemCountIndex(position)) {
                ITEM_INDEX_ITEM -> {
                    val user = getUser(position)!!
                    return user.hashCode().toLong()
                }
                else -> {
                    return Integer.MAX_VALUE.toLong() + getItemViewType(position)
                }
            }
        }

        fun getUser(position: Int): ParcelableUser? {
            val itemPos = position - itemCounts.getItemStartPosition(ITEM_INDEX_ITEM)
            return conversation?.participants?.getOrNull(itemPos)
        }

        interface Listener {
            fun onUserClick(position: Int) {}
            fun onAddUserClick(position: Int) {}
        }

        companion object {
            private const val ITEM_INDEX_HEADER = 0
            private const val ITEM_INDEX_ITEM = 1
            private const val ITEM_INDEX_ADD_USER = 2
            private const val ITEM_INDEX_SPACE = 3

            internal const val VIEW_TYPE_HEADER = 1
            internal const val VIEW_TYPE_USER = 2
            internal const val VIEW_TYPE_ADD_USER = 3
            internal const val VIEW_TYPE_SPACE = 4
        }


    }

    internal class SpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    internal class AddUserViewHolder(itemView: View, adapter: ConversationInfoAdapter) : RecyclerView.ViewHolder(itemView) {
        private val itemContent = itemView.findViewById(R.id.itemContent)

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
        private val headerIcon = itemView.findViewById(R.id.headerIcon)
        private val itemContent = itemView.findViewById(R.id.itemContent)

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

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val muteSwitch = itemView.muteNotifications

        fun display(conversation: ParcelableMessageConversation) {
            muteSwitch.isChecked = conversation.notificationDisabled
        }

        companion object {
            const val layoutResource = R.layout.header_message_conversation_info

        }
    }

    internal class LayoutManager(
            context: Context
    ) : FixedLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {

        override fun getDecoratedMeasuredHeight(child: View): Int {
            if (getItemViewType(child) == VIEW_TYPE_SPACE) {
                return calculateSpaceItemHeight(child, VIEW_TYPE_SPACE, VIEW_TYPE_HEADER)
            }
            return super.getDecoratedMeasuredHeight(child)
        }

    }

    class DestroyConversationConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.message_destroy_conversation_confirm)
            builder.setPositiveButton(R.string.action_leave_conversation) { dialog, which ->
                (parentFragment as MessageConversationInfoFragment).performDestroyConversation()
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }

    }

    companion object {
        const val RESULT_CLOSE = 101
    }
}
