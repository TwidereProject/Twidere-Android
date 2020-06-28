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
import android.database.CursorIndexOutOfBoundsException
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.contains
import org.mariotaku.ktextension.rangeOfSize
import org.mariotaku.ktextension.safeGetLong
import org.mariotaku.ktextension.safeMoveToPosition
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.exception.UnsupportedCountIndexException
import org.mariotaku.twidere.extension.model.activityStatus
import org.mariotaku.twidere.fragment.CursorActivitiesFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.JsonSerializer
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

    private val inflater = LayoutInflater.from(context)
    private val twidereLinkify = TwidereLinkify(OnLinkClickHandler(context, null, preferences))
    private val statusAdapterDelegate = DummyItemAdapter(context, twidereLinkify, this, requestManager)
    private val eventListener: EventListener
    private var data: List<ParcelableActivity>? = null
    private var activityAdapterListener: ActivityAdapterListener? = null
    private var filteredUserKeys: Array<UserKey>? = null
    private var filteredUserNames: Array<String>? = null
    private var filteredUserDescriptions: Array<String>? = null
    private val gapLoadingIds: MutableSet<ObjectId> = HashSet()
    private val reuseActivity = ParcelableActivity()
    private var infoCache: Array<ActivityInfo?>? = null

    init {
        eventListener = EventListener(this)
        statusAdapterDelegate.updateOptions()
        setHasStableIds(true)
    }

    override fun isGapItem(position: Int): Boolean {
        return getFieldValue(position, readInfoValueAction = {
            it.gap
        }, readStatusValueAction = { activity ->
            activity.is_gap
        }, defValue = false, raw = false)
    }

    override fun getItemId(position: Int): Long {
        return when (val countIndex = itemCounts.getItemCountIndex(position)) {
            ITEM_INDEX_ACTIVITY -> {
                getRowId(position, false)
            }
            else -> {
                (countIndex.toLong() shl 32) or getItemViewType(position).toLong()
            }
        }
    }

    override fun getActivity(position: Int, raw: Boolean): ParcelableActivity {
        return getActivityInternal(position, raw, false)
    }

    override fun getActivityCount(raw: Boolean): Int {
        if (data == null) return 0
        return data!!.size
    }

    override fun setData(data: List<ParcelableActivity>?) {
        if (data is CursorActivitiesFragment.CursorActivitiesLoader.ActivityCursor) {
            filteredUserKeys = data.filteredUserIds
            filteredUserNames = data.filteredUserNames
            filteredUserDescriptions = data.filteredUserDescriptions
        }
        this.data = data
        this.infoCache = if (data != null) arrayOfNulls(data.size) else null
        gapLoadingIds.clear()
        updateItemCount()
        notifyDataSetChanged()
    }

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
        throw UnsupportedOperationException("Unsupported viewType $viewType")
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_STATUS -> {
                val activity = getActivityInternal(position, raw = false, reuse = true)
                (holder as IStatusViewHolder).display(activity, displayInReplyTo = true)
            }
            ITEM_VIEW_TYPE_TITLE_SUMMARY -> {
                val activity = getActivityInternal(position, raw = false, reuse = true)
                val sources = getAfterFilteredSources(position, false)
                activity.after_filtered_sources = sources
                (holder as ActivityTitleSummaryViewHolder).displayActivity(activity)
            }
            ITEM_VIEW_TYPE_STUB -> {
                val activity = getActivityInternal(position, raw = false, reuse = true)
                (holder as StubViewHolder).displayActivity(activity)
            }
            ITEM_VIEW_TYPE_GAP -> {
                val activity = getActivityInternal(position, raw = false, reuse = true)
                val loading = gapLoadingIds.any { it.accountKey == activity.account_key && it.id == activity.id }
                (holder as GapViewHolder).display(loading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (val countIndex = getItemCountIndex(position)) {
            ITEM_INDEX_ACTIVITY -> {
                if (isGapItem(position)) {
                    return ITEM_VIEW_TYPE_GAP
                }
                when (getAction(position)) {
                    Activity.Action.MENTION, Activity.Action.QUOTE, Activity.Action.REPLY -> {
                        return ITEM_VIEW_TYPE_STATUS
                    }
                    Activity.Action.FOLLOW, Activity.Action.FAVORITE, Activity.Action.RETWEET,
                    Activity.Action.FAVORITED_RETWEET, Activity.Action.RETWEETED_RETWEET,
                    Activity.Action.RETWEETED_MENTION, Activity.Action.FAVORITED_MENTION,
                    Activity.Action.LIST_CREATED, Activity.Action.LIST_MEMBER_ADDED,
                    Activity.Action.MEDIA_TAGGED, Activity.Action.RETWEETED_MEDIA_TAGGED,
                    Activity.Action.FAVORITED_MEDIA_TAGGED, Activity.Action.JOINED_TWITTER -> {
                        if (mentionsOnly) return ITEM_VIEW_TYPE_EMPTY
                        val afterFiltered = getAfterFilteredSources(position, false)
                        if (afterFiltered != null && afterFiltered.isEmpty()) {
                            return ITEM_VIEW_TYPE_EMPTY
                        }
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
        return getFieldValue(adapterPosition, readInfoValueAction = {
            it.timestamp
        }, readStatusValueAction = { activity ->
            activity.timestamp
        }, defValue = -1, raw = raw)
    }

    fun getAction(adapterPosition: Int, raw: Boolean = false): String? {
        return getFieldValue(adapterPosition, readInfoValueAction = {
            it.action
        }, readStatusValueAction = { activity ->
            activity.action
        }, defValue = null, raw = raw)
    }

    fun getRowId(adapterPosition: Int, raw: Boolean = false): Long {
        return getFieldValue(adapterPosition, readInfoValueAction = {
            it._id
        }, readStatusValueAction = { activity ->
            activity.hashCode().toLong()
        }, defValue = -1L, raw = raw)
    }

    fun getData(): List<ParcelableActivity>? {
        return data
    }

    private fun updateItemCount() {
        itemCounts[0] = getActivityCount(false)
        itemCounts[1] = if (ILoadMoreSupportAdapter.END in loadMoreIndicatorPosition) 1 else 0
    }

    private fun getActivityInternal(position: Int, raw: Boolean, reuse: Boolean): ParcelableActivity {
        val dataPosition = position - activityStartIndex
        val activityCount = getActivityCount(raw)
        if (dataPosition < 0 || dataPosition >= activityCount) {
            val validRange = rangeOfSize(activityStartIndex, getActivityCount(raw))
            throw IndexOutOfBoundsException("index: $position, valid range is $validRange")
        }
        val data = this.data!!
        return if (reuse && data is ObjectCursor) {
            val activity = data.setInto(dataPosition, reuseActivity)
            activity.after_filtered_sources = null
            activity
        } else {
            data[dataPosition]
        }
    }

    private fun getAfterFilteredSources(position: Int, raw: Boolean): Array<ParcelableLiteUser>? {
        return getFieldValue(position, readInfoValueAction = {
            it.filteredSources
        }, readStatusValueAction = lambda2@ { activity ->
            if (activity.after_filtered_sources != null) return@lambda2 activity.after_filtered_sources
            val sources = ParcelableActivityUtils.filterSources(activity.sources_lite,
                    filteredUserKeys, filteredUserNames, filteredUserDescriptions, followingOnly)
            activity.after_filtered_sources = sources
            return@lambda2 sources
        }, defValue = null, raw = raw)
    }

    private inline fun <T> getFieldValue(position: Int,
            readInfoValueAction: (ActivityInfo) -> T,
            readStatusValueAction: (status: ParcelableActivity) -> T,
            defValue: T, raw: Boolean = false): T {
        val data = this.data
        if (data is ObjectCursor) {
            val dataPosition = position - activityStartIndex
            if (dataPosition < 0 || dataPosition >= getActivityCount(true)) {
                throw CursorIndexOutOfBoundsException("index: $position, valid range is $0..${getActivityCount(true)}")
            }
            val info = infoCache?.get(dataPosition) ?: run {
                val cursor = data.cursor
                if (!cursor.safeMoveToPosition(dataPosition)) return defValue
                val indices = data.indices
                val _id = cursor.safeGetLong(indices[Activities._ID])
                val timestamp = cursor.safeGetLong(indices[Activities.TIMESTAMP])
                val action = cursor.getString(indices[Activities.ACTION])
                val gap = cursor.getInt(indices[Activities.IS_GAP]) == 1
                val sources = cursor.getString(indices[Activities.SOURCES_LITE])?.let {
                    JsonSerializer.parseArray(it, ParcelableLiteUser::class.java)
                }
                val filteredSources = ParcelableActivityUtils.filterSources(sources, filteredUserKeys,
                        filteredUserNames, filteredUserDescriptions, followingOnly)
                val newInfo = ActivityInfo(_id, timestamp, gap, action, filteredSources)
                infoCache?.set(dataPosition, newInfo)
                return@run newInfo
            }
            return readInfoValueAction(info)
        }
        return readStatusValueAction(getActivityInternal(position, raw, false))
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

        internal val text1 = itemView.findViewById<TextView>(android.R.id.text1)
        internal val text2 = itemView.findViewById<TextView>(android.R.id.text2)

        init {
            text2.isSingleLine = false
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
            val status = adapter.getActivity(position).activityStatus ?: return
            IntentUtils.openUserProfile(adapter.context, status.account_key, status.user_key,
                    status.user_screen_name, status.extras?.user_statusnet_profile_url,
                    adapter.preferences[newDocumentApiKey], null)
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

    data class ActivityInfo(
            val _id: Long,
            val timestamp: Long,
            val gap: Boolean,
            val action: String,
            val filteredSources: Array<ParcelableLiteUser>?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ActivityInfo

            if (_id != other._id) return false
            if (timestamp != other.timestamp) return false
            if (gap != other.gap) return false
            if (action != other.action) return false
            if (!Arrays.equals(filteredSources, other.filteredSources)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _id.hashCode()
            result = 31 * result + timestamp.hashCode()
            result = 31 * result + gap.hashCode()
            result = 31 * result + action.hashCode()
            result = 31 * result + (filteredSources?.contentHashCode() ?: 0)
            return result
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
