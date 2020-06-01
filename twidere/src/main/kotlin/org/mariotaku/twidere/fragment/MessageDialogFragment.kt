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

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_MESSAGE
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_TITLE
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow

/**
 * Created by mariotaku on 14-6-24.
 */
class MessageDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity()
        val builder = AlertDialog.Builder(activity)
        val args = requireArguments()
        builder.setTitle(args.getString(EXTRA_TITLE))
        builder.setMessage(args.getString(EXTRA_MESSAGE))
        builder.setPositiveButton(android.R.string.ok, null)
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    companion object {

        fun show(fm: FragmentManager, title: String? = null, message: String, tag: String): MessageDialogFragment {
            val df = create(title, message)
            df.show(fm, tag)
            return df
        }

        fun create(title: String? = null, message: String): MessageDialogFragment {
            val df = MessageDialogFragment()
            df.arguments = Bundle {
                this[EXTRA_TITLE] = title
                this[EXTRA_MESSAGE] = message
            }
            return df
        }
    }
}
