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

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.Loader
import com.squareup.otto.Subscribe
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.ArgsArray
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.HomeActivity
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_FROM_USER
import org.mariotaku.twidere.loader.ExtendedObjectCursorLoader
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.message.*
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.DataStoreUtils.getTableNameByUri
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.Utils

/**
 * Displays statuses from database
 * Created by mariotaku on 14/12/3.
 */
abstract class CursorActivitiesFragment : AbsActivitiesFragment() {

    override fun onLoadingFinished() {
        val accountKeys = accountKeys
        val adapter = adapter
        if (adapter!!.itemCount > 0) {
            showContent()
        } else if (accountKeys.size > 0) {
            val errorInfo = ErrorInfoStore.getErrorInfo(context,
                    errorInfoStore.get(errorInfoKey, accountKeys[0]))
            if (errorInfo != null) {
                showEmpty(errorInfo.icon, errorInfo.message)
            } else {
                showEmpty(R.drawable.ic_info_refresh, getString(R.string.swipe_down_to_refresh))
            }
        } else {
            showError(R.drawable.ic_info_accounts, getString(R.string.no_account_selected))
        }
    }

    protected abstract val errorInfoKey: String

    private var contentObserver: ContentObserver? = null

    abstract val contentUri: Uri

    override fun onCreateActivitiesLoader(context: Context,
                                          args: Bundle,
                                          fromUser: Boolean): Loader<List<ParcelableActivity>> {
        val uri = contentUri
        val table = getTableNameByUri(uri)
        val sortOrder = sortOrder
        val accountKeys = accountKeys
        val accountWhere = Expression.`in`(Column(Activities.ACCOUNT_KEY),
                ArgsArray(accountKeys.size))
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
        val adapter = adapter!!
        adapter.showAccountsColor = accountKeys.size > 1
        val projection = Activities.COLUMNS
        return CursorActivitiesLoader(context, uri, projection, selection, expression.parameters,
                sortOrder, fromUser)
    }

    override fun createMessageBusCallback(): Any {
        return CursorActivitiesBusCallback()
    }

    override val accountKeys: Array<UserKey>
        get() {
            val context = context!!
            val args = arguments
            val accountKeys = Utils.getAccountKeys(context, args)
            if (accountKeys != null) {
                return accountKeys
            }
            if (context is HomeActivity) {
                return context.activatedAccountKeys
            }
            return DataStoreUtils.getActivatedAccountKeys(context)
        }

    override fun onStart() {
        super.onStart()
        val cr = contentResolver
        contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean) {
                reloadActivities()
            }
        }
        cr.registerContentObserver(Accounts.CONTENT_URI, true, contentObserver!!)
        cr.registerContentObserver(Filters.CONTENT_URI, true, contentObserver!!)
        updateRefreshState()
        reloadActivities()
    }

    protected fun reloadActivities() {
        if (activity == null || isDetached) return
        val args = Bundle()
        val fragmentArgs = arguments
        if (fragmentArgs != null) {
            args.putAll(fragmentArgs)
            args.putBoolean(EXTRA_FROM_USER, true)
        }
        loaderManager.restartLoader(0, args, this)
    }

    override fun onStop() {
        val cr = contentResolver
        cr.unregisterContentObserver(contentObserver!!)
        super.onStop()
    }

    override fun hasMoreData(data: List<ParcelableActivity>?): Boolean {
        return data?.size != 0
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableActivity>>) {
        adapter!!.setData(null)
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L || refreshing) return
        super.onLoadMoreContents(position.toLong())
        if (position == 0L) return
        getActivities(object : SimpleRefreshTaskParam() {
            override fun getAccountKeysWorker(): Array<UserKey> {
                return this@CursorActivitiesFragment.accountKeys
            }

            override val maxIds: Array<String?>?
                get() = getOldestActivityIds(accountKeys)

            override val maxSortIds: LongArray?
                get() {
                    val context = context ?: return null
                    return DataStoreUtils.getOldestActivityMaxSortPositions(context,
                            contentUri, accountKeys)
                }

            override val hasMaxIds: Boolean
                get() = true

            override val shouldAbort: Boolean
                get() = context == null
        })
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        getActivities(object : SimpleRefreshTaskParam() {
            override fun getAccountKeysWorker(): Array<UserKey> {
                return this@CursorActivitiesFragment.accountKeys
            }

            override val sinceIds: Array<String?>?
                get() = getNewestActivityIds(accountKeys)

            override val sinceSortIds: LongArray?
                get() {
                    val context = context ?: return null
                    return DataStoreUtils.getNewestActivityMaxSortPositions(context,
                            contentUri, accountKeys)
                }

            override val hasSinceIds: Boolean
                get() = true

            override val shouldAbort: Boolean
                get() = context == null
        })
        return true
    }

    protected fun getFiltersWhere(table: String): Expression? {
        if (!isFilterEnabled) return null
        return DataStoreUtils.buildActivityFilterWhereClause(table, null)
    }

    protected fun getNewestActivityIds(accountKeys: Array<UserKey>): Array<String?>? {
        val context = context ?: return null
        return DataStoreUtils.getNewestActivityMaxPositions(context, contentUri, accountKeys)
    }

    protected abstract val notificationType: Int

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val context = context
        if (context != null && isVisibleToUser) {
            val accountKeys = accountKeys
            for (accountKey in accountKeys) {
                twitterWrapper.clearNotificationAsync(notificationType, accountKey)
            }
        }
    }

    protected fun getOldestActivityIds(accountKeys: Array<UserKey>): Array<String?>? {
        val context = context ?: return null
        return DataStoreUtils.getOldestActivityMaxPositions(context, contentUri, accountKeys)
    }

    protected abstract val isFilterEnabled: Boolean

    protected open fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        return ParameterizedExpression(where, whereArgs)
    }

    protected abstract fun updateRefreshState()

    private val sortOrder: String
        get() = Activities.DEFAULT_SORT_ORDER


    private fun updateFavoritedStatus(status: ParcelableStatus) {
        activity ?: return
        replaceStatusStates(status)
    }


    fun replaceStatusStates(result: ParcelableStatus?) {
        if (result == null) return
        val lm = layoutManager
        val adapter = adapter
        val rangeStart = Math.max(adapter!!.activityStartIndex, lm!!.findFirstVisibleItemPosition())
        val rangeEnd = Math.min(lm.findLastVisibleItemPosition(), adapter.activityStartIndex + adapter.activityCount - 1)
        loop@ for (i in rangeStart..rangeEnd) {
            val activity = adapter.getActivity(i)
            if (result.account_key == activity!!.account_key && result.id == activity.status_id) {
                if (result.id != activity.status_id) {
                    continue@loop
                }
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                for (statusesArray in statusesMatrix) {
                    if (statusesArray == null) continue
                    for (status in statusesArray) {
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

    protected inner class CursorActivitiesBusCallback {

        @Subscribe
        fun notifyGetStatusesTaskChanged(event: GetActivitiesTaskEvent) {
            if (event.uri != contentUri) return
            refreshing = event.running
            if (!event.running) {
                setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
                refreshEnabled = true
                onLoadingFinished()
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
            adapter!!.notifyDataSetChanged()
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
                                 sortOrder: String, fromUser: Boolean) : ExtendedObjectCursorLoader<ParcelableActivity>(context, ParcelableActivityCursorIndices::class.java, uri, projection, selection, selectionArgs, sortOrder, fromUser) {

        override fun createObjectCursor(cursor: Cursor, indices: ObjectCursor.CursorIndices<ParcelableActivity>): ObjectCursor<ParcelableActivity> {
            val filteredUserIds = DataStoreUtils.getFilteredUserIds(context)
            return ActivityCursor(cursor, indices, filteredUserIds)
        }

        class ActivityCursor(cursor: Cursor, indies: ObjectCursor.CursorIndices<ParcelableActivity>,
                             val filteredUserIds: Array<UserKey>) : ObjectCursor<ParcelableActivity>(cursor, indies)
    }
}
