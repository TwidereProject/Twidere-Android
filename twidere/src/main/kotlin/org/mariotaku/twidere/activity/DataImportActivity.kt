package org.mariotaku.twidere.activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.fragment.DataExportImportTypeSelectorDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.util.DataImportExportUtils
import java.io.File
import java.io.IOException

class DataImportActivity : BaseActivity(), DataExportImportTypeSelectorDialogFragment.Callback {

    private var importSettingsTask: ImportSettingsTask? = null
    private var openImportTypeTask: OpenImportTypeTask? = null
    private var resumeFragmentsRunnable: Runnable? = null

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
            REQUEST_PICK_FILE -> {
                resumeFragmentsRunnable = Runnable {
                    if (resultCode == RESULT_OK && data != null) {
                        val path = data.data.path
                        if (openImportTypeTask == null || openImportTypeTask!!.status != AsyncTask.Status.RUNNING) {
                            openImportTypeTask = OpenImportTypeTask(this@DataImportActivity, path)
                            openImportTypeTask!!.execute()
                        }
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


    override fun onResumeFragments() {
        super.onResumeFragments()
        if (resumeFragmentsRunnable != null) {
            resumeFragmentsRunnable!!.run()
        }
    }

    override fun onPositiveButtonClicked(path: String?, flags: Int) {
        if (path == null || flags == 0) {
            finish()
            return
        }
        if (importSettingsTask == null || importSettingsTask!!.status != AsyncTask.Status.RUNNING) {
            importSettingsTask = ImportSettingsTask(this, path, flags)
            importSettingsTask!!.execute()
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
            intent.action = INTENT_ACTION_PICK_FILE
            startActivityForResult(intent, REQUEST_PICK_FILE)
        }
    }

    internal class ImportSettingsTask(
            private val activity: DataImportActivity,
            private val path: String?,
            private val flags: Int
    ) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            if (path == null) return false
            val file = File(path)
            if (!file.isFile) return false
            try {
                DataImportExportUtils.importData(activity, file, flags)
                return true
            } catch (e: IOException) {
                Log.w(LOGTAG, e)
                return false
            }

        }

        override fun onPostExecute(result: Boolean?) {
            activity.executeAfterFragmentResumed {
                val activity = it as DataImportActivity
                val fm = activity.supportFragmentManager
                val f = fm.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment
                f?.dismiss()
            }
            if (result != null && result) {
                activity.setResult(RESULT_OK)
            } else {
                activity.setResult(RESULT_CANCELED)
            }
            activity.finish()
        }

        override fun onPreExecute() {
            activity.executeAfterFragmentResumed {
                val activity = it as DataImportActivity
                ProgressDialogFragment.show(activity.supportFragmentManager, FRAGMENT_TAG).isCancelable = false
            }
        }

        companion object {
            private val FRAGMENT_TAG = "import_settings_dialog"
        }

    }

    internal class OpenImportTypeTask(private val activity: DataImportActivity, private val path: String?) : AsyncTask<Any, Any, Int>() {

        override fun doInBackground(vararg params: Any): Int? {
            if (path == null) return 0
            val file = File(path)
            if (!file.isFile) return 0
            try {
                return DataImportExportUtils.getImportedSettingsFlags(file)
            } catch (e: IOException) {
                return 0
            }

        }

        override fun onPostExecute(flags: Int?) {
            activity.executeAfterFragmentResumed {
                val activity = it as DataImportActivity
                val fm = activity.supportFragmentManager
                val f = fm.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment
                f?.dismiss()
            }
            val df = DataExportImportTypeSelectorDialogFragment()
            val args = Bundle()
            args.putString(EXTRA_PATH, path)
            args.putString(EXTRA_TITLE, activity.getString(R.string.import_settings_type_dialog_title))
            if (flags != null) {
                args.putInt(EXTRA_FLAGS, flags)
            } else {
                args.putInt(EXTRA_FLAGS, 0)
            }
            df.arguments = args
            df.show(activity.supportFragmentManager, "select_import_type")
        }

        override fun onPreExecute() {
            activity.executeAfterFragmentResumed {
                val activity = it as DataImportActivity
                ProgressDialogFragment.show(activity.supportFragmentManager, FRAGMENT_TAG).isCancelable = false
            }
        }

        companion object {

            private val FRAGMENT_TAG = "read_settings_data_dialog"
        }

    }
}
