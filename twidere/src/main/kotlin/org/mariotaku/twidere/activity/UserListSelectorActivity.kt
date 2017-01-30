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
import android.support.v4.app.LoaderManager
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_user_list_selector.*
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.http.HttpResponseCode
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.adapter.SimpleParcelableUserListsAdapter
import org.mariotaku.twidere.adapter.UserAutoCompleteAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.CreateUserListDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.UserListCreatedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.util.AsyncTaskUtils
import org.mariotaku.twidere.util.DataStoreUtils.getAccountScreenName
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import java.util.*

class UserListSelectorActivity : BaseActivity(), OnClickListener, OnItemClickListener {

    private lateinit var userListsAdapter: SimpleParcelableUserListsAdapter

    private var screenName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (!intent.hasExtra(EXTRA_ACCOUNT_KEY)) {
            finish()
            return
        }
        setContentView(R.layout.activity_user_list_selector)
        if (savedInstanceState == null) {
            screenName = intent.getStringExtra(EXTRA_SCREEN_NAME)
        } else {
            screenName = savedInstanceState.getString(EXTRA_SCREEN_NAME)
        }

        if (!isEmpty(screenName)) {
            getUserLists(screenName)
        }
        val adapter = UserAutoCompleteAdapter(this)
        adapter.accountKey = accountKey
        userListsAdapter = SimpleParcelableUserListsAdapter(this)
        userListsList.adapter = userListsAdapter
        userListsList.onItemClickListener = this
        createList.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
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
        val data = Intent()
        data.putExtra(EXTRA_USER_LIST, userListsAdapter.getItem(position - list.headerViewsCount))
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_SCREEN_NAME, screenName)
    }

    @Subscribe
    fun onUserListCreated(event: UserListCreatedEvent) {
        getUserLists(screenName)
    }

    private val accountKey: UserKey
        get() = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)

    private fun getUserLists(screenName: String?) {
        if (screenName == null) return
        this.screenName = screenName
        val task = GetUserListsTask(this, accountKey, screenName)
        AsyncTaskUtils.executeTask(task)
    }

    private val isSelectingUser: Boolean
        get() = INTENT_ACTION_SELECT_USER == intent.action

    private fun setUserListsData(data: List<ParcelableUserList>, isMyAccount: Boolean) {
        userListsAdapter.setData(data, true)
        userListsContainer.visibility = View.VISIBLE
        createListContainer.visibility = if (isMyAccount) View.VISIBLE else View.GONE
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

    private class GetUserListsTask(
            private val activity: UserListSelectorActivity,
            private val accountKey: UserKey,
            private val screenName: String
    ) : AsyncTask<Any, Any, SingleResponse<List<ParcelableUserList>>>() {

        override fun doInBackground(vararg params: Any): SingleResponse<List<ParcelableUserList>> {
            val twitter = MicroBlogAPIFactory.getInstance(activity, accountKey) ?: return SingleResponse.getInstance<List<ParcelableUserList>>()
            try {
                val lists = twitter.getUserListsByScreenName(screenName, true)
                val data = ArrayList<ParcelableUserList>()
                var isMyAccount = screenName.equals(getAccountScreenName(activity,
                        accountKey), ignoreCase = true)
                for (item in lists) {
                    val user = item.user
                    if (user != null && screenName.equals(user.screenName, ignoreCase = true)) {
                        if (!isMyAccount && TextUtils.equals(user.id, accountKey.id)) {
                            isMyAccount = true
                        }
                        data.add(ParcelableUserListUtils.from(item, accountKey))
                    }
                }
                val result = SingleResponse.getInstance<List<ParcelableUserList>>(data)
                result.extras.putBoolean(EXTRA_IS_MY_ACCOUNT, isMyAccount)
                return result
            } catch (e: MicroBlogException) {
                Log.w(LOGTAG, e)
                return SingleResponse.getInstance<List<ParcelableUserList>>(e)
            }

        }

        override fun onPostExecute(result: SingleResponse<List<ParcelableUserList>>) {
            activity.dismissDialogFragment(FRAGMENT_TAG_GET_USER_LISTS)
            if (result.data != null) {
                activity.setUserListsData(result.data, result.extras.getBoolean(EXTRA_IS_MY_ACCOUNT))
            } else if (result.exception is MicroBlogException) {
                if (result.exception.statusCode == HttpResponseCode.NOT_FOUND) {
//                    activity.searchUser(screenName)
                }
            }
        }

        override fun onPreExecute() {
            val df = ProgressDialogFragment()
            df.isCancelable = false
            activity.showDialogFragment(df, FRAGMENT_TAG_GET_USER_LISTS)
        }

        companion object {

            private const val FRAGMENT_TAG_GET_USER_LISTS = "get_user_lists"
        }

    }


}
