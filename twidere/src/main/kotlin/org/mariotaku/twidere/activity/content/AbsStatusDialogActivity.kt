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

package org.mariotaku.twidere.activity.content

import android.content.Intent
import android.os.Bundle
import org.mariotaku.twidere.TwidereConstants.REQUEST_SELECT_ACCOUNT
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

abstract class AbsStatusDialogActivity : BaseActivity() {

    private val statusId: String?
        get() = intent.getStringExtra(EXTRA_STATUS_ID)

    private val accountKey: UserKey?
        get() = intent.getParcelableExtra(EXTRA_ACCOUNT_KEY)

    private val accountHost: String?
        get() = intent.getStringExtra(EXTRA_ACCOUNT_HOST)

    private val status: ParcelableStatus?
        get() = intent.getParcelableExtra(EXTRA_STATUS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val statusId = this.statusId ?: run {
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            val accountKey = this.accountKey
            if (accountKey != null) {
                showDialogFragment(accountKey, statusId, status)
            } else {
                val intent = Intent(this, AccountSelectorActivity::class.java)
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
                intent.putExtra(EXTRA_ACCOUNT_HOST, accountHost)
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode == RESULT_OK && data != null) {
                    val statusId = this.statusId ?: run {
                        setResult(RESULT_CANCELED)
                        finish()
                        return
                    }
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    if (accountKey != null) {
                        showDialogFragment(accountKey, statusId, status)
                    }
                    return
                }
            }
        }
        finish()
    }

    protected abstract fun showDialogFragment(accountKey: UserKey, statusId: String,
            status: ParcelableStatus?)
}
