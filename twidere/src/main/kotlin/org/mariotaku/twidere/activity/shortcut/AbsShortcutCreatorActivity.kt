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

package org.mariotaku.twidere.activity.shortcut

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.extension.dismissProgressDialog
import org.mariotaku.twidere.extension.showProgressDialog
import org.mariotaku.twidere.model.UserKey
import java.lang.ref.WeakReference

abstract class AbsShortcutCreatorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        selectAccount()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val extras = data.getBundleExtra(EXTRA_EXTRAS)
                val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY) ?: run {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                onAccountSelected(accountKey, extras)
            }
        }
    }


    protected abstract fun onAccountSelected(accountKey: UserKey, extras: Bundle?)

    protected fun selectAccount() {
        val selectAccountIntent = Intent(this, AccountSelectorActivity::class.java)
        startActivityForResult(selectAccountIntent, REQUEST_SELECT_ACCOUNT)
    }

    protected fun addShortcut(task: () -> Promise<ShortcutInfoCompat, Exception>) {
        val weakThis = WeakReference(this)
        val promise = showProgressDialog(TAG_PROCESS_SHORTCUT_PROGRESS)
                .and(task())
        promise.successUi { (_, shortcut) ->
            val activity = weakThis.get() ?: return@successUi
            activity.setResult(Activity.RESULT_OK,
                    ShortcutManagerCompat.createShortcutResultIntent(activity, shortcut))
            activity.finish()
        }.failUi {
            val activity = weakThis.get() ?: return@failUi
            activity.setResult(Activity.RESULT_CANCELED)
            activity.finish()
        }.alwaysUi {
            val activity = weakThis.get() ?: return@alwaysUi
            activity.dismissProgressDialog(TAG_PROCESS_SHORTCUT_PROGRESS)
        }
    }

    companion object {
        private const val TAG_PROCESS_SHORTCUT_PROGRESS = "process_shortcut_progress"

    }
}
