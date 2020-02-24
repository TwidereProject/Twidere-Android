package org.mariotaku.twidere.view.holder

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user_list.view.*
import org.mariotaku.ktextension.spannable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.view.ProfileImageView

/**
 * Created by mariotaku on 2016/12/1.
 */

class SimpleUserListViewHolder(
        val adapter: IUserListsAdapter<*>,
        itemView: View
) : RecyclerView.ViewHolder(itemView) {

    val createdByView: TextView = itemView.createdBy
    val nameView: TextView = itemView.name
    val profileImageView: ProfileImageView = itemView.profileImage

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    fun display(userList: ParcelableUserList) {
        nameView.spannable = userList.name
        createdByView.spannable = createdByView.context.getString(R.string.created_by,
                adapter.userColorNameManager.getDisplayName(userList, false))
        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            val context = itemView.context
            adapter.requestManager.loadProfileImage(context, userList, adapter.profileImageStyle,
                    profileImageView.cornerRadius, profileImageView.cornerRadiusRatio).into(profileImageView)
        } else {
            profileImageView.visibility = View.GONE
        }
    }
}
