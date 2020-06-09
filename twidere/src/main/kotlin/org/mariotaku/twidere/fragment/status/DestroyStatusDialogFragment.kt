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

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.model.ParcelableStatus

class DestroyStatusDialogFragment : AbsSimpleStatusOperationDialogFragment() {

    override val title: String?
        get() = getString(R.string.destroy_status)
    override val message: String
        get() = getString(R.string.destroy_status_confirm_message)

    override fun onPerformAction(status: ParcelableStatus) {
        twitterWrapper.destroyStatusAsync(status.account_key, status.id)
    }

    companion object {

        const val FRAGMENT_TAG = "destroy_status"

        fun show(fm: FragmentManager, status: ParcelableStatus): DestroyStatusDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_STATUS, status)
            val f = DestroyStatusDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
