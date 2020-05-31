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
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_URI
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow

class PhishingLinkWarningDialogFragment : BaseDialogFragment(), OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val args = arguments ?: return
                val uri = args.getParcelable<Uri>(EXTRA_URI) ?: return
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = uri
                startActivity(intent)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(android.R.string.dialog_alert_title)
        builder.setView(R.layout.dialog_phishing_link_warning)
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

}
