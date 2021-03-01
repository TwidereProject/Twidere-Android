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
import android.text.TextUtils.isEmpty
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_user_selector.*
import kotlinx.android.synthetic.main.layout_list_with_empty_view.*
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.isNotNullOrEmpty
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.CacheUserSearchLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.EditTextEnterHandler
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.view.SimpleTextWatcher

class UserSelectorActivity : BaseActivity(), OnItemClickListener, LoaderManager.LoaderCallbacks<List<ParcelableUser>> {

    private lateinit var adapter: SimpleParcelableUsersAdapter

    private val accountKey: UserKey?
        get() = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)

    private var loaderInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val accountKey = this.accountKey ?: run {
            finish()
            return
        }
        setContentView(R.layout.activity_user_selector)

        val enterHandler = EditTextEnterHandler.attach(editScreenName, object : EditTextEnterHandler.EnterListener {
            override fun onHitEnter(): Boolean {
                val screenName = ParseUtils.parseString(editScreenName.text)
                searchUser(accountKey, screenName, false)
                return true
            }

            override fun shouldCallListener(): Boolean {
                return true
            }

        }, true)

        enterHandler.addTextChangedListener(object : SimpleTextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchUser(accountKey, s.toString(), true)
            }
        })

        screenNameConfirm.setOnClickListener {
            val screenName = ParseUtils.parseString(editScreenName.text)
            searchUser(accountKey, screenName, false)
        }

        if (savedInstanceState == null) {
            editScreenName.setText(intent.getStringExtra(EXTRA_SCREEN_NAME))
        }
        adapter = SimpleParcelableUsersAdapter(this, requestManager = requestManager)
        listView.adapter = adapter
        listView.onItemClickListener = this

        showSearchHint()
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val list = view as ListView
        val user = adapter.getItem(position - list.headerViewsCount) ?: return
        val data = Intent()
        data.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        data.putExtra(EXTRA_USER, user)
        data.putExtra(EXTRA_EXTRAS, intent.getBundleExtra(EXTRA_EXTRAS))
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableUser>> {
        val accountKey = args?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)!!
        val query = args.getString(EXTRA_QUERY).orEmpty()
        val fromCache = args.getBoolean(EXTRA_FROM_CACHE)
        if (!fromCache) {
            showProgress()
        }
        return CacheUserSearchLoader(this, accountKey, query, !fromCache,
            fromCache = true,
            fromUser = true
        )
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>>) {
        adapter.setData(null, true)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>>, data: List<ParcelableUser>?) {
        progressContainer.visibility = View.GONE
        listContainer.visibility = View.VISIBLE
        adapter.setData(data, true)
        loader as CacheUserSearchLoader
        when {
            data.isNotNullOrEmpty() -> {
                showList()
            }
            loader.query.isEmpty() -> {
                showSearchHint()
            }
            else -> {
                showNotFound()
            }
        }
    }

    private fun searchUser(accountKey: UserKey, query: String, fromCache: Boolean) {
        if (isEmpty(query)) {
            showSearchHint()
            return
        }
        val args = Bundle {
            this[EXTRA_ACCOUNT_KEY] = accountKey
            this[EXTRA_QUERY] = query
            this[EXTRA_FROM_CACHE] = fromCache
        }
        if (loaderInitialized) {
            LoaderManager.getInstance(this).initLoader(0, args, this)
            loaderInitialized = true
        } else {
            LoaderManager.getInstance(this).restartLoader(0, args, this)
        }
    }

    private fun showProgress() {
        progressContainer.visibility = View.VISIBLE
        listContainer.visibility = View.GONE
    }

    private fun showSearchHint() {
        progressContainer.visibility = View.GONE
        listContainer.visibility = View.VISIBLE
        emptyView.visibility = View.VISIBLE
        listView.visibility = View.GONE
        emptyIcon.setImageResource(R.drawable.ic_info_search)
        emptyText.text = getText(R.string.search_hint_users)
    }

    private fun showNotFound() {
        progressContainer.visibility = View.GONE
        listContainer.visibility = View.VISIBLE
        emptyView.visibility = View.VISIBLE
        listView.visibility = View.GONE
        emptyIcon.setImageResource(R.drawable.ic_info_search)
        emptyText.text = getText(R.string.search_hint_users)
    }

    private fun showList() {
        progressContainer.visibility = View.GONE
        listContainer.visibility = View.VISIBLE
        listView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }
}
