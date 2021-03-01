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
import android.content.Intent
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import androidx.loader.app.hasRunningLoadersSafe
import kotlinx.android.synthetic.main.layout_list_with_empty_view.*
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.contains
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_SELECT_USER
import org.mariotaku.twidere.adapter.SimpleParcelableUserListsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.loader.userlists.UserListOwnershipsLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.util.ContentScrollHandler
import org.mariotaku.twidere.util.ListViewScrollHandler

class UserListSelectorActivity : BaseActivity(),
        ContentScrollHandler.ContentListSupport<SimpleParcelableUserListsAdapter>,
        LoaderManager.LoaderCallbacks<List<ParcelableUserList>> {

    override lateinit var adapter: SimpleParcelableUserListsAdapter

    override var refreshing: Boolean
        get() {
            return LoaderManager.getInstance(this).hasRunningLoadersSafe()
        }
        set(value) {
        }

    override val reachingStart: Boolean
        get() = listView.firstVisiblePosition <= 0

    override val reachingEnd: Boolean
        get() = listView.lastVisiblePosition >= listView.count - 1

    private val accountKey: UserKey?
        get() = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
    private val showMyLists: Boolean
        get() = intent.getBooleanExtra(EXTRA_SHOW_MY_LISTS, false)

    private var userKey: UserKey? = null
    private var nextPagination: Pagination? = null

    private var loaderInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accountKey = accountKey ?: run {
            finish()
            return
        }
        setContentView(R.layout.activity_user_list_selector)

        adapter = SimpleParcelableUserListsAdapter(this, requestManager)
        adapter.loadMoreSupportedPosition = ILoadMoreSupportAdapter.END
        listView.addFooterView(layoutInflater.inflate(R.layout.simple_list_item_activated_1,
                listView, false).apply {
            findViewById<TextView>(android.R.id.text1).setText(R.string.action_select_user)
        }, SelectUserAction, true)
        listView.adapter = adapter
        val handler = ListViewScrollHandler(this, listView)
        listView.setOnScrollListener(handler)
        listView.setOnTouchListener(handler.touchListener)
        listView.onItemClickListener = OnItemClickListener { view, _, position, _ ->
            when (val item = view.getItemAtPosition(position)) {
                is ParcelableUserList -> {
                    val data = Intent()
                    data.putExtra(EXTRA_USER_LIST, item)
                    data.putExtra(EXTRA_EXTRAS, intent.getBundleExtra(EXTRA_EXTRAS))
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
                is SelectUserAction -> {
                    selectUser()
                }
            }
        }

        val userKey = intent.getParcelableExtra<UserKey>(EXTRA_USER_KEY) ?: if (showMyLists) {
            accountKey
        } else {
            null
        }

        if (userKey != null) {
            loadUserLists(accountKey, userKey)
        } else if (savedInstanceState == null) {
            selectUser()
        }
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_USER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER) ?: return
                    loadUserLists(accountKey!!, user.key)
                }
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableUserList>> {
        val accountKey = args?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val userKey = args?.getParcelable<UserKey>(EXTRA_USER_KEY)
        return UserListOwnershipsLoader(this, accountKey, userKey, null, adapter.all).apply {
            pagination = args?.getParcelable(EXTRA_PAGINATION)
        }
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUserList>>) {
        adapter.setData(null)
    }


    override fun onLoadFinished(loader: Loader<List<ParcelableUserList>>, data: List<ParcelableUserList>) {
        adapter.loadMoreIndicatorPosition = ILoadMoreSupportAdapter.NONE
        adapter.loadMoreSupportedPosition = if (adapter.all != data) {
            ILoadMoreSupportAdapter.END
        } else {
            ILoadMoreSupportAdapter.NONE
        }
        adapter.setData(data)
        refreshing = false
        if (loader is IPaginationLoader) {
            nextPagination = loader.nextPagination
        }
        showList()
    }

    override fun setControlVisible(visible: Boolean) {
    }


    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        val accountKey = this.accountKey ?: return
        val userKey = this.userKey ?: return
        if (refreshing || position !in adapter.loadMoreSupportedPosition) {
            return
        }
        adapter.loadMoreIndicatorPosition = position
        loadUserLists(accountKey, userKey, nextPagination)
    }

    private fun loadUserLists(accountKey: UserKey, userKey: UserKey, pagination: Pagination? = null) {
        if (userKey != this.userKey) {
            adapter.clear()
            showProgress()
            this.userKey = userKey
        }
        val args = Bundle {
            this[EXTRA_ACCOUNT_KEY] = accountKey
            this[EXTRA_USER_KEY] = userKey
            this[EXTRA_PAGINATION] = pagination
        }
        if (!loaderInitialized) {
            loaderInitialized = true
            LoaderManager.getInstance(this).initLoader(0, args, this)
        } else {
            LoaderManager.getInstance(this).restartLoader(0, args, this)
        }
    }

    private fun showProgress() {
        progressContainer.visibility = View.VISIBLE
        listContainer.visibility = View.GONE
    }

    private fun showList() {
        progressContainer.visibility = View.GONE
        listContainer.visibility = View.VISIBLE
        listView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun selectUser() {
        val selectUserIntent = Intent(this, UserSelectorActivity::class.java)
        selectUserIntent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        startActivityForResult(selectUserIntent, REQUEST_SELECT_USER)
    }

    object SelectUserAction

}
