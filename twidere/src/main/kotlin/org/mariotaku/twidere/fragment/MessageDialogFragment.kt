/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentActivity

/**
 * Created by mariotaku on 14-6-24.
 */
class MessageDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity
        val builder = AlertDialog.Builder(activity)
        val args = arguments
        builder.setMessage(args.getString(EXTRA_MESSAGE))
        builder.setPositiveButton(android.R.string.ok, null)
        return builder.create()
    }

    companion object {
        private val EXTRA_MESSAGE = "message"

        fun show(activity: FragmentActivity, message: String, tag: String): MessageDialogFragment {
            val df = MessageDialogFragment()
            val args = Bundle()
            args.putString(EXTRA_MESSAGE, message)
            df.arguments = args
            df.show(activity.supportFragmentManager, tag)
            return df
        }

        fun create(message: String): MessageDialogFragment {
            val df = MessageDialogFragment()
            val args = Bundle()
            args.putString(EXTRA_MESSAGE, message)
            df.arguments = args
            return df
        }
    }
}
