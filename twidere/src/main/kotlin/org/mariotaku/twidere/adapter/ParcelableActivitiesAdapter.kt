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

import android.content.Context
import android.support.v4.widget.Space
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.ktextension.safeMoveToPosition
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import org.mariotaku.twidere.fragment.CursorActivitiesFragment
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableActivityCursorIndices
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.model.util.getActivityStatus
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.MediaLoadingHandler
import org.mariotaku.twidere.util.OnLinkClickHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.*
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import java.lang.ref.WeakReference

/**
 * Created by mariotaku on 15/1/3.
 */
class ParcelableActivitiesAdapter(
        context: Context,
        private val byFriends: Boolean
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context), IActivitiesAdapter<List<ParcelableActivity>> {

    private val inflater: LayoutInflater
    override val mediaLoadingHandler: MediaLoadingHandler
    private val statusAdapterDelegate: DummyItemAdapter
    private val eventListener: EventListener
    private var data: List<ParcelableActivity>? = null
    private var activityAdapterListener: ActivityAdapterListener? = null
    private var filteredUserIds: Array<UserKey>? = null
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

    init {
        statusAdapterDelegate = DummyItemAdapter(context,
                TwidereLinkify(OnLinkClickHandler(context, null, preferences)), this)
        inflater = LayoutInflater.from(context)
        mediaLoadingHandler = MediaLoadingHandler(R.id.media_preview_progress)
        eventListener = EventListener(this)
        statusAdapterDelegate.updateOptions()
    }

    override fun isGapItem(position: Int): Boolean {
        val dataPosition = position - activityStartIndex
        val activityCount = activityCount
        if (dataPosition < 0 || dataPosition >= activityCount) return false
        // Don't show gap if it's last item
        if (dataPosition == activityCount - 1) {
            return false
        }
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.moveToPosition(dataPosition)) return false
            val indices = (data as ObjectCursor).indices as ParcelableActivityCursorIndices
            return cursor.getShort(indices.is_gap).toInt() == 1
        }
        return data!![dataPosition].is_gap
    }

    override fun getItemId(position: Int): Long {
        val dataPosition = position - activityStartIndex
        if (dataPosition < 0 || dataPosition >= activityCount) return RecyclerView.NO_ID
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.moveToPosition(dataPosition)) return -1
            val indices = (data as ObjectCursor).indices as ParcelableActivityCursorIndices
            val accountKey = UserKey.valueOf(cursor.getString(indices.account_key))
            val timestamp = cursor.getLong(indices.timestamp)
            val maxPosition = cursor.getLong(indices.max_position)
            val minPosition = cursor.getLong(indices.min_position)
            return ParcelableActivity.calculateHashCode(accountKey, timestamp, maxPosition,
                    minPosition).toLong()
        }
        return data!![dataPosition].hashCode().toLong()
    }

    fun getActivityAction(adapterPosition: Int): String? {
        val dataPosition = adapterPosition - activityStartIndex
        if (dataPosition < 0 || dataPosition >= activityCount) return null
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return null
            val indices = (data as ObjectCursor).indices as ParcelableActivityCursorIndices
            return cursor.getString(indices.action)
        }
        return data!![dataPosition].action
    }

    fun getTimestamp(adapterPosition: Int): Long {
        val dataPosition = adapterPosition - activityStartIndex
        if (dataPosition < 0 || dataPosition >= activityCount) return RecyclerView.NO_ID
        if (data is ObjectCursor) {
            val cursor = (data as ObjectCursor).cursor
            if (!cursor.safeMoveToPosition(dataPosition)) return -1
            val indices = (data as ObjectCursor).indices as ParcelableActivityCursorIndices
            return cursor.getLong(indices.timestamp)
        }
        return data!![dataPosition].timestamp
    }

    override fun getActivity(position: Int): ParcelableActivity? {
        val dataPosition = position - activityStartIndex
        if (dataPosition < 0 || dataPosition >= activityCount) return null
        return data!![dataPosition]
    }

    override val activityCount: Int
        get() {
            if (data == null) return 0
            return data!!.size
        }

    protected fun bindTitleSummaryViewHolder(holder: ActivityTitleSummaryViewHolder, position: Int) {
        holder.displayActivity(getActivity(position)!!, byFriends)
    }

    fun getData(): List<ParcelableActivity>? {
        return data
    }

    override fun setData(data: List<ParcelableActivity>?) {
        if (data is CursorActivitiesFragment.CursorActivitiesLoader.ActivityCursor) {
            filteredUserIds = data.filteredUserIds
        }
        this.data = data
        notifyDataSetChanged()
    }

    override val activityEventListener: IActivitiesAdapter.ActivityEventListener?
        get() = eventListener

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = eventListener

    override val profileImageStyle: Int
        get() = statusAdapterDelegate.profileImageStyle

    override val mediaPreviewStyle: Int
        get() = statusAdapterDelegate.mediaPreviewStyle

    override val textSize: Float
        get() = statusAdapterDelegate.textSize

    val isNameFirst: Boolean
        get() = statusAdapterDelegate.nameFirst

    override val profileImageEnabled: Boolean
        get() = statusAdapterDelegate.profileImageEnabled

    override val isShowAbsoluteTime: Boolean
        get() = statusAdapterDelegate.isShowAbsoluteTime

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
                val view = inflater.inflate(R.layout.card_item_activity_summary_compact, parent, false)
                val holder = ActivityTitleSummaryViewHolder(this,
                        view)
                holder.setOnClickListeners()
                holder.setTextSize(textSize)
                return holder
            }
            ITEM_VIEW_TYPE_GAP -> {
                val view = inflater.inflate(R.layout.card_item_gap, parent, false)
                return GapViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.card_item_load_indicator, parent, false)
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
                val status = getActivity(position)?.getActivityStatus() ?: return
                val statusViewHolder = holder as IStatusViewHolder
                statusViewHolder.displayStatus(status, true, true)
            }
            ITEM_VIEW_TYPE_TITLE_SUMMARY -> {
                bindTitleSummaryViewHolder(holder as ActivityTitleSummaryViewHolder, position)
            }
            ITEM_VIEW_TYPE_STUB -> {
                (holder as StubViewHolder).displayActivity(getActivity(position)!!)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START != 0L && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        } else if (position == activityCount) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP
        }
        val action = getActivityAction(position)!!
        val activity = getActivity(position)!!
        when (action) {
            Activity.Action.MENTION -> {
                if (ArrayUtils.isEmpty(activity.target_object_statuses)) {
                    return ITEM_VIEW_TYPE_STUB
                }
                return ITEM_VIEW_TYPE_STATUS
            }
            Activity.Action.REPLY -> {
                if (ArrayUtils.isEmpty(activity.target_statuses)) {
                    return ITEM_VIEW_TYPE_STUB
                }
                return ITEM_VIEW_TYPE_STATUS
            }
            Activity.Action.QUOTE -> {
                if (ArrayUtils.isEmpty(activity.target_statuses)) {
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
                if (filteredUserIds != null) {
                    ParcelableActivityUtils.initAfterFilteredSourceIds(activity, filteredUserIds!!, followingOnly)
                    if (activity.after_filtered_source_ids.isEmpty()) {
                        return ITEM_VIEW_TYPE_EMPTY
                    }
                }
                return ITEM_VIEW_TYPE_TITLE_SUMMARY
            }
        }
        return ITEM_VIEW_TYPE_STUB
    }


    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = 0
        if (position and ILoadMoreSupportAdapter.START != 0L) {
            count += 1
        }
        count += activityCount
        if (position and ILoadMoreSupportAdapter.END != 0L) {
            count += 1
        }
        return count
    }

    fun setListener(listener: ActivityAdapterListener) {
        activityAdapterListener = listener
    }

    override val mediaPreviewEnabled: Boolean
        get() = statusAdapterDelegate.mediaPreviewEnabled

    override var showAccountsColor: Boolean
        get() = statusAdapterDelegate.showAccountsColor
        set(value) {
            statusAdapterDelegate.showAccountsColor = value
            notifyDataSetChanged()
        }


    fun isActivity(position: Int): Boolean {
        return position < activityCount
    }

    val activityStartIndex: Int
        get() {
            val position = loadMoreIndicatorPosition
            var start = 0
            if (position and ILoadMoreSupportAdapter.START != 0L) {
                start += 1
            }
            return start
        }


    interface ActivityAdapterListener {
        fun onGapClick(holder: GapViewHolder, position: Int)

        fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int)

        fun onStatusActionClick(holder: IStatusViewHolder, id: Int, position: Int)

        fun onStatusMenuClick(holder: IStatusViewHolder, menuView: View, position: Int)

        fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, position: Int)

        fun onStatusClick(holder: IStatusViewHolder, position: Int)

    }

    internal class StubViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val text1: TextView
        val text2: TextView

        init {
            text1 = itemView.findViewById(android.R.id.text1) as TextView
            text2 = itemView.findViewById(android.R.id.text2) as TextView

            text2.setSingleLine(false)
        }

        fun displayActivity(activity: ParcelableActivity) {
            text1.text = text1.resources.getString(R.string.unsupported_activity_action_title,
                    activity.action)
            text2.text = "host: ${activity.account_key.host}, id: ${activity.status_id}\n"
            text2.append(itemView.context.getString(R.string.unsupported_activity_action_summary))
        }
    }

    internal class EventListener(adapter: ParcelableActivitiesAdapter) : IStatusViewHolder.StatusClickListener, IGapSupportedAdapter.GapClickListener, IActivitiesAdapter.ActivityEventListener {

        val adapterRef: WeakReference<ParcelableActivitiesAdapter>

        init {
            adapterRef = WeakReference(adapter)
        }

        override fun onGapClick(holder: GapViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            if (adapter.activityAdapterListener != null) {
                adapter.activityAdapterListener!!.onGapClick(holder, position)
            }
        }

        override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
            val adapter = adapterRef.get() ?: return
            if (adapter.activityAdapterListener != null) {
                adapter.activityAdapterListener!!.onStatusActionClick(holder as IStatusViewHolder, id, position)
            }
        }

        override fun onStatusLongClick(holder: IStatusViewHolder, position: Int): Boolean {
            return true
        }

        override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            val status = adapter.getActivity(position)?.getActivityStatus() ?: return
            IntentUtils.openUserProfile(adapter.context, status.account_key, status.user_key,
                    status.user_screen_name, null, adapter.preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                    Referral.TIMELINE_STATUS)
        }

        override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            if (adapter.activityAdapterListener != null) {
                adapter.activityAdapterListener!!.onStatusClick(holder, position)
            }
        }

        override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int) {
            val adapter = adapterRef.get() ?: return
            if (adapter.activityAdapterListener != null) {
                adapter.activityAdapterListener!!.onMediaClick(holder, view, media, statusPosition)
            }
        }

        override fun onActivityClick(holder: ActivityTitleSummaryViewHolder, position: Int) {
            val adapter = adapterRef.get() ?: return
            if (adapter.activityAdapterListener == null) return
            adapter.activityAdapterListener!!.onActivityClick(holder, position)
        }

        override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
            val adapter = adapterRef.get() ?: return
            if (adapter.activityAdapterListener != null) {
                adapter.activityAdapterListener!!.onStatusMenuClick(holder as StatusViewHolder, menuView, position)
            }
        }
    }

    companion object {

        val ITEM_VIEW_TYPE_STUB = 0
        val ITEM_VIEW_TYPE_GAP = 1
        val ITEM_VIEW_TYPE_LOAD_INDICATOR = 2
        val ITEM_VIEW_TYPE_TITLE_SUMMARY = 3
        val ITEM_VIEW_TYPE_STATUS = 4
        val ITEM_VIEW_TYPE_EMPTY = 5
    }
}
