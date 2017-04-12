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
import android.content.Context
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.model.id
import org.mariotaku.twidere.fragment.CursorActivitiesFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.model.util.getActivityStatus
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.OnLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.*
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by mariotaku on 15/1/3.
 */
class ParcelableActivitiesAdapter(
        context: Context,
        requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager),
        IActivitiesAdapter<List<ParcelableActivity>>, IItemCountsAdapter {

    override val itemCounts: ItemCounts = ItemCounts(2)

    private val inflater = LayoutInflater.from(context)
    private val twidereLinkify = TwidereLinkify(OnLinkClickHandler(context, null, preferences))
    private val statusAdapterDelegate = DummyItemAdapter(context, twidereLinkify, this, requestManager)
    private val eventListener: EventListener
    private var data: List<ParcelableActivity>? = null
    private var activityAdapterListener: ActivityAdapterListener? = null
    private var filteredUserKeys: Array<UserKey>? = null
    private val gapLoadingIds: MutableSet<ObjectId> = HashSet()

    var followingOnly: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var mentionsOnly: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
        eventListener = EventListener(this)
        statusAdapterDelegate.updateOptions()
    }

    override fun isGapItem(position: Int): Boolean {
        val dataPosition = position - activityStartIndex
        val activityCount = getActivityCount(false)
        if (dataPosition < 0 || dataPosition >= activityCount) return false
        // Don't show gap if it's last item
        if (dataPosition == activityCount - 1) {
            return false
        }
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.moveToPosition(dataPosition)) return false
            val indices = (data as ObjectCursor).indices
            return cursor.getInt(indices[Activities.IS_GAP]) == 1
        }
        return data!![dataPosition].is_gap
    }

    override fun getItemId(position: Int): Long {
        val countIndex = itemCounts.getItemCountIndex(position)
        when (countIndex) {
            ITEM_INDEX_ACTIVITY -> {
                val dataPosition = position - activityStartIndex
                if (data is ObjectCursor) {
                    val cursor = (data as ObjectCursor).cursor
                    if (!cursor.moveToPosition(dataPosition)) return -1
                    val indices = (data as ObjectCursor).indices
                    val accountKey = UserKey.valueOf(cursor.getString(indices[Activities.ACCOUNT_KEY]))
                    val timestamp = cursor.getLong(indices[Activities.TIMESTAMP])
                    val maxPosition = cursor.getLong(indices[Activities.MAX_SORT_POSITION])
                    val minPosition = cursor.getLong(indices[Activities.MIN_SORT_POSITION])
                    return ParcelableActivity.calculateHashCode(accountKey, timestamp, maxPosition,
                            minPosition).toLong()
                }
                return (countIndex.toLong() shl 32) or getActivity(position, false).hashCode().toLong()
            }
            else -> {
                return (countIndex.toLong() shl 32) or getItemViewType(position).toLong()
            }
        }
    }

    fun getTimestamp(adapterPosition: Int, raw: Boolean = false): Long {
        val dataPosition = adapterPosition - activityStartIndex
        if (dataPosition < 0 || dataPosition >= getActivityCount(raw)) return RecyclerView.NO_ID
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return -1
            val indices = (data as ObjectCursor).indices
            return cursor.safeGetLong(indices[Activities.TIMESTAMP])
        }
        return getActivity(adapterPosition, raw).timestamp
    }

    override fun getActivity(position: Int, raw: Boolean): ParcelableActivity {
        val dataPosition = position - activityStartIndex
        val activityCount = getActivityCount(raw)
        if (dataPosition < 0 || dataPosition >= activityCount) {
            val validRange = rangeOfSize(activityStartIndex, getActivityCount(raw))
            throw IndexOutOfBoundsException("index: $position, valid range is $validRange")
        }
        return data!![dataPosition]
    }

    override fun getActivityCount(raw: Boolean): Int {
        if (data == null) return 0
        return data!!.size
    }

    private fun bindTitleSummaryViewHolder(holder: ActivityTitleSummaryViewHolder, position: Int) {
        holder.displayActivity(getActivity(position))
    }

    fun getData(): List<ParcelableActivity>? {
        return data
    }

    override fun setData(data: List<ParcelableActivity>?) {
        if (data is CursorActivitiesFragment.CursorActivitiesLoader.ActivityCursor) {
            filteredUserKeys = data.filteredUserIds
        }
        this.data = data
        gapLoadingIds.clear()
        updateItemCount()
        notifyDataSetChanged()
    }

    override val activityEventListener: IActivitiesAdapter.ActivityEventListener?
        get() = eventListener

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = eventListener

    override val mediaPreviewStyle: Int
        get() = statusAdapterDelegate.mediaPreviewStyle

    val isNameFirst: Boolean
        get() = statusAdapterDelegate.nameFirst

    override val useStarsForLikes: Boolean
        get() = statusAdapterDelegate.useStarsForLikes


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                val holder = ListParcelableStatusesAdapter.createStatusViewHolder(statusAdapterDelegate,
                        inflater, parent)
                holder.setStatusClickListener(eventListener)
                return holder
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
            ITEM_VIEW_TYPE_EMPTY -> {
                return EmptyViewHolder(Space(context))
            }
        }
        throw UnsupportedOperationException("Unsupported viewType " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                val status = getActivity(position).getActivityStatus() ?: return
                val statusViewHolder = holder as IStatusViewHolder
                statusViewHolder.displayStatus(status = status, displayInReplyTo = true)
            }
            ITEM_VIEW_TYPE_TITLE_SUMMARY -> {
                bindTitleSummaryViewHolder(holder as ActivityTitleSummaryViewHolder, position)
            }
            ITEM_VIEW_TYPE_STUB -> {
                (holder as StubViewHolder).displayActivity(getActivity(position))
            }
            ITEM_VIEW_TYPE_GAP -> {
                val activity = getActivity(position)
                val loading = gapLoadingIds.any { it.accountKey == activity.account_key && it.id == activity.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (getItemCountIndex(position)) {
            ITEM_INDEX_ACTIVITY -> {
                if (isGapItem(position)) {
                    return ITEM_VIEW_TYPE_GAP
                }
                val activity = getActivity(position, false)
                when (activity.action) {
                    Activity.Action.MENTION -> {
                        if (activity.target_object_statuses.isNullOrEmpty()) {
                            return ITEM_VIEW_TYPE_STUB
                        }
                        return ITEM_VIEW_TYPE_STATUS
                    }
                    Activity.Action.REPLY -> {
                        if (activity.target_statuses.isNullOrEmpty()) {
                            return ITEM_VIEW_TYPE_STUB
                        }
                        return ITEM_VIEW_TYPE_STATUS
                    }
                    Activity.Action.QUOTE -> {
                        if (activity.target_statuses.isNullOrEmpty()) {
                            return ITEM_VIEW_TYPE_STUB
                        }
                        return ITEM_VIEW_TYPE_STATUS
                    }
                    Activity.Action.FOLLOW, Activity.Action.FAVORITE, Activity.Action.RETWEET,
                    Activity.Action.FAVORITED_RETWEET, Activity.Action.RETWEETED_RETWEET,
                    Activity.Action.RETWEETED_MENTION, Activity.Action.FAVORITED_MENTION,
                    Activity.Action.LIST_CREATED, Activity.Action.LIST_MEMBER_ADDED,
                    Activity.Action.MEDIA_TAGGED, Activity.Action.RETWEETED_MEDIA_TAGGED,
                    Activity.Action.FAVORITED_MEDIA_TAGGED, Activity.Action.JOINED_TWITTER -> {
                        if (mentionsOnly) return ITEM_VIEW_TYPE_EMPTY
                        filteredUserKeys?.let {
                            ParcelableActivityUtils.initAfterFilteredSourceIds(activity, it, followingOnly)
                            if (activity.after_filtered_source_ids.isEmpty()) {
                                return ITEM_VIEW_TYPE_EMPTY
                            }
                        }
                        return ITEM_VIEW_TYPE_TITLE_SUMMARY
                    }
                }
                return ITEM_VIEW_TYPE_STUB
            }
            ITEM_INDEX_LOAD_MORE_INDICATOR -> {
                return ITEM_VIEW_TYPE_LOAD_INDICATOR
            }
        }
        throw UnsupportedOperationException()
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

    fun setListener(listener: ActivityAdapterListener) {
        activityAdapterListener = listener
    }

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


    fun isActivity(position: Int, raw: Boolean = false): Boolean {
        return position < getActivityCount(raw)
    }

    val activityStartIndex: Int
        get() = getItemStartPosition(ITEM_INDEX_ACTIVITY)

    fun findPositionBySortTimestamp(timestamp: Long, raw: Boolean = false): Int {
        if (timestamp <= 0) return RecyclerView.NO_POSITION
        val range = rangeOfSize(activityStartIndex, getActivityCount(raw))
        if (range.isEmpty()) return RecyclerView.NO_POSITION
        if (timestamp < getTimestamp(range.last, raw)) {
            return range.last
        }
        return range.indexOfFirst { timestamp >= getTimestamp(it, raw) }
    }

    private fun updateItemCount() {
        itemCounts[0] = getActivityCount(false)
        itemCounts[1] = if (ILoadMoreSupportAdapter.END in loadMoreIndicatorPosition) 1 else 0
    }

    interface ActivityAdapterListener {
        fun onGapClick(holder: GapViewHolder, position: Int)

        fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int)

        fun onStatusActionClick(holder: IStatusViewHolder, id: Int, position: Int)

        fun onStatusActionLongClick(holder: IStatusViewHolder, id: Int, position: Int): Boolean

        fun onStatusMenuClick(holder: IStatusViewHolder, menuView: View, position: Int)

        fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, position: Int)

        fun onStatusClick(holder: IStatusViewHolder, position: Int)

        fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int)

    }

    internal class StubViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val text1 = itemView.findViewById(android.R.id.text1) as TextView
        internal val text2 = itemView.findViewById(android.R.id.text2) as TextView

        init {

            text2.setSingleLine(false)
        }

        @SuppressLint("SetTextI18n")
        fun displayActivity(activity: ParcelableActivity) {
            text1.text = text1.resources.getString(R.string.unsupported_activity_action_title,
                    activity.action)
            text2.text = "host: ${activity.account_key.host}, id: ${activity.status_id}\n"
            text2.append(itemView.context.getString(R.string.unsupported_activity_action_summary))
        }
    }

    internal class EventListener(adapter: ParcelableActivitiesAdapter) : IStatusViewHolder.StatusClickListener, IGapSupportedAdapter.GapClickListener, IActivitiesAdapter.ActivityEventListener {

        val adapterRef = WeakReference(adapter)

        override fun onGapClick(holder: GapViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            val activity = adapter.getActivity(position)
            adapter.addGapLoadingId(ObjectId(activity.account_key, activity.id))
            adapter.activityAdapterListener?.onGapClick(holder, position)
        }

        override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
            val listener = adapterRef.get()?.activityAdapterListener ?: return
            listener.onStatusActionClick(holder as IStatusViewHolder, id, position)
        }

        override fun onItemActionLongClick(holder: RecyclerView.ViewHolder, id: Int, position: Int): Boolean {
            val listener = adapterRef.get()?.activityAdapterListener ?: return false
            return listener.onStatusActionLongClick(holder as IStatusViewHolder, id, position)
        }

        override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
            return true
        }

        override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            val status = adapter.getActivity(position).getActivityStatus() ?: return
            IntentUtils.openUserProfile(adapter.context, status.account_key, status.user_key,
                    status.user_screen_name, status.extras?.user_statusnet_profile_url,
                    adapter.preferences[newDocumentApiKey], Referral.TIMELINE_STATUS, null)
        }

        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityAdapterListener?.onStatusClick(holder, position)
        }


        override fun onQuotedStatusClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityAdapterListener?.onQuotedStatusClick(holder, position)
        }

        override fun onMediaClick(holder: IStatusViewHolder, view: View, current: ParcelableMedia, statusPosition: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityAdapterListener?.onMediaClick(holder, view, current, statusPosition)
        }

        override fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityAdapterListener?.onActivityClick(holder, position)
        }

        override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
            val adapter = adapterRef.get() ?: return
            adapter.activityAdapterListener?.onStatusMenuClick(holder as StatusViewHolder, menuView, position)
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_STUB = 0
        const val ITEM_VIEW_TYPE_GAP = 1
        const val ITEM_VIEW_TYPE_LOAD_INDICATOR = 2
        const val ITEM_VIEW_TYPE_TITLE_SUMMARY = 3
        const val ITEM_VIEW_TYPE_STATUS = 4
        const val ITEM_VIEW_TYPE_EMPTY = 5

        const val ITEM_INDEX_ACTIVITY = 0
        const val ITEM_INDEX_LOAD_MORE_INDICATOR = 1

    }
}
