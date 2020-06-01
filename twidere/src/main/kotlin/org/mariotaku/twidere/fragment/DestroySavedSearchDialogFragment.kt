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
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.model.UserKey

class DestroySavedSearchDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val accountKey = accountKey
                val searchId = searchId
                val twitter = twitterWrapper
                if (searchId <= 0) return
                twitter.destroySavedSearchAsync(accountKey, searchId)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity
        val builder = AlertDialog.Builder(requireContext())
        val name = searchName
        builder.setTitle(getString(R.string.destroy_saved_search, name))
        builder.setMessage(getString(R.string.destroy_saved_search_confirm_message, name))
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.applyOnShow { applyTheme() }
        return dialog
    }

    private val accountKey: UserKey
        get() = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!

    private val searchId: Long
        get() = arguments?.getLong(EXTRA_SEARCH_ID, -1)!!

    private val searchName: String
        get() = arguments?.getString(EXTRA_NAME)!!

    companion object {

        private const val FRAGMENT_TAG = "destroy_saved_search"

        fun show(fm: FragmentManager, accountKey: UserKey, searchId: Long, name: String): DestroySavedSearchDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
            args.putLong(EXTRA_SEARCH_ID, searchId)
            args.putString(EXTRA_NAME, name)
            val f = DestroySavedSearchDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}
