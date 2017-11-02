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

import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapterHelper
import android.content.Context
import android.support.v4.widget.Space
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.ktextension.findPositionByItemId
import org.mariotaku.ktextension.rangeOfSize
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter.Companion.ITEM_VIEW_TYPE_GAP
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ObjectId
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.paging.DiffCallbacks
import org.mariotaku.twidere.view.holder.EmptyViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.TimelineFilterHeaderViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import org.mariotaku.twidere.view.holder.status.LargeMediaStatusViewHolder
import org.mariotaku.twidere.view.holder.status.MediaStatusViewHolder
import org.mariotaku.twidere.view.holder.status.StatusViewHolder
import java.util.*

class ParcelableStatusesAdapter(
        context: Context,
        requestManager: RequestManager,
        @TimelineStyle private val timelineStyle: Int
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IStatusesAdapter, IItemCountsAdapter {

    override val twidereLinkify: TwidereLinkify

    @PreviewStyle
    override val mediaPreviewStyle: Int = preferences[mediaPreviewStyleKey]
    override val nameFirst: Boolean = preferences[nameFirstKey]
    override val useStarsForLikes: Boolean = preferences[iWantMyStarsBackKey]
    @TwidereLinkify.HighlightStyle
    override val linkHighlightingStyle: Int = preferences[linkHighlightOptionKey]
    override val lightFont: Boolean = preferences[lightFontKey]
    override val mediaPreviewEnabled: Boolean = Utils.isMediaPreviewEnabled(context, preferences)
    override val sensitiveContentEnabled: Boolean = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false)

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = statusClickListener

    override var showAccountsColor: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null

    override val itemCounts = ItemCounts(5)

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
            updateItemCount()
            notifyDataSetChanged()
        }

    var statuses: PagedList<ParcelableStatus>?
        get() = pagedStatusesHelper.currentList
        set(value) {
            pagedStatusesHelper.setList(value)
            gapLoadingIds.clear()
            if (value == null) {
                itemCounts[ITEM_INDEX_STATUS] = 0
            }
        }

    val statusStartIndex: Int
        get() = getItemStartPosition(ITEM_INDEX_STATUS)

    override var loadMoreIndicatorPosition: Int
        get() = super.loadMoreIndicatorPosition
        set(value) {
            super.loadMoreIndicatorPosition = value
            updateItemCount()
        }

    override var loadMoreSupportedPosition: Int
        get() = super.loadMoreSupportedPosition
        set(value) {
            super.loadMoreSupportedPosition = value
            updateItemCount()
        }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val showCardActions: Boolean = !preferences[hideCardActionsKey]

    private val gapLoadingIds: MutableSet<ObjectId> = HashSet()
    private var showingActionCardId = RecyclerView.NO_ID

    private val showingFullTextStates = SparseBooleanArray()

    private var pagedStatusesHelper = PagedListAdapterHelper<ParcelableStatus>(object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            itemCounts[ITEM_INDEX_STATUS] += count
            updateItemCount()
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            itemCounts[ITEM_INDEX_STATUS] -= count
            updateItemCount()
            notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(position, count, payload)
        }

    }, ListAdapterConfig.Builder<ParcelableStatus>().setDiffCallback(DiffCallbacks.status).build())

    init {
        val handler = StatusAdapterLinkClickHandler<List<ParcelableStatus>>(context, preferences)
        twidereLinkify = TwidereLinkify(handler)
        handler.setAdapter(this)
        isShowInReplyTo = true
        setHasStableIds(true)
    }

    override fun isGapItem(position: Int): Boolean {
        return getStatusInternal(false, false, position = position)?.is_gap == true
    }

    override fun getStatus(position: Int, raw: Boolean): ParcelableStatus {
        return getStatusInternal(raw, position = position) ?: ParcelableStatusPlaceholder
    }

    override fun getStatusCount(raw: Boolean): Int {
        return itemCounts[ITEM_INDEX_STATUS]
    }

    override fun getItemId(position: Int): Long {
        val countIndex = getItemCountIndex(position)
        /*            ID memory layout
         *             0xFFFFFFFFFFFFFFFFL
         * For pinned:   ------IIHHHHHHHH
         * For others:   ------IIPPPPPPPP
         *
         * I: Count index
         * P: Position relative to count index start
         * H: Hashcode
         */
        when (countIndex) {
            ITEM_INDEX_STATUS -> {
                val status = getStatus(position, false)
                if (status._id > 0) return status._id
                return status.hashCode().toLong()
            }
            ITEM_INDEX_PINNED_STATUS -> {
                val countIndexFlag = countIndex.toLong() shl 32
                val status = pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
                return -(countIndexFlag or status.hashCode().toLong())
            }
            else -> {
                val countIndexFlag = countIndex.toLong() shl 32
                val relativePosition = getItemStartPosition(countIndex).toLong()
                return -(countIndexFlag or relativePosition)
            }
        }
    }

    override fun getStatusId(position: Int, raw: Boolean): String {
        return getStatus(position, raw).id
    }

    fun getStatusSortId(position: Int, raw: Boolean): Long {
        return getStatus(position, raw).sort_id
    }

    override fun getStatusTimestamp(position: Int, raw: Boolean): Long {
        return getStatus(position, raw).timestamp
    }

    override fun getStatusPositionKey(position: Int, raw: Boolean): Long {
        return getStatus(position, raw).position_key
    }

    override fun getAccountKey(position: Int, raw: Boolean): UserKey {
        return getStatus(position, raw).account_key
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
                return createStatusViewHolder(this, inflater, parent, timelineStyle) as RecyclerView.ViewHolder
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
                holder as IStatusViewHolder
                val countIndex: Int = getItemCountIndex(position)
                val status = getStatusInternal(loadAround = true, position = position,
                        countIndex = countIndex) ?: return
                holder.display(status, displayInReplyTo = isShowInReplyTo,
                        displayPinned = countIndex == ITEM_INDEX_PINNED_STATUS)
            }
            VIEW_TYPE_FILTER_HEADER -> {
                (holder as TimelineFilterHeaderViewHolder).display(timelineFilter!!)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val status = getStatusInternal(loadAround = true, position = position) ?: return
                val loading = gapLoadingIds.any { it.accountKey == status.account_key && it.id == status.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val countIndex = getItemCountIndex(position)
        when (countIndex) {
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
        val status = getStatus(adapterPosition, raw)
        return if (status._id < 0) status.hashCode().toLong() else status._id
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
                ITEM_INDEX_STATUS -> statuses?.size ?: 0
                else -> itemCounts[i]
            }
            if (position < sum) {
                return i
            }
        }
        return -1
    }

    private fun getStatusInternal(raw: Boolean = false, loadAround: Boolean = false,
            position: Int, countIndex: Int = getItemCountIndex(position, raw)): ParcelableStatus? {
        when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                return pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
            }
            ITEM_INDEX_STATUS -> {
                val dataPosition = position - statusStartIndex
                return if (loadAround) {
                    pagedStatusesHelper.getItem(dataPosition)
                } else {
                    pagedStatusesHelper.currentList?.get(dataPosition)
                }
            }
        }
        val validStart = getItemStartPosition(ITEM_INDEX_PINNED_STATUS)
        val validEnd = getItemStartPosition(ITEM_INDEX_STATUS) + getStatusCount(raw) - 1
        throw IndexOutOfBoundsException("index: $position, valid range is $validStart..$validEnd")
    }

    private fun updateItemCount() {
        itemCounts[ITEM_INDEX_LOAD_START_INDICATOR] = if (LoadMorePosition.START in loadMoreIndicatorPosition) 1 else 0
        itemCounts[ITEM_INDEX_FILTER_HEADER] = if (timelineFilter != null) 1 else 0
        itemCounts[ITEM_INDEX_PINNED_STATUS] = pinnedStatuses?.size ?: 0
        itemCounts[ITEM_INDEX_LOAD_END_INDICATOR] = if (LoadMorePosition.END in loadMoreIndicatorPosition) 1 else 0
    }

    object ParcelableStatusPlaceholder : ParcelableStatus() {
        init {
            id = "none"
            account_key = UserKey.INVALID
            user_key = UserKey.INVALID
        }

        override fun hashCode(): Int {
            return -1
        }
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


        fun createStatusViewHolder(adapter: IStatusesAdapter, inflater: LayoutInflater,
                parent: ViewGroup, @TimelineStyle timelineStyle: Int): IStatusViewHolder {
            when (timelineStyle) {
                TimelineStyle.STAGGERED -> {
                    val view = inflater.inflate(MediaStatusViewHolder.layoutResource, parent, false)
                    val holder = MediaStatusViewHolder(adapter, view)
                    holder.setOnClickListeners()
                    holder.setupViewOptions()
                    return holder
                }
                TimelineStyle.PLAIN -> {
                    val view = inflater.inflate(StatusViewHolder.layoutResource, parent, false)
                    val holder = StatusViewHolder(adapter, view)
                    holder.setOnClickListeners()
                    holder.setupViewOptions()
                    return holder
                }
                TimelineStyle.GALLERY -> {
                    val view = inflater.inflate(LargeMediaStatusViewHolder.layoutResource, parent, false)
                    val holder = LargeMediaStatusViewHolder(adapter, view)
                    holder.setOnClickListeners()
                    holder.setupViewOptions()
                    return holder
                }
                else -> throw AssertionError()
            }
        }
    }


}
