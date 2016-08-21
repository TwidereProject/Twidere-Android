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

package org.mariotaku.twidere.view.holder

import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_user.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.*
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.util.UserKeyUtils
import org.mariotaku.twidere.util.Utils.getLocalizedNumber
import org.mariotaku.twidere.util.Utils.getUserTypeIconRes
import org.mariotaku.twidere.view.NameView
import org.mariotaku.twidere.view.iface.IColorLabelView
import java.util.*

class UserViewHolder(
        private val adapter: IUsersAdapter<*>,
        itemView: View
) : ViewHolder(itemView), OnClickListener, OnLongClickListener {

    private val itemContent: IColorLabelView
    val profileImageView: ImageView
    val profileTypeView: ImageView
    private val nameView: NameView
    private val externalIndicator: TextView
    private val descriptionView: TextView
    private val locationView: TextView
    private val urlView: TextView
    private val statusesCountView: TextView
    private val followersCountView: TextView
    private val friendsCountView: TextView

    private val acceptRequestButton: ImageButton
    private val denyRequestButton: ImageButton
    private val unblockButton: ImageButton
    private val unmuteButton: ImageButton
    private val followButton: ImageButton
    private val actionsProgressContainer: View
    private val actionsContainer: View
    private val processingRequestProgress: View

    private var userClickListener: UserClickListener? = null
    private var requestClickListener: RequestClickListener? = null
    private var friendshipClickListener: FriendshipClickListener? = null

    init {
        itemContent = itemView.findViewById(R.id.itemContent) as IColorLabelView
        profileImageView = itemView.findViewById(R.id.profileImage) as ImageView
        profileTypeView = itemView.findViewById(R.id.profileType) as ImageView
        nameView = itemView.findViewById(R.id.name) as NameView
        externalIndicator = itemView.findViewById(R.id.externalIndicator) as TextView
        descriptionView = itemView.findViewById(R.id.description) as TextView
        locationView = itemView.findViewById(R.id.location) as TextView
        urlView = itemView.findViewById(R.id.url) as TextView
        statusesCountView = itemView.findViewById(R.id.statusesCount) as TextView
        followersCountView = itemView.findViewById(R.id.followersCount) as TextView
        friendsCountView = itemView.findViewById(R.id.friendsCount) as TextView
        actionsProgressContainer = itemView.findViewById(R.id.actionsProgressContainer)
        actionsContainer = itemView.findViewById(R.id.actionsContainer)
        acceptRequestButton = itemView.acceptRequest
        denyRequestButton = itemView.denyRequest
        unblockButton = itemView.unblock
        unmuteButton = itemView.unmute
        followButton = itemView.follow
        processingRequestProgress = itemView.findViewById(R.id.processingRequest)
    }

    fun displayUser(user: ParcelableUser) {

        val context = adapter.context
        val loader = adapter.mediaLoader
        val manager = adapter.userColorNameManager
        val twitter = adapter.twitterWrapper


        itemContent.drawStart(manager.getUserColor(user.key))

        val userTypeRes = getUserTypeIconRes(user.is_verified, user.is_protected)
        if (userTypeRes != 0) {
            profileTypeView.setImageResource(userTypeRes)
        } else {
            profileTypeView.setImageDrawable(null)
        }
        nameView.setName(manager.getUserNickname(user.key, user.name))
        nameView.setScreenName("@${user.screen_name}")
        nameView.updateText(adapter.bidiFormatter)
        descriptionView.visibility = if (TextUtils.isEmpty(user.description_unescaped)) View.GONE else View.VISIBLE
        descriptionView.text = user.description_unescaped
        locationView.visibility = if (TextUtils.isEmpty(user.location)) View.GONE else View.VISIBLE
        locationView.text = user.location
        urlView.visibility = if (TextUtils.isEmpty(user.url_expanded)) View.GONE else View.VISIBLE
        urlView.text = user.url_expanded
        val locale = Locale.getDefault()
        statusesCountView.text = getLocalizedNumber(locale, user.statuses_count)
        followersCountView.text = getLocalizedNumber(locale, user.followers_count)
        friendsCountView.text = getLocalizedNumber(locale, user.friends_count)
        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            loader.displayProfileImage(profileImageView, user)
        } else {
            profileImageView.visibility = View.GONE
            loader.cancelDisplayTask(profileImageView)
        }

        if (twitter.isUpdatingRelationship(user.account_key, user.key)) {
            processingRequestProgress.visibility = View.VISIBLE
            actionsContainer.visibility = View.GONE
        } else {
            processingRequestProgress.visibility = View.GONE
            actionsContainer.visibility = View.VISIBLE
        }
        if (UserKeyUtils.isSameHost(user.account_key, user.key)) {
            externalIndicator.visibility = View.GONE
        } else {
            externalIndicator.visibility = View.VISIBLE
            externalIndicator.text = context.getString(R.string.external_user_host_format, user.key.host)
        }

        followButton.setImageResource(if (user.is_following)
            R.drawable.ic_action_confirm
        else
            R.drawable.ic_action_add)
        followButton.isActivated = user.is_following

        val isMySelf = user.account_key == user.key

        if (requestClickListener != null && !isMySelf) {
            acceptRequestButton.visibility = View.VISIBLE
            denyRequestButton.visibility = View.VISIBLE
        } else {
            acceptRequestButton.visibility = View.GONE
            denyRequestButton.visibility = View.GONE
        }
        if (friendshipClickListener != null && !isMySelf) {
            if (user.extras?.blocking ?: false) {
                followButton.visibility = View.GONE
                unblockButton.visibility = View.VISIBLE
            } else {
                followButton.visibility = View.VISIBLE
                unblockButton.visibility = View.GONE
            }
            unmuteButton.visibility = if (user.extras?.muting ?: false) View.VISIBLE else View.GONE
        } else {
            followButton.visibility = View.GONE
            unblockButton.visibility = View.GONE
            unmuteButton.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemContent -> {
                userClickListener?.onUserClick(this, layoutPosition)
            }
            R.id.acceptRequest -> {
                requestClickListener?.onAcceptClicked(this, layoutPosition)
            }
            R.id.denyRequest -> {
                requestClickListener?.onDenyClicked(this, layoutPosition)
            }
            R.id.follow -> {
                friendshipClickListener?.onFollowClicked(this, layoutPosition)
            }
            R.id.unblock -> {
                friendshipClickListener?.onUnblockClicked(this, layoutPosition)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.itemContent -> {
                return userClickListener?.onUserLongClick(this, layoutPosition) ?: false
            }
        }
        return false
    }

    fun setOnClickListeners() {
        setUserClickListener(adapter.userClickListener)
        setActionClickListeners(adapter.requestClickListener, adapter.followClickListener)
    }

    private fun setActionClickListeners(requestClickListener: RequestClickListener?,
                                        friendshipClickListener: FriendshipClickListener?) {
        this.requestClickListener = requestClickListener
        this.friendshipClickListener = friendshipClickListener
        if (requestClickListener != null || friendshipClickListener != null) {
            nameView.setTwoLine(true)
            actionsProgressContainer.visibility = View.VISIBLE
        } else {
            nameView.setTwoLine(false)
            actionsProgressContainer.visibility = View.GONE
        }
        nameView.updateText()
        acceptRequestButton.setOnClickListener(this)
        denyRequestButton.setOnClickListener(this)
        followButton.setOnClickListener(this)
        unblockButton.setOnClickListener(this)
        unmuteButton.setOnClickListener(this)
    }

    fun setTextSize(textSize: Float) {
        descriptionView.textSize = textSize
        externalIndicator.textSize = textSize
        nameView.setPrimaryTextSize(textSize)
        nameView.setSecondaryTextSize(textSize * 0.75f)
        locationView.textSize = textSize
        urlView.textSize = textSize
        statusesCountView.textSize = textSize
        followersCountView.textSize = textSize
        friendsCountView.textSize = textSize
    }

    fun setUserClickListener(listener: UserClickListener?) {
        userClickListener = listener
        (itemContent as View).setOnClickListener(this)
        itemContent.setOnLongClickListener(this)
    }

    fun setupViewOptions() {
        setTextSize(adapter.textSize)
    }

}
