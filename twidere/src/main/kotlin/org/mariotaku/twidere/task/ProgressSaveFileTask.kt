/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.task

import android.content.Context
import androidx.fragment.app.DialogFragment
import org.mariotaku.twidere.activity.iface.IBaseActivity
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import java.io.File

/**
 * Created by mariotaku on 15/12/28.
 */
abstract class ProgressSaveFileTask(
        context: Context,
        destination: File,
        fileInfo: FileInfo
) : SaveFileTask(context, destination, fileInfo) {

    override fun showProgress() {
        val context = this.context ?: return
        (context as IBaseActivity<*>).executeAfterFragmentResumed { activity ->
            val fragment = ProgressDialogFragment()
            fragment.isCancelable = false
            fragment.show(activity.supportFragmentManager, PROGRESS_FRAGMENT_TAG)
        }
    }

    override fun dismissProgress() {
        val context = this.context ?: return
        (context as IBaseActivity<*>).executeAfterFragmentResumed { activity ->
            val fm = activity.supportFragmentManager
            val fragment = fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG) as? DialogFragment
            fragment?.dismiss()
        }
    }

    companion object {
        private const val PROGRESS_FRAGMENT_TAG = "progress"
    }
}
