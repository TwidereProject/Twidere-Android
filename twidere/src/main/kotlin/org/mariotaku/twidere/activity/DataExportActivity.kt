package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import org.mariotaku.ktextension.dismissDialogFragment
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.fragment.DataExportImportTypeSelectorDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.util.DataImportExportUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DataExportActivity : BaseActivity(), DataExportImportTypeSelectorDialogFragment.Callback {

    private var task: ExportSettingsTask? = null

    override fun onCancelled(df: DialogFragment) {
        if (!isFinishing) {
            finish()
        }
    }

    override fun onDismissed(df: DialogFragment) {
        if (df is DataExportImportTypeSelectorDialogFragment) {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_DIRECTORY -> {
                executeAfterFragmentResumed {
                    if (resultCode == RESULT_OK && data != null) {
                        val path = data.data
                        val df = DataExportImportTypeSelectorDialogFragment()
                        val args = Bundle()
                        args.putParcelable(EXTRA_PATH, path)
                        args.putString(EXTRA_TITLE, getString(R.string.export_settings_type_dialog_title))
                        df.arguments = args
                        df.show(supportFragmentManager, "select_export_type")
                    } else {
                        if (!isFinishing) {
                            finish()
                        }
                    }
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPositiveButtonClicked(path: Uri?, flags: Int) {
        if (path == null || flags == 0) {
            finish()
            return
        }
        if (task == null || task!!.status != AsyncTask.Status.RUNNING) {
            val folder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                DocumentFile.fromTreeUri(this, path)
            } else {
                DocumentFile.fromFile(File(path.path))
            }
            task = ExportSettingsTask(this, folder, flags)
            task!!.execute()
        }
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val intent = Intent(this, FileSelectorActivity::class.java)
            intent.action = IntentConstants.INTENT_ACTION_PICK_DIRECTORY
            startActivityForResult(intent, REQUEST_PICK_DIRECTORY)
        }
    }

    internal class ExportSettingsTask(
            private val activity: DataExportActivity,
            private val folder: DocumentFile?,
            private val flags: Int
    ) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            if (folder == null || !folder.isDirectory) return false
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = String.format("Twidere_Settings_%s.zip", sdf.format(Date()))
            val file = folder.findFile(fileName) ?: folder.createFile("application/zip", fileName)
            ?: return false
//            val file = File(folder, fileName)
//            file.delete()
            return try {
                DataImportExportUtils.exportData(activity, file, flags)
                true
            } catch (e: IOException) {
                Log.w(LOGTAG, e)
                false
            }

        }

        override fun onPostExecute(result: Boolean?) {
            activity.executeAfterFragmentResumed { activity ->
                activity.supportFragmentManager.dismissDialogFragment(FRAGMENT_TAG)
            }
            if (result != null && result) {
                activity.setResult(Activity.RESULT_OK)
            } else {
                activity.setResult(Activity.RESULT_CANCELED)
            }
            activity.finish()
        }

        override fun onPreExecute() {
            activity.executeAfterFragmentResumed {
                ProgressDialogFragment.show(it.supportFragmentManager, FRAGMENT_TAG).isCancelable = false
            }
        }

        companion object {
            private const val FRAGMENT_TAG = "import_settings_dialog"
        }

    }
}
