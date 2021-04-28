package org.mariotaku.twidere.activity

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import org.mariotaku.ktextension.dismissDialogFragment
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
                        val path = data.data!!
                        if (openImportTypeTask == null || openImportTypeTask!!.status != AsyncTask.Status.RUNNING) {
                            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                DocumentFile.fromSingleUri(this, path)
                            } else {
                                DocumentFile.fromFile(File(path.path))
                            }
                            openImportTypeTask = OpenImportTypeTask(this@DataImportActivity, file)
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

    override fun onPositiveButtonClicked(path: Uri?, flags: Int) {
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
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
                i.type = "*/*"
                i
            } else {
                val i = Intent(this, FileSelectorActivity::class.java)
                i.action = INTENT_ACTION_PICK_FILE
                i
            }
            startActivityForResult(intent, REQUEST_PICK_FILE)
        }
    }

    internal class ImportSettingsTask(
            private val activity: DataImportActivity,
            private val uri: Uri?,
            private val flags: Int
    ) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            return try {
                DataImportExportUtils.importData(activity, uri, flags)
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
                activity.setResult(RESULT_OK)
            } else {
                activity.setResult(RESULT_CANCELED)
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

    internal class OpenImportTypeTask(private val activity: DataImportActivity, private val file: DocumentFile?) : AsyncTask<Any, Any, Int>() {

        override fun doInBackground(vararg params: Any): Int? {
            if (file == null) {
                return 0
            }
            if (!file.isFile) return 0
            return try {
                DataImportExportUtils.getImportedSettingsFlags(activity, file)
            } catch (e: IOException) {
                0
            }

        }

        override fun onPostExecute(flags: Int?) {
            activity.executeAfterFragmentResumed { activity ->
                activity.supportFragmentManager.dismissDialogFragment(FRAGMENT_TAG)
            }
            val df = DataExportImportTypeSelectorDialogFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_PATH, file?.uri)
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
                ProgressDialogFragment.show(it.supportFragmentManager, FRAGMENT_TAG).isCancelable = false
            }
        }

        companion object {

            private const val FRAGMENT_TAG = "read_settings_data_dialog"
        }

    }
}
