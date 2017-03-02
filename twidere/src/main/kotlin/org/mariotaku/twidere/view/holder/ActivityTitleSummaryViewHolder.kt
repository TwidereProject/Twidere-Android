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

import android.graphics.PorterDuff
import android.support.v4.view.MarginLayoutParamsCompat
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.mariotaku.ktextension.applyFontFamily
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.ActivityTitleSummaryMessage
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.view.BadgeView
import org.mariotaku.twidere.view.IconActionView
import org.mariotaku.twidere.view.ProfileImageView
import org.mariotaku.twidere.view.ShortTimeView
import org.mariotaku.twidere.view.iface.IColorLabelView

/**
 * Created by mariotaku on 15/1/3.
 */
class ActivityTitleSummaryViewHolder(
        itemView: View,
        private val adapter: ParcelableActivitiesAdapter
) : ViewHolder(itemView), View.OnClickListener {

    private val itemContent = itemView.findViewById(R.id.itemContent) as IColorLabelView
    private val activityTypeView = itemView.findViewById(R.id.activity_type) as IconActionView
    private val titleView = itemView.findViewById(R.id.title) as TextView
    private val summaryView = itemView.findViewById(R.id.summary) as TextView
    private val timeView = itemView.findViewById(R.id.time) as ShortTimeView
    private val profileImagesContainer = itemView.findViewById(R.id.profile_images_container) as ViewGroup
    private val profileImageMoreNumber = itemView.findViewById(R.id.activity_profile_image_more_number) as BadgeView
    private val profileImageViews = arrayOf(
            itemView.findViewById(R.id.activity_profile_image_0) as ProfileImageView,
            itemView.findViewById(R.id.activity_profile_image_1) as ProfileImageView,
            itemView.findViewById(R.id.activity_profile_image_2) as ProfileImageView,
            itemView.findViewById(R.id.activity_profile_image_3) as ProfileImageView,
            itemView.findViewById(R.id.activity_profile_image_4) as ProfileImageView
    )
    private val profileImageSpace: View = itemView.findViewById(R.id.profile_image_space)

    private var activityEventListener: IActivitiesAdapter.ActivityEventListener? = null

    init {
        val resources = adapter.context.resources
        val lp = titleView.layoutParams as ViewGroup.MarginLayoutParams
        val spacing = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
        lp.leftMargin = spacing
        MarginLayoutParamsCompat.setMarginStart(lp, spacing)
        timeView.showAbsoluteTime = adapter.showAbsoluteTime
        titleView.applyFontFamily(adapter.lightFont)
        summaryView.applyFontFamily(adapter.lightFont)
        timeView.applyFontFamily(adapter.lightFont)
    }

    fun displayActivity(activity: ParcelableActivity) {
        val context = adapter.context
        val sources = ParcelableActivityUtils.getAfterFilteredSources(activity)
        val message = ActivityTitleSummaryMessage.get(context,
                adapter.userColorNameManager, activity, sources, activityTypeView.defaultColor,
                adapter.useStarsForLikes, adapter.isNameFirst)
        if (message == null) {
            showNotSupported()
            return
        }
        activityTypeView.setColorFilter(message.color, PorterDuff.Mode.SRC_ATOP)
        activityTypeView.setImageResource(message.icon)
        titleView.text = message.title
        summaryView.text = message.summary
        summaryView.visibility = if (summaryView.length() > 0) View.VISIBLE else View.GONE
        timeView.time = activity.timestamp
        if (adapter.showAccountsColor) {
            itemContent.drawEnd(activity.account_color)
        } else {
            itemContent.drawEnd()
        }
        displayUserProfileImages(sources)
    }

    private fun showNotSupported() {

    }

    fun setupViewOptions() {
        val textSize = adapter.textSize
        titleView.textSize = textSize
        summaryView.textSize = textSize * 0.85f
        timeView.textSize = textSize * 0.80f

        profileImageViews.forEach {
            it.style = adapter.profileImageStyle
        }
    }

    private fun displayUserProfileImages(users: Array<ParcelableUser>?) {
        val shouldDisplayImages = adapter.profileImageEnabled
        profileImagesContainer.visibility = if (shouldDisplayImages) View.VISIBLE else View.GONE
        profileImageSpace.visibility = if (shouldDisplayImages) View.VISIBLE else View.GONE
        if (!shouldDisplayImages) return
        if (users == null) {
            for (view in profileImageViews) {
                view.visibility = View.GONE
            }
            return
        }
        val length = Math.min(profileImageViews.size, users.size)
        for (i in 0 until profileImageViews.size) {
            val view = profileImageViews[i]
            view.setImageDrawable(null)
            if (i < length) {
                view.visibility = View.VISIBLE
                val context = adapter.context
                adapter.requestManager.loadProfileImage(context, users[i]).into(view)
            } else {
                view.visibility = View.GONE
            }
        }
        if (users.size > profileImageViews.size) {
            val moreNumber = users.size - profileImageViews.size
            profileImageMoreNumber.visibility = View.VISIBLE
            profileImageMoreNumber.setText(moreNumber.toString())
        } else {
            profileImageMoreNumber.visibility = View.GONE
        }
    }

    fun setOnClickListeners() {
        setActivityEventListener(adapter.activityEventListener!!)
    }

    fun setActivityEventListener(listener: IActivitiesAdapter.ActivityEventListener) {
        activityEventListener = listener
        (itemContent as View).setOnClickListener(this)
        //        ((View) itemContent).setOnLongClickListener(this);

    }

    override fun onClick(v: View) {
        if (activityEventListener == null) return
        val position = layoutPosition
        when (v.id) {
            R.id.itemContent -> {
                activityEventListener!!.onActivityClick(this, position)
            }
        }
    }

}
