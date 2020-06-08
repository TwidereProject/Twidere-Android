/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_USER
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.ParcelableUser

class CreateUserMuteDialogFragment : AbsUserMuteBlockDialogFragment() {

    override fun getMessage(user: ParcelableUser): String {
        val displayName = userColorNameManager.getDisplayName(user, kPreferences[nameFirstKey])
        return getString(R.string.mute_user_confirm_message, displayName)
    }

    override fun getTitle(user: ParcelableUser): String {
        val displayName = userColorNameManager.getDisplayName(user, kPreferences[nameFirstKey])
        return getString(R.string.mute_user, displayName)
    }

    override fun getPositiveButtonTitle(user: ParcelableUser): String {
        return getString(R.string.action_mute)
    }

    override fun performUserAction(user: ParcelableUser, filterEverywhere: Boolean) {
        val accountKey = user.account_key ?: return
        twitterWrapper.createMuteAsync(accountKey, user.key, filterEverywhere)
    }

    companion object {

        const val FRAGMENT_TAG = "create_user_mute"

        fun show(fm: FragmentManager, user: ParcelableUser): CreateUserMuteDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER, user)
            val f = CreateUserMuteDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
