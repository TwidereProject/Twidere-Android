/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.activity

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_account_selector.*
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.adapter.AccountDetailsAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.util.AccountUtils

class AccountSelectorActivity : BaseActivity(), OnClickListener, OnItemClickListener {

    private var adapter: AccountDetailsAdapter? = null

    private var firstCreated: Boolean = false

    override fun onClick(view: View) {
        when (view.id) {
            R.id.save -> {
                val checkedIds = accountsList.checkedItemIds
                if (checkedIds.isEmpty() && !isSelectNoneAllowed) {
                    Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show()
                    return
                }
                val data = Intent()
                data.putExtra(EXTRA_IDS, checkedIds)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        selectSingleAccount(position)
    }

    fun selectSingleAccount(position: Int) {
        val adapter = adapter!!
        val account = adapter.getItem(position)
        val data = Intent()
        data.putExtra(EXTRA_ID, account.key.id)
        data.putExtra(EXTRA_ACCOUNT_KEY, account.key)

        val startIntent = startIntent
        if (startIntent != null) {
            startIntent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            startActivity(startIntent)
        }

        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstCreated = savedInstanceState == null
        setContentView(R.layout.activity_account_selector)
        adapter = AccountDetailsAdapter(this).apply {
            setSwitchEnabled(isSingleSelection)
            setSortEnabled(false)
            isProfileImageDisplayed = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)
            val am = AccountManager.get(context)
            val allAccountDetails = AccountUtils.getAllAccountDetails(am, AccountUtils.getAccounts(am))
            addAll(allAccountDetails.filter {
                if (isOAuthOnly && it.credentials_type != Credentials.Type.OAUTH && it.credentials_type != Credentials.Type.XAUTH) {
                    return@filter false
                }
                if (USER_TYPE_TWITTER_COM == accountHost) {
                    if (it.key.host == null || it.type == AccountType.TWITTER) return@filter false
                } else if (accountHost != null) {
                    if (accountHost != it.key.host) return@filter false
                }
                return@filter true
            })
        }
        accountsList.choiceMode = if (isSingleSelection) ListView.CHOICE_MODE_NONE else ListView.CHOICE_MODE_MULTIPLE
        if (isSingleSelection) {
            accountsList.onItemClickListener = this
        }
        selectAccountButtons.visibility = if (isSingleSelection) View.GONE else View.VISIBLE
        accountsList.adapter = adapter
    }

    private val intentExtraIds: Array<UserKey>?
        get() {
            return intent.getParcelableArrayExtra(EXTRA_ACCOUNT_KEYS)?.toTypedArray(UserKey.CREATOR)
        }

    private val isOAuthOnly: Boolean
        get() {
            return intent.getBooleanExtra(EXTRA_OAUTH_ONLY, false)
        }

    private val accountHost: String?
        get() {
            return intent.getStringExtra(EXTRA_ACCOUNT_HOST)
        }

    private val isSelectNoneAllowed: Boolean
        get() {
            return intent.getBooleanExtra(EXTRA_ALLOW_SELECT_NONE, false)
        }

    private val isSingleSelection: Boolean
        get() {
            return intent.getBooleanExtra(EXTRA_SINGLE_SELECTION, false)
        }

    private fun shouldSelectOnlyItem(): Boolean {
        return intent.getBooleanExtra(EXTRA_SELECT_ONLY_ITEM, false)
    }

    private val startIntent: Intent?
        get() {
            val startIntent = intent.getParcelableExtra<Intent>(EXTRA_START_INTENT)
            startIntent?.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
            return startIntent
        }

}
