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
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.message
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.title

class MessageDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity!!
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(arguments!!.title)
        builder.setMessage(arguments!!.message)
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
                this.title = title
                this.message = message
            }
            return df
        }
    }
}
