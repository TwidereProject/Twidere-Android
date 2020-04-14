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

package org.mariotaku.twidere.preference

import android.content.Context
import android.os.AsyncTask
import android.os.AsyncTask.Status
import androidx.preference.Preference
import android.util.AttributeSet
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.ktextension.dismissDialogFragment
import org.mariotaku.twidere.activity.iface.IBaseActivity
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import java.lang.ref.WeakReference

abstract class AsyncTaskPreference(context: Context, attrs: AttributeSet? = null) :
        Preference(context, attrs) {

    private var task: InternalTask? = null

    override fun onClick() {
        if (task?.status != Status.RUNNING) {
            task = InternalTask(this).apply { execute() }
        }
    }

    protected abstract fun doInBackground()

    private class InternalTask(preference: AsyncTaskPreference) : AsyncTask<Any, Any, Unit>() {
        private val preferenceRef = WeakReference<AsyncTaskPreference>(preference)

        override fun doInBackground(vararg args: Any) {
            val preference = preferenceRef.get() ?: return
            preference.doInBackground()
        }

        override fun onPostExecute(result: Unit) {
            val context = preferenceRef.get()?.context ?: return
            val activity = ChameleonUtils.getActivity(context) as? IBaseActivity<*> ?: return
            activity.executeAfterFragmentResumed {
                it.supportFragmentManager.dismissDialogFragment(FRAGMENT_TAG)
            }
        }

        override fun onPreExecute() {
            val context = preferenceRef.get()?.context ?: return
            val activity = ChameleonUtils.getActivity(context) as? IBaseActivity<*> ?: return
            activity.executeAfterFragmentResumed {
                ProgressDialogFragment.show(it.supportFragmentManager, FRAGMENT_TAG)
            }
        }

        companion object {
            private const val FRAGMENT_TAG = "task_progress"
        }

    }

}
