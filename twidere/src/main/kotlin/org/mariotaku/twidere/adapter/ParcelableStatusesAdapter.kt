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

import android.arch.paging.AsyncPagedListDiffer
import android.arch.paging.PagedList
import android.arch.paging.setPagedListListener
import android.content.Context
import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Space
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.ktextension.findPositionByItemId
import org.mariotaku.ktextension.isNotNullOrEmpty
import org.mariotaku.ktextension.rangeOfSize
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.callback.ItemCountsAdapterListUpdateCallback
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
import org.mariotaku.twidere.model.placeholder.ParcelableStatusPlaceholder
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.paging.DiffItemCallbacks
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
    override val mediaPreviewStyle: Int = PreferencesSingleton.get(this.context)[mediaPreviewStyleKey]
    override val nameFirst: Boolean = PreferencesSingleton.get(this.context)[nameFirstKey]
    override val useStarsForLikes: Boolean = PreferencesSingleton.get(this.context)[iWantMyStarsBackKey]
    @TwidereLinkify.HighlightStyle
    override val linkHighlightingStyle: Int = PreferencesSingleton.get(this.context)[linkHighlightOptionKey]
    override val lightFont: Boolean = PreferencesSingleton.get(this.context)[lightFontKey]
    override val mediaPreviewEnabled: Boolean = Utils.isMediaPreviewEnabled(context, PreferencesSingleton.get(this.context))
    override val sensitiveContentEnabled: Boolean = PreferencesSingleton.get(this.context).getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false)

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

    override var loadMoreIndicatorPosition: Int
        get() = super.loadMoreIndicatorPosition
        set(value) {
            super.loadMoreIndicatorPosition = value
            updateItemCounts()
        }

    override var loadMoreSupportedPosition: Int
        get() = super.loadMoreSupportedPosition
        set(value) {
            super.loadMoreSupportedPosition = value
            updateItemCounts()
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
            updateItemCounts()
            notifyDataSetChanged()
        }

    var timelineFilter: TimelineFilter? = null
        set(value) {
            field = value
            updateItemCounts()
            notifyDataSetChanged()
        }

    var statuses: PagedList<ParcelableStatus>?
        get() = pagedStatusesHelper.currentList
        set(value) {
            pagedStatusesHelper.submitList(value)
            gapLoadingIds.clear()
            if (value == null) {
                itemCounts[ITEM_INDEX_STATUS] = 0
            }
        }

    val statusStartIndex: Int
        get() = getItemStartPosition(ITEM_INDEX_STATUS)

    var pagedListListener: ((list: PagedList<ParcelableStatus>?) -> Unit)? = null
        set(value) {
            field = value
            pagedStatusesHelper.setPagedListListener(value)
        }

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val showCardActions: Boolean = !PreferencesSingleton.get(this.context)[hideCardActionsKey]

    private val gapLoadingIds: MutableSet<ObjectId<String>> = HashSet()
    private var showingActionCardId = RecyclerView.NO_ID

    private val showingFullTextStates = SparseBooleanArray()

    private var pagedStatusesHelper = AsyncPagedListDiffer<ParcelableStatus>(ItemCountsAdapterListUpdateCallback(this, ITEM_INDEX_STATUS),
            AsyncDifferConfig.Builder<ParcelableStatus>(DiffItemCallbacks.status).build())

    init {
        val handler = StatusAdapterLinkClickHandler<List<ParcelableStatus>>(context, PreferencesSingleton.get(this.context))
        twidereLinkify = TwidereLinkify(handler)
        handler.setAdapter(this)
        isShowInReplyTo = true
        setHasStableIds(true)
    }

    override fun getStatus(position: Int): ParcelableStatus {
        return getStatusInternal(position = position)
    }

    override fun getStatusCount(): Int {
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
                val status = getStatus(position)
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

    override fun getStatusId(position: Int): String {
        return getStatus(position).id
    }

    fun getStatusSortId(position: Int): Long {
        return getStatus(position).sort_id
    }

    override fun getStatusTimestamp(position: Int): Long {
        return getStatus(position).timestamp
    }

    override fun getStatusPositionKey(position: Int): Long {
        return getStatus(position).position_key
    }

    override fun getAccountKey(position: Int): UserKey {
        return getStatus(position).account_key
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
        when (viewType.and(MASK_VIEW_TYPE).shr(BIT_SHIFT_VIEW_TYPE)) {
            ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(GapViewHolder.layoutResource, parent, false)
                return GapViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
            VIEW_TYPE_STATUS -> {
                val subtype = viewType.and(MASK_VIEW_SUBTYPE)
                return createStatusViewHolder(this, inflater, parent, timelineStyle, subtype) as RecyclerView.ViewHolder
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
        val viewType = holder.itemViewType
        when (viewType.and(MASK_VIEW_TYPE).shr(BIT_SHIFT_VIEW_TYPE)) {
            VIEW_TYPE_STATUS -> {
                holder as IStatusViewHolder
                val countIndex: Int = getItemCountIndex(position)
                val status = getStatusInternal(loadAround = true, position = position,
                        countIndex = countIndex)
                holder.display(status, displayInReplyTo = isShowInReplyTo,
                        displayPinned = countIndex == ITEM_INDEX_PINNED_STATUS)
            }
            VIEW_TYPE_FILTER_HEADER -> {
                (holder as TimelineFilterHeaderViewHolder).display(timelineFilter!!)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val status = getStatusInternal(loadAround = true, position = position)
                val loading = gapLoadingIds.any { it.accountKey == status.account_key && it.id == status.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val countIndex = getItemCountIndex(position)
        when (countIndex) {
            ITEM_INDEX_LOAD_START_INDICATOR, ITEM_INDEX_LOAD_END_INDICATOR -> {
                return ITEM_VIEW_TYPE_LOAD_INDICATOR.shl(BIT_SHIFT_VIEW_TYPE)
            }
            ITEM_INDEX_PINNED_STATUS -> {
                return VIEW_TYPE_STATUS.shl(BIT_SHIFT_VIEW_TYPE)
            }
            ITEM_INDEX_STATUS -> {
                val status = getStatus(position)
                if (status.is_gap) {
                    return ITEM_VIEW_TYPE_GAP.shl(BIT_SHIFT_VIEW_TYPE)
                } else {
                    val subtype = when {
                        status.attachment?.quoted != null -> VIEW_SUBTYPE_STATUS_QUOTE
                        status.attachment?.media.isNotNullOrEmpty() -> VIEW_SUBTYPE_STATUS_MEDIA
                        else -> 0
                    }
                    return VIEW_TYPE_STATUS.shl(BIT_SHIFT_VIEW_TYPE).or(subtype)
                }
            }
            ITEM_INDEX_FILTER_HEADER -> {
                return VIEW_TYPE_FILTER_HEADER.shl(BIT_SHIFT_VIEW_TYPE)
            }
            else -> throw UnsupportedCountIndexException(countIndex, position)
        }
    }

    override fun addGapLoadingId(id: ObjectId<String>) {
        gapLoadingIds.add(id)
    }

    override fun removeGapLoadingId(id: ObjectId<String>) {
        gapLoadingIds.remove(id)
    }

    override fun getItemCount(): Int {
        return itemCounts.itemCount
    }

    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        for (i in 0 until getStatusCount()) {
            if (accountKey == getAccountKey(i) && statusId == getStatusId(i)) {
                return getStatus(i)
            }
        }
        return null
    }

    fun isStatus(position: Int): Boolean {
        return position < getStatusCount()
    }

    fun getRowId(adapterPosition: Int): Long {
        val status = getStatus(adapterPosition)
        return if (status._id < 0) status.hashCode().toLong() else status._id
    }

    fun findPositionByPositionKey(positionKey: Long): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (positionKey <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, getStatusCount())
        if (range.isEmpty() || range.start < 0) return RecyclerView.NO_POSITION
        if (positionKey < getStatusPositionKey(range.last)) {
            return range.last
        }
        return range.indexOfFirst { positionKey >= getStatusPositionKey(it) }
    }

    fun findPositionBySortId(sortId: Long): Int {
        // Assume statuses are descend sorted by id, so break at first status with id
        // lesser equals than read position
        if (sortId <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(statusStartIndex, getStatusCount())
        if (range.isEmpty() || range.start < 0) return RecyclerView.NO_POSITION
        if (sortId < getStatusSortId(range.last)) {
            return range.last
        }
        return range.indexOfFirst { sortId >= getStatusSortId(it) }
    }

    private fun getStatusInternal(loadAround: Boolean = false, position: Int,
            countIndex: Int = getItemCountIndex(position)): ParcelableStatus {
        when (countIndex) {
            ITEM_INDEX_PINNED_STATUS -> {
                return pinnedStatuses!![position - getItemStartPosition(ITEM_INDEX_PINNED_STATUS)]
            }
            ITEM_INDEX_STATUS -> {
                val dataPosition = position - statusStartIndex
                return if (loadAround) {
                    pagedStatusesHelper.getItem(dataPosition) ?: ParcelableStatusPlaceholder
                } else {
                    pagedStatusesHelper.currentList?.get(dataPosition)
                            ?: ParcelableStatusPlaceholder
                }
            }
        }
        val validStart = getItemStartPosition(ITEM_INDEX_PINNED_STATUS)
        val validEnd = getItemStartPosition(ITEM_INDEX_STATUS) + getStatusCount() - 1
        throw IndexOutOfBoundsException("index: $position, valid range is $validStart..$validEnd")
    }

    override fun updateItemCounts() {
        itemCounts[ITEM_INDEX_LOAD_START_INDICATOR] = if (LoadMorePosition.START in loadMoreIndicatorPosition) 1 else 0
        itemCounts[ITEM_INDEX_FILTER_HEADER] = if (timelineFilter != null) 1 else 0
        itemCounts[ITEM_INDEX_PINNED_STATUS] = pinnedStatuses?.size ?: 0
        itemCounts[ITEM_INDEX_LOAD_END_INDICATOR] = if (LoadMorePosition.END in loadMoreIndicatorPosition) 1 else 0
    }

    companion object {

        const val BIT_SHIFT_VIEW_TYPE = 8
        const val MASK_VIEW_TYPE = 0xFF00
        const val MASK_VIEW_SUBTYPE = 0x00FF

        const val VIEW_TYPE_STATUS = 2
        const val VIEW_TYPE_EMPTY = 3
        const val VIEW_TYPE_FILTER_HEADER = 4

        const val VIEW_SUBTYPE_STATUS_MEDIA = 1
        const val VIEW_SUBTYPE_STATUS_QUOTE = 2

        const val ITEM_INDEX_LOAD_START_INDICATOR = 0
        const val ITEM_INDEX_FILTER_HEADER = 1
        const val ITEM_INDEX_PINNED_STATUS = 2
        const val ITEM_INDEX_STATUS = 3
        const val ITEM_INDEX_LOAD_END_INDICATOR = 4


        fun createStatusViewHolder(adapter: IStatusesAdapter, inflater: LayoutInflater,
                parent: ViewGroup, @TimelineStyle timelineStyle: Int, subtype: Int = 0): IStatusViewHolder {
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
                    val holder = StatusViewHolder(adapter, view, subtype)
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
