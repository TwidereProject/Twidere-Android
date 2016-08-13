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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.commons.parcel.ParcelUtils
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.UserClickListener
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.FriendshipTaskEvent
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper
import org.mariotaku.twidere.view.holder.UserViewHolder

abstract class ParcelableUsersFragment : AbsContentListRecyclerViewFragment<ParcelableUsersAdapter>,
        LoaderCallbacks<List<ParcelableUser>?>, UserClickListener, KeyboardShortcutCallback,
        IUsersAdapter.FriendshipClickListener {

    private val usersBusCallback: Any

    private var navigationHelper: RecyclerViewNavigationHelper? = null

    protected constructor() {
        usersBusCallback = createMessageBusCallback()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = adapter!!
        adapter.userClickListener = this

        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager!!, adapter,
                this)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(IntentConstants.EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
    }

    override fun onStart() {
        super.onStart()
        bus.register(usersBusCallback)
    }

    override fun onStop() {
        bus.unregister(usersBusCallback)
        super.onStop()
    }

    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return loaderManager.hasRunningLoaders()
        }
        set(value) {
            super.refreshing = value
        }

    override fun onCreateAdapter(context: Context): ParcelableUsersAdapter {
        val adapter = ParcelableUsersAdapter(context)
        adapter.followClickListener = this
        return adapter
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        val adapter = adapter ?: return
        adapter.setData(data)
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData(data)) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
            refreshEnabled = true
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        showContent()
        refreshEnabled = true
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int,
                                              event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper!!.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
                                              repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper!!.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int,
                                           event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper!!.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableUser>?> {
        val fromUser = args.getBoolean(IntentConstants.EXTRA_FROM_USER)
        args.remove(IntentConstants.EXTRA_FROM_USER)
        return onCreateUsersLoader(activity, args, fromUser)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
    }

    override fun onUserClick(holder: UserViewHolder, position: Int) {
        val user = adapter?.getUser(position) ?: return
        IntentUtils.openUserProfile(activity, user, null,
                preferences.getBoolean(SharedPreferenceConstants.KEY_NEW_DOCUMENT_API), userReferral)
    }

    override fun onFollowClicked(holder: UserViewHolder, position: Int) {
        val user = adapter?.getUser(position) ?: return
        if (twitterWrapper.isUpdatingRelationship(user.account_key, user.key)) return
        if (user.is_following) {
            DestroyFriendshipDialogFragment.show(fragmentManager, user)
        } else {
            twitterWrapper.createFriendshipAsync(user.account_key, user.key)
        }
    }

    override fun onUnblockClicked(holder: UserViewHolder, position: Int) {
        val user = adapter?.getUser(position) ?: return
        if (twitterWrapper.isUpdatingRelationship(user.account_key, user.key)) return
        twitterWrapper.destroyBlockAsync(user.account_key, user.key)
    }

    override fun onUnmuteClicked(holder: UserViewHolder, position: Int) {
        val user = adapter?.getUser(position) ?: return
        if (twitterWrapper.isUpdatingRelationship(user.account_key, user.key)) return
        twitterWrapper.destroyMuteAsync(user.account_key, user.key)
    }

    protected open val userReferral: String?
        @Referral
        get() = null

    override fun onUserLongClick(holder: UserViewHolder, position: Int): Boolean {
        return true
    }

    protected abstract fun onCreateUsersLoader(context: Context,
                                               args: Bundle,
                                               fromUser: Boolean): Loader<List<ParcelableUser>?>

    override fun createItemDecoration(context: Context, recyclerView: RecyclerView,
                                      layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val adapter = adapter ?: return null
        val itemDecoration = DividerItemDecoration(context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation)
        val res = context.resources
        if (adapter.profileImageEnabled) {
            val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 2 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
            itemDecoration.setPadding { position, rect ->
                val itemViewType = adapter.getItemViewType(position)
                var nextItemIsUser = false
                if (position < adapter.itemCount - 1) {
                    nextItemIsUser = adapter.getItemViewType(position + 1) == ParcelableUsersAdapter.ITEM_VIEW_TYPE_USER
                }
                if (nextItemIsUser && itemViewType == ParcelableUsersAdapter.ITEM_VIEW_TYPE_USER) {
                    rect.left = decorPaddingLeft
                } else {
                    rect.left = 0
                }
                true
            }
        }
        itemDecoration.setDecorationEndOffset(1)
        return itemDecoration
    }

    private fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
        return adapter?.findPosition(accountKey, userKey) ?: RecyclerView.NO_POSITION
    }

    protected open fun shouldRemoveUser(position: Int, event: FriendshipTaskEvent): Boolean {
        return false
    }

    protected fun hasMoreData(data: List<ParcelableUser>?): Boolean {
        return data == null || !data.isEmpty()
    }

    protected fun createMessageBusCallback(): Any {
        return UsersBusCallback()
    }

    protected inner class UsersBusCallback {

        @Subscribe
        fun onFriendshipTaskEvent(event: FriendshipTaskEvent) {
            val adapter = adapter ?: return
            val position = findPosition(event.accountKey, event.userKey)
            val data = adapter.getData() ?: return
            if (position < 0 || position >= data.size) return
            if (shouldRemoveUser(position, event)) {
                adapter.removeUserAt(position)
            } else {
                val adapterUser = data[position]
                val eventUser = event.user
                if (eventUser != null) {
                    if (adapterUser.account_key == eventUser.account_key) {
                        val clone = ParcelUtils.clone(eventUser)
                        clone.position = adapterUser.position
                        adapter.setUserAt(position, clone)
                    }
                }
                adapter.notifyItemChanged(position)
            }
        }

    }
}
