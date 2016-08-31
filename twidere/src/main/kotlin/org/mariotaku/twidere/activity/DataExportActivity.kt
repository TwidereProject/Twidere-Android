package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
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
                        val path = data.data.path
                        val df = DataExportImportTypeSelectorDialogFragment()
                        val args = Bundle()
                        args.putString(EXTRA_PATH, path)
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

    override fun onPositiveButtonClicked(path: String?, flags: Int) {
        if (path == null || flags == 0) {
            finish()
            return
        }
        if (task == null || task!!.status != AsyncTask.Status.RUNNING) {
            task = ExportSettingsTask(this, path, flags)
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

    internal class ExportSettingsTask(private val activity: DataExportActivity, private val mPath: String?, private val mFlags: Int) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            if (mPath == null) return false
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = String.format("Twidere_Settings_%s.zip", sdf.format(Date()))
            val file = File(mPath, fileName)
            file.delete()
            try {
                DataImportExportUtils.exportData(activity, file, mFlags)
                return true
            } catch (e: IOException) {
                Log.w(LOGTAG, e)
                return false
            }

        }

        override fun onPostExecute(result: Boolean?) {
            activity.executeAfterFragmentResumed {
                val activity = it as DataExportActivity
                val fm = activity.supportFragmentManager
                val f = fm.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment
                f?.dismiss()
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
                val activity = it as DataExportActivity
                ProgressDialogFragment.show(activity.supportFragmentManager, FRAGMENT_TAG).isCancelable = false
            }
        }

        companion object {
            private val FRAGMENT_TAG = "import_settings_dialog"
        }

    }
}
