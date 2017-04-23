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
import android.database.CursorIndexOutOfBoundsException
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
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
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ObjectId
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.view.holder.EmptyViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.TimelineFilterHeaderViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.util.*

/**
 * Created by mariotaku on 15/10/26.
 */
abstract class ParcelableStatusesAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IStatusesAdapter<List<ParcelableStatus>>, IItemCountsAdapter {

    protected val inflater: LayoutInflater = LayoutInflater.from(context)

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

    var timelineFilter: TimelineFilter? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var data: List<ParcelableStatus>? = null
    private var displayPositions: IntArray? = null
    private var displayDataCount: Int = 0


    private var showingActionCardId = RecyclerView.NO_ID

    override val itemCounts = ItemCounts(5)

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
        val handler = StatusAdapterLinkClickHandler<List<ParcelableStatus>>(context, preferences)
        twidereLinkify = TwidereLinkify(handler)
        handler.setAdapter(this)
        isShowInReplyTo = true
        setHasStableIds(true)
    }

    override fun isGapItem(position: Int): Boolean {
        val dataPosition = position - statusStartIndex
        val statusCount = getStatusCount(false)
        if (dataPosition < 0 || dataPosition >= statusCount) return false
        // Don't show gap if it's last item
        if (dataPosition == statusCount - 1) return false
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.moveToPosition(dataPosition)) return false
            val indices = (data as ObjectCursor).indices
            return cursor.safeGetInt(indices[Statuses.IS_GAP]) == 1
        }
        return getStatus(position).is_gap
    }

    override fun getStatus(position: Int, raw: Boolean): ParcelableStatus {
        return getStatus(position, getItemCountIndex(position, raw), raw)
    }

    override fun getStatusCount(raw: Boolean): Int {
        if (raw) return data?.size ?: 0
        return displayDataCount
    }

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
            changed = this.data != data
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
                return (countIndex.toLong() shl 32) or status.hashCode().toLong()
            }
            ITEM_INDEX_STATUS -> return (countIndex.toLong() shl 32) or getFieldValue(position, { cursor, indices ->
                val accountKey = UserKey.valueOf(cursor.getString(indices[Statuses.ACCOUNT_KEY]))
                val id = cursor.getString(indices[Statuses.STATUS_ID])
                return@getFieldValue ParcelableStatus.calculateHashCode(accountKey, id)
            }, { status ->
                return@getFieldValue status.hashCode()
            }, -1).toLong()
            else -> return (countIndex.toLong() shl 32) or position.toLong()
        }
    }

    override fun getStatusId(position: Int, raw: Boolean): String {
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue cursor.getString(indices[Statuses.STATUS_ID])
        }, { status ->
            return@getFieldValue status.id
        }, "")
    }

    fun getStatusSortId(position: Int, raw: Boolean): Long {
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue cursor.safeGetLong(indices[Statuses.SORT_ID])
        }, { status ->
            return@getFieldValue status.sort_id
        }, -1L, raw)
    }

    override fun getStatusTimestamp(position: Int, raw: Boolean): Long {
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue cursor.safeGetLong(indices[Statuses.STATUS_TIMESTAMP])
        }, { status ->
            return@getFieldValue status.timestamp
        }, -1L)
    }

    override fun getStatusPositionKey(position: Int, raw: Boolean): Long {
        return getFieldValue(position, { cursor, indices ->
            val positionKey = cursor.safeGetLong(indices[Statuses.POSITION_KEY])
            if (positionKey > 0) return@getFieldValue positionKey
            return@getFieldValue cursor.safeGetLong(indices[Statuses.STATUS_TIMESTAMP])
        }, { status ->
            val positionKey = status.position_key
            if (positionKey > 0) return@getFieldValue positionKey
            return@getFieldValue status.timestamp
        }, -1L)
    }

    override fun getAccountKey(position: Int, raw: Boolean): UserKey {
        val def: UserKey? = null
        return getFieldValue(position, { cursor, indices ->
            return@getFieldValue UserKey.valueOf(cursor.getString(indices[Statuses.ACCOUNT_KEY]))
        }, { status ->
            return@getFieldValue status.account_key
        }, def, raw)!!
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
            ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(GapViewHolder.layoutResource, parent, false)
                return GapViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
            VIEW_TYPE_STATUS -> {
                return onCreateStatusViewHolder(parent) as RecyclerView.ViewHolder
            }
            VIEW_TYPE_EMPTY -> {
                return EmptyViewHolder(Space(context))
            }
            VIEW_TYPE_FILTER_HEADER -> {
                val view = inflater.inflate(TimelineFilterHeaderViewHolder.layoutResource,
                        parent, false)
                return TimelineFilterHeaderViewHolder(this, view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_STATUS -> {
                val countIdx = getItemCountIndex(position)
                val status = getStatus(position, countIdx)
                (holder as IStatusViewHolder).display(status, displayInReplyTo = isShowInReplyTo,
                        displayPinned = countIdx == ITEM_INDEX_PINNED_STATUS)
            }
            VIEW_TYPE_FILTER_HEADER -> {
                (holder as TimelineFilterHeaderViewHolder).display(timelineFilter!!)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val status = getStatus(position)
                val loading = gapLoadingIds.any { it.accountKey == status.account_key && it.id == status.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && ILoadMoreSupportAdapter.START in loadMoreIndicatorPosition) {
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
            ITEM_INDEX_FILTER_HEADER -> {
                return VIEW_TYPE_FILTER_HEADER
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

    fun isStatus(position: Int, raw: Boolean = false): Boolean {
        return position < getStatusCount(raw)
    }

    override fun getItemCount(): Int {
        return itemCounts.itemCount
    }

    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        for (i in 0 until getStatusCount(true)) {
            if (accountKey == getAccountKey(i, true) && statusId == getStatusId(i, true)) {
                return getStatus(i, true)
            }
        }
        return null
    }

    fun findPositionByPositionKey(positionKey: Long, raw: Boolean = false): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (positionKey <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, getStatusCount(raw))
        if (range.isEmpty() || range.start < 0) return RecyclerView.NO_POSITION
        if (positionKey < getStatusPositionKey(range.last, raw)) {
            return range.last
        }
        return range.indexOfFirst { positionKey >= getStatusPositionKey(it, raw) }
    }

    fun findPositionBySortId(sortId: Long, raw: Boolean = false): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (sortId <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, getStatusCount(raw))
        if (range.isEmpty() || range.start < 0) return RecyclerView.NO_POSITION
        if (sortId < getStatusSortId(range.last, raw)) {
            return range.last
        }
        return range.indexOfFirst { sortId >= getStatusSortId(it, raw) }
    }

    private fun getItemCountIndex(position: Int, raw: Boolean): Int {
        if (!raw) return itemCounts.getItemCountIndex(position)
        var sum = 0
        for (i in 0 until itemCounts.size) {
            sum += when (i) {
                ITEM_INDEX_STATUS -> data?.size ?: 0
                else -> itemCounts[i]
            }
            if (position < sum) {
                return i
            }
        }
        return -1
    }

    private inline fun <T> getFieldValue(position: Int,
            readCursorValueAction: (cursor: Cursor, indices: ObjectCursor.CursorIndices<ParcelableStatus>) -> T,
            readStatusValueAction: (status: ParcelableStatus) -> T,
            defValue: T, raw: Boolean = false): T {
        if (data is ObjectCursor) {
            val dataPosition = position - statusStartIndex
            if (dataPosition < 0 || dataPosition >= getStatusCount(true)) {
                throw CursorIndexOutOfBoundsException("index: $position, valid range is $0..${getStatusCount(true)}")
            }
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return defValue
            val indices = (data as ObjectCursor).indices
            return readCursorValueAction(cursor, indices)
        }
        return readStatusValueAction(getStatus(position, raw))
    }

    private fun getStatus(position: Int, countIndex: Int, raw: Boolean = false): ParcelableStatus {
        when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                return pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
            }
            ITEM_INDEX_STATUS -> {
                val data = this.data!!
                val dataPosition = position - statusStartIndex
                val positions = displayPositions
                if (positions != null && !raw) {
                    return data[positions[dataPosition]]
                } else {
                    return data[dataPosition]
                }
            }
        }
        val validStart = getItemStartPosition(ITEM_INDEX_PINNED_STATUS)
        val validEnd = getItemStartPosition(ITEM_INDEX_STATUS) + getStatusCount(raw) - 1
        throw IndexOutOfBoundsException("index: $position, valid range is $validStart..$validEnd")
    }

    private fun updateItemCount() {
        itemCounts[ITEM_INDEX_LOAD_START_INDICATOR] = if (ILoadMoreSupportAdapter.START in loadMoreIndicatorPosition) 1 else 0
        itemCounts[ITEM_INDEX_FILTER_HEADER] = if (timelineFilter != null) 1 else 0
        itemCounts[ITEM_INDEX_PINNED_STATUS] = pinnedStatuses?.size ?: 0
        itemCounts[ITEM_INDEX_STATUS] = getStatusCount(false)
        itemCounts[ITEM_INDEX_LOAD_END_INDICATOR] = if (ILoadMoreSupportAdapter.END in loadMoreIndicatorPosition) 1 else 0
    }

    companion object {
        const val VIEW_TYPE_STATUS = 2
        const val VIEW_TYPE_EMPTY = 3
        const val VIEW_TYPE_FILTER_HEADER = 4

        const val ITEM_INDEX_LOAD_START_INDICATOR = 0
        const val ITEM_INDEX_FILTER_HEADER = 1
        const val ITEM_INDEX_PINNED_STATUS = 2
        const val ITEM_INDEX_STATUS = 3
        const val ITEM_INDEX_LOAD_END_INDICATOR = 4
    }


}
