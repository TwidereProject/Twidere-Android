/*
 *                 Twidere - Twitter client for Android
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

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.contains
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.toNulls
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_FROM_USER
import org.mariotaku.twidere.loader.ExtendedObjectCursorLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.event.*
import org.mariotaku.twidere.model.pagination.SinceMaxPagination
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.task.twitter.GetStatusesTask
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DataStoreUtils.getTableNameByUri
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.Utils

/**
 * Displays statuses from database
 * Created by mariotaku on 14/12/3.
 */
abstract class CursorActivitiesFragment : AbsActivitiesFragment() {

    override val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(context, arguments) ?: DataStoreUtils.getActivatedAccountKeys(context)

    abstract val contentUri: Uri

    protected abstract val errorInfoKey: String

    protected abstract val notificationType: Int

    protected abstract val isFilterEnabled: Boolean

    private var contentObserver: ContentObserver? = null

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                clearNotifications()
            }
        }
    }

    private val accountListener: OnAccountsUpdateListener = OnAccountsUpdateListener {
        reloadActivities()
    }

    private val sortOrder: String
        get() = Activities.DEFAULT_SORT_ORDER

    override fun onStart() {
        super.onStart()
        if (contentObserver == null) {
            contentObserver = object : ContentObserver(Handler()) {
                override fun onChange(selfChange: Boolean) {
                    reloadActivities()
                }
            }
            context.contentResolver.registerContentObserver(Filters.CONTENT_URI, true, contentObserver)
        }
        AccountManager.get(context).addOnAccountsUpdatedListenerSafe(accountListener, updateImmediately = false)
        recyclerView.addOnScrollListener(onScrollListener)
        updateRefreshState()
        reloadActivities()
    }

    override fun onStop() {
        recyclerView.removeOnScrollListener(onScrollListener)
        if (contentObserver != null) {
            context.contentResolver.unregisterContentObserver(contentObserver)
            contentObserver = null
        }
        AccountManager.get(context).removeOnAccountsUpdatedListenerSafe(accountListener)
        super.onStop()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            clearNotifications()
        }
    }

    override fun onCreateActivitiesLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableActivity>> {
        val uri = contentUri
        val table = getTableNameByUri(uri)!!
        val sortOrder = sortOrder
        val accountKeys = accountKeys
        val accountWhere = Expression.inArgs(Column(Activities.ACCOUNT_KEY), accountKeys.size)
        val filterWhere = getFiltersWhere(table)
        val where: Expression
        if (filterWhere != null) {
            where = Expression.and(accountWhere, filterWhere)
        } else {
            where = accountWhere
        }

        val accountSelectionArgs = Array(accountKeys.size) {
            accountKeys[it].toString()
        }
        val expression = processWhere(where, accountSelectionArgs)
        val selection = expression.sql
        adapter.showAccountsColor = accountKeys.size > 1
        val projection = Activities.COLUMNS
        return CursorActivitiesLoader(context, uri, projection, selection, expression.parameters,
                sortOrder, fromUser)
    }

    override fun onContentLoaded(loader: Loader<List<ParcelableActivity>>, data: List<ParcelableActivity>?) {
        showContentOrError()
    }

    override fun createMessageBusCallback(): Any {
        return CursorActivitiesBusCallback()
    }

    override fun hasMoreData(data: List<ParcelableActivity>?): Boolean {
        return data?.size != 0
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableActivity>>) {
        adapter.setData(null)
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (ILoadMoreSupportAdapter.START in position || refreshing) return
        super.onLoadMoreContents(position)
        if (position == 0L) return
        val contentUri = this.contentUri
        getActivities(object : RefreshTaskParam {
            override val accountKeys by lazy {
                this@CursorActivitiesFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val maxIds = DataStoreUtils.getRefreshOldestActivityMaxPositions(context, contentUri,
                        keys)
                val maxSortIds = DataStoreUtils.getRefreshOldestActivityMaxSortPositions(context,
                        contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.maxId(maxIds[idx], maxSortIds[idx])
                }
            }

            override val shouldAbort: Boolean
                get() = context == null
        })
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        val contentUri = this.contentUri
        getActivities(object : RefreshTaskParam {
            override val accountKeys by lazy {
                this@CursorActivitiesFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val sinceIds = DataStoreUtils.getRefreshNewestActivityMaxPositions(context,
                        contentUri, keys)
                val sinceSortIds = DataStoreUtils.getRefreshNewestActivityMaxSortPositions(context,
                        contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.sinceId(sinceIds[idx], sinceSortIds[idx])
                }
            }

            override val shouldAbort: Boolean
                get() = context == null
        })
        return true
    }

    protected fun getFiltersWhere(table: String): Expression? {
        if (!isFilterEnabled) return null
        return DataStoreUtils.buildActivityFilterWhereClause(table, null)
    }

    protected open fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        return ParameterizedExpression(where, whereArgs)
    }

    protected abstract fun updateRefreshState()

    protected fun reloadActivities() {
        if (activity == null || isDetached) return
        val args = Bundle()
        val fragmentArgs = arguments
        if (fragmentArgs != null) {
            args.putAll(fragmentArgs)
            args.putBoolean(EXTRA_FROM_USER, true)
        }
        loaderManager.restartLoader(loaderId, args, this)
    }

    fun replaceStatusStates(result: ParcelableStatus?) {
        if (result == null) return
        val lm = layoutManager
        val rangeStart = Math.max(adapter.activityStartIndex, lm.findFirstVisibleItemPosition())
        val rangeEnd = Math.min(lm.findLastVisibleItemPosition(), adapter.activityStartIndex + adapter.getActivityCount(false) - 1)
        loop@ for (i in rangeStart..rangeEnd) {
            val activity = adapter.getActivity(i, false)
            if (result.account_key == activity.account_key && result.id == activity.status_id) {
                if (result.id != activity.status_id) {
                    continue@loop
                }
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                statusesMatrix.filterNotNull().forEach { statuses ->
                    for (status in statuses) {
                        if (result.id == status.id || result.id == status.retweet_id
                                || result.id == status.my_retweet_id) {
                            status.is_favorite = result.is_favorite
                            status.reply_count = result.reply_count
                            status.retweet_count = result.retweet_count
                            status.favorite_count = result.favorite_count
                        }
                    }
                }
            }
        }
        adapter.notifyItemRangeChanged(rangeStart, rangeEnd)
    }

    private fun showContentOrError() {
        val accountKeys = accountKeys
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

    private fun updateFavoritedStatus(status: ParcelableStatus) {
        activity ?: return
        replaceStatusStates(status)
    }

    private fun clearNotifications() {
        if (context != null && userVisibleHint) {
            for (accountKey in accountKeys) {
                twitterWrapper.clearNotificationAsync(notificationType, accountKey)
            }
        }
    }

    protected inner class CursorActivitiesBusCallback {

        @Subscribe
        fun notifyGetStatusesTaskChanged(event: GetActivitiesTaskEvent) {
            if (event.uri != contentUri) return
            refreshing = event.running
            if (!event.running) {
                setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
                refreshEnabled = true
                showContentOrError()

                val exception = event.exception
                if (exception is GetStatusesTask.GetTimelineException && userVisibleHint) {
                    Toast.makeText(context, exception.getToastMessage(context), Toast.LENGTH_SHORT).show()
                }
            }
        }

        @Subscribe
        fun notifyFavoriteTask(event: FavoriteTaskEvent) {
            if (event.isSucceeded) {
                updateFavoritedStatus(event.status!!)
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

    class CursorActivitiesLoader(context: Context, uri: Uri, projection: Array<String>,
            selection: String, selectionArgs: Array<String>,
            sortOrder: String, fromUser: Boolean
    ) : ExtendedObjectCursorLoader<ParcelableActivity>(context, ParcelableActivity::class.java, uri,
            projection, selection, selectionArgs, sortOrder, fromUser) {

        override fun createObjectCursor(cursor: Cursor, indices: ObjectCursor.CursorIndices<ParcelableActivity>): ObjectCursor<ParcelableActivity> {
            val filteredUserKeys = DataStoreUtils.getFilteredUserKeys(context)
            return ActivityCursor(cursor, indices, filteredUserKeys)
        }

        class ActivityCursor(cursor: Cursor, indies: ObjectCursor.CursorIndices<ParcelableActivity>,
                val filteredUserIds: Array<UserKey>) : ObjectCursor<ParcelableActivity>(cursor, indies)
    }

    companion object {

    }
}
