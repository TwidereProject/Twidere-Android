package org.mariotaku.twidere.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user_list.view.*

/**
 * Created by mariotaku on 2016/12/1.
 */

class SimpleUserListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val createdByView: TextView = itemView.createdBy
    val nameView: TextView = itemView.name
    val profileImageView: ImageView = itemView.profileImage

}
