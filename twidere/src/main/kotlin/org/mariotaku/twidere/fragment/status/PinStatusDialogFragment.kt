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
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.task.status.PinStatusTask

class PinStatusDialogFragment : AbsSimpleStatusOperationDialogFragment() {

    override val title: String?
        get() = getString(R.string.title_pin_status_confirm)
    override val message: String
        get() = getString(R.string.message_pin_status_confirm)

    override fun onPerformAction(status: ParcelableStatus) {
        val task = PinStatusTask(requireContext(), status.account_key, status.id)
        TaskStarter.execute(task)
    }

    companion object {

        const val FRAGMENT_TAG = "pin_status"

        fun show(fm: FragmentManager, status: ParcelableStatus): PinStatusDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_STATUS, status)
            val f = PinStatusDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
