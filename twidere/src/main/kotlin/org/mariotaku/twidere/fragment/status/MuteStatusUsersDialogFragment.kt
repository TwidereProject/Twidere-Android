/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment.status

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.referencedUsers
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.CreateUserMuteDialogFragment
import org.mariotaku.twidere.model.ParcelableStatus

/**
 * Created by mariotaku on 2017/2/28.
 */

class MuteStatusUsersDialogFragment : BaseDialogFragment() {

    private val status: ParcelableStatus get() = arguments?.getParcelable(EXTRA_STATUS)!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val referencedUsers = status.referencedUsers
        val nameFirst = preferences[nameFirstKey]
        val displayNames = referencedUsers.map {
            userColorNameManager.getDisplayName(it, nameFirst)
        }.toTypedArray()
        builder.setTitle(R.string.action_status_mute_users)
        builder.setItems(displayNames) { _, which ->
            CreateUserMuteDialogFragment.show(parentFragmentManager, referencedUsers[which])
        }
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }
}
