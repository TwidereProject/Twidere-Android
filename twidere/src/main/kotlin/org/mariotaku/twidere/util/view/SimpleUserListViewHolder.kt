package org.mariotaku.twidere.util.view

import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.view.holder.SimpleUserListViewHolder

fun SimpleUserListViewHolder.display(userList: ParcelableUserList, mediaLoader: MediaLoaderWrapper,
                                     userColorNameManager: UserColorNameManager,
                                     displayProfileImage: Boolean) {
    nameView.text = userList.name
    createdByView.text = createdByView.context.getString(R.string.created_by,
            userColorNameManager.getDisplayName(userList, false))
    profileImageView.visibility = if (displayProfileImage) View.VISIBLE else View.GONE
    if (displayProfileImage) {
        mediaLoader.displayProfileImage(profileImageView, userList)
    } else {
        mediaLoader.cancelDisplayTask(profileImageView)
    }
}