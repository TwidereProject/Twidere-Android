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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.TwidereConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.TwidereConstants.INTENT_ACTION_COMPOSE
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.data.fetcher.GroupTimelineFetcher
import org.mariotaku.twidere.extension.groupId
import org.mariotaku.twidere.extension.groupName
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.extension.model.tab.applyToSelection
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.model.refresh.ContentRefreshParam
import org.mariotaku.twidere.model.refresh.GroupTimelineContentRefreshParam
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.task.statuses.GetGroupTimelineTask
import java.util.*

class GroupTimelineFragment : AbsTimelineFragment() {
    override val filterScope: Int = FilterScope.LIST_GROUP_TIMELINE

    override val contentUri: Uri
        get() = Statuses.GroupTimeline.CONTENT_URI.withAppendedPath(tabId)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        linkHandlerTitle = getString(R.string.title_group_timeline)
    }

    override fun getStatuses(param: ContentRefreshParam): Boolean {
        val task = GetGroupTimelineTask(context!!)
        task.params = GroupTimelineContentRefreshParam(arguments!!.groupId,
                arguments!!.groupName, param)
        TaskStarter.execute(task)
        return true
    }

    override fun onCreateStatusesFetcher() = GroupTimelineFetcher(arguments!!.groupId,
            arguments!!.groupName)

    override fun getExtraSelection(): Pair<Expression, Array<String>?>? {
        val extras = arguments!!.getParcelable<HomeTabExtras>(IntentConstants.EXTRA_EXTRAS) ?: return null
        val expressions = ArrayList<Expression>()
        val expressionArgs = ArrayList<String>()
        extras.applyToSelection(expressions, expressionArgs)
        if (expressions.isEmpty()) return null
        val expression = Expression.and(*expressions.toTypedArray())
        return Pair(expression, expressionArgs.toTypedArray())
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_group_timeline, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.compose -> {
                val accountKey = accountKeys.singleOrNull() ?: return true
                val groupName = arguments!!.getString(TwidereConstants.EXTRA_GROUP_NAME) ?: return true
                val intent = Intent(activity, ComposeActivity::class.java)
                intent.action = INTENT_ACTION_COMPOSE
                intent.putExtra(Intent.EXTRA_TEXT, "!$groupName ")
                intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


}