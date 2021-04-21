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

package org.mariotaku.twidere.adapter

import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.microblog.library.twitter.model.TranslationResult
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.extension.model.originalId
import org.mariotaku.twidere.extension.model.retweet_sort_id
import org.mariotaku.twidere.fragment.status.StatusFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.EmptyViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import org.mariotaku.twidere.view.holder.status.DetailStatusViewHolder

class StatusDetailsAdapter(
        val fragment: StatusFragment
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(fragment.requireContext(), fragment.requestManager),
        IStatusesAdapter<List<ParcelableStatus>>, IItemCountsAdapter {

    override val twidereLinkify: TwidereLinkify

    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null

    override val itemCounts = ItemCounts(ITEM_TYPES_SUM)

    override val nameFirst = preferences[nameFirstKey]
    override val mediaPreviewStyle = preferences[mediaPreviewStyleKey]
    override val linkHighlightingStyle = preferences[linkHighlightOptionKey]
    override val lightFont = preferences[lightFontKey]
    override val mediaPreviewEnabled = preferences[mediaPreviewKey]
    override val sensitiveContentEnabled = preferences[displaySensitiveContentsKey]
    override val useStarsForLikes = preferences[iWantMyStarsBackKey]

    private val inflater: LayoutInflater
    private val cardBackgroundColor: Int
    private val showCardActions = !preferences[hideCardActionsKey]
    private val showCardNumbers = !preferences[hideCardNumbersKey]
    private val showLinkPreview = preferences[showLinkPreviewKey]
    private var recyclerView: RecyclerView? = null
    private var detailMediaExpanded: Boolean = false

    var status: ParcelableStatus? = null
        internal set
    var translationResult: TranslationResult? = null
        internal set(translation) {
            field = if (translation == null || status?.originalId != translation.id) {
                null
            } else {
                translation
            }
            notifyDataSetChanged()
        }
    var statusActivity: StatusFragment.StatusActivity? = null
        internal set(value) {
            val status = status ?: return
            if (value != null && !value.isStatus(status)) {
                return
            }
            field = value
            val statusIndex = getIndexStart(ITEM_IDX_STATUS)
            notifyItemChanged(statusIndex, value)
        }
    var statusAccount: AccountDetails? = null
        internal set

    private var data: List<ParcelableStatus>? = null
    private var replyError: CharSequence? = null
    private var conversationError: CharSequence? = null
    private var replyStart: Int = 0
    private var showingActionCardPosition = RecyclerView.NO_POSITION
    private val showingFullTextStates = SparseBooleanArray()

    init {
        setHasStableIds(true)
        val context = fragment.activity
        // There's always a space at the end of the list
        itemCounts[ITEM_IDX_SPACE] = 1
        itemCounts[ITEM_IDX_STATUS] = 1
        itemCounts[ITEM_IDX_CONVERSATION_LOAD_MORE] = 1
        itemCounts[ITEM_IDX_REPLY_LOAD_MORE] = 1
        inflater = LayoutInflater.from(context)
        cardBackgroundColor = ThemeUtils.getCardBackgroundColor(context!!,
                preferences[themeBackgroundOptionKey], preferences[themeBackgroundAlphaKey])
        val listener = StatusAdapterLinkClickHandler<List<ParcelableStatus>>(context, preferences)
        listener.setAdapter(this)
        twidereLinkify = TwidereLinkify(listener)
    }

    override fun getStatus(position: Int, raw: Boolean): ParcelableStatus {
        when (getItemCountIndex(position, raw)) {
            ITEM_IDX_CONVERSATION -> {
                data?.let { data ->
                    var idx = position - getIndexStart(ITEM_IDX_CONVERSATION)
                    if (idx in data.indices) {
                        if (data[idx].is_filtered) {
                            idx++
                        }
                        return data[idx]
                    }
                }
            }
            ITEM_IDX_REPLY -> {
                data?.let { data ->
                    var idx = position - getIndexStart(ITEM_IDX_CONVERSATION) -
                            getTypeCount(ITEM_IDX_CONVERSATION) - getTypeCount(ITEM_IDX_STATUS) +
                            replyStart
                    if (idx in data.indices) {
                        if (data[idx].is_filtered) {
                            idx++
                        }
                        return data[idx]
                    }
                }
            }
            ITEM_IDX_STATUS -> {
                return status!!
            }
        }
        throw IndexOutOfBoundsException("index: $position")
    }

    fun getIndexStart(index: Int): Int {
        if (index == 0) return 0
        return itemCounts.getItemStartPosition(index)
    }

    override fun getStatusId(position: Int, raw: Boolean): String {
        return getStatus(position, raw).id
    }

    override fun getStatusTimestamp(position: Int, raw: Boolean): Long {
        return getStatus(position, raw).timestamp
    }

    override fun getStatusPositionKey(position: Int, raw: Boolean): Long {
        val status = getStatus(position, raw)
        return if (status.position_key > 0) status.timestamp else getStatusTimestamp(position, raw)
    }

    override fun getAccountKey(position: Int, raw: Boolean) = getStatus(position, raw).account_key

    override fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus? {
        if (status != null && accountKey == status!!.account_key && TextUtils.equals(statusId, status!!.id)) {
            return status
        }
        return data?.firstOrNull { accountKey == it.account_key && TextUtils.equals(it.id, statusId) }
    }

    override fun getStatusCount(raw: Boolean): Int {
        return getTypeCount(ITEM_IDX_CONVERSATION) + getTypeCount(ITEM_IDX_STATUS) + getTypeCount(ITEM_IDX_REPLY)
    }

    override fun isCardNumbersShown(position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return showCardNumbers
        return showCardNumbers || showingActionCardPosition == position
    }

    override fun isLinkPreviewShown(position: Int): Boolean {
        return showLinkPreview
    }

    override fun isCardActionsShown(position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return showCardActions
        return showCardActions || showingActionCardPosition == position
    }

    override fun showCardActions(position: Int) {
        if (showingActionCardPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(showingActionCardPosition)
        }
        showingActionCardPosition = position
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

    override fun setData(data: List<ParcelableStatus>?): Boolean {
        val status = this.status ?: return false
        val changed = this.data != data
        this.data = data
        if (data == null || data.isEmpty()) {
            setTypeCount(ITEM_IDX_CONVERSATION, 0)
            setTypeCount(ITEM_IDX_REPLY, 0)
            replyStart = -1
        } else {
            val sortId = if (status.is_retweet) {
                status.retweet_sort_id
            } else {
                status.sort_id
            }

            var conversationCount = 0
            var replyCount = 0
            var replyStart = -1
            data.forEachIndexed { i, item ->
                if (item.sort_id < sortId) {
                    if (!item.is_filtered) {
                        conversationCount++
                    }
                } else if (status.id == item.id) {
                    this.status = item
                } else if (item.sort_id > sortId) {
                    if (replyStart < 0) {
                        replyStart = i
                    }
                    if (!item.is_filtered) {
                        replyCount++
                    }
                }
            }
            setTypeCount(ITEM_IDX_CONVERSATION, conversationCount)
            setTypeCount(ITEM_IDX_REPLY, replyCount)
            this.replyStart = replyStart
        }
        notifyDataSetChanged()
        updateItemDecoration()
        return changed
    }

    override val showAccountsColor: Boolean
        get() = false

    var isDetailMediaExpanded: Boolean
        get() {
            if (detailMediaExpanded) return true
            if (mediaPreviewEnabled) {
                val status = this.status
                return status != null && (sensitiveContentEnabled || !status.is_possibly_sensitive)
            }
            return false
        }
        set(expanded) {
            detailMediaExpanded = expanded
            notifyDataSetChanged()
            updateItemDecoration()
        }

    override fun isGapItem(position: Int): Boolean {
        return false
    }

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = statusClickListener


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_DETAIL_STATUS -> {
                val view = inflater.inflate(R.layout.header_status, parent, false)
                view.setBackgroundColor(cardBackgroundColor)
                return DetailStatusViewHolder(this, view)
            }
            VIEW_TYPE_LIST_STATUS -> {
                return ListParcelableStatusesAdapter.createStatusViewHolder(this, inflater, parent)
            }
            VIEW_TYPE_CONVERSATION_LOAD_INDICATOR, VIEW_TYPE_REPLIES_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent,
                        false)
                return LoadIndicatorViewHolder(view)
            }
            VIEW_TYPE_SPACE -> {
                return EmptyViewHolder(Space(context))
            }
            VIEW_TYPE_REPLY_ERROR -> {
                val view = inflater.inflate(R.layout.adapter_item_status_error, parent,
                        false)
                return StatusErrorItemViewHolder(view)
            }
        }
        return EmptyViewHolder(View(context))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        var handled = false
        when (holder.itemViewType) {
            VIEW_TYPE_DETAIL_STATUS -> {
                holder as DetailStatusViewHolder
                payloads.forEach {
                    when (it) {
                        is StatusFragment.StatusActivity -> {
                            holder.updateStatusActivity(it)
                        }
                        is ParcelableStatus -> {
                            holder.displayStatus(statusAccount, status, statusActivity,
                                    translationResult)
                        }
                    }
                    handled = true
                }
            }
        }
        if (handled) return
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_DETAIL_STATUS -> {
                val status = getStatus(position)
                val detailHolder = holder as DetailStatusViewHolder
                detailHolder.displayStatus(statusAccount, status, statusActivity, translationResult)
            }
            VIEW_TYPE_LIST_STATUS -> {
                val status = getStatus(position)
                val statusHolder = holder as IStatusViewHolder
                // Display 'in reply to' for first item
                // useful to indicate whether first tweet has reply or not
                // We only display that indicator for first conversation item
                val itemType = getItemType(position)
                val displayInReplyTo = itemType == ITEM_IDX_CONVERSATION && position - getItemTypeStart(position) == 0
                statusHolder.display(status = status, displayInReplyTo = displayInReplyTo)
            }
            VIEW_TYPE_REPLY_ERROR -> {
                val errorHolder = holder as StatusErrorItemViewHolder
                errorHolder.showError(replyError!!)
            }
            VIEW_TYPE_CONVERSATION_ERROR -> {
                val errorHolder = holder as StatusErrorItemViewHolder
                errorHolder.showError(conversationError!!)
            }
            VIEW_TYPE_CONVERSATION_LOAD_INDICATOR -> {
                val indicatorHolder = holder as LoadIndicatorViewHolder
                indicatorHolder.setLoadProgressVisible(isConversationsLoading)
            }
            VIEW_TYPE_REPLIES_LOAD_INDICATOR -> {
                val indicatorHolder = holder as LoadIndicatorViewHolder
                indicatorHolder.setLoadProgressVisible(isRepliesLoading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemViewTypeByItemType(getItemType(position))
    }

    override fun addGapLoadingId(id: ObjectId) {

    }

    override fun removeGapLoadingId(id: ObjectId) {

    }

    private fun getItemViewTypeByItemType(type: Int): Int {
        when (type) {
            ITEM_IDX_CONVERSATION, ITEM_IDX_REPLY -> return VIEW_TYPE_LIST_STATUS
            ITEM_IDX_CONVERSATION_LOAD_MORE -> return VIEW_TYPE_CONVERSATION_LOAD_INDICATOR
            ITEM_IDX_REPLY_LOAD_MORE -> return VIEW_TYPE_REPLIES_LOAD_INDICATOR
            ITEM_IDX_STATUS -> return VIEW_TYPE_DETAIL_STATUS
            ITEM_IDX_SPACE -> return VIEW_TYPE_SPACE
            ITEM_IDX_REPLY_ERROR -> return VIEW_TYPE_REPLY_ERROR
            ITEM_IDX_CONVERSATION_ERROR -> return VIEW_TYPE_CONVERSATION_ERROR
        }
        throw IllegalStateException()
    }

    private fun getItemCountIndex(position: Int, raw: Boolean): Int {
        return itemCounts.getItemCountIndex(position)
    }

    fun getItemType(position: Int): Int {
        var typeStart = 0
        for (type in 0 until ITEM_TYPES_SUM) {
            val typeCount = getTypeCount(type)
            val typeEnd = typeStart + typeCount
            if (position in typeStart until typeEnd) return type
            typeStart = typeEnd
        }
        throw IllegalStateException("Unknown position $position")
    }

    fun getItemTypeStart(position: Int): Int {
        var typeStart = 0
        for (type in 0 until ITEM_TYPES_SUM) {
            val typeCount = getTypeCount(type)
            val typeEnd = typeStart + typeCount
            if (position in typeStart until typeEnd) return typeStart
            typeStart = typeEnd
        }
        throw IllegalStateException()
    }

    override fun getItemId(position: Int): Long {
        val countIndex = getItemCountIndex(position)
        when (countIndex) {
            ITEM_IDX_CONVERSATION, ITEM_IDX_STATUS, ITEM_IDX_REPLY -> {
                val status = getStatus(position)
                val hashCode = ParcelableStatus.calculateHashCode(status.account_key, status.id)
                return (countIndex.toLong() shl 32) or hashCode.toLong()
            }
        }
        val countPos = (position - getItemStartPosition(countIndex)).toLong()
        return (countIndex.toLong() shl 32) or countPos
    }

    override fun getItemCount(): Int {
        if (status == null) return 0
        return itemCounts.itemCount
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    private fun setTypeCount(idx: Int, size: Int) {
        itemCounts[idx] = size
        notifyDataSetChanged()
    }

    fun getTypeCount(idx: Int): Int {
        return itemCounts[idx]
    }

    fun setReplyError(error: CharSequence?) {
        replyError = error
        setTypeCount(ITEM_IDX_REPLY_ERROR, if (error != null) 1 else 0)
        updateItemDecoration()
    }

    fun setConversationError(error: CharSequence?) {
        conversationError = error
        setTypeCount(ITEM_IDX_CONVERSATION_ERROR, if (error != null) 1 else 0)
        updateItemDecoration()
    }

    fun setStatus(status: ParcelableStatus, account: AccountDetails?): Boolean {
        val oldStatus = this.status
        val oldAccount = this.statusAccount
        val changed = oldStatus != status && oldAccount != account
        this.status = status
        this.statusAccount = account
        if (changed) {
            notifyDataSetChanged()
            updateItemDecoration()
        } else {
            val statusIndex = getIndexStart(ITEM_IDX_STATUS)
            notifyItemChanged(statusIndex, status)
        }
        return changed
    }

    fun updateItemDecoration() {
        if (recyclerView == null) return
    }

    fun getFirstPositionOfItem(itemIdx: Int): Int {
        var position = 0
        for (i in 0 until ITEM_TYPES_SUM) {
            if (itemIdx == i) return position
            position += getTypeCount(i)
        }
        return RecyclerView.NO_POSITION
    }


    fun getData(): List<ParcelableStatus>? {
        return data
    }

    var isConversationsLoading: Boolean
        get() = ILoadMoreSupportAdapter.START in loadMoreIndicatorPosition
        set(loading) {
            loadMoreIndicatorPosition = if (loading) {
                loadMoreIndicatorPosition or ILoadMoreSupportAdapter.START
            } else {
                loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START.inv()
            }
            updateItemDecoration()
        }

    var isRepliesLoading: Boolean
        get() = ILoadMoreSupportAdapter.END in loadMoreIndicatorPosition
        set(loading) {
            loadMoreIndicatorPosition = if (loading) {
                loadMoreIndicatorPosition or ILoadMoreSupportAdapter.END
            } else {
                loadMoreIndicatorPosition and ILoadMoreSupportAdapter.END.inv()
            }
            updateItemDecoration()
        }

    class StatusErrorItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView = itemView.findViewById<TextView>(android.R.id.text1)

        init {
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.linksClickable = true
        }

        fun showError(text: CharSequence) {
            textView.text = text
        }
    }

    companion object {

        const val VIEW_TYPE_LIST_STATUS = 0
        const val VIEW_TYPE_DETAIL_STATUS = 1
        const val VIEW_TYPE_CONVERSATION_LOAD_INDICATOR = 2
        const val VIEW_TYPE_REPLIES_LOAD_INDICATOR = 3
        const val VIEW_TYPE_REPLY_ERROR = 4
        const val VIEW_TYPE_CONVERSATION_ERROR = 5
        const val VIEW_TYPE_SPACE = 6
        const val VIEW_TYPE_EMPTY = 7

        const val ITEM_IDX_CONVERSATION_LOAD_MORE = 0
        const val ITEM_IDX_CONVERSATION_ERROR = 1
        const val ITEM_IDX_CONVERSATION = 2
        const val ITEM_IDX_STATUS = 3
        const val ITEM_IDX_REPLY = 4
        const val ITEM_IDX_REPLY_ERROR = 5
        const val ITEM_IDX_REPLY_LOAD_MORE = 6
        const val ITEM_IDX_SPACE = 7
        const val ITEM_TYPES_SUM = 8
    }
}