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
import androidx.appcompat.app.AlertDialog
import org.mariotaku.ktextension.getNullableTypedArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils

class SensitiveContentWarningDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val context = activity ?: return
                val args = arguments ?: return
                val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                val current = args.getParcelable<ParcelableMedia>(EXTRA_CURRENT_MEDIA)
                val status = args.getParcelable<ParcelableStatus>(EXTRA_STATUS)
                val option = args.getBundle(EXTRA_ACTIVITY_OPTIONS)
                val newDocument = args.getBoolean(EXTRA_NEW_DOCUMENT)
                val media: Array<ParcelableMedia> = args.getNullableTypedArray(EXTRA_MEDIA) ?: emptyArray()
                IntentUtils.openMediaDirectly(context, accountKey, media, current, option, newDocument,
                        status)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(android.R.string.dialog_alert_title)
        builder.setMessage(R.string.sensitive_content_warning)
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

}
