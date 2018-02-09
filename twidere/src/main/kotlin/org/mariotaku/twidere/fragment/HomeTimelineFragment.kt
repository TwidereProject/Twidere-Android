/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants.NOTIFICATION_ID_HOME_TIMELINE
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.model.ParameterizedExpression
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.ErrorInfoStore
import java.util.*

/**
 * Created by mariotaku on 14/12/3.
 */
class HomeTimelineFragment : CursorStatusesFragment() {

    override val errorInfoKey = ErrorInfoStore.KEY_HOME_TIMELINE

    override val contentUri = Statuses.CONTENT_URI

    override val notificationType = NOTIFICATION_ID_HOME_TIMELINE

    override val isFilterEnabled = true

    override val readPositionTag = ReadPositionTag.HOME_TIMELINE

    override val timelineSyncTag: String?
        get() = getTimelineSyncTag(accountKeys)

    override val filterScopes: Int
        get() = FilterScope.HOME

    override fun updateRefreshState() {
        val twitter = twitterWrapper
        refreshing = twitter.isStatusTimelineRefreshing(contentUri)
    }

    override fun getStatuses(param: RefreshTaskParam): Boolean {
        if (!param.hasMaxIds) return twitterWrapper.refreshAll(param.accountKeys)
        return twitterWrapper.getHomeTimelineAsync(param)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val context = context
        if (isVisibleToUser && context != null) {
            accountKeys.forEach { accountKey ->
                notificationManager.cancel("home_$accountKey", NOTIFICATION_ID_HOME_TIMELINE)
            }
        }
    }

    override fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<HomeTabExtras>(EXTRA_EXTRAS)
            if (extras != null) {
                val expressions = ArrayList<Expression>()
                val expressionArgs = ArrayList<String>()
                Collections.addAll(expressionArgs, *whereArgs)
                expressions.add(where)
                DataStoreUtils.processTabExtras(expressions, expressionArgs, extras)
                val expression = Expression.and(*expressions.toTypedArray())
                return ParameterizedExpression(expression, expressionArgs.toTypedArray())
            }
        }
        return super.processWhere(where, whereArgs)
    }

    companion object {

        fun getTimelineSyncTag(accountKeys: Array<UserKey>): String {
            return "${ReadPositionTag.HOME_TIMELINE}_${accountKeys.sorted().joinToString(",")}"
        }

    }
}

