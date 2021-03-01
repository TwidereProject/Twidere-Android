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

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.app.hasRunningLoadersSafe
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.KeyEvent
import androidx.loader.app.LoaderManager
import com.bumptech.glide.RequestManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.commons.parcel.ParcelUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.UserClickListener
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.model.getAccountType
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.loader.users.AbsRequestUsersLoader
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper
import org.mariotaku.twidere.view.holder.UserViewHolder

abstract class ParcelableUsersFragment : AbsContentListRecyclerViewFragment<ParcelableUsersAdapter>(),
        LoaderCallbacks<List<ParcelableUser>?>, UserClickListener, KeyboardShortcutCallback,
        IUsersAdapter.FriendshipClickListener {

    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return LoaderManager.getInstance(this).hasRunningLoadersSafe()
        }
        set(value) {
            super.refreshing = value
        }

    protected open val simpleLayout: Boolean
        get() = arguments?.getBoolean(EXTRA_SIMPLE_LAYOUT) ?: false

    protected open val showFollow: Boolean
        get() = true

    protected var nextPagination: Pagination? = null
        private set

    protected var prevPagination: Pagination? = null
        private set

    private lateinit var navigationHelper: RecyclerViewNavigationHelper
    private val usersBusCallback: Any

    init {
        usersBusCallback = createMessageBusCallback()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            nextPagination = savedInstanceState.getParcelable(EXTRA_NEXT_PAGINATION)
            prevPagination = savedInstanceState.getParcelable(EXTRA_PREV_PAGINATION)
        }
        adapter.userClickListener = this

        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        LoaderManager.getInstance(this).initLoader(0, loaderArgs, this)
    }

    override fun onStart() {
        super.onStart()
        bus.register(usersBusCallback)
    }

    override fun onStop() {
        bus.unregister(usersBusCallback)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_NEXT_PAGINATION, nextPagination)
        outState.putParcelable(EXTRA_PREV_PAGINATION, prevPagination)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableUser>?> {
        val fromUser = args?.getBoolean(EXTRA_FROM_USER)
        args?.remove(EXTRA_FROM_USER)
        return onCreateUsersLoader(requireActivity(), args!!, fromUser!!).apply {
            if (this is AbsRequestUsersLoader) {
                pagination = args.getParcelable(EXTRA_PAGINATION)
            }
        }
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        adapter.setData(data)
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData(data)) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
            refreshEnabled = true
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        if (loader is IPaginationLoader && data?.loadSuccess() == true) {
            nextPagination = loader.nextPagination
            prevPagination = loader.prevPagination
        }
        showContent()
        refreshEnabled = true
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
    }

    override fun onLoadMoreContents(@ILoadMoreSupportAdapter.IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START != 0L) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderArgs.putParcelable(EXTRA_PAGINATION, nextPagination)
        LoaderManager.getInstance(this).restartLoader(0, loaderArgs, this)
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableUsersAdapter {
        val adapter = ParcelableUsersAdapter(context, this.requestManager)
        adapter.simpleLayout = simpleLayout
        adapter.showFollow = showFollow
        val accountType = arguments?.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)?.let { key ->
            val am = AccountManager.get(context)
            return@let AccountUtils.findByAccountKey(am, key)?.getAccountType(am)
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
        activity?.let { IntentUtils.openUserProfile(it, user, preferences[newDocumentApiKey]) }
    }

    override fun onFollowClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        if (twitterWrapper.isUpdatingRelationship(accountKey, user.key)) return
        if (user.is_following) {
            parentFragmentManager.let { DestroyFriendshipDialogFragment.show(it, user) }
        } else {
            twitterWrapper.createFriendshipAsync(accountKey, user.key, user.screen_name)
        }
    }

    override fun onUnblockClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        if (twitterWrapper.isUpdatingRelationship(accountKey, user.key)) return
        twitterWrapper.destroyBlockAsync(accountKey, user.key)
    }

    override fun onUnmuteClicked(holder: UserViewHolder, position: Int) {
        val user = adapter.getUser(position) ?: return
        val accountKey = user.account_key ?: return
        if (twitterWrapper.isUpdatingRelationship(accountKey, user.key)) return
        twitterWrapper.destroyMuteAsync(accountKey, user.key)
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

    abstract fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableUser>?>

    protected open fun shouldRemoveUser(position: Int, event: FriendshipTaskEvent): Boolean {
        return false
    }

    protected fun hasMoreData(data: List<ParcelableUser>?): Boolean {
        return data == null || data.isNotEmpty()
    }

    protected fun createMessageBusCallback(): Any {
        return UsersBusCallback()
    }

    private fun findPosition(accountKey: UserKey, userKey: UserKey): Int {
        return adapter.findPosition(accountKey, userKey)
    }

    private fun List<ParcelableUser>.loadSuccess(): Boolean {
        return this !is ListResponse<*> || !this.hasException()
    }

    protected inner class UsersBusCallback {

        @Subscribe
        fun onFriendshipTaskEvent(event: FriendshipTaskEvent) {
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
