package org.mariotaku.twidere.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.twidere.adapter.iface.IContentCardAdapter
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 2016/12/1.
 */

open class SimpleUserViewHolder(itemView: View, val adapter: IContentCardAdapter) : RecyclerView.ViewHolder(itemView) {

    val nameView: TextView
    val secondaryNameView: TextView
    val profileImageView: ProfileImageView
    val checkBox: CheckBox

    init {
        nameView = itemView.name
        secondaryNameView = itemView.screenName
        profileImageView = itemView.profileImage
        checkBox = itemView.checkBox

        profileImageView.style = adapter.profileImageStyle
    }

    open fun displayUser(user: ParcelableUser) {
        nameView.text = user.name
        secondaryNameView.text = "@${user.screen_name}"
        adapter.mediaLoader.displayProfileImage(profileImageView, user)
    }
}
