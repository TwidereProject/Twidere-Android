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

package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.widget.Toast
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.checkAllSelfPermissionsGranted
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_REQUEST_PERMISSIONS
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.FileSelectorDialogFragment
import java.io.File
import android.Manifest.permission as AndroidPermissions

class FileSelectorActivity : BaseActivity(), FileSelectorDialogFragment.Callback {

    override fun onCancelled(df: DialogFragment) {
        if (!isFinishing) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    override fun onDismissed(df: DialogFragment) {
        if (!isFinishing) {
            finish()
        }
    }

    override fun onFilePicked(file: File) {
        val intent = Intent()
        intent.data = Uri.fromFile(file)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_REQUEST_PERMISSIONS) {
            executeAfterFragmentResumed {
                if (checkAllSelfPermissionsGranted(AndroidPermissions.READ_EXTERNAL_STORAGE, AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
                    showPickFileDialog()
                } else {
                    finishWithDeniedMessage()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.action
        if (INTENT_ACTION_PICK_FILE != action && INTENT_ACTION_PICK_DIRECTORY != action) {
            finish()
            return
        }
        if (checkAllSelfPermissionsGranted(AndroidPermissions.READ_EXTERNAL_STORAGE, AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
            showPickFileDialog()
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            val permissions = arrayOf(AndroidPermissions.READ_EXTERNAL_STORAGE, AndroidPermissions.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_REQUEST_PERMISSIONS)
        } else {
            finishWithDeniedMessage()
        }
    }

    private fun finishWithDeniedMessage() {
        if (isFinishing) return
        Toast.makeText(this, R.string.message_toast_select_file_no_storage_permission, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showPickFileDialog() {
        val initialDirectory = intent?.data?.path?.let(::File) ?: getExternalStorageDirectory() ?: File("/")
        val f = FileSelectorDialogFragment()
        f.arguments = Bundle {
            this[EXTRA_ACTION] = intent.action
            this[EXTRA_PATH] = initialDirectory.absolutePath
            this[EXTRA_FILE_EXTENSIONS] = intent.getStringArrayExtra(EXTRA_FILE_EXTENSIONS)
        }
        f.show(supportFragmentManager, "select_file")
    }

}
