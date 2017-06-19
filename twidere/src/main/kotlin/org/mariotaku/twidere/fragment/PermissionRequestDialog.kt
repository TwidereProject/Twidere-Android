package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow

/**
 * Created by mariotaku on 2016/12/13.
 */
class PermissionRequestDialog : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val permissions = arguments.getStringArray(EXTRA_PERMISSIONS)
        val requestCode = arguments.getInt(EXTRA_REQUEST_CODE)
        builder.setMessage(arguments.getString(EXTRA_MESSAGE))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
        builder.setNegativeButton(R.string.action_later) { _, _ ->
            val callback = parentFragment as? PermissionRequestCancelCallback ?: activity as?
                    PermissionRequestCancelCallback ?: return@setNegativeButton
            callback.onPermissionRequestCancelled(requestCode)
        }
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    interface PermissionRequestCancelCallback {
        fun onPermissionRequestCancelled(requestCode: Int)
    }

    companion object {

        fun show(fragmentManager: FragmentManager, message: String, permissions: Array<String>,
                requestCode: Int): PermissionRequestDialog {
            val df = PermissionRequestDialog()
            df.arguments = Bundle {
                this[EXTRA_MESSAGE] = message
                this[EXTRA_PERMISSIONS] = permissions
                this[EXTRA_REQUEST_CODE] = requestCode
            }
            df.show(fragmentManager, "request_permission_message")
            return df
        }

    }
}