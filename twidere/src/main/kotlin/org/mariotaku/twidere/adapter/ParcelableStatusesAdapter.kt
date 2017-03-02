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
import com.bumptech.glide.RequestManager
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
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.MediaLoadingHandler
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.EmptyViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.util.*

/**
 * Created by mariotaku on 15/10/26.
 */
abstract class ParcelableStatusesAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager), IStatusesAdapter<List<ParcelableStatus>>,
        IItemCountsAdapter {

    protected val inflater: LayoutInflater = LayoutInflater.from(context)

    override final val mediaLoadingHandler: MediaLoadingHandler
    final override val twidereLinkify: TwidereLinkify
    @PreviewStyle
    final override val mediaPreviewStyle: Int = preferences[mediaPreviewStyleKey]
    final override val nameFirst: Boolean = preferences[nameFirstKey]
    final override val useStarsForLikes: Boolean = preferences[iWantMyStarsBackKey]
    @TwidereLinkify.HighlightStyle
    final override val linkHighlightingStyle: Int = preferences[linkHighlightOptionKey]
    final override val lightFont: Boolean = preferences[lightFontKey]
    final override val mediaPreviewEnabled: Boolean = Utils.isMediaPreviewEnabled(context, preferences)
    final override val sensitiveContentEnabled: Boolean = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false)
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
            updateItemCount()
            notifyDataSetChanged()
        }

    private var data: List<ParcelableStatus>? = null
    private var displayPositions: IntArray? = null
    private var displayDataCount: Int = 0


    private var showingActionCardId = RecyclerView.NO_ID

    override val itemCounts = ItemCounts(4)

    protected abstract val progressViewIds: IntArray

    val statusStartIndex: Int
        get() = getItemStartPosition(ITEM_INDEX_STATUS)

    override var loadMoreIndicatorPosition: Long
        get() = super.loadMoreIndicatorPosition
        set(value) {
            super.loadMoreIndicatorPosition = value
            updateItemCount()
        }

    override var loadMoreSupportedPosition: Long
        get() = super.loadMoreSupportedPosition
        set(value) {
            super.loadMoreSupportedPosition = value
            updateItemCount()
        }

    init {
        mediaLoadingHandler = MediaLoadingHandler(*progressViewIds)
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
        return getStatus(position)!!.is_gap
    }

    override fun getStatus(position: Int): ParcelableStatus? {
        return getStatus(position, getItemCountIndex(position))
    }

    override val statusCount: Int
        get() = displayDataCount

    override val rawStatusCount: Int
        get() = data?.size ?: 0

    override fun setData(data: List<ParcelableStatus>?): Boolean {
        var changed = true
        if (data == null) {
            displayPositions = null
            displayDataCount = 0
        } else if (data is ObjectCursor) {
            displayPositions = null
            displayDataCount = data.size
        } else {
            var filteredCount = 0
            displayPositions = IntArray(data.size).apply {
                data.forEachIndexed { i, item ->
                    if (!item.is_gap && item.is_filtered) {
                        filteredCount++
                    } else {
                        this[i - filteredCount] = i
                    }
                }
            }
            displayDataCount = data.size - filteredCount
            changed = data != data
        }
        this.data = data
        gapLoadingIds.clear()
        updateItemCount()
        notifyDataSetChanged()
        return changed
    }

    fun getData(): List<ParcelableStatus>? {
        return data
    }

    override fun getItemId(position: Int): Long {
        val countIndex = getItemCountIndex(position)
        when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                val status = pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
                val mask = ITEM_INDEX_PINNED_STATUS.toLong() shl 32
                return mask + status.hashCode()
            }
            ITEM_INDEX_STATUS -> return getFieldValue(position, { cursor, indices ->
                val accountKey = UserKey.valueOf(cursor.getString(indices.account_key))
                val id = cursor.getString(indices.id)
                return@getFieldValue ParcelableStatus.calculateHashCode(accountKey, id).toLong()
            }, { status ->
                return@getFieldValue status.hashCode().toLong()
            }, -1L)
            else -> return (countIndex.toLong() shl 32) + position
        }
    }

    override fun getStatusId(position: Int): String? {
        val def: String? = null
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue cursor.getString(indices.id)
        }, { status ->
            return@getFieldValue status.id
        }, def)
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
        val def: UserKey? = null
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue UserKey.valueOf(cursor.getString(indices.account_key))
        }, { status ->
            return@getFieldValue status.account_key
        }, def)
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
            VIEW_TYPE_STATUS -> {
                return onCreateStatusViewHolder(parent) as RecyclerView.ViewHolder
            }
            ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(GapViewHolder.layoutResource, parent, false)
                return GapViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
            VIEW_TYPE_EMPTY -> {
                return EmptyViewHolder(Space(context))
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_STATUS -> {
                val countIdx = getItemCountIndex(position)
                val status = getStatus(position, countIdx)!!
                (holder as IStatusViewHolder).displayStatus(status, displayInReplyTo = isShowInReplyTo,
                        displayPinned = countIdx == ITEM_INDEX_PINNED_STATUS)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val status = getStatus(position)!!
                val loading = gapLoadingIds.any { it.accountKey == status.account_key && it.id == status.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START != 0L && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        when (getItemCountIndex(position)) {
            ITEM_INDEX_LOAD_START_INDICATOR, ITEM_INDEX_LOAD_END_INDICATOR -> {
                return ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
            ITEM_INDEX_PINNED_STATUS -> {
                return VIEW_TYPE_STATUS
            }
            ITEM_INDEX_STATUS -> {
                if (isGapItem(position)) {
                    return ITEM_VIEW_TYPE_GAP
                } else {
                    return VIEW_TYPE_STATUS
                }
            }
        }
        throw AssertionError()
    }

    protected abstract fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder

    override fun addGapLoadingId(id: ObjectId) {
        gapLoadingIds.add(id)
    }

    override fun removeGapLoadingId(id: ObjectId) {
        gapLoadingIds.remove(id)
    }

    fun isStatus(position: Int): Boolean {
        return position < statusCount
    }

    override fun getItemCount(): Int {
        return itemCounts.itemCount
    }

    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        for (i in 0 until statusCount) {
            if (accountKey == getAccountKey(i) && statusId == getStatusId(i)) {
                return getStatus(i)
            }
        }
        return null
    }

    fun findPositionByPositionKey(positionKey: Long): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (positionKey <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, statusCount)
        if (range.isEmpty()) return RecyclerView.NO_POSITION
        if (positionKey < getStatusPositionKey(range.last)) {
            return range.last
        }
        return range.indexOfFirst { positionKey >= getStatusPositionKey(it) }
    }

    fun findPositionBySortId(sortId: Long): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (sortId <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, statusCount)
        if (range.isEmpty()) return RecyclerView.NO_POSITION
        if (sortId < getStatusSortId(range.last)) {
            return range.last
        }
        return range.indexOfFirst { sortId >= getStatusSortId(it) }
    }

    private inline fun <T> getFieldValue(
            position: Int,
            readCursorValueAction: (cursor: Cursor, indices: ParcelableStatusCursorIndices) -> T,
            readStatusValueAction: (status: ParcelableStatus) -> T,
            defValue: T
    ): T {
        if (data is ObjectCursor) {
            val dataPosition = position - getItemStartPosition(ITEM_INDEX_STATUS)
            if (dataPosition < 0 || dataPosition >= rawStatusCount) return defValue
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return defValue
            val indices = (data as ObjectCursor).indices as ParcelableStatusCursorIndices
            return readCursorValueAction(cursor, indices)
        }
        return readStatusValueAction(getStatus(position)!!)
    }

    private fun getStatus(position: Int, countIndex: Int): ParcelableStatus? {
        when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                return pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
            }
            ITEM_INDEX_STATUS -> {
                val data = this.data!!
                val dataPosition = position - getItemStartPosition(ITEM_INDEX_STATUS)
                val positions = displayPositions
                if (positions != null) {
                    return data[positions[dataPosition]]
                } else {
                    return data[dataPosition]
                }
            }
        }
        return null
    }

    private fun updateItemCount() {
        val position = loadMoreIndicatorPosition
        itemCounts[ITEM_INDEX_LOAD_START_INDICATOR] = if (position and ILoadMoreSupportAdapter.START != 0L) 1 else 0
        itemCounts[ITEM_INDEX_PINNED_STATUS] = pinnedStatuses?.size ?: 0
        itemCounts[ITEM_INDEX_STATUS] = statusCount
        itemCounts[ITEM_INDEX_LOAD_END_INDICATOR] = if (position and ILoadMoreSupportAdapter.END != 0L) 1 else 0
    }

    companion object {
        const val VIEW_TYPE_STATUS = 2
        const val VIEW_TYPE_EMPTY = 3

        const val ITEM_INDEX_LOAD_START_INDICATOR = 0
        const val ITEM_INDEX_PINNED_STATUS = 1
        const val ITEM_INDEX_STATUS = 2
        const val ITEM_INDEX_LOAD_END_INDICATOR = 3
    }


}
