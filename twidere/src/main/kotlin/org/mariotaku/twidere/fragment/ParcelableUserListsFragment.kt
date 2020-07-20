/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.app.hasRunningLoadersSafe
import androidx.loader.content.Loader
import android.view.KeyEvent
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.adapter.ParcelableUserListsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter.UserListClickListener
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper
import org.mariotaku.twidere.view.holder.UserListViewHolder

abstract class ParcelableUserListsFragment : AbsContentListRecyclerViewFragment<ParcelableUserListsAdapter>(), LoaderCallbacks<List<ParcelableUserList>>, UserListClickListener, KeyboardShortcutCallback {

    private lateinit var navigationHelper: RecyclerViewNavigationHelper

    var nextPagination: Pagination? = null
        private set
    var prevPagination: Pagination? = null
        private set

    protected val accountKey: UserKey?
        get() = arguments?.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)

    val data: List<ParcelableUserList>?
        get() = adapter.getData()

    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return LoaderManager.getInstance(this).hasRunningLoadersSafe()
        }
        set(value) {
            super.refreshing = value
        }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ParcelableUserListsAdapter {
        return ParcelableUserListsAdapter(context, this.requestManager)
    }

    protected fun hasMoreData(data: List<ParcelableUserList>?): Boolean {
        return data == null || data.isNotEmpty()
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUserList>>, data: List<ParcelableUserList>) {
        adapter.setData(data)
        if (loader !is IExtendedLoader || loader.fromUser) {
            adapter.loadMoreSupportedPosition = if (hasMoreData(data)) ILoadMoreSupportAdapter.END else ILoadMoreSupportAdapter.NONE
            refreshEnabled = true
        }
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
        if (loader is IPaginationLoader) {
            nextPagination = loader.nextPagination
            prevPagination = loader.nextPagination
        }
        showContent()
        refreshEnabled = true
        refreshing = false
        setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START != 0L) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderArgs.putParcelable(EXTRA_PAGINATION, nextPagination)
        LoaderManager.getInstance(this).restartLoader(0, loaderArgs, this)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = layoutManager
        adapter.userListClickListener = this

        navigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        LoaderManager.getInstance(this).initLoader(0, loaderArgs, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableUserList>> {
        val fromUser = args?.getBoolean(EXTRA_FROM_USER)
        args?.remove(EXTRA_FROM_USER)
        return onCreateUserListsLoader(requireActivity(), args!!, fromUser!!)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUserList>>) {
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
    }

    override fun onUserListClick(holder: UserListViewHolder, position: Int) {
        val userList = adapter.getUserList(position) ?: return
        activity?.let { IntentUtils.openUserListDetails(it, userList) }
    }

    override fun onUserListLongClick(holder: UserListViewHolder, position: Int): Boolean {
        return true
    }

    override fun triggerRefresh(): Boolean {
        adapter.setData(null)
        val loaderArgs = Bundle(arguments).apply {
            this[EXTRA_FROM_USER] = true
        }
        LoaderManager.getInstance(this).restartLoader(0, loaderArgs, this)
        showProgress()
        return true
    }

    protected abstract fun onCreateUserListsLoader(context: Context, args: Bundle, fromUser: Boolean):
            Loader<List<ParcelableUserList>>
}
