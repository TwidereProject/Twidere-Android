/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.accounts.AccountManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.commons.parcel.ParcelUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.UserClickListener
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.LoadMorePosition
import org.mariotaku.twidere.constant.RecyclerViewTypes
import org.mariotaku.twidere.constant.loadItemLimitKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.data.ExceptionLiveData
import org.mariotaku.twidere.data.UsersDataSourceFactory
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.data.observe
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.promise.BlockPromises
import org.mariotaku.twidere.promise.FriendshipPromises
import org.mariotaku.twidere.promise.MutePromises
import org.mariotaku.twidere.singleton.BusSingleton
import org.mariotaku.twidere.singleton.PreferencesSingleton
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper
import org.mariotaku.twidere.view.holder.UserViewHolder

abstract class AbsUsersFragment : AbsContentListRecyclerViewFragment<ParcelableUsersAdapter>(),
        UserClickListener, KeyboardShortcutCallback, IUsersAdapter.FriendshipClickListener {

    protected open val simpleLayout: Boolean
        get() = arguments!!.simpleLayout

    protected open val showFollow: Boolean
        get() = true

    private lateinit var navigationHelper: RecyclerViewNavigationHelper
    private val usersBusCallback: Any
    private var users: LiveData<SingleResponse<PagedList<ParcelableUser>?>>? = null

    init {
        usersBusCallback = createMessageBusCallback()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.userClickListener = this

        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this)

        setupLiveData()
        showProgress()
    }

    override fun onStart() {
        super.onStart()
        BusSingleton.register(usersBusCallback)
    }

    override fun onStop() {
        BusSingleton.unregister(usersBusCallback)
        super.onStop()
    }

    override fun onLoadMoreContents(@LoadMorePosition position: Int) {
        // No-op
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableUsersAdapter {
        val adapter = ParcelableUsersAdapter(context, Glide.with(this))
        adapter.simpleLayout = simpleLayout
        adapter.showFollow = showFollow
        val accountType = arguments?.accountKey?.let { key ->
            val am = AccountManager.get(context)
            return@let am.findAccount(key)?.getAccountType(am)
        }
        when (accountType) {
            AccountType.TWITTER, AccountType.FANFOU, AccountType.STATUSNET -> {
                adapter.friendshipClickListener = this
            }
            else -> {
                adapter.friendshipClickListener = null
            }
        }
        return adapter
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int,
            event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
            repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }


    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int,
            event: KeyEvent, metaState: Int): Boolean {
        return navigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun onUserClick(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        IntentUtils.openUserProfile(activity!!, user, PreferencesSingleton.get(context!!)[newDocumentApiKey])
    }

    override fun onFollowClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        if (FriendshipPromises.isRunning(accountKey, user.key)) return
        if (user.is_following) {
            DestroyFriendshipDialogFragment.show(fragmentManager!!, user)
        } else {
            FriendshipPromises.get(context!!).create(accountKey, user.key, user.screen_name)
        }
    }

    override fun onUnblockClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        if (FriendshipPromises.isRunning(accountKey, user.key)) return
        BlockPromises.get(context!!).unblock(accountKey, user.key)
    }

    override fun onUnmuteClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        if (FriendshipPromises.isRunning(accountKey, user.key)) return
        MutePromises.get(context!!).unmute(accountKey, user.key)
    }


    override fun onUserLongClick(holder: UserViewHolder, position: Int): Boolean {
        return true
    }

    override fun onCreateItemDecoration(context: Context, recyclerView: RecyclerView,
            layoutManager: LinearLayoutManager): RecyclerView.ItemDecoration? {
        val itemDecoration = ExtendedDividerItemDecoration(context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation)
        val res = context.resources
        if (adapter.profileImageEnabled) {
            val decorPaddingLeft = res.getDimensionPixelSize(R.dimen.element_spacing_normal) * 2 + res.getDimensionPixelSize(R.dimen.icon_size_status_profile_image)
            itemDecoration.setPadding { position, rect ->
                val itemViewType = adapter.getItemViewType(position)
                var nextItemIsUser = false
                if (position < adapter.itemCount - 1) {
                    nextItemIsUser = adapter.getItemViewType(position + 1) == RecyclerViewTypes.USER
                }
                if (nextItemIsUser && itemViewType == RecyclerViewTypes.USER) {
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

    protected open fun onDataLoaded(data: PagedList<ParcelableUser>?) {
        adapter.users = data
        when {
            data == null || data.isEmpty() -> {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
            else -> {
                showContent()
            }
        }
    }

    protected open fun getMaxLoadItemLimit(forAccount: UserKey): Int {
        return 200
    }

    private fun onCreateLiveData(): LiveData<SingleResponse<PagedList<ParcelableUser>?>>? {
        val merger = MediatorLiveData<SingleResponse<PagedList<ParcelableUser>?>>()
        val context = context!!
        val accountKey = arguments!!.accountKey!!

        val errorLiveData = MutableLiveData<SingleResponse<PagedList<ParcelableUser>?>>()
        val factory = UsersDataSourceFactory(context.applicationContext,
                onCreateUsersFetcher(), accountKey) {
            errorLiveData.postValue(SingleResponse(it))
        }
        val maxLoadLimit = getMaxLoadItemLimit(accountKey)
        val loadLimit = PreferencesSingleton.get(this.context!!)[loadItemLimitKey]
        val apiLiveData = ExceptionLiveData.wrap(LivePagedListBuilder(factory, PagedList.Config.Builder()
                .setPageSize(loadLimit.coerceAtMost(maxLoadLimit))
                .setInitialLoadSizeHint(loadLimit.coerceAtMost(maxLoadLimit))
                .build()).build())
        merger.addSource(errorLiveData) {
            merger.removeSource(apiLiveData)
            merger.removeSource(errorLiveData)
            merger.value = it
        }
        merger.addSource(apiLiveData) {
            merger.value = it
        }
        return merger
    }

    protected abstract fun onCreateUsersFetcher(): UsersFetcher

    protected open fun shouldRemoveUser(position: Int, event: FriendshipTaskEvent): Boolean {
        return false
    }

    protected fun createMessageBusCallback(): Any {
        return UsersBusCallback()
    }

    private fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
        return adapter.findPosition(accountKey, userKey)
    }

    private fun setupLiveData() {
        users = onCreateLiveData()
        users?.observe(this, success = this::onDataLoaded, fail = {
            showError(R.drawable.ic_info_error_generic, it.getErrorMessage(context!!))
        })
    }

    protected inner class UsersBusCallback {

        @Subscribe
        fun onFriendshipTaskEvent(event: FriendshipTaskEvent) {
            val position = findPosition(event.accountKey, event.userKey)
            val data = adapter.users ?: return
            if (position < 0 || position >= data.size) return
            if (shouldRemoveUser(position, event)) {
                adapter.removeUserAt(position)
            } else {
                val adapterUser = data[position] ?: return
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
