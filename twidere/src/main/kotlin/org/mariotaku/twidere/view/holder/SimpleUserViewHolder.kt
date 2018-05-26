package org.mariotaku.twidere.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 2016/12/1.
 */

open class SimpleUserViewHolder<out A : IContentAdapter>(
        itemView: View,
        val adapter: A
) : RecyclerView.ViewHolder(itemView) {

    val nameView: TextView = itemView.name
    val secondaryNameView: TextView = itemView.screenName
    val profileImageView: ProfileImageView = itemView.profileImage
    val checkBox: CheckBox = itemView.checkBox

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    open fun displayUser(user: ParcelableUser) {
        nameView.spannable = user.name
        secondaryNameView.spannable = "@${user.screen_name}"
        if (adapter.profileImageEnabled) {
            profileImageView.profileImage = user.profile_image_url
            profileImageView.visibility = View.VISIBLE
        } else {
            profileImageView.profileImage = null
            profileImageView.visibility = View.GONE
        }
    }

    companion object {
        const val layoutResource = R.layout.list_item_simple_user
    }
}
