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

package org.mariotaku.twidere.fragment.timeline

import android.net.Uri
import android.os.Bundle
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.data.fetcher.PublicTimelineFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.model.tab.applyToSelection
import org.mariotaku.twidere.extension.promise
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetPublicTimelineTask
import java.util.*

class PublicTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int = FilterScope.PUBLIC_TIMELINE

    override val contentUri: Uri = Statuses.Public.CONTENT_URI

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_public_timeline)
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val task = GetPublicTimelineTask(context!!)
        task.params = param
        task.promise()
        return true
    }

    override fun onCreateStatusesFetcher() = PublicTimelineFetcher()

    override fun getExtraSelection(): Pair<Expression, Array<String>?>? {
        val extras = arguments!!.getParcelable<HomeTabExtras>(EXTRA_EXTRAS) ?: return null
        val expressions = ArrayList<Expression>()
        val expressionArgs = ArrayList<String>()
        extras.applyToSelection(expressions, expressionArgs)
        if (expressions.isEmpty()) return null
        val expression = Expression.and(*expressions.toTypedArray())
        return Pair(expression, expressionArgs.toTypedArray())
    }

    companion object {

        fun getTimelineSyncTag(accountKeys: Array<UserKey>): String {
            return "${ReadPositionTag.PUBLIC_TIMELINE}_${accountKeys.sorted().joinToString(",")}"
        }

    }
}