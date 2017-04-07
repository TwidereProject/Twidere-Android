package org.mariotaku.twidere.adapter

import android.content.Context
import android.support.v4.text.BidiFormatter
import android.support.v7.widget.RecyclerView
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.getActivityStatus
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder
import javax.inject.Inject

/**
 * Created by mariotaku on 16/1/22.
 */
class DummyItemAdapter(
        val context: Context,
        override val twidereLinkify: TwidereLinkify = TwidereLinkify(null),
        private val adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>? = null,
        override val requestManager: RequestManager
) : IStatusesAdapter<Any>, IUsersAdapter<Any>, IUserListsAdapter<Any> {

    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    override lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    override lateinit var userColorNameManager: UserColorNameManager
    @Inject
    override lateinit var bidiFormatter: BidiFormatter

    override var profileImageSize: String = context.getString(R.string.profile_image_size)
    override var profileImageStyle: Int = 0
    override var mediaPreviewStyle: Int = 0
    override var textSize: Float = 0f
    override var linkHighlightingStyle: Int = 0
    override var nameFirst: Boolean = false
    override var lightFont: Boolean = false
    override var profileImageEnabled: Boolean = false
    override var sensitiveContentEnabled: Boolean = false
    override var mediaPreviewEnabled: Boolean = false
    override var showAbsoluteTime: Boolean = false
    override var friendshipClickListener: IUsersAdapter.FriendshipClickListener? = null
    override var requestClickListener: IUsersAdapter.RequestClickListener? = null
    override var statusClickListener: IStatusViewHolder.StatusClickListener? = null
    override var userClickListener: IUsersAdapter.UserClickListener? = null
    override var showAccountsColor: Boolean = false
    override var useStarsForLikes: Boolean = false
    override var simpleLayout: Boolean = false
    override var showFollow: Boolean = true

    private var showCardActions: Boolean = false
    private var showingActionCardPosition = RecyclerView.NO_POSITION

    init {
        GeneralComponentHelper.build(context).inject(this)
        updateOptions()
    }

    fun setShouldShowAccountsColor(shouldShowAccountsColor: Boolean) {
        this.showAccountsColor = shouldShowAccountsColor
    }


    override fun getItemCount(): Int {
        return 0
    }

    override fun getStatus(position: Int, raw: Boolean): ParcelableStatus {
        if (adapter is ParcelableStatusesAdapter) {
            return adapter.getStatus(position, raw)
        } else if (adapter is VariousItemsAdapter) {
            return adapter.getItem(position) as ParcelableStatus
        } else if (adapter is ParcelableActivitiesAdapter) {
            return adapter.getActivity(position).getActivityStatus()!!
        }
        throw IndexOutOfBoundsException()
    }

    override fun getStatusCount(raw: Boolean) = 0

    override fun getStatusId(position: Int, raw: Boolean) = ""

    override fun getStatusTimestamp(position: Int, raw: Boolean) = -1L

    override fun getStatusPositionKey(position: Int, raw: Boolean) = -1L

    override fun getAccountKey(position: Int, raw: Boolean) = UserKey.INVALID

    override fun findStatusById(accountKey: UserKey, statusId: String) = null

    override fun isCardActionsShown(position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return showCardActions
        return showCardActions || showingActionCardPosition == position
    }

    override fun showCardActions(position: Int) {
        if (showingActionCardPosition != RecyclerView.NO_POSITION && adapter != null) {
            adapter.notifyItemChanged(showingActionCardPosition)
        }
        showingActionCardPosition = position
        if (position != RecyclerView.NO_POSITION && adapter != null) {
            adapter.notifyItemChanged(position)
        }
    }

    override fun getUser(position: Int): ParcelableUser? {
        if (adapter is ParcelableUsersAdapter) {
            return adapter.getUser(position)
        } else if (adapter is VariousItemsAdapter) {
            return adapter.getItem(position) as ParcelableUser
        }
        return null
    }

    override val userCount: Int
        get() = 0

    override val userListsCount: Int
        get() = 0

    override val gapClickListener: IGapSupportedAdapter.GapClickListener?
        get() = null
    override val userListClickListener: IUserListsAdapter.UserListClickListener?
        get() = null

    override fun getUserId(position: Int): String? {
        return null
    }

    override fun getUserList(position: Int): ParcelableUserList? {
        return null
    }

    override fun getUserListId(position: Int): String? {
        return null
    }

    override fun setData(data: Any?): Boolean {
        return false
    }

    override fun isGapItem(position: Int): Boolean {
        return false
    }

    override fun addGapLoadingId(id: ObjectId) {

    }

    override fun removeGapLoadingId(id: ObjectId) {

    }

    fun updateOptions() {
        profileImageStyle = preferences[profileImageStyleKey]
        mediaPreviewStyle = preferences[mediaPreviewStyleKey]
        textSize = preferences[textSizeKey].toFloat()
        nameFirst = preferences[nameFirstKey]
        profileImageEnabled = preferences[displayProfileImageKey]
        mediaPreviewEnabled = preferences[mediaPreviewKey]
        sensitiveContentEnabled = preferences[displaySensitiveContentsKey]
        showCardActions = !preferences[hideCardActionsKey]
        linkHighlightingStyle = preferences[linkHighlightOptionKey]
        lightFont = preferences[lightFontKey]
        useStarsForLikes = preferences[iWantMyStarsBackKey]
        showAbsoluteTime = preferences[showAbsoluteTimeKey]
    }
}
