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
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.CheckBox
import com.rengwuxian.materialedittext.MaterialEditText
import com.squareup.otto.Subscribe
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.UserList
import org.mariotaku.microblog.library.twitter.model.UserListUpdate
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.AccountSelectorActivity
import org.mariotaku.twidere.activity.UserListSelectorActivity
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.UserListSubscriptionEvent
import org.mariotaku.twidere.model.message.UserListUpdatedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.text.validator.UserListNameValidator
import org.mariotaku.twidere.util.*

class UserListFragment : AbsToolbarTabPagesFragment(), OnClickListener, LoaderCallbacks<SingleResponse<ParcelableUserList>>, SystemWindowsInsetsCallback, SupportFragmentCallback {

    private var userListLoaderInitialized: Boolean = false

    var userList: ParcelableUserList? = null
        private set

    fun displayUserList(userList: ParcelableUserList?) {
        val activity = activity ?: return
        loaderManager.destroyLoader(0)
        this.userList = userList

        if (userList != null) {
            activity.title = userList.name
        } else {
            activity.setTitle(R.string.user_list)
        }
        invalidateOptionsMenu()
    }

    fun getUserListInfo(omitIntentExtra: Boolean) {
        val lm = loaderManager
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
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER)
                twitter.addUserListMembersAsync(userList.account_key, userList.id, user)
                return
            }
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null || !data.hasExtra(EXTRA_ID)) return
                    val userList = this.userList
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    IntentUtils.openUserListDetails(activity, accountKey, userList!!.id,
                            userList.user_key, userList.user_screen_name, userList.name)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity
        setHasOptionsMenu(true)

        Utils.setNdefPushMessageCallback(activity, CreateNdefMessageCallback {
            val userList = userList ?: return@CreateNdefMessageCallback null
            NdefMessage(arrayOf(NdefRecord.createUri(LinkCreator.getTwitterUserListLink(userList.user_screen_name, userList.name))))
        })

        getUserListInfo(false)
    }

    override fun addTabs(adapter: SupportTabsAdapter) {
        val args = arguments
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
        adapter.addTab(UserListTimelineFragment::class.java, tabArgs, getString(R.string.statuses), null, 0, null)
        adapter.addTab(UserListMembersFragment::class.java, tabArgs, getString(R.string.members), null, 1, null)
        adapter.addTab(UserListSubscribersFragment::class.java, tabArgs, getString(R.string.subscribers), null, 2, null)
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
        loaderManager.destroyLoader(0)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_user_list, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val userList = this.userList
        MenuUtils.setItemAvailability(menu, R.id.info, userList != null)
        menu!!.removeGroup(MENU_GROUP_USER_LIST_EXTENSION)
        if (userList != null) {
            val isMyList = userList.user_key == userList.account_key
            val isFollowing = userList.is_following
            MenuUtils.setItemAvailability(menu, R.id.edit, isMyList)
            MenuUtils.setItemAvailability(menu, R.id.follow, !isMyList)
            MenuUtils.setItemAvailability(menu, R.id.add, isMyList)
            MenuUtils.setItemAvailability(menu, R.id.delete, isMyList)
            val followItem = menu.findItem(R.id.follow)
            if (isFollowing) {
                followItem.setIcon(R.drawable.ic_action_cancel)
                followItem.setTitle(R.string.unsubscribe)
            } else {
                followItem.setIcon(R.drawable.ic_action_add)
                followItem.setTitle(R.string.subscribe)
            }
            val extensionsIntent = Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST)
            extensionsIntent.setExtrasClassLoader(activity.classLoader)
            extensionsIntent.putExtra(EXTRA_USER_LIST, userList)
            MenuUtils.addIntentToMenu(activity, menu, extensionsIntent, MENU_GROUP_USER_LIST_EXTENSION)
        } else {
            MenuUtils.setItemAvailability(menu, R.id.edit, false)
            MenuUtils.setItemAvailability(menu, R.id.follow, false)
            MenuUtils.setItemAvailability(menu, R.id.add, false)
            MenuUtils.setItemAvailability(menu, R.id.delete, false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val twitter = twitterWrapper
        val userList = userList ?: return false
        when (item!!.itemId) {
            R.id.add -> {
                if (userList.user_key != userList.account_key) return false
                val intent = Intent(INTENT_ACTION_SELECT_USER)
                intent.setClass(activity, UserListSelectorActivity::class.java)
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
                df.arguments = Bundle()
                df.arguments.putParcelable(EXTRA_USER_LIST, userList)
                df.show(childFragmentManager, "user_list_details")
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
                IntentUtils.openUserProfile(activity, userList.account_key,
                        userList.user_key, userList.user_screen_name, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API), null)
            }
        }

    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<SingleResponse<ParcelableUserList>> {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val listId = args.getString(EXTRA_LIST_ID)
        val listName = args.getString(EXTRA_LIST_NAME)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        val omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
        return ParcelableUserListLoader(activity, omitIntentExtra, arguments, accountKey, listId,
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

    class EditUserListDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        private var mName: String? = null
        private var mDescription: String? = null
        private var mAccountKey: UserKey? = null
        private var mListId: String? = null
        private var mIsPublic: Boolean = false

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val alertDialog = dialog as AlertDialog
                    val editName = alertDialog.findViewById(R.id.name) as MaterialEditText?
                    val editDescription = alertDialog.findViewById(R.id.description) as MaterialEditText?
                    val editIsPublic = alertDialog.findViewById(R.id.is_public) as CheckBox?
                    assert(editName != null && editDescription != null && editIsPublic != null)
                    val name = ParseUtils.parseString(editName!!.text)
                    val description = ParseUtils.parseString(editDescription!!.text)
                    val isPublic = editIsPublic!!.isChecked
                    if (TextUtils.isEmpty(name)) return
                    val update = UserListUpdate()
                    update.setMode(if (isPublic) UserList.Mode.PUBLIC else UserList.Mode.PRIVATE)
                    update.setName(name)
                    update.setDescription(description)
                    twitterWrapper.updateUserListDetails(mAccountKey, mListId, update)
                }
            }

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val bundle = savedInstanceState ?: arguments
            mAccountKey = bundle?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            mListId = bundle?.getString(EXTRA_LIST_ID)
            mName = bundle?.getString(EXTRA_LIST_NAME)
            mDescription = bundle?.getString(EXTRA_DESCRIPTION)
            mIsPublic = bundle == null || bundle.getBoolean(EXTRA_IS_PUBLIC, true)
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_user_list_detail_editor)
            builder.setTitle(R.string.user_list)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, this)
            val dialog = builder.create()
            dialog.setOnShowListener { dialog ->
                val alertDialog = dialog as AlertDialog
                val editName = alertDialog.findViewById(R.id.name) as MaterialEditText?
                val editDescription = alertDialog.findViewById(R.id.description) as MaterialEditText?
                val editPublic = alertDialog.findViewById(R.id.is_public) as CheckBox?
                assert(editName != null && editDescription != null && editPublic != null)
                editName!!.addValidator(UserListNameValidator(getString(R.string.invalid_list_name)))
                if (mName != null) {
                    editName.setText(mName)
                }
                if (mDescription != null) {
                    editDescription!!.setText(mDescription)
                }
                editPublic!!.isChecked = mIsPublic
            }
            return dialog
        }

        override fun onSaveInstanceState(outState: Bundle?) {
            outState!!.putParcelable(EXTRA_ACCOUNT_KEY, mAccountKey)
            outState.putString(EXTRA_LIST_ID, mListId)
            outState.putString(EXTRA_LIST_NAME, mName)
            outState.putString(EXTRA_DESCRIPTION, mDescription)
            outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic)
            super.onSaveInstanceState(outState)
        }

    }

    internal class ParcelableUserListLoader(
            context: Context,
            private val omitIntentExtra: Boolean,
            private val extras: Bundle?,
            private val accountKey: UserKey,
            private val listId: String?,
            private val listName: String?,
            private val userKey: UserKey?,
            private val screenName: String?
    ) : AsyncTaskLoader<SingleResponse<ParcelableUserList>>(context) {

        override fun loadInBackground(): SingleResponse<ParcelableUserList> {
            if (!omitIntentExtra && extras != null) {
                val cache = extras.getParcelable<ParcelableUserList>(EXTRA_USER_LIST)
                if (cache != null) return SingleResponse.Companion.getInstance(cache)
            }
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey,
                    true) ?: return SingleResponse.Companion.getInstance<ParcelableUserList>()
            try {
                val list: UserList
                when {
                    listId != null -> {
                        list = twitter.showUserList(listId)
                    }
                    listName != null && userKey != null -> {
                        list = twitter.showUserList(listName, userKey.id)
                    }
                    listName != null && screenName != null -> {
                        list = twitter.showUserListByScrenName(listName, screenName)
                    }
                    else -> {
                        return SingleResponse.Companion.getInstance<ParcelableUserList>()
                    }
                }
                return SingleResponse.Companion.getInstance(ParcelableUserListUtils.from(list, accountKey))
            } catch (e: MicroBlogException) {
                return SingleResponse.Companion.getInstance<ParcelableUserList>(e)
            }

        }

        public override fun onStartLoading() {
            forceLoad()
        }

    }

    class UserListDetailsDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val userList = arguments.getParcelable<ParcelableUserList>(EXTRA_USER_LIST)
            val builder = AlertDialog.Builder(context)
            builder.setTitle(userList.name)
            builder.setMessage(userList.description)
            builder.setPositiveButton(android.R.string.ok, null)
            return builder.create()
        }
    }

}
