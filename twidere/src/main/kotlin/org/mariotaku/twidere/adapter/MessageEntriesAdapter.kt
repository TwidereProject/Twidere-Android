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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IContentCardAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.model.StringLongPair
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries
import org.mariotaku.twidere.util.ReadStateManager.OnReadStateChangeListener
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.MessageEntryViewHolder

class MessageEntriesAdapter(context: Context) : LoadMoreSupportAdapter<ViewHolder>(context), Constants, IContentCardAdapter, OnClickListener, OnReadStateChangeListener {

    private val inflater: LayoutInflater
    override val textSize: Float
    override val profileImageStyle: Int
    private val mMediaPreviewStyle: Int
    override val profileImageEnabled: Boolean
    override val isShowAbsoluteTime: Boolean

    private val mReadStateChangeListener: OnSharedPreferenceChangeListener
    private var mShowAccountsColor: Boolean = false
    private var mCursor: Cursor? = null
    var listener: MessageEntriesAdapterListener? = null
    private var mPositionPairs: Array<StringLongPair>? = null

    init {
        inflater = LayoutInflater.from(context)
        profileImageStyle = Utils.getProfileImageStyle(preferences.getString(SharedPreferenceConstants.KEY_PROFILE_IMAGE_STYLE, null))
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(SharedPreferenceConstants.KEY_MEDIA_PREVIEW_STYLE, null))
        profileImageEnabled = preferences.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE, true)
        textSize = preferences.getInt(SharedPreferenceConstants.KEY_TEXT_SIZE, context.resources.getInteger(R.integer.default_text_size)).toFloat()
        isShowAbsoluteTime = preferences.getBoolean(SharedPreferenceConstants.KEY_SHOW_ABSOLUTE_TIME, false)
        mReadStateChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key -> updateReadState() }
    }


    fun getEntry(position: Int): DirectMessageEntry? {
        val c = mCursor
        if (c == null || c.isClosed || !c.moveToPosition(position)) return null
        return DirectMessageEntry(c)
    }

    override fun onClick(view: View) {
        //        if (mMultiSelectManager.isActive()) return;
        //        final Object tag = view.getTag();
        //        final int position = tag instanceof Integer ? (Integer) tag : -1;
        //        if (position == -1) return;
        //        switch (view.getId()) {
        //            case R.id.profileImage: {
        //                if (mContext instanceof Activity) {
        //                    final long account_id = getAccountKey(position);
        //                    final long user_id = getConversationId(position);
        //                    final String screen_name = getScreenName(position);
        //                    openUserProfile(mContext, account_id, user_id, screen_name, null);
        //                }
        //                break;
        //            }
        //        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_MESSAGE -> {
                val view = inflater.inflate(R.layout.list_item_message_entry, parent, false)
                return MessageEntryViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.card_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_MESSAGE -> {
                val c = mCursor
                c!!.moveToPosition(position)
                (holder as MessageEntryViewHolder).displayMessage(c, isUnread(c))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START !== 0L && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == messagesCount) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        return ITEM_VIEW_TYPE_MESSAGE
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = messagesCount
        if (position and ILoadMoreSupportAdapter.START !== 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END !== 0L) {
            count++
        }
        return count
    }

    fun onMessageClick(position: Int) {
        if (listener == null) return
        listener!!.onEntryClick(position, getEntry(position)!!)
    }

    override fun onReadStateChanged() {

    }

    fun onUserProfileClick(position: Int) {
        listener!!.onUserClick(position, getEntry(position)!!)
    }

    fun setCursor(cursor: Cursor?) {
        mCursor = cursor
        readStateManager.unregisterOnSharedPreferenceChangeListener(mReadStateChangeListener)
        if (cursor != null) {
            updateReadState()
            readStateManager.registerOnSharedPreferenceChangeListener(mReadStateChangeListener)
        }
        notifyDataSetChanged()
    }


    fun updateReadState() {
        mPositionPairs = readStateManager.getPositionPairs(CustomTabType.DIRECT_MESSAGES)
        notifyDataSetChanged()
    }

    private val messagesCount: Int
        get() {
            val c = mCursor
            if (c == null || c.isClosed) return 0
            return c.count
        }

    private fun isUnread(c: Cursor): Boolean {
        if (mPositionPairs == null) return true
        val accountId = c.getLong(ConversationEntries.IDX_ACCOUNT_KEY)
        val conversationId = c.getLong(ConversationEntries.IDX_CONVERSATION_ID)
        val messageId = c.getLong(ConversationEntries.IDX_MESSAGE_ID)
        val key = "$accountId-$conversationId"
        for (pair in mPositionPairs!!) {
            if (key == pair.key) return messageId > pair.value
        }
        return true
    }

    fun setShowAccountsColor(showAccountsColor: Boolean) {
        if (mShowAccountsColor == showAccountsColor) return
        mShowAccountsColor = showAccountsColor
        notifyDataSetChanged()
    }

    fun shouldShowAccountsColor(): Boolean {
        return mShowAccountsColor
    }

    interface MessageEntriesAdapterListener {
        fun onEntryClick(position: Int, entry: DirectMessageEntry)

        fun onUserClick(position: Int, entry: DirectMessageEntry)
    }

    class DirectMessageEntry internal constructor(cursor: Cursor) {

        val account_key: UserKey
        val conversation_id: String
        val screen_name: String
        val name: String

        init {
            account_key = UserKey.valueOf(cursor.getString(ConversationEntries.IDX_ACCOUNT_KEY))!!
            conversation_id = cursor.getString(ConversationEntries.IDX_CONVERSATION_ID)
            screen_name = cursor.getString(ConversationEntries.IDX_SCREEN_NAME)
            name = cursor.getString(ConversationEntries.IDX_NAME)
        }

    }

    companion object {

        val ITEM_VIEW_TYPE_MESSAGE = 0
        val ITEM_VIEW_TYPE_LOAD_INDICATOR = 1
    }

}
