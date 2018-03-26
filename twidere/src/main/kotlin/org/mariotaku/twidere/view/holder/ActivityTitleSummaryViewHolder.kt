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

import android.content.res.ColorStateList
import android.support.v4.view.MarginLayoutParamsCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_activity_summary_compact.view.*
import org.mariotaku.ktextension.applyFontFamily
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.ActivityTitleSummaryMessage.Companion.get
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableLiteUser
import org.mariotaku.twidere.model.placeholder.PlaceholderObject
import org.mariotaku.twidere.text.style.PlaceholderLineSpan
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.view.ShortTimeView
import org.mariotaku.twidere.view.holder.status.StatusViewHolder

class ActivityTitleSummaryViewHolder(
        itemView: View,
        private val adapter: ParcelableActivitiesAdapter
) : ViewHolder(itemView), View.OnClickListener {

    private val itemContent = itemView.itemContent
    private val activityTypeView = itemView.activityType
    private val titleView = itemView.title
    private val summaryView = itemView.summary
    private val timeView = itemView.time
    private val profileImagesContainer = itemView.profileImagesContainer
    private val profileImageMoreNumber = itemView.activityProfileImageMoreNumber
    private val profileImageViews = arrayOf(
            itemView.activityProfileImage0,
            itemView.activityProfileImage1,
            itemView.activityProfileImage2,
            itemView.activityProfileImage3,
            itemView.activityProfileImage4
    )
    private val profileImageSpace: View = itemView.profileImageSpace

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

    fun placeholder() {
        activityTypeView.setImageResource(R.drawable.ic_activity_type_placeholder)
        titleView.text = placeholderTitleText
        summaryView.text = StatusViewHolder.placeholderText
        timeView.time = ShortTimeView.PLACEHOLDER
        profileImageMoreNumber.visibility = View.GONE

        profileImageViews.forEach {
            adapter.requestManager.clear(it)
            it.setImageDrawable(null)
        }
    }

    fun display(activity: ParcelableActivity) {
        if (activity is PlaceholderObject) {
            placeholder()
            return
        }
        val context = adapter.context
        val sources = activity.after_filtered_sources ?: activity.sources_lite
        if (sources == null || sources.isEmpty()) {
            showEmpty()
            return
        }
        itemView.visibility = View.VISIBLE
        val message = get(context, UserColorNameManager.get(adapter.context),
                activity, sources, summaryView.currentTextColor, adapter.useStarsForLikes,
                adapter.isNameFirst)
        if (message == null) {
            showNotSupported()
            return
        }

        ImageViewCompat.setImageTintList(activityTypeView, ColorStateList.valueOf(message.color))
        activityTypeView.setImageResource(message.icon)
        titleView.spannable = message.title
        summaryView.spannable = message.summary
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

    private fun showEmpty() {
        itemView.visibility = View.GONE
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

    private fun displayUserProfileImages(users: Array<ParcelableLiteUser>?) {
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
                adapter.requestManager.loadProfileImage(context, users[i], adapter.profileImageStyle)
                        .into(view)
            } else {
                view.visibility = View.GONE
            }
        }
        if (users.size > profileImageViews.size) {
            val moreNumber = users.size - profileImageViews.size
            profileImageMoreNumber.visibility = View.VISIBLE
            profileImageMoreNumber.text = moreNumber.toString()
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

    companion object {
        private val placeholderTitleText: Spanned = SpannableString(" ").apply {
            setSpan(PlaceholderLineSpan(0.8f), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

}
