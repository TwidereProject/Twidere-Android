package org.mariotaku.twidere.util

import android.view.View
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder

/**
 * Created by mariotaku on 2016/12/1.
 */

fun TwoLineWithIconViewHolder.display(
        user: ParcelableUser,
        mediaLoader: MediaLoaderWrapper,
        userColorNameManager: UserColorNameManager,
        displayProfileImage: Boolean
) {
    text1.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            Utils.getUserTypeIconRes(user.is_verified, user.is_protected), 0)
    text1.text = userColorNameManager.getUserNickname(user.key, user.name)
    text2.text = String.format("@%s", user.screen_name)
    icon.visibility = if (displayProfileImage) View.VISIBLE else View.GONE
    if (displayProfileImage) {
        mediaLoader.displayProfileImage(icon, user)
    } else {
        mediaLoader.cancelDisplayTask(icon)
    }
}