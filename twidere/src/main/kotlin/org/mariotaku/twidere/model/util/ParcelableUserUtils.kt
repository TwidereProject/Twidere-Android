package org.mariotaku.twidere.model.util

import android.graphics.Color
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.util.UserColorNameManager

/**
 * Processing ParcelableUser
 *
 * Created by mariotaku on 16/2/24.
 */
object ParcelableUserUtils {

    fun parseColor(colorString: String?): Int {
        if (colorString == null) return 0
        var str: String = colorString
        if (!str.startsWith("#")) {
            str = "#" + str
        }
        return try {
            Color.parseColor(str)
        } catch (e: IllegalArgumentException) {
            0
        }
    }

    fun updateExtraInformation(user: ParcelableUser, account: AccountDetails,
            manager: UserColorNameManager) {
        user.account_color = account.color
        user.color = manager.getUserColor(user.key)
        user.nickname = manager.getUserNickname(user.key)
    }

}
