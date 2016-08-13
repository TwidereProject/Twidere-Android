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
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.twidere.adapter.ParcelableGroupsAdapter
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter.GroupAdapterListener
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper
import org.mariotaku.twidere.view.holder.GroupViewHolder

abstract class ParcelableGroupsFragment : AbsContentListRecyclerViewFragment<ParcelableGroupsAdapter>(),
        LoaderCallbacks<List<ParcelableGroup>?>, GroupAdapterListener, KeyboardShortcutCallback {

    private var mNavigationHelper: RecyclerViewNavigationHelper? = null
    val nextCursor: Long = 0
    val prevCursor: Long = 0

    override var refreshing: Boolean
        get() {
            if (context == null || isDetached) return false
            return loaderManager.hasRunningLoaders()
        }
        set(value) {
            super.refreshing = value
        }

    override fun onCreateAdapter(context: Context): ParcelableGroupsAdapter {
        return ParcelableGroupsAdapter(context)
    }

    override fun setupRecyclerView(context: Context, recyclerView: RecyclerView) {
        super.setupRecyclerView(context, recyclerView)
    }

    protected val accountKey: UserKey?
        get() {
            val args = arguments
            return args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        }

    protected fun hasMoreData(data: List<ParcelableGroup>?): Boolean {
        return data == null || !data.isEmpty()
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableGroup>?>, data: List<ParcelableGroup>?) {
        val adapter = adapter
        adapter!!.setData(data)
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

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L) return
        super.onLoadMoreContents(position.toLong())
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderArgs.putLong(EXTRA_NEXT_CURSOR, nextCursor)
        loaderManager.restartLoader(0, loaderArgs, this)
    }

    protected fun removeUsers(vararg ids: Long) {
        //TODO remove from adapter
    }

    val data: List<ParcelableGroup>?
        get() = adapter!!.getData()

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        return mNavigationHelper!!.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return mNavigationHelper!!.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        return mNavigationHelper!!.isKeyboardShortcutHandled(handler, keyCode, event, metaState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter = adapter
        val layoutManager = layoutManager
        adapter!!.groupAdapterListener = this

        mNavigationHelper = RecyclerViewNavigationHelper(recyclerView, layoutManager!!, adapter,
                this)
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderManager.initLoader(0, loaderArgs, this)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableGroup>?> {
        val fromUser = args.getBoolean(EXTRA_FROM_USER)
        args.remove(EXTRA_FROM_USER)
        return onCreateUserListsLoader(activity, args, fromUser)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableGroup>?>) {
        if (loader is IExtendedLoader) {
            loader.fromUser = false
        }
    }

    override fun onGroupClick(holder: GroupViewHolder, position: Int) {
        IntentUtils.openGroupDetails(context, adapter!!.getGroup(position)!!)
    }

    override fun onGroupLongClick(holder: GroupViewHolder, position: Int): Boolean {
        return false
    }

    protected abstract fun onCreateUserListsLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableGroup>?>
}
