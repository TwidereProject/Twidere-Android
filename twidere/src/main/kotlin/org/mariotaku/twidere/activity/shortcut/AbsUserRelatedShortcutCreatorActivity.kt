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
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2017/8/26.
 */
abstract class AbsUserRelatedShortcutCreatorActivity : AbsShortcutCreatorActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_USER -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                val extras = data.getBundleExtra(EXTRA_EXTRAS)
                val accountKey = extras?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                if (user != null) {
                    onUserSelected(accountKey, user)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    final override fun onAccountSelected(accountKey: UserKey, extras: Bundle?) {
        val selectUserIntent = Intent(this, UserSelectorActivity::class.java)
        selectUserIntent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        selectUserIntent.putExtra(EXTRA_EXTRAS, Bundle {
            this[EXTRA_ACCOUNT_KEY] = accountKey
        })
        startActivityForResult(selectUserIntent, REQUEST_SELECT_USER)
    }

    protected abstract fun onUserSelected(accountKey: UserKey?, user: ParcelableUser)
}