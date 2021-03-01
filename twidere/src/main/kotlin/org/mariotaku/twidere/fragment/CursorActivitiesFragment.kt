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
import androidx.loader.content.Loader
import android.widget.Toast
import androidx.loader.app.LoaderManager
import com.squareup.otto.Subscribe
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_FROM_USER
import org.mariotaku.twidere.extension.queryOne
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
import kotlin.math.max
import kotlin.math.min

/**
 * Displays statuses from database
 * Created by mariotaku on 14/12/3.
 */
abstract class CursorActivitiesFragment : AbsActivitiesFragment() {

    override val accountKeys: Array<UserKey>
        get() = Utils.getAccountKeys(requireContext(), arguments) ?: DataStoreUtils.getActivatedAccountKeys(requireContext())

    abstract val contentUri: Uri

    protected abstract val errorInfoKey: String

    protected abstract val notificationType: Int

    protected abstract val isFilterEnabled: Boolean

    @FilterScope
    protected abstract val filterScopes: Int

    private var contentObserver: ContentObserver? = null

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
            context?.contentResolver?.registerContentObserver(Filters.CONTENT_URI, true, contentObserver!!)
        }
        AccountManager.get(context).addOnAccountsUpdatedListenerSafe(accountListener, updateImmediately = false)
        updateRefreshState()
        reloadActivities()
    }

    override fun onStop() {
        if (contentObserver != null) {
            context?.contentResolver?.unregisterContentObserver(contentObserver!!)
            contentObserver = null
        }
        AccountManager.get(context).removeOnAccountsUpdatedListenerSafe(accountListener)
        super.onStop()
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
        val projection = activityColumnsLite
        return CursorActivitiesLoader(context, uri, projection, selection, expression.parameters,
                sortOrder, fromUser, filterScopes).apply {
            isUseCache = false
        }
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
        val currentContext = context ?: return
        val contentUri = this.contentUri
        getActivities(object : RefreshTaskParam {
            override val accountKeys by lazy {
                this@CursorActivitiesFragment.accountKeys
            }

            override val pagination by lazy {
                val keys = accountKeys.toNulls()
                val maxIds = DataStoreUtils.getRefreshOldestActivityMaxPositions(currentContext, contentUri,
                        keys)
                val maxSortIds = DataStoreUtils.getRefreshOldestActivityMaxSortPositions(currentContext,
                        contentUri, keys)
                return@lazy Array(keys.size) { idx ->
                    SinceMaxPagination.maxId(maxIds[idx], maxSortIds[idx])
                }
            }

            override val shouldAbort: Boolean
                get() = false
        })
    }

    override fun triggerRefresh(): Boolean {
        super.triggerRefresh()
        val contentUri = this.contentUri
        getActivities(object : RefreshTaskParam {
            override val accountKeys by lazy {
                this@CursorActivitiesFragment.accountKeys
            }

            override val pagination: Array<SinceMaxPagination?>? by lazy {
                val context = context ?: return@lazy null
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

    override fun saveReadPosition(position: Int) {
        super.saveReadPosition(position)
        if (position == 0) {
            clearNotifications()
        }
    }

    override fun getFullActivity(position: Int): ParcelableActivity? {
        val _id = adapter.getRowId(position)
        val where = Expression.equals(Activities._ID, _id).sql
        return context?.contentResolver?.queryOne(contentUri, Activities.COLUMNS, where, null, null,
                ParcelableActivity::class.java)
    }

    protected fun getFiltersWhere(table: String): Expression? {
        if (!isFilterEnabled) return null
        return DataStoreUtils.buildStatusFilterWhereClause(preferences, table, null, filterScopes)
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
        LoaderManager.getInstance(this).restartLoader(loaderId, args, this)
    }

    fun replaceStatusStates(result: ParcelableStatus?) {
        if (result == null) return
        val lm = layoutManager
        val rangeStart = max(adapter.activityStartIndex, lm.findFirstVisibleItemPosition())
        val rangeEnd = min(lm.findLastVisibleItemPosition(), adapter.activityStartIndex + adapter.getActivityCount(false) - 1)
        loop@ for (i in rangeStart..rangeEnd) {
            val activity = adapter.getActivity(i, false)
            if (result.account_key == activity.account_key && result.id == activity.id) {
                if (result.id != activity.id) {
                    continue@loop
                }
                if (result.id == activity.id || result.id == activity.retweet_id
                        || result.id == activity.my_retweet_id) {
                    activity.is_favorite = result.is_favorite
                    activity.reply_count = result.reply_count
                    activity.retweet_count = result.retweet_count
                    activity.favorite_count = result.favorite_count
                }
            }
        }
        adapter.notifyItemRangeChanged(rangeStart, rangeEnd)
    }

    private fun showContentOrError() {
        val accountKeys = accountKeys
        val currentContext = context ?: return
        if (adapter.itemCount > 0) {
            showContent()
        } else if (accountKeys.isNotEmpty()) {
            val errorInfo = ErrorInfoStore.getErrorInfo(currentContext,
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
            val currentContext = context ?: return
            refreshing = event.running
            if (!event.running) {
                setLoadMoreIndicatorPosition(ILoadMoreSupportAdapter.NONE)
                refreshEnabled = true
                showContentOrError()

                val exception = event.exception
                if (exception is GetStatusesTask.GetTimelineException && userVisibleHint) {
                    Toast.makeText(currentContext, exception.getToastMessage(currentContext), Toast.LENGTH_SHORT).show()
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
            sortOrder: String, fromUser: Boolean, @FilterScope val filterScope: Int
    ) : ExtendedObjectCursorLoader<ParcelableActivity>(context, ParcelableActivity::class.java, uri,
            projection, selection, selectionArgs, sortOrder, fromUser) {

        override fun createObjectCursor(cursor: Cursor, indices: ObjectCursor.CursorIndices<ParcelableActivity>): ObjectCursor<ParcelableActivity> {
            val filteredUserKeys = DataStoreUtils.getFilteredUserKeys(context, filterScope)
            val filteredNameKeywords = DataStoreUtils.getFilteredKeywords(context, filterScope or FilterScope.TARGET_NAME)
            val filteredDescriptionKeywords = DataStoreUtils.getFilteredKeywords(context, filterScope or FilterScope.TARGET_DESCRIPTION)
            return ActivityCursor(cursor, indices, filteredUserKeys, filteredNameKeywords, filteredDescriptionKeywords)
        }

        class ActivityCursor(
                cursor: Cursor,
                indies: CursorIndices<ParcelableActivity>,
                val filteredUserIds: Array<UserKey>,
                val filteredUserNames: Array<String>,
                val filteredUserDescriptions: Array<String>
        ) : ObjectCursor<ParcelableActivity>(cursor, indies)
    }

    companion object {
        val activityColumnsLite = Activities.COLUMNS - arrayOf(Activities.SOURCES, Activities.TARGETS,
                Activities.TARGET_OBJECTS, Activities.MENTIONS_JSON, Activities.CARD,
                Activities.FILTER_FLAGS, Activities.FILTER_USERS, Activities.FILTER_LINKS,
                Activities.FILTER_SOURCES, Activities.FILTER_NAMES, Activities.FILTER_TEXTS,
                Activities.FILTER_DESCRIPTIONS)

    }
}
