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

package org.mariotaku.twidere.fragment

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.FixedAsyncTaskLoader
import androidx.loader.content.Loader
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import com.squareup.otto.Subscribe
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.UserList
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.neutral
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowInsetsCallback
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.fragment.statuses.UserListTimelineFragment
import org.mariotaku.twidere.fragment.users.UserListMembersFragment
import org.mariotaku.twidere.fragment.users.UserListSubscribersFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListSubscriptionEvent
import org.mariotaku.twidere.model.event.UserListUpdatedEvent
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.shortcut.ShortcutCreator

class UserListFragment : AbsToolbarTabPagesFragment(), OnClickListener,
        LoaderCallbacks<SingleResponse<ParcelableUserList>>, SystemWindowInsetsCallback,
        SupportFragmentCallback {

    private var userListLoaderInitialized: Boolean = false

    var userList: ParcelableUserList? = null
        private set

    fun displayUserList(userList: ParcelableUserList?) {
        val activity = activity ?: return
        LoaderManager.getInstance(this).destroyLoader(0)
        this.userList = userList

        if (userList != null) {
            activity.title = userList.name
        } else {
            activity.setTitle(R.string.title_user_list)
        }
        activity.invalidateOptionsMenu()
    }

    fun getUserListInfo(omitIntentExtra: Boolean) {
        val lm = LoaderManager.getInstance(this)
        lm.destroyLoader(0)
        val args = Bundle(arguments)
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra)
        if (!userListLoaderInitialized) {
            lm.initLoader(0, args, this)
            userListLoaderInitialized = true
        } else {
            lm.restartLoader(0, args, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val twitter = twitterWrapper
        when (requestCode) {
            REQUEST_SELECT_USER -> {
                val userList = this.userList
                if (resultCode != Activity.RESULT_OK || !data!!.hasExtra(EXTRA_USER) || userList == null)
                    return
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER) ?: return
                twitter.addUserListMembersAsync(userList.account_key, userList.id, user)
                return
            }
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return
                    val userList = this.userList
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    activity?.let {
                        IntentUtils.openUserListDetails(it, accountKey, userList!!.id,
                                userList.user_key, userList.user_screen_name, userList.name)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity
        setHasOptionsMenu(true)

        if (activity != null) {
            Utils.setNdefPushMessageCallback(activity, CreateNdefMessageCallback {
                val userList = userList ?: return@CreateNdefMessageCallback null
                NdefMessage(arrayOf(NdefRecord.createUri(LinkCreator.getTwitterUserListLink(userList.user_screen_name, userList.name))))
            })
        }

        getUserListInfo(false)
    }

    override fun addTabs(adapter: SupportTabsAdapter) {
        val args = arguments ?: return
        val tabArgs = Bundle()
        if (args.containsKey(EXTRA_USER_LIST)) {
            val userList = args.getParcelable<ParcelableUserList>(EXTRA_USER_LIST)!!
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, userList.account_key)
            tabArgs.putParcelable(EXTRA_USER_KEY, userList.user_key)
            tabArgs.putString(EXTRA_SCREEN_NAME, userList.user_screen_name)
            tabArgs.putString(EXTRA_LIST_ID, userList.id)
            tabArgs.putString(EXTRA_LIST_NAME, userList.name)
        } else {
            tabArgs.putParcelable(EXTRA_ACCOUNT_KEY, args.getParcelable(EXTRA_ACCOUNT_KEY))
            tabArgs.putParcelable(EXTRA_USER_KEY, args.getParcelable(EXTRA_USER_KEY))
            tabArgs.putString(EXTRA_SCREEN_NAME, args.getString(EXTRA_SCREEN_NAME))
            tabArgs.putString(EXTRA_LIST_ID, args.getString(EXTRA_LIST_ID))
            tabArgs.putString(EXTRA_LIST_NAME, args.getString(EXTRA_LIST_NAME))
        }
        adapter.add(cls = UserListTimelineFragment::class.java, args = tabArgs, name = getString(R.string.title_statuses))
        adapter.add(cls = UserListMembersFragment::class.java, args = tabArgs, name = getString(R.string.members))
        adapter.add(cls = UserListSubscribersFragment::class.java, args = tabArgs, name = getString(R.string.title_userlist_subscribers))
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onDestroyView() {
        userList = null
        LoaderManager.getInstance(this).destroyLoader(0)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_user_list, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val userList = this.userList
        menu.setItemAvailability(R.id.info, userList != null)
        menu.removeGroup(MENU_GROUP_USER_LIST_EXTENSION)
        if (userList != null) {
            val isMyList = userList.user_key == userList.account_key
            val isFollowing = userList.is_following
            menu.setItemAvailability(R.id.edit, isMyList)
            menu.setItemAvailability(R.id.follow, !isMyList)
            menu.setItemAvailability(R.id.add, isMyList)
            menu.setItemAvailability(R.id.delete, isMyList)
            val followItem = menu.findItem(R.id.follow)
            if (isFollowing) {
                followItem.setIcon(R.drawable.ic_action_cancel)
                followItem.setTitle(R.string.action_unsubscribe)
            } else {
                followItem.setIcon(R.drawable.ic_action_add)
                followItem.setTitle(R.string.action_subscribe)
            }
            val extensionsIntent = Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST)
            extensionsIntent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
            extensionsIntent.putExtra(EXTRA_USER_LIST, userList)
            activity?.let {
                MenuUtils.addIntentToMenu(it, menu, extensionsIntent, MENU_GROUP_USER_LIST_EXTENSION)
                menu.setItemAvailability(R.id.add_to_home_screen_submenu, ShortcutManagerCompat.isRequestPinShortcutSupported(it))
            }
        } else {
            menu.setItemAvailability(R.id.edit, false)
            menu.setItemAvailability(R.id.follow, false)
            menu.setItemAvailability(R.id.add, false)
            menu.setItemAvailability(R.id.delete, false)
            menu.setItemAvailability(R.id.add_to_home_screen_submenu, false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val twitter = twitterWrapper
        val userList = userList ?: return false
        val activity = activity ?: return false
        val fragmentManager = parentFragmentManager
        when (item.itemId) {
            R.id.add -> {
                if (userList.user_key != userList.account_key) return false
                val intent = Intent(INTENT_ACTION_SELECT_USER)
                intent.setClass(activity, UserSelectorActivity::class.java)
                intent.putExtra(EXTRA_ACCOUNT_KEY, userList.account_key)
                startActivityForResult(intent, REQUEST_SELECT_USER)
            }
            R.id.delete -> {
                if (userList.user_key != userList.account_key) return false
                DestroyUserListDialogFragment.show(fragmentManager, userList)
            }
            R.id.edit -> {
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT_KEY, userList.account_key)
                args.putString(EXTRA_LIST_NAME, userList.name)
                args.putString(EXTRA_DESCRIPTION, userList.description)
                args.putBoolean(EXTRA_IS_PUBLIC, userList.is_public)
                args.putString(EXTRA_LIST_ID, userList.id)
                val f = EditUserListDialogFragment()
                f.arguments = args
                f.show(fragmentManager, "edit_user_list_details")
                return true
            }
            R.id.follow -> {
                if (userList.is_following) {
                    DestroyUserListSubscriptionDialogFragment.show(fragmentManager, userList)
                } else {
                    twitter.createUserListSubscriptionAsync(userList.account_key, userList.id)
                }
                return true
            }
            R.id.open_with_account -> {
                val intent = Intent(INTENT_ACTION_SELECT_ACCOUNT)
                intent.setClass(activity, AccountSelectorActivity::class.java)
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
            }
            R.id.info -> {
                val df = UserListDetailsDialogFragment()
                df.arguments = Bundle().apply {
                    putParcelable(EXTRA_USER_LIST, userList)
                }
                df.show(childFragmentManager, "user_list_details")
            }
            R.id.add_statuses_to_home_screen -> {
                ShortcutCreator.performCreation(this) {
                    ShortcutCreator.userListTimeline(activity, userList.account_key, userList)
                }
            }
            else -> {
                if (item.intent != null) {
                    try {
                        startActivity(item.intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.w(LOGTAG, e)
                        return false
                    }

                }
            }
        }
        return true
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.errorContainer -> {
                getUserListInfo(true)
            }
            R.id.profileImage -> {
                val userList = this.userList ?: return
                activity?.let {
                    IntentUtils.openUserProfile(it, userList.account_key, userList.user_key,
                            userList.user_screen_name, null, preferences[newDocumentApiKey], null)
                }
            }
        }

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<SingleResponse<ParcelableUserList>> {
        val accountKey = args!!.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey?>(EXTRA_USER_KEY)
        val listId = args.getString(EXTRA_LIST_ID)
        val listName = args.getString(EXTRA_LIST_NAME)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
        return ParcelableUserListLoader(requireActivity(), omitIntentExtra, arguments, accountKey, listId,
                listName, userKey, screenName)
    }

    override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableUserList>>,
                                data: SingleResponse<ParcelableUserList>?) {
        if (data == null) return
        if (activity == null) return
        if (data.hasData()) {
            val list = data.data
            displayUserList(list)
        } else if (data.hasException()) {
        }
    }

    override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableUserList>>) {

    }

    @Subscribe
    fun onUserListUpdated(event: UserListUpdatedEvent) {
        if (userList == null) return
        if (TextUtils.equals(event.userList.id, userList!!.id)) {
            getUserListInfo(true)
        }
    }

    @Subscribe
    fun onUserListSubscriptionChanged(event: UserListSubscriptionEvent) {
        if (userList == null) return
        if (TextUtils.equals(event.userList.id, userList!!.id)) {
            getUserListInfo(true)
        }
    }

    internal class ParcelableUserListLoader(
            context: Context,
            private val omitIntentExtra: Boolean,
            private val extras: Bundle?,
            private val accountKey: UserKey?,
            private val listId: String?,
            private val listName: String?,
            private val userKey: UserKey?,
            private val screenName: String?
    ) : FixedAsyncTaskLoader<SingleResponse<ParcelableUserList>>(context) {

        override fun loadInBackground(): SingleResponse<ParcelableUserList> {
            if (!omitIntentExtra && extras != null) {
                val cache = extras.getParcelable<ParcelableUserList>(EXTRA_USER_LIST)
                if (cache != null) return SingleResponse(cache)
            }
            try {
                if (accountKey == null) throw MicroBlogException("No account")
                val twitter = MicroBlogAPIFactory.getInstance(context, accountKey)
                        ?: throw MicroBlogException("No account")
                val list: UserList
                list = when {
                    listId != null -> {
                        twitter.showUserList(listId)
                    }
                    listName != null && userKey != null -> {
                        twitter.showUserList(listName, userKey.id)
                    }
                    listName != null && screenName != null -> {
                        twitter.showUserListByScrenName(listName, screenName)
                    }
                    else -> {
                        return SingleResponse(MicroBlogException("Invalid argument"))
                    }
                }
                return SingleResponse(list.toParcelable(accountKey))
            } catch (e: MicroBlogException) {
                return SingleResponse(e)
            }

        }

        override fun onStartLoading() {
            forceLoad()
        }

    }

    class UserListDetailsDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val userList = requireArguments().getParcelable<ParcelableUserList>(EXTRA_USER_LIST)!!
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(userList.name)
            builder.setMessage(userList.description)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.neutral(R.string.action_list_creator) {
                startActivity(IntentUtils.userProfile(userList.account_key, userList.user_key,
                        userList.user_screen_name))
            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }
    }

}
