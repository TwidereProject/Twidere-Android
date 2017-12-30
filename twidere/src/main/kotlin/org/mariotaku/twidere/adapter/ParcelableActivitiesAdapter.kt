/*
 * Twidere - Twitter client for Android
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

import android.annotation.SuppressLint
import android.arch.paging.PagedList
import android.arch.paging.PagedListAdapterHelper
import android.arch.paging.setPagedListListener
import android.content.Context
import android.support.v7.recyclerview.extensions.ListAdapterConfig
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.ktextension.rangeOfSize
import org.mariotaku.microblog.library.model.twitter.Activity.Action
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.callback.ItemCountsAdapterListUpdateCallback
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.annotation.TimelineStyle
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
import org.mariotaku.twidere.extension.model.activityStatus
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.model.ObjectId
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.placeholder.ParcelableActivityPlaceholder
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.OnLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.paging.DiffCallbacks
import org.mariotaku.twidere.view.holder.ActivityTitleSummaryViewHolder
import org.mariotaku.twidere.view.holder.GapViewHolder
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import org.mariotaku.twidere.view.holder.status.StatusViewHolder
import java.lang.ref.WeakReference
import java.util.*

class ParcelableActivitiesAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager), IActivitiesAdapter,
        IItemCountsAdapter {

    override val itemCounts: ItemCounts = ItemCounts(2)

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

    override val activityEventListener: IActivitiesAdapter.ActivityEventListener?
        get() = eventListener

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = eventListener

    override val mediaPreviewStyle: Int
        get() = statusAdapterDelegate.mediaPreviewStyle

    override val useStarsForLikes: Boolean
        get() = statusAdapterDelegate.useStarsForLikes

    override val mediaPreviewEnabled: Boolean
        get() = statusAdapterDelegate.mediaPreviewEnabled

    override val lightFont: Boolean
        get() = statusAdapterDelegate.lightFont

    override var showAccountsColor: Boolean
        get() = statusAdapterDelegate.showAccountsColor
        set(value) {
            statusAdapterDelegate.showAccountsColor = value
            notifyDataSetChanged()
        }

    val isNameFirst: Boolean
        get() = statusAdapterDelegate.nameFirst

    val activityStartIndex: Int
        get() = getItemStartPosition(ITEM_INDEX_ACTIVITY)

    var activities: PagedList<ParcelableActivity>?
        get() = pagedActivitiesHelper.currentList
        set(value) {
            pagedActivitiesHelper.setList(value)
            gapLoadingIds.clear()
        }

    var activityClickListener: ActivityAdapterListener? = null

    var pagedListListener: ((list: PagedList<ParcelableActivity>?) -> Unit)? = null
        set(value) {
            field = value
            pagedActivitiesHelper.setPagedListListener(value)
        }

    private val inflater = LayoutInflater.from(context)
    private val twidereLinkify = TwidereLinkify(OnLinkClickHandler(context, null, preferences))
    private val statusAdapterDelegate = DummyItemAdapter(context, twidereLinkify, this, requestManager)
    private val eventListener: EventListener
    private val gapLoadingIds: MutableSet<ObjectId<String>> = HashSet()

    private var pagedActivitiesHelper = PagedListAdapterHelper<ParcelableActivity>(ItemCountsAdapterListUpdateCallback(this, ITEM_INDEX_ACTIVITY),
            ListAdapterConfig.Builder<ParcelableActivity>().setDiffCallback(DiffCallbacks.activity).build())

    init {
        eventListener = EventListener(this)
        statusAdapterDelegate.updateOptions()
        setHasStableIds(true)
    }

    override fun isGapItem(position: Int): Boolean {
        return getActivity(position, false).is_gap
    }

    override fun getItemId(position: Int): Long {
        val countIndex = itemCounts.getItemCountIndex(position)
        return when (countIndex) {
            ITEM_INDEX_ACTIVITY -> {
                getRowId(position, false)
            }
            else -> {
                (countIndex.toLong() shl 32) or getItemViewType(position).toLong()
            }
        }
    }

    override fun getActivity(position: Int, raw: Boolean): ParcelableActivity {
        return getActivityInternal(position, raw) ?: ParcelableActivityPlaceholder
    }

    override fun getActivityCount(raw: Boolean): Int {
        return itemCounts[ITEM_INDEX_ACTIVITY]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                val holder = ParcelableStatusesAdapter.createStatusViewHolder(statusAdapterDelegate,
                        inflater, parent, TimelineStyle.PLAIN)
                holder.setStatusClickListener(eventListener)
                return holder as RecyclerView.ViewHolder
            }
            ITEM_VIEW_TYPE_TITLE_SUMMARY -> {
                val view = inflater.inflate(R.layout.list_item_activity_summary_compact, parent, false)
                val holder = ActivityTitleSummaryViewHolder(view, this)
                holder.setOnClickListeners()
                holder.setupViewOptions()
                return holder
            }
            ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(GapViewHolder.layoutResource, parent, false)
                return GapViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
            ITEM_VIEW_TYPE_STUB -> {
                val view = inflater.inflate(R.layout.list_item_two_line, parent, false)
                return StubViewHolder(view)
            }
        }
        throw UnsupportedOperationException("Unsupported viewType " + viewType)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                val activity = getActivityInternal(position, raw = false) ?: return
                (holder as IStatusViewHolder).display(activity, displayInReplyTo = true)
            }
            ITEM_VIEW_TYPE_TITLE_SUMMARY -> {
                val activity = getActivityInternal(position, raw = false) ?: return
                (holder as ActivityTitleSummaryViewHolder).displayActivity(activity)
            }
            ITEM_VIEW_TYPE_STUB -> {
                val activity = getActivityInternal(position, raw = false) ?: return
                (holder as StubViewHolder).displayActivity(activity)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val activity = getActivityInternal(position, raw = false) ?: return
                val loading = gapLoadingIds.any { it.accountKey == activity.account_key && it.id == activity.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val countIndex = getItemCountIndex(position)
        when (countIndex) {
            ITEM_INDEX_ACTIVITY -> {
                if (isGapItem(position)) {
                    return ITEM_VIEW_TYPE_GAP
                }
                val action = getAction(position)
                when (action) {
                    Action.MENTION, Action.QUOTE, Action.REPLY -> {
                        return ITEM_VIEW_TYPE_STATUS
                    }
                    Action.FOLLOW, Action.FAVORITE, Action.RETWEET,
                    Action.FAVORITED_RETWEET, Action.RETWEETED_RETWEET,
                    Action.RETWEETED_MENTION, Action.FAVORITED_MENTION,
                    Action.LIST_CREATED, Action.LIST_MEMBER_ADDED,
                    Action.MEDIA_TAGGED, Action.RETWEETED_MEDIA_TAGGED,
                    Action.FAVORITED_MEDIA_TAGGED, Action.JOINED_TWITTER -> {
                        return ITEM_VIEW_TYPE_TITLE_SUMMARY
                    }
                }
                return ITEM_VIEW_TYPE_STUB
            }
            ITEM_INDEX_LOAD_MORE_INDICATOR -> {
                return ITEM_VIEW_TYPE_LOAD_INDICATOR
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

    override fun updateItemCounts() {
        itemCounts[1] = if (LoadMorePosition.END in loadMoreIndicatorPosition) 1 else 0
    }

    fun setListener(listener: ActivityAdapterListener) {
        activityClickListener = listener
    }

    fun isActivity(position: Int, raw: Boolean = false): Boolean {
        return position < getActivityCount(raw)
    }

    fun findPositionBySortTimestamp(timestamp: Long, raw: Boolean = false): Int {
        if (timestamp <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(activityStartIndex, getActivityCount(raw))
        if (range.isEmpty()) return RecyclerView.NO_POSITION
        if (timestamp < getTimestamp(range.last, raw)) {
            return range.last
        }
        return range.indexOfFirst { timestamp >= getTimestamp(it, raw) }
    }

    fun getTimestamp(adapterPosition: Int, raw: Boolean = false): Long {
        return getActivity(adapterPosition, raw).timestamp
    }

    fun getAction(adapterPosition: Int, raw: Boolean = false): String? {
        return getActivity(adapterPosition, raw).action
    }

    fun getRowId(adapterPosition: Int, raw: Boolean = false): Long {
        return getActivity(adapterPosition, raw)._id
    }

    private fun getActivityInternal(position: Int, raw: Boolean): ParcelableActivity? {
        val dataPosition = position - activityStartIndex
        val activityCount = getActivityCount(raw)
        if (dataPosition < 0 || dataPosition >= activityCount) {
            val validRange = rangeOfSize(activityStartIndex, getActivityCount(raw))
            throw IndexOutOfBoundsException("index: $position, valid range is $validRange")
        }
        return activities?.get(dataPosition)
    }

    interface ActivityAdapterListener {
        fun onGapClick(holder: GapViewHolder, position: Int)

        fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int)

        fun onStatusActionClick(holder: IStatusViewHolder, id: Int, position: Int)

        fun onStatusActionLongClick(holder: IStatusViewHolder, id: Int, position: Int): Boolean

        fun onStatusMenuClick(holder: IStatusViewHolder, menuView: View, position: Int)

        fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, position: Int)

        fun onStatusClick(holder: IStatusViewHolder, position: Int)

        fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean

        fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int)

    }

    internal class StubViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val text1 = itemView.findViewById<TextView>(android.R.id.text1)
        internal val text2 = itemView.findViewById<TextView>(android.R.id.text2)

        init {
            text2.setSingleLine(false)
        }

        @SuppressLint("SetTextI18n")
        fun displayActivity(activity: ParcelableActivity) {
            text1.text = text1.resources.getString(R.string.unsupported_activity_action_title,
                    activity.action)
            text2.text = "host: ${activity.account_key.host}, id: ${activity.id}\n"
            text2.append(itemView.context.getString(R.string.unsupported_activity_action_summary))
        }
    }

    internal class EventListener(adapter: ParcelableActivitiesAdapter) :
            IStatusViewHolder.StatusClickListener, IGapSupportedAdapter.GapClickListener,
            IActivitiesAdapter.ActivityEventListener {

        val adapterRef = WeakReference(adapter)

        override fun onGapClick(holder: GapViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            val activity = adapter.getActivity(position)
            adapter.addGapLoadingId(ObjectId(activity.account_key, activity.id))
            adapter.activityClickListener?.onGapClick(holder, position)
        }

        override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
            val listener = adapterRef.get()?.activityClickListener ?: return
            listener.onStatusActionClick(holder as IStatusViewHolder, id, position)
        }

        override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
            val listener = adapterRef.get()?.activityClickListener ?: return false
            return listener.onStatusActionLongClick(holder as IStatusViewHolder, id, position)
        }

        override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            val status = adapter.getActivity(position).activityStatus ?: return
            IntentUtils.openUserProfile(adapter.context, status.account_key, status.user_key,
                    status.user_screen_name, status.extras?.user_statusnet_profile_url,
                    adapter.preferences[newDocumentApiKey], null)
        }

        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityClickListener?.onStatusClick(holder, position)
        }

        override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
            val listener = adapterRef.get()?.activityClickListener ?: return false
            return listener.onStatusLongClick(holder, position)
        }


        override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityClickListener?.onQuotedStatusClick(holder, position)
        }

        override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityClickListener?.onMediaClick(holder, view, current, statusPosition)
        }

        override fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityClickListener?.onActivityClick(holder, position)
        }

        override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityClickListener?.onStatusMenuClick(holder as StatusViewHolder, menuView, position)
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_STUB = 0
        const val ITEM_VIEW_TYPE_GAP = 1
        const val ITEM_VIEW_TYPE_LOAD_INDICATOR = 2
        const val ITEM_VIEW_TYPE_TITLE_SUMMARY = 3
        const val ITEM_VIEW_TYPE_STATUS = 4

        const val ITEM_INDEX_ACTIVITY = 0
        const val ITEM_INDEX_LOAD_MORE_INDICATOR = 1

    }
}
