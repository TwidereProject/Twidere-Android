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
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils

class SensitiveContentWarningDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val context = activity
                val args = arguments
                if (args == null || context == null) return
                val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                val current = args.getParcelable<ParcelableMedia>(EXTRA_CURRENT_MEDIA)
                val status = args.getParcelable<ParcelableStatus>(EXTRA_STATUS)
                val option = args.getBundle(EXTRA_ACTIVITY_OPTIONS)
                val newDocument = args.getBoolean(EXTRA_NEW_DOCUMENT)
                val media = args.getParcelableArray(EXTRA_MEDIA).toTypedArray(ParcelableMedia.CREATOR)
                IntentUtils.openMediaDirectly(context, accountKey, status, null, current, media,
                        option, newDocument)
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity
        val builder = AlertDialog.Builder(context)
        builder.setTitle(android.R.string.dialog_alert_title)
        builder.setMessage(R.string.sensitive_content_warning)
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }

}
