/*
 * Twidere - Twitter client for Android
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
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.Loader
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.sqliteqb.library.ArgsArray
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ListParcelableStatusesAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_FROM_USER
import org.mariotaku.twidere.loader.ExtendedObjectCursorLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.message.*
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.buildStatusFilterWhereClause

/**
 * Created by mariotaku on 14/12/3.
 */
abstract class CursorStatusesFragment : AbsStatusesFragment() {

    private var contentObserver: ContentObserver? = null
    private val accountListener: OnAccountsUpdateListener = OnAccountsUpdateListener { accounts ->
        reloadStatuses()
    }

    abstract val errorInfoKey: String
    abstract val isFilterEnabled: Boolean
    abstract val notificationType: Int
    abstract val contentUri: Uri
    override var refreshing: Boolean
        get() = swipeLayout.isRefreshing
        set(value) {
            super.refreshing = value
        }
    override val useSortIdAsReadPosition: Boolean
        get() = false

    override fun onStatusesLoaded(loader: Loader<List<ParcelableStatus>?>, data: List<ParcelableStatus>?) {
        showContentOrError()
    }

    override fun onCreateStatusesLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        val uri = contentUri
        val table = DataStoreUtils.getTableNameByUri(uri)
        val sortOrder = Statuses.DEFAULT_SORT_ORDER
        val accountKeys = this.accountKeys
        val accountWhere = Expression.`in`(Column(Statuses.ACCOUNT_KEY),
                ArgsArray(accountKeys.size))
        val filterWhere = getFiltersWhere(table)
        val where: Expression
        if (filterWhere != null) {
            where = Expression.and(accountWhere, filterWhere)
        } else {
            where = accountWhere
        }
        adapter.showAccountsColor = accountKeys.size > 1
        val projection = Statuses.COLUMNS
        val selectionArgs = Array(accountKeys.size) {
            accountKeys[it].toString()
        }
        val expression = processWhere(where, selectionArgs)
        return ExtendedObjectCursorLoader(context, ParcelableStatusCursorIndices::class.java, uri,
                projection, expression.sql, expression.parameters, sortOrder, fromUser)
    }

    override fun createMessageBusCallback(): Any {
        return CursorStatusesBusCallback()
    }


    private fun showContentOrError() {
        val accountKeys = this.accountKeys
        if (adapter.itemCount > 0) {
            showContent()
        } else if (accountKeys.isNotEmpty()) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context,
                    errorInfoStore[errorInfoKey, accountKeys[0]])
            if (errorInfo != null) {
                showEmpty(errorInfo.icon, errorInfo.message)
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.message_toast_no_account_selected))
        }
    }

    override val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context)

    override fun onStart() {
        super.onStart()
        if (contentObserver == null) {
            contentObserver = object : ContentObserver(Handler()) {
                override fun onChange(selfChange: Boolean) {
                    reloadStatuses()
                }
            }
            context.contentResolver.registerContentObserver(Filters.CONTENT_URI, true, contentObserver)
        }
        AccountManager.get(context).addOnAccountsUpdatedListenerSafe(accountListener, updateImmediately = false)
        updateRefreshState()
        reloadStatuses()
    }

    override fun onStop() {
        if (contentObserver != null) {
            context.contentResolver.unregisterContentObserver(contentObserver)
            contentObserver = null
        }
        AccountManager.get(context).removeOnAccountsUpdatedListenerSafe(accountListener)
        super.onStop()
    }

    protected fun reloadStatuses() {
        if (context == null || isDetached) return
        val args = Bundle()
        val fragmentArgs = arguments
        if (fragmentArgs != null) {
            args.putAll(fragmentArgs)
            args.putBoolean(EXTRA_FROM_USER, true)
        }
        loaderManager.restartLoader(0, args, this)
    }

    override fun hasMoreData(data: List<ParcelableStatus>?): Boolean {
        return data?.size != 0
    }

    override fun onCreateAdapter(context: Context): ListParcelableStatusesAdapter {
        return ListParcelableStatusesAdapter(context)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableStatus>?>) {
        adapter.setData(null)
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        getStatuses(object : SimpleRefreshTaskParam() {
            override fun getAccountKeysWorker(): Array<UserKey> {
                return this@CursorStatusesFragment.accountKeys
            }

            override val maxIds: Array<String?>?
                get() = getOldestStatusIds(accountKeys)

            override val maxSortIds: LongArray?
                get() {
                    val context = context ?: return null
                    return DataStoreUtils.getOldestStatusSortIds(context, contentUri,
                            accountKeys)
                }

            override val hasMaxIds: Boolean
                get() = true

            override val shouldAbort: Boolean
                get() = context == null
        })
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        getStatuses(object : SimpleRefreshTaskParam() {
            override fun getAccountKeysWorker(): Array<UserKey> {
                return this@CursorStatusesFragment.accountKeys
            }

            override val hasMaxIds: Boolean
                get() = false

            override val sinceIds: Array<String?>?
                get() = getNewestStatusIds(accountKeys)

            override val sinceSortIds: LongArray?
                get() = DataStoreUtils.getNewestStatusSortIds(context, contentUri, accountKeys)

            override val shouldAbort: Boolean
                get() = context == null
        })
        return true
    }

    protected fun getFiltersWhere(table: String): Expression? {
        if (!isFilterEnabled) return null
        return buildStatusFilterWhereClause(preferences, table, null)
    }

    protected fun getNewestStatusIds(accountKeys: Array<UserKey>): Array<String?>? {
        val context = context ?: return null
        return DataStoreUtils.getNewestStatusIds(context, contentUri, accountKeys)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val context = context
        if (context != null && isVisibleToUser) {
            for (accountId in accountKeys) {
                twitterWrapper.clearNotificationAsync(notificationType, accountId)
            }
        }
    }


    protected fun getOldestStatusIds(accountKeys: Array<UserKey>): Array<String?>? {
        val context = context ?: return null
        return DataStoreUtils.getOldestStatusIds(context, contentUri, accountKeys)
    }

    protected open fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        return ParameterizedExpression(where, whereArgs)
    }

    protected abstract fun updateRefreshState()

    protected inner class CursorStatusesBusCallback {

        @Subscribe
        fun notifyGetStatusesTaskChanged(event: GetStatusesTaskEvent) {
            if (event.uri != contentUri) return
            refreshing = event.running
            if (!event.running) {
                setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
                refreshEnabled = true
                showContentOrError()
            }
        }


        @Subscribe
        fun notifyFavoriteTask(event: FavoriteTaskEvent) {
            if (event.isSucceeded) {
                val status = event.status
                val data = adapterData
                if (status == null || data == null || data.isEmpty()) return
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                val startIndex = adapter.statusStartIndex
                for (i in firstVisiblePosition..lastVisiblePosition) {
                    if (status.account_key == adapter.getAccountKey(i) && status.id == adapter.getStatusId(i)) {
                        if (data is MutableList) {
                            data[i - startIndex] = status
                        }
                        return
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }

        @Subscribe
        fun notifyStatusDestroyed(event: StatusDestroyedEvent) {
        }

        @Subscribe
        fun notifyStatusListChanged(event: StatusListChangedEvent) {
            adapter.notifyDataSetChanged()
        }

        @Subscribe
        fun notifyStatusRetweeted(event: StatusRetweetedEvent) {
        }

        @Subscribe
        fun notifyAccountChanged(event: AccountChangedEvent) {

        }

    }

}
