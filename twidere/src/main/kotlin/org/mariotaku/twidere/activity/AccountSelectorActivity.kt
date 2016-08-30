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

import android.app.Activity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_account_selector.*
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.adapter.AccountsAdapter
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.model.ParcelableAccount
import org.mariotaku.twidere.model.ParcelableCredentials
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import java.util.*

class AccountSelectorActivity : BaseActivity(), LoaderCallbacks<Cursor?>, OnClickListener, OnItemClickListener {

    private val mContentObserver = object : ContentObserver(null) {

        override fun onChange(selfChange: Boolean) {
            onChange(selfChange, null)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            // Handle change.
            if (!isFinishing) {
                loaderManager.restartLoader(0, null, this@AccountSelectorActivity)
            }
        }
    }


    private var adapter: AccountsAdapter? = null

    private var firstCreated: Boolean = false

    override fun onClick(view: View) {
        when (view.id) {
            R.id.save -> {
                val checkedIds = accountsList.checkedItemIds
                if (checkedIds.size == 0 && !isSelectNoneAllowed) {
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

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val conditions = ArrayList<Expression>()
        val conditionArgs = ArrayList<String>()
        if (isOAuthOnly) {
            conditions.add(Expression.equalsArgs(Accounts.AUTH_TYPE))
            conditionArgs.add(ParcelableCredentials.AuthType.OAUTH.toString())
        }
        val accountHost = accountHost
        if (accountHost != null) {
            if (USER_TYPE_TWITTER_COM == accountHost) {
                conditions.add(Expression.or(
                        Expression.equalsArgs(Accounts.ACCOUNT_TYPE),
                        Expression.isNull(Columns.Column(Accounts.ACCOUNT_TYPE)),
                        Expression.likeRaw(Columns.Column(Accounts.ACCOUNT_KEY), "'%@'||?"),
                        Expression.notLikeRaw(Columns.Column(Accounts.ACCOUNT_KEY), "'%@%'")))
                conditionArgs.add(ParcelableAccount.Type.TWITTER)
                conditionArgs.add(accountHost)
            } else {
                conditions.add(Expression.likeRaw(Columns.Column(Accounts.ACCOUNT_KEY), "'%@'||?"))
                conditionArgs.add(accountHost)
            }
        }
        val where: String?
        val whereArgs: Array<String>?
        if (conditions.isEmpty()) {
            where = null
            whereArgs = null
        } else {
            where = Expression.and(*conditions.toTypedArray()).sql
            whereArgs = conditionArgs.toTypedArray()
        }
        return CursorLoader(this, Accounts.CONTENT_URI, Accounts.COLUMNS, where, whereArgs,
                Accounts.SORT_POSITION)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {
        val adapter = adapter!!
        adapter.swapCursor(cursor)
        if (cursor != null && firstCreated) {
            val activatedKeys = intentExtraIds
            for (i in 0..adapter.count - 1) {
                accountsList.setItemChecked(i, activatedKeys?.contains(adapter.getAccount(i)!!.account_key) ?: false)
            }
        }
        if (adapter.count == 1 && shouldSelectOnlyItem()) {
            selectSingleAccount(0)
        } else if (adapter.isEmpty) {
            Toast.makeText(this, R.string.no_account, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        adapter!!.swapCursor(null)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        selectSingleAccount(position)
    }

    fun selectSingleAccount(position: Int) {
        val adapter = adapter!!
        val account = adapter.getAccount(position)
        val data = Intent()
        data.putExtra(EXTRA_ID, account!!.account_key.id)
        data.putExtra(EXTRA_ACCOUNT_KEY, account.account_key)

        val startIntent = startIntent
        if (startIntent != null) {
            startIntent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key)
            startActivity(startIntent)
        }

        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firstCreated = savedInstanceState == null
        setContentView(R.layout.activity_account_selector)
        adapter = AccountsAdapter(this)
        val isSingleSelection = isSingleSelection
        accountsList.choiceMode = if (isSingleSelection) ListView.CHOICE_MODE_NONE else ListView.CHOICE_MODE_MULTIPLE
        adapter!!.setSwitchEnabled(!isSingleSelection)
        adapter!!.setSortEnabled(false)
        if (isSingleSelection) {
            accountsList.onItemClickListener = this
        }
        selectAccountButtons.visibility = if (isSingleSelection) View.GONE else View.VISIBLE
        accountsList.adapter = adapter
        loaderManager.initLoader(0, null, this)

    }

    override fun onStart() {
        super.onStart()
        contentResolver.registerContentObserver(Accounts.CONTENT_URI, true, mContentObserver)
    }

    override fun onResume() {
        super.onResume()
        val displayProfileImage = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)
        adapter!!.isProfileImageDisplayed = displayProfileImage
    }

    override fun onStop() {
        contentResolver.unregisterContentObserver(mContentObserver)
        super.onStop()
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
