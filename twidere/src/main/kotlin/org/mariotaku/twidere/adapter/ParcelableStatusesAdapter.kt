/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mariotaku.ktextension.safeMoveToPosition
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableStatusCursorIndices
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.MediaLoadingHandler
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.CardMediaContainer
import org.mariotaku.twidere.view.ShapedImageView
import org.mariotaku.twidere.view.holder.EmptyViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 15/10/26.
 */
abstract class ParcelableStatusesAdapter(
        context: Context
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context), Constants, IStatusesAdapter<List<ParcelableStatus>> {

    protected val inflater: LayoutInflater

    override val mediaLoadingHandler: MediaLoadingHandler
    override val twidereLinkify: TwidereLinkify
    override val textSize: Float
    @ShapedImageView.ShapeStyle
    override val profileImageStyle: Int
    @CardMediaContainer.PreviewStyle
    override val mediaPreviewStyle: Int
    @TwidereLinkify.HighlightStyle
    override val linkHighlightingStyle: Int
    override val nameFirst: Boolean
    override val mediaPreviewEnabled: Boolean
    override val profileImageEnabled: Boolean
    override val sensitiveContentEnabled: Boolean
    override val useStarsForLikes: Boolean
    override val isShowAbsoluteTime: Boolean
    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null

    private val showCardActions: Boolean

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = statusClickListener

    override var showAccountsColor: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    var isShowInReplyTo: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    private var data: List<ParcelableStatus>? = null
    private var showingActionCardId = RecyclerView.NO_ID
    private var lastItemFiltered: Boolean = false

    init {
        inflater = LayoutInflater.from(context)
        mediaLoadingHandler = MediaLoadingHandler(*progressViewIds)
        textSize = preferences.getInt(KEY_TEXT_SIZE, context.resources.getInteger(R.integer.default_text_size)).toFloat()
        profileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null))
        mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null))
        linkHighlightingStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null))
        nameFirst = preferences.getBoolean(KEY_NAME_FIRST, true)
        profileImageEnabled = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)
        mediaPreviewEnabled = Utils.isMediaPreviewEnabled(context, preferences)
        sensitiveContentEnabled = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false)
        showCardActions = !preferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false)
        useStarsForLikes = preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)
        isShowAbsoluteTime = preferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME, false)
        val handler = StatusAdapterLinkClickHandler<List<ParcelableStatus>>(context, preferences)
        twidereLinkify = TwidereLinkify(handler)
        handler.setAdapter(this)
        isShowInReplyTo = true
        setHasStableIds(true)
    }

    override fun isGapItem(position: Int): Boolean {
        val dataPosition = position - statusStartIndex
        val statusCount = statusCount
        if (dataPosition < 0 || dataPosition >= statusCount) return false
        // Don't show gap if it's last item
        if (dataPosition == statusCount - 1) return false
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.moveToPosition(dataPosition)) return false
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            return cursor.getShort(indices.is_gap).toInt() == 1
        }
        return data!![dataPosition].is_gap
    }

    override fun getStatus(position: Int): ParcelableStatus? {
        val dataPosition = position - statusStartIndex
        if (dataPosition < 0 || dataPosition >= rawStatusCount) return null
        return data!![dataPosition]
    }

    override val statusCount: Int
        get() {
            if (data == null) return 0
            if (lastItemFiltered) return data!!.size - 1
            return data!!.size
        }

    override val rawStatusCount: Int
        get() = data?.size ?: 0

    override fun getItemId(position: Int): Long {
        val dataPosition = position - statusStartIndex
        if (dataPosition < 0 || dataPosition >= statusCount) return position.toLong()
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return -1
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            val accountKey = UserKey.valueOf(cursor.getString(indices.account_key))
            val id = cursor.getString(indices.id)
            return ParcelableStatus.calculateHashCode(accountKey, id).toLong()
        }
        return data!![dataPosition].hashCode().toLong()
    }

    override fun getStatusId(position: Int): String? {
        val dataPosition = position - statusStartIndex
        if (dataPosition < 0 || dataPosition >= rawStatusCount) return null
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return null
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            return cursor.getString(indices.id)
        }
        return data!![dataPosition].id
    }

    override fun getStatusTimestamp(position: Int): Long {
        val dataPosition = position - statusStartIndex
        if (dataPosition < 0 || dataPosition >= rawStatusCount) return -1
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return -1
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            return cursor.getLong(indices.timestamp)
        }
        return data!![dataPosition].timestamp
    }

    override fun getStatusPositionKey(position: Int): Long {
        val dataPosition = position - statusStartIndex
        if (dataPosition < 0 || dataPosition >= rawStatusCount) return -1
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return -1
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            val positionKey = cursor.getLong(indices.position_key)
            if (positionKey > 0) return positionKey
            return cursor.getLong(indices.timestamp)
        }
        val status = data!![dataPosition]
        val positionKey = status.position_key
        if (positionKey > 0) return positionKey
        return status.timestamp
    }

    override fun getAccountKey(position: Int): UserKey? {
        val dataPosition = position - statusStartIndex
        if (dataPosition < 0 || dataPosition >= rawStatusCount) return null
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return null
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            return UserKey.valueOf(cursor.getString(indices.account_key))
        }
        return data!![dataPosition].account_key
    }

    override fun setData(data: List<ParcelableStatus>?): Boolean {
        var changed = true
        if (data is ObjectCursor || data == null || data.isEmpty()) {
            lastItemFiltered = false
        } else {
            lastItemFiltered = data[data.size - 1].is_filtered
            changed = data != data
        }
        this.data = data
        notifyDataSetChanged()
        return changed
    }

    fun getData(): List<ParcelableStatus>? {
        return data
    }

    protected abstract val progressViewIds: IntArray

    override fun isCardActionsShown(position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return showCardActions
        return showCardActions || showingActionCardId == getItemId(position)
    }

    override fun showCardActions(position: Int) {
        if (showingActionCardId != RecyclerView.NO_ID) {
            val pos = findPositionByItemId(showingActionCardId)
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos)
            }

        }
        showingActionCardId = getItemId(position)
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }


    fun isStatus(position: Int): Boolean {
        return position < statusCount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                return onCreateStatusViewHolder(parent) as RecyclerView.ViewHolder
            }
            IGapSupportedAdapter.ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(R.layout.card_item_gap, parent, false)
                return GapViewHolder(this, view)
            }
            ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.card_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
            ITEM_VIEW_TYPE_EMPTY -> {
                return EmptyViewHolder(Space(context))
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    protected abstract fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                bindStatus(holder as IStatusViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START != 0L && position == 0) {
            return ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == statusCount) {
            return ILoadMoreSupportAdapter.ITEM_VIEW_TYPE_LOAD_INDICATOR
        } else if (isGapItem(position)) {
            return IGapSupportedAdapter.ITEM_VIEW_TYPE_GAP
        }
        if (isFiltered(position)) return ITEM_VIEW_TYPE_EMPTY
        return ITEM_VIEW_TYPE_STATUS
    }


    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = 0
        if (position and ILoadMoreSupportAdapter.START != 0L) {
            count += 1
        }
        count += statusCount
        if (position and ILoadMoreSupportAdapter.END != 0L) {
            count += 1
        }
        return count
    }


    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        var i = 0
        val j = statusCount
        while (i < j) {
            if (accountKey == getAccountKey(i) && statusId == getStatusId(i)) {
                return getStatus(i)
            }
            i++
        }
        return null
    }

    protected fun bindStatus(holder: IStatusViewHolder, position: Int) {
        holder.displayStatus(getStatus(position)!!, isShowInReplyTo)
    }

    val statusStartIndex: Int
        get() {
            val position = loadMoreIndicatorPosition
            var start = 0
            if (position and ILoadMoreSupportAdapter.START != 0L) {
                start += 1
            }
            return start
        }

    private fun isFiltered(position: Int): Boolean {
        if (data is ObjectCursor) return false
        return getStatus(position)!!.is_filtered
    }

    companion object {
        const val ITEM_VIEW_TYPE_STATUS = 2
        const val ITEM_VIEW_TYPE_EMPTY = 3
    }

}
