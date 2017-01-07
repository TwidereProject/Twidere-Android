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
import android.database.Cursor
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.findPositionByItemId
import org.mariotaku.ktextension.rangeOfSize
import org.mariotaku.ktextension.safeMoveToPosition
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter.Companion.ITEM_VIEW_TYPE_GAP
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_LINK_HIGHLIGHT_OPTION
import org.mariotaku.twidere.constant.hideCardActionsKey
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.constant.mediaPreviewStyleKey
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.ObjectId
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableStatusCursorIndices
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.MediaLoadingHandler
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.CardMediaContainer
import org.mariotaku.twidere.view.holder.EmptyViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.util.*

/**
 * Created by mariotaku on 15/10/26.
 */
abstract class ParcelableStatusesAdapter(
        context: Context
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context), IStatusesAdapter<List<ParcelableStatus>>,
        IItemCountsAdapter {

    protected val inflater: LayoutInflater = LayoutInflater.from(context)

    override final val mediaLoadingHandler: MediaLoadingHandler
    final override val twidereLinkify: TwidereLinkify
    @CardMediaContainer.PreviewStyle
    final override val mediaPreviewStyle: Int = preferences[mediaPreviewStyleKey]
    final override val nameFirst: Boolean = preferences[nameFirstKey]
    final override val useStarsForLikes: Boolean = preferences[iWantMyStarsBackKey]
    @TwidereLinkify.HighlightStyle
    final override val linkHighlightingStyle: Int
    final override val mediaPreviewEnabled: Boolean
    final override val sensitiveContentEnabled: Boolean
    private val showCardActions: Boolean = !preferences[hideCardActionsKey]

    private val gapLoadingIds: MutableSet<ObjectId> = HashSet()

    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = statusClickListener

    val hasPinnedStatuses: Boolean
        get() = pinnedStatuses != null

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

    var pinnedStatuses: List<ParcelableStatus>? = null
        set(value) {
            field = value
            value?.forEach { it.is_pinned_status = true }
            notifyDataSetChanged()
        }

    private var data: List<ParcelableStatus>? = null
    private var showingActionCardId = RecyclerView.NO_ID
    private var lastItemFiltered: Boolean = false

    override val itemCounts: IntArray = IntArray(4)

    protected abstract val progressViewIds: IntArray

    init {
        mediaLoadingHandler = MediaLoadingHandler(*progressViewIds)
        linkHighlightingStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null))
        mediaPreviewEnabled = Utils.isMediaPreviewEnabled(context, preferences)
        sensitiveContentEnabled = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false)
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
        when (getItemCountIndex(position)) {
            1 -> {
                return pinnedStatuses!![position - getItemStartPosition(1)]
            }
            2 -> {
                return data!![position - getItemStartPosition(2)]
            }
        }
        return null
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
        return getFieldValue(position, { cursor, indices ->
            val accountKey = UserKey.valueOf(cursor.getString(indices.account_key))
            val id = cursor.getString(indices.id)
            return@getFieldValue ParcelableStatus.calculateHashCode(accountKey, id).toLong()
        }, { status ->
            return@getFieldValue status.hashCode().toLong()
        }, -1L)
    }

    override fun getStatusId(position: Int): String? {
        return getFieldValue<String?>(position, { cursor, indices ->
            return@getFieldValue cursor.getString(indices.id)
        }, { status ->
            return@getFieldValue status.id
        }, null)
    }

    fun getStatusSortId(position: Int): Long {
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue cursor.getLong(indices.sort_id)
        }, { status ->
            return@getFieldValue status.sort_id
        }, -1L)
    }

    override fun getStatusTimestamp(position: Int): Long {
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue cursor.getLong(indices.timestamp)
        }, { status ->
            return@getFieldValue status.timestamp
        }, -1L)
    }

    override fun getStatusPositionKey(position: Int): Long {
        return getFieldValue(position, { cursor, indices ->
            val positionKey = cursor.getLong(indices.position_key)
            if (positionKey > 0) return@getFieldValue positionKey
            return@getFieldValue cursor.getLong(indices.timestamp)
        }, { status ->
            val positionKey = status.position_key
            if (positionKey > 0) return@getFieldValue positionKey
            return@getFieldValue status.timestamp
        }, -1L)
    }

    override fun getAccountKey(position: Int): UserKey? {
        val dataPosition = position - getItemStartPosition(2)
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
        gapLoadingIds.clear()
        notifyDataSetChanged()
        return changed
    }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                return onCreateStatusViewHolder(parent) as RecyclerView.ViewHolder
            }
            ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(GapViewHolder.layoutResource, parent, false)
                return GapViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.card_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
            ITEM_VIEW_TYPE_EMPTY -> {
                return EmptyViewHolder(Space(context))
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemCountIndex(position)) {
            1 -> {
                val status = pinnedStatuses!![position - getItemStartPosition(1)]
                (holder as IStatusViewHolder).displayStatus(status, isShowInReplyTo)
            }
            2 -> {
                val status = data!![position - getItemStartPosition(2)]
                when (holder.itemViewType) {
                    ITEM_VIEW_TYPE_STATUS -> {
                        (holder as IStatusViewHolder).displayStatus(status, isShowInReplyTo)
                    }
                    ITEM_VIEW_TYPE_GAP -> {
                        val loading = gapLoadingIds.find { it.accountKey == status.account_key && it.id == status.id } != null
                        (holder as GapViewHolder).display(loading)
                    }
                }
            }
        }

    }


    protected abstract fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder

    override fun addGapLoadingId(id: ObjectId) {
        gapLoadingIds.add(id)
    }

    override fun removeGapLoadingId(id: ObjectId) {
        gapLoadingIds.remove(id)
    }

    fun getData(): List<ParcelableStatus>? {
        return data
    }

    fun isStatus(position: Int): Boolean {
        return position < statusCount
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START != 0L && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        when (getItemCountIndex(position)) {
            0, 3 -> {
                return ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
            1 -> {
                return ITEM_VIEW_TYPE_STATUS
            }
            2 -> {
                if (isGapItem(position)) {
                    return ITEM_VIEW_TYPE_GAP
                } else if (isFiltered(position)) {
                    return ITEM_VIEW_TYPE_EMPTY
                } else {
                    return ITEM_VIEW_TYPE_STATUS
                }
            }
        }
        throw AssertionError()
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        itemCounts[0] = if (position and ILoadMoreSupportAdapter.START != 0L) 1 else 0
        itemCounts[1] = pinnedStatuses?.size ?: 0
        itemCounts[2] = statusCount
        itemCounts[3] = if (position and ILoadMoreSupportAdapter.END != 0L) 1 else 0
        return itemCounts.sum()
    }


    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        for (i in 0 until statusCount) {
            if (accountKey == getAccountKey(i) && statusId == getStatusId(i)) {
                return getStatus(i)
            }
        }
        return null
    }


    val statusStartIndex: Int
        get() = getItemStartPosition(2)

    private inline fun <T> getFieldValue(
            position: Int,
            readCursorValueAction: (cursor: Cursor, indices: ParcelableStatusCursorIndices) -> T,
            readStatusValueAction: (status: ParcelableStatus) -> T,
            defValue: T
    ): T {
        val dataPosition = position - getItemStartPosition(2)
        if (dataPosition < 0 || dataPosition >= rawStatusCount) return defValue
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return defValue
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            return readCursorValueAction(cursor, indices)
        }
        val status = data!![dataPosition]
        return readStatusValueAction(status)
    }

    private fun isFiltered(position: Int): Boolean {
        if (data is ObjectCursor) return false
        return getStatus(position)!!.is_filtered
    }

    companion object {
        const val ITEM_VIEW_TYPE_STATUS = 2
        const val ITEM_VIEW_TYPE_EMPTY = 3
    }

    fun findPositionByPositionKey(positionKey: Long): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        return rangeOfSize(statusStartIndex, statusCount - 1).indexOfFirst { positionKey > 0 && getStatusPositionKey(it) <= positionKey }
    }

    fun findPositionBySortId(sortId: Long): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        return rangeOfSize(statusStartIndex, statusCount - 1).indexOfFirst { sortId > 0 && getStatusSortId(it) <= sortId }
    }


}
