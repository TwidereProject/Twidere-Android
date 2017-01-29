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
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_user_selector.*
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter
import org.mariotaku.twidere.adapter.UserAutoCompleteAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.CreateUserListDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.util.AsyncTaskUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.ParseUtils
import java.util.*

class UserSelectorActivity : BaseActivity(), OnClickListener, OnItemClickListener {

    private lateinit var usersAdapter: SimpleParcelableUsersAdapter

    private var screenName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (!intent.hasExtra(EXTRA_ACCOUNT_KEY)) {
            finish()
            return
        }
        setContentView(R.layout.activity_user_selector)
        if (savedInstanceState == null) {
            screenName = intent.getStringExtra(EXTRA_SCREEN_NAME)
        } else {
            screenName = savedInstanceState.getString(EXTRA_SCREEN_NAME)
        }

        if (!isEmpty(screenName)) {
            searchUser(screenName!!)
        }
        val adapter = UserAutoCompleteAdapter(this)
        adapter.accountKey = accountKey
        editScreenName.setAdapter(adapter)
        editScreenName.setText(screenName)
        usersAdapter = SimpleParcelableUsersAdapter(this)
        usersList.adapter = usersAdapter
        usersList.onItemClickListener = this
        screenNameConfirm.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.screenNameConfirm -> {
                val screen_name = ParseUtils.parseString(editScreenName.text)
                if (isEmpty(screen_name)) return
                searchUser(screen_name)
            }
            R.id.createList -> {
                val f = CreateUserListDialogFragment()
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
                f.arguments = args
                f.show(supportFragmentManager, null)
            }
        }
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val list = view as ListView
        val user = usersAdapter.getItem(position - list.headerViewsCount) ?: return
        val data = Intent()
        data.setExtrasClassLoader(classLoader)
        data.putExtra(EXTRA_USER, user)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    fun setUsersData(data: List<ParcelableUser>) {
        usersAdapter.setData(data, true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_SCREEN_NAME, screenName)
    }

    private val accountKey: UserKey
        get() = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)


    private fun searchUser(name: String) {
        val task = SearchUsersTask(this, accountKey, name)
        AsyncTaskUtils.executeTask(task)
    }

    private fun dismissDialogFragment(tag: String) {
        executeAfterFragmentResumed { activity ->
            val fm = activity.supportFragmentManager
            val f = fm.findFragmentByTag(tag)
            if (f is DialogFragment) {
                f.dismiss()
            }
        }
    }

    private fun showDialogFragment(df: DialogFragment, tag: String) {
        executeAfterFragmentResumed { activity ->
            df.show(activity.supportFragmentManager, tag)
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


    private class SearchUsersTask(
            private val activity: UserSelectorActivity,
            private val accountKey: UserKey,
            private val name: String
    ) : AsyncTask<Any, Any, SingleResponse<List<ParcelableUser>>>() {

        override fun doInBackground(vararg params: Any): SingleResponse<List<ParcelableUser>> {
            val twitter = MicroBlogAPIFactory.getInstance(activity, accountKey) ?: return SingleResponse.getInstance<List<ParcelableUser>>()
            try {
                val paging = Paging()
                val lists = twitter.searchUsers(name, paging)
                val data = ArrayList<ParcelableUser>()
                for (item in lists) {
                    data.add(ParcelableUserUtils.fromUser(item, accountKey))
                }
                return SingleResponse.getInstance<List<ParcelableUser>>(data)
            } catch (e: MicroBlogException) {
                Log.w(LOGTAG, e)
                return SingleResponse.getInstance<List<ParcelableUser>>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<List<ParcelableUser>>) {
            activity.dismissDialogFragment(FRAGMENT_TAG_SEARCH_USERS)
            if (result.data != null) {
                activity.setUsersData(result.data)
            }
        }

        override fun onPreExecute() {
            val df = ProgressDialogFragment()
            df.isCancelable = false
            activity.showDialogFragment(df, FRAGMENT_TAG_SEARCH_USERS)
        }

        companion object {

            private const val FRAGMENT_TAG_SEARCH_USERS = "search_users"
        }

    }


}
