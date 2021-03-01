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
import android.database.CursorIndexOutOfBoundsException
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
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
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
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
    private val showCardNumbers: Boolean = !preferences[hideCardNumbersKey]
    private val showLinkPreview: Boolean = preferences[showLinkPreviewKey]

    private val gapLoadingIds: MutableSet<ObjectId> = HashSet()

    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null

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
    private val showingFullTextStates = SparseBooleanArray()
    private val reuseStatus = ParcelableStatus()
    private var infoCache: Array<StatusInfo?>? = null

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
        return getFieldValue(position, { info ->
            return@getFieldValue info.gap
        }, { status ->
            return@getFieldValue status.is_gap
        }, false)
    }

    override fun getStatus(position: Int, raw: Boolean): ParcelableStatus {
        return getStatusInternal(position, getItemCountIndex(position, raw), raw, reuse = false)
    }

    override fun getStatusCount(raw: Boolean): Int {
        if (raw) return data?.size ?: 0
        return displayDataCount
    }

    override fun setData(data: List<ParcelableStatus>?): Boolean {
        var changed = true
        when (data) {
            null -> {
                displayPositions = null
                displayDataCount = 0
            }
            is ObjectCursor -> {
                displayPositions = null
                displayDataCount = data.size
            }
            else -> {
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
        }
        this.data = data
        this.infoCache = if (data != null) arrayOfNulls(data.size) else null
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
        return (countIndex.toLong() shl 32) or when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                val status = pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
                return status.hashCode().toLong()
            }
            ITEM_INDEX_STATUS -> getFieldValue(position, { (_, accountKey, id) ->
                return@getFieldValue ParcelableStatus.calculateHashCode(accountKey, id)
            }, { status ->
                return@getFieldValue status.hashCode()
            }, -1).toLong()
            else -> position.toLong()
        }
    }

    override fun getStatusId(position: Int, raw: Boolean): String {
        return getFieldValue(position, { info ->
            return@getFieldValue info.id
        }, { status ->
            return@getFieldValue status.id
        }, "")
    }

    fun getStatusSortId(position: Int, raw: Boolean): Long {
        return getFieldValue(position, { info ->
            return@getFieldValue info.sortId
        }, { status ->
            return@getFieldValue status.sort_id
        }, -1L, raw)
    }

    override fun getStatusTimestamp(position: Int, raw: Boolean): Long {
        return getFieldValue(position, { info ->
            return@getFieldValue info.timestamp
        }, { status ->
            return@getFieldValue status.timestamp
        }, -1L)
    }

    override fun getStatusPositionKey(position: Int, raw: Boolean): Long {
        return getFieldValue(position, { info ->
            if (info.positionKey > 0) return@getFieldValue info.positionKey
            return@getFieldValue info.timestamp
        }, { status ->
            val positionKey = status.position_key
            if (positionKey > 0) return@getFieldValue positionKey
            return@getFieldValue status.timestamp
        }, -1L)
    }

    override fun getAccountKey(position: Int, raw: Boolean): UserKey {
        val def: UserKey? = null
        return getFieldValue(position, { info ->
            return@getFieldValue info.accountKey
        }, { status ->
            return@getFieldValue status.account_key
        }, def, raw)!!
    }

    override fun isCardNumbersShown(position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return showCardNumbers
        return showCardNumbers || showingActionCardId == getItemId(position)
    }

    override fun isLinkPreviewShown(position: Int): Boolean {
        return showLinkPreview
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

    override fun isFullTextVisible(position: Int): Boolean {
        return showingFullTextStates.get(position)
    }

    override fun setFullTextVisible(position: Int, visible: Boolean) {
        showingFullTextStates.put(position, visible)
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
        throw IllegalStateException("Unknown view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_STATUS -> {
                val countIndex: Int = getItemCountIndex(position)
                val status = getStatusInternal(position, countIndex = countIndex, reuse = true)
                (holder as IStatusViewHolder).display(status, displayInReplyTo = isShowInReplyTo,
                        displayPinned = countIndex == ITEM_INDEX_PINNED_STATUS)
            }
            VIEW_TYPE_FILTER_HEADER -> {
                (holder as TimelineFilterHeaderViewHolder).display(timelineFilter!!)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val status = getStatusInternal(position, reuse = true)
                val loading = gapLoadingIds.any { it.accountKey == status.account_key && it.id == status.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && ILoadMoreSupportAdapter.START in loadMoreIndicatorPosition) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        when (val countIndex = getItemCountIndex(position)) {
            ITEM_INDEX_LOAD_START_INDICATOR, ITEM_INDEX_LOAD_END_INDICATOR -> {
                return ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
            ITEM_INDEX_PINNED_STATUS -> {
                return VIEW_TYPE_STATUS
            }
            ITEM_INDEX_STATUS -> return if (isGapItem(position)) {
                ITEM_VIEW_TYPE_GAP
            } else {
                VIEW_TYPE_STATUS
            }
            ITEM_INDEX_FILTER_HEADER -> {
                return VIEW_TYPE_FILTER_HEADER
            }
            else -> throw UnsupportedCountIndexException(countIndex, position)
        }
    }

    protected abstract fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder

    override fun addGapLoadingId(id: ObjectId) {
        gapLoadingIds.add(id)
    }

    override fun removeGapLoadingId(id: ObjectId) {
        gapLoadingIds.remove(id)
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

    fun isStatus(position: Int, raw: Boolean = false): Boolean {
        return position < getStatusCount(raw)
    }

    fun getRowId(adapterPosition: Int, raw: Boolean = false): Long {
        return getFieldValue(adapterPosition, readInfoValueAction = {
            it._id
        }, readStatusValueAction = { status ->
            status.hashCode().toLong()
        }, defValue = -1L, raw = raw)
    }

    fun findPositionByPositionKey(positionKey: Long, raw: Boolean = false): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (positionKey <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, getStatusCount(raw))
        if (range.isEmpty() || range.first < 0) return RecyclerView.NO_POSITION
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
        if (range.isEmpty() || range.first < 0) return RecyclerView.NO_POSITION
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
            readInfoValueAction: (StatusInfo) -> T,
            readStatusValueAction: (status: ParcelableStatus) -> T,
            defValue: T, raw: Boolean = false): T {
        val data = this.data
        if (data is ObjectCursor) {
            val dataPosition = position - statusStartIndex
            if (dataPosition < 0 || dataPosition >= getStatusCount(true)) {
                throw CursorIndexOutOfBoundsException("index: $position, valid range is $0..${getStatusCount(true)}")
            }
            val info = infoCache?.get(dataPosition) ?: run {
                val cursor = data.cursor
                if (!cursor.safeMoveToPosition(dataPosition)) return defValue
                val indices = data.indices
                val _id = cursor.safeGetLong(indices[Statuses._ID])
                val accountKey = UserKey.valueOf(cursor.safeGetString(indices[Statuses.ACCOUNT_KEY]))
                val id = cursor.safeGetString(indices[Statuses.ID])
                val timestamp = cursor.safeGetLong(indices[Statuses.TIMESTAMP])
                val sortId = cursor.safeGetLong(indices[Statuses.SORT_ID])
                val positionKey = cursor.safeGetLong(indices[Statuses.POSITION_KEY])
                val gap = cursor.safeGetInt(indices[Statuses.IS_GAP]) == 1
                val newInfo = StatusInfo(_id, accountKey, id, timestamp, sortId, positionKey, gap)
                infoCache?.set(dataPosition, newInfo)
                return@run newInfo
            }
            return readInfoValueAction(info)
        }
        return readStatusValueAction(getStatus(position, raw))
    }

    private fun getStatusInternal(position: Int, countIndex: Int = getItemCountIndex(position),
            raw: Boolean = false, reuse: Boolean): ParcelableStatus {
        when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                return pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
            }
            ITEM_INDEX_STATUS -> {
                val data = this.data!!
                val dataPosition = position - statusStartIndex
                val positions = displayPositions
                val listPosition = if (positions != null && !raw) {
                    positions[dataPosition]
                } else {
                    dataPosition
                }
                return if (reuse && data is ObjectCursor) {
                    reuseStatus.is_filtered = false
                    data.setInto(listPosition, reuseStatus)
                } else {
                    data[listPosition]
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


    data class StatusInfo(
            val _id: Long,
            val accountKey: UserKey,
            val id: String,
            val timestamp: Long,
            val sortId: Long,
            val positionKey: Long,
            val gap: Boolean
    )


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
