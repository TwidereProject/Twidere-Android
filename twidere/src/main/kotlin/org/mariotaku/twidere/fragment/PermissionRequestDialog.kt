package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
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
        val builder = AlertDialog.Builder(requireContext())
        val permissions = requireArguments().getStringArray(EXTRA_PERMISSIONS).orEmpty()
        val requestCode = requireArguments().getInt(EXTRA_REQUEST_CODE)
        builder.setMessage(requireArguments().getString(EXTRA_MESSAGE))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            activity?.let { ActivityCompat.requestPermissions(it, permissions, requestCode) }
        }
        builder.setNegativeButton(R.string.action_later) { _, _ ->
            val callback = parentFragment as? PermissionRequestCancelCallback ?: activity as?
                    PermissionRequestCancelCallback ?: return@setNegativeButton
            callback.onRequestPermissionCancelled(requestCode)
        }
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    interface PermissionRequestCancelCallback {
        fun onRequestPermissionCancelled(requestCode: Int)
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