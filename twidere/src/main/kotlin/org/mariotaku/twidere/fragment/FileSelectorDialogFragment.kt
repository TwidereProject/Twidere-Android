/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.ContextCompat
import android.support.v4.content.FixedAsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.text.TextUtils.TruncateAt
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.iface.ISupportDialogFragmentCallback
import org.mariotaku.twidere.util.ThemeUtils
import java.io.File
import java.util.*

class FileSelectorDialogFragment : BaseDialogFragment(), LoaderCallbacks<List<File>>, OnClickListener, OnItemClickListener {

    private lateinit var adapter: FilesAdapter

    private val currentDirectory: File?
        get() {
            val args = arguments
            val path = args!!.getString(EXTRA_PATH)
            return if (path != null) File(path) else null
        }

    private val isPickDirectory: Boolean
        get() {
            val args = arguments
            val action = args?.getString(EXTRA_ACTION)
            return INTENT_ACTION_PICK_DIRECTORY == action
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = arguments
        loaderManager.initLoader(0, args, this)
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        val a = activity
        if (a is Callback) {
            (a as Callback).onCancelled(this)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val a = activity
                if (isPickDirectory && a is Callback) {
                    (a as Callback).onFilePicked(currentDirectory!!)
                }
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                val a = activity
                if (a is Callback) {
                    (a as Callback).onCancelled(this)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        adapter = FilesAdapter(activity!!)
        val builder = AlertDialog.Builder(activity!!)
        builder.setAdapter(adapter, this)
        builder.setTitle(R.string.pick_file)
        builder.setNegativeButton(android.R.string.cancel, this)
        if (isPickDirectory) {
            builder.setPositiveButton(android.R.string.ok, this)
        }
        val dialog = builder.create()
        dialog.onShow { d ->
            val alertDialog = d as AlertDialog
            alertDialog.applyTheme()
            val listView = alertDialog.listView
            listView.onItemClickListener = this@FileSelectorDialogFragment
        }
        return dialog
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<File>> {
        val extensions = args.getStringArray(EXTRA_FILE_EXTENSIONS)
        val path = args.getString(EXTRA_PATH)
        var currentDir: File? = if (path != null) File(path) else getExternalStorageDirectory()
        if (currentDir == null) {
            currentDir = File("/")
        }
        arguments!!.putString(EXTRA_PATH, currentDir.absolutePath)
        return FilesLoader(activity!!, currentDir, extensions)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val a = activity
        if (a is Callback) {
            (a as Callback).onDismissed(this)
        }
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val file = adapter.getItem(position) ?: return
        if (file.isDirectory) {
            val args = arguments
            args!!.putString(EXTRA_PATH, file.absolutePath)
            loaderManager.restartLoader(0, args, this)
        } else if (file.isFile && !isPickDirectory) {
            val a = activity
            if (a is Callback) {
                (a as Callback).onFilePicked(file)
            }
            dismiss()
        }
    }

    override fun onLoaderReset(loader: Loader<List<File>>) {
        adapter.setData(null, null)
    }

    override fun onLoadFinished(loader: Loader<List<File>>, data: List<File>) {
        val currentDir = currentDirectory
        if (currentDir != null) {
            adapter.setData(currentDir, data)
            if (currentDir.parent == null) {
                setTitle("/")
            } else {
                setTitle(currentDir.name)
            }
        }
    }

    private fun setTitle(title: CharSequence) {
        val dialog = dialog ?: return
        dialog.setTitle(title)
    }

    interface Callback : ISupportDialogFragmentCallback {

        fun onFilePicked(file: File)
    }

    private class FilesAdapter(context: Context) : ArrayAdapter<File>(context, android.R.layout.simple_list_item_1) {

        private val padding: Int
        private val actionIconColor = ThemeUtils.getColorForeground(context)
        private val resources = context.resources

        private var currentPath: File? = null

        init {
            padding = (4 * resources.displayMetrics.density).toInt()
        }

        override fun getItemId(position: Int): Long {
            return getItem(position)!!.hashCode().toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val text = (view as? TextView ?: view.findViewById<View>(android.R.id.text1)) as TextView
            val file = getItem(position)!!
            if (file == currentPath?.parentFile) {
                text.text = ".."
            } else {
                text.text = file.name
            }
            text.setSingleLine(true)
            text.ellipsize = TruncateAt.MARQUEE
            text.setPadding(padding, padding, position, padding)
            val icon = ContextCompat.getDrawable(context, file.icon)!!.apply {
                mutate()
                DrawableCompat.setTint(this, actionIconColor)
            }
            text.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
            return view
        }

        fun setData(current: File?, data: List<File>?) {
            currentPath = current
            clear()
            if (data != null) {
                addAll(data)
            }
        }

        private val File.icon: Int
            get() = if (isDirectory) R.drawable.ic_folder else R.drawable.ic_file
    }

    private class FilesLoader(context: Context, private val path: File?, private val extensions: Array<String>?) : FixedAsyncTaskLoader<List<File>>(context) {

        override fun loadInBackground(): List<File>? {
            if (path == null || !path.isDirectory) return emptyList()
            val listedFiles = path.listFiles() ?: return emptyList()
            val dirs = ArrayList<File>()
            val files = ArrayList<File>()
            for (file in listedFiles) {
                if (!file.canRead() || file.isHidden) {
                    continue
                }
                if (file.isDirectory) {
                    dirs.add(file)
                } else if (file.isFile) {
                    val extension = file.extension
                    if (extensions == null || extension.isEmpty() || extensions.any { it.equals(extension, ignoreCase = true) }) {
                        files.add(file)
                    }
                }
            }
            Collections.sort(dirs, NAME_COMPARATOR)
            Collections.sort(files, NAME_COMPARATOR)
            val list = ArrayList<File>()
            val parent = path.parentFile
            if (path.parentFile != null) {
                list += parent
            }
            list += dirs
            list += files
            return list
        }

        override fun onStartLoading() {
            forceLoad()
        }

        override fun onStopLoading() {
            cancelLoad()
        }

        companion object {

            private val NAME_COMPARATOR = Comparator<File> { file1, file2 ->
                val loc = Locale.getDefault()
                file1.name.toLowerCase(loc).compareTo(file2.name.toLowerCase(loc))
            }
        }
    }

}
