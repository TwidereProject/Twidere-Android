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
import android.net.Uri
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import org.mariotaku.twidere.activity.iface.IExtendedActivity
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import java.io.File

/**
 * Created by mariotaku on 15/12/28.
 */
abstract class ProgressSaveFileTask(
        context: Context,
        source: Uri,
        destination: File,
        getMimeType: SaveFileTask.FileInfoCallback
) : SaveFileTask(context, source, destination, getMimeType) {

    override fun showProgress() {
        (context as IExtendedActivity).executeAfterFragmentResumed { activity ->
            val fragment = ProgressDialogFragment()
            fragment.isCancelable = false
            fragment.show((activity as FragmentActivity).supportFragmentManager, PROGRESS_FRAGMENT_TAG)
        }
    }

    override fun dismissProgress() {
        (context as IExtendedActivity).executeAfterFragmentResumed { activity ->
            val fm = (activity as FragmentActivity).supportFragmentManager
            val fragment = fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG) as? DialogFragment
            fragment?.dismiss()
        }
    }

    companion object {
        private val PROGRESS_FRAGMENT_TAG = "progress"
    }
}
