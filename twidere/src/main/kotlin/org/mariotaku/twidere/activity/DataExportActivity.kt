package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
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
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                i
            } else {
                val i = Intent(this, FileSelectorActivity::class.java)
                i.action = IntentConstants.INTENT_ACTION_PICK_DIRECTORY
                i
            }
            startActivityForResult(intent, REQUEST_PICK_DIRECTORY)
        }
    }

    internal class ExportSettingsTask(
            private val activity: DataExportActivity,
            private val folderUri: Uri?,
            private val flags: Int
    ) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            if (folderUri == null) return false
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = String.format("Twidere_Settings_%s.zip", sdf.format(Date()))
//            val file = File(folder, fileName)
//            file.delete()
            return try {
                val createdDocumentUri = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val docId = DocumentsContract.getTreeDocumentId(folderUri)
                    val dirUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)
                    DocumentsContract.createDocument(
                            activity.contentResolver,
                            dirUri,
                            "application/zip",
                            fileName)
                } else {
                    val folder = DocumentFile.fromFile(File(folderUri.path!!))
                    val file = folder.findFile(fileName)
                            ?: folder.createFile("application/zip", fileName) ?: return false
                    file.uri
                })
                        ?: return false

                DataImportExportUtils.exportData(activity, createdDocumentUri, flags)
                true
            } catch (e: Throwable) {
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
