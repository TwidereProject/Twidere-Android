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
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_user.view.*
import org.mariotaku.ktextension.`true`
import org.mariotaku.ktextension.hideIfEmpty
import org.mariotaku.ktextension.spannable
import org.mariotaku.ktextension.toString
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.*
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.hasSameHost
import org.mariotaku.twidere.extension.model.urlDisplay
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.placeholder.PlaceholderObject
import org.mariotaku.twidere.promise.FriendshipPromises
import org.mariotaku.twidere.util.Utils.getUserTypeIconRes
import org.mariotaku.twidere.view.holder.status.StatusViewHolder
import java.util.*

class UserViewHolder(
        itemView: View,
        private val adapter: IUsersAdapter,
        private val simple: Boolean = false,
        private val showFollow: Boolean = false
) : ViewHolder(itemView) {

    var showCheckbox: Boolean = false

    private val itemContent = itemView.itemContent
    private val profileImageView = itemView.profileImage
    private val profileTypeView = itemView.profileType
    private val nameView = itemView.name
    private val externalIndicator = itemView.externalIndicator
    private val descriptionView = itemView.description
    private val locationView = itemView.location
    private val urlView = itemView.url
    private val statusesCountView = itemView.statusesCount
    private val followersCountView = itemView.followersCount
    private val friendsCountView = itemView.friendsCount

    private val acceptRequestButton = itemView.acceptRequest
    private val denyRequestButton = itemView.denyRequest
    private val unblockButton = itemView.unblock
    private val unmuteButton = itemView.unmute
    private val followButton = itemView.follow
    private val processingRequestProgress = itemView.processingRequest
    private val countsContainer = itemView.countsContainer
    private val checkBoxSpace = itemView.checkBoxSpace
    private val checkBox = itemView.checkBox

    private val eventHandler = EventHandler()

    private var userClickListener: UserClickListener? = null
    private var requestClickListener: RequestClickListener? = null
    private var friendshipClickListener: FriendshipClickListener? = null

    init {
        if (simple) {
            externalIndicator.visibility = View.GONE
            descriptionView.visibility = View.GONE
            locationView.visibility = View.GONE
            urlView.visibility = View.GONE
            countsContainer.visibility = View.GONE
        }
    }

    fun display(user: ParcelableUser, selectionState: ParcelableUsersAdapter.SelectionState? = null) {
        if (user is PlaceholderObject) {
            placeholder()
            return
        }
        val context = itemView.context
        val manager = adapter.userColorNameManager

        itemContent.drawStart(manager.getUserColor(user.key))

        val userTypeRes = getUserTypeIconRes(user.is_verified, user.is_protected)
        if (userTypeRes != 0) {
            profileTypeView.setImageResource(userTypeRes)
        } else {
            profileTypeView.setImageDrawable(null)
        }
        nameView.placeholder = false
        nameView.name = manager.getUserNickname(user.key, user.name)
        nameView.screenName = "@${user.screen_name}"
        nameView.updateText(adapter.bidiFormatter)

        if (adapter.profileImageEnabled) {
            profileImageView.setVisible(true)
            adapter.requestManager.loadProfileImage(context, user, adapter.profileImageStyle,
                    profileImageView.cornerRadius, profileImageView.cornerRadiusRatio,
                    adapter.profileImageSize).into(profileImageView)
        } else {
            profileImageView.setVisible(false)
        }

        val accountKey = user.account_key
        val showRelationshipButtons: Boolean
        if (accountKey != null && FriendshipPromises.isRunning(accountKey, user.key)) {
            processingRequestProgress.setVisible(true)
            showRelationshipButtons = false
        } else {
            processingRequestProgress.setVisible(false)
            showRelationshipButtons = true
        }
        if (accountKey != null && user.key.hasSameHost(accountKey)) {
            externalIndicator.setVisible(false)
        } else {
            externalIndicator.setVisible(true)
            externalIndicator.text = context.getString(R.string.external_user_host_format, user.key.host)
        }

        followButton.setImageResource(if (user.is_following) R.drawable.ic_action_confirm else R.drawable.ic_action_add)
        followButton.isActivated = user.is_following

        val isMySelf = accountKey == user.key

        if (showRelationshipButtons && requestClickListener != null && !isMySelf) {
            acceptRequestButton.setVisible(true)
            denyRequestButton.setVisible(true)
        } else {
            acceptRequestButton.setVisible(false)
            denyRequestButton.setVisible(false)
        }
        if (showRelationshipButtons && friendshipClickListener != null && !isMySelf) {
            if (user.extras?.blocking == true) {
                followButton.setVisible(false)
                unblockButton.setVisible(true)
            } else {
                followButton.setVisible(showFollow)
                unblockButton.setVisible(false)
            }
            unmuteButton.setVisible(user.extras?.muting == true)
        } else {
            followButton.setVisible(false)
            unblockButton.setVisible(false)
            unmuteButton.setVisible(false)
        }

        if (!simple) {
            descriptionView.spannable = user.description_unescaped
            descriptionView.hideIfEmpty()
            locationView.spannable = user.location
            locationView.hideIfEmpty()
            urlView.spannable = user.urlDisplay
            urlView.hideIfEmpty()
            val locale = Locale.getDefault()
            statusesCountView.text = user.statuses_count.toString(locale)
            followersCountView.text = user.followers_count.toString(locale)
            friendsCountView.text = user.friends_count.toString(locale)
        }

        checkBox.setVisible(showCheckbox)
        checkBoxSpace.setVisible(showCheckbox)

        checkBox.isChecked = selectionState?.checked == `true`
        checkBox.isEnabled = selectionState?.locked != true
    }


    fun setOnClickListeners() {
        setUserClickListener(adapter.userClickListener)
        setActionClickListeners(adapter.requestClickListener, adapter.friendshipClickListener)
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

        nameView.updateTextAppearance()
    }

    fun setUserClickListener(listener: UserClickListener?) {
        userClickListener = listener
        (itemContent as View).setOnClickListener(eventHandler)
        itemContent.setOnLongClickListener(eventHandler)
    }

    fun setupViewOptions() {
        profileImageView.style = adapter.profileImageStyle
        setTextSize(adapter.textSize)
    }

    fun toggleChecked(): Boolean {
        checkBox.toggle()
        return checkBox.isChecked
    }

    fun placeholder() {
        Glide.clear(profileImageView)
        profileImageView.setImageDrawable(null)
        profileTypeView.visibility = View.GONE

        nameView.placeholder = true
        nameView.updateText()

        if (!simple) {
            descriptionView.text = StatusViewHolder.placeholderText
            descriptionView.setVisible(true)
        } else {
            descriptionView.setVisible(false)
        }

        externalIndicator.setVisible(false)
        locationView.setVisible(false)
        urlView.setVisible(false)

        countsContainer.setVisible(false)

        processingRequestProgress.setVisible(false)
        acceptRequestButton.setVisible(false)
        denyRequestButton.setVisible(false)
        followButton.setVisible(false)
        unblockButton.setVisible(false)
        unmuteButton.setVisible(false)
    }

    private fun RelativeLayout.LayoutParams.clearVerticalRules() {
        intArrayOf(RelativeLayout.ABOVE, RelativeLayout.BELOW, RelativeLayout.ALIGN_BASELINE,
                RelativeLayout.ALIGN_TOP, RelativeLayout.ALIGN_BOTTOM).forEach { verb ->
            addRule(verb, 0)
        }
    }

    private fun setActionClickListeners(requestClickListener: RequestClickListener?,
            friendshipClickListener: FriendshipClickListener?) {
        this.requestClickListener = requestClickListener
        this.friendshipClickListener = friendshipClickListener
        if (requestClickListener != null || friendshipClickListener != null) {
            processingRequestProgress.visibility = View.VISIBLE
        } else {
            processingRequestProgress.visibility = View.GONE
        }
        nameView.updateText()
        acceptRequestButton.setOnClickListener(eventHandler)
        denyRequestButton.setOnClickListener(eventHandler)
        followButton.setOnClickListener(eventHandler)
        unblockButton.setOnClickListener(eventHandler)
        unmuteButton.setOnClickListener(eventHandler)
    }

    private inner class EventHandler : OnClickListener, OnLongClickListener {

        override fun onClick(v: View) {
            when (v.id) {
                R.id.itemContent -> {
                    userClickListener?.onUserClick(this@UserViewHolder, layoutPosition)
                }
                R.id.acceptRequest -> {
                    requestClickListener?.onAcceptClicked(this@UserViewHolder, layoutPosition)
                }
                R.id.denyRequest -> {
                    requestClickListener?.onDenyClicked(this@UserViewHolder, layoutPosition)
                }
                R.id.follow -> {
                    friendshipClickListener?.onFollowClicked(this@UserViewHolder, layoutPosition)
                }
                R.id.unblock -> {
                    friendshipClickListener?.onUnblockClicked(this@UserViewHolder, layoutPosition)
                }
                R.id.unmute -> {
                    friendshipClickListener?.onUnmuteClicked(this@UserViewHolder, layoutPosition)
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            when (v.id) {
                R.id.itemContent -> {
                    return userClickListener?.onUserLongClick(this@UserViewHolder, layoutPosition) ?: false
                }
            }
            return false
        }
    }

}
