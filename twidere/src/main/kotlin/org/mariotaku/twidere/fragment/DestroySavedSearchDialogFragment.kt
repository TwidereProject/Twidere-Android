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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_SEARCH_ID
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.promise.SavedSearchPromises

class DestroySavedSearchDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    private val searchId: Long
        get() = arguments!!.getLong(EXTRA_SEARCH_ID, -1)

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val accountKey = arguments!!.accountKey!!
                val searchId = searchId
                if (searchId <= 0) return
                SavedSearchPromises.get(context!!).destroy(accountKey, searchId)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context!!
        val builder = AlertDialog.Builder(context)
        val name = arguments!!.name
        builder.setTitle(getString(R.string.destroy_saved_search, name))
        builder.setMessage(getString(R.string.destroy_saved_search_confirm_message, name))
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.applyOnShow { applyTheme() }
        return dialog
    }

    companion object {

        private const val FRAGMENT_TAG = "destroy_saved_search"

        fun show(fm: FragmentManager, accountKey: UserKey, searchId: Long, name: String): DestroySavedSearchDialogFragment {
            val args = Bundle {
                this.name = name
                this.accountKey = accountKey
                putLong(EXTRA_SEARCH_ID, searchId)
            }
            val f = DestroySavedSearchDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
