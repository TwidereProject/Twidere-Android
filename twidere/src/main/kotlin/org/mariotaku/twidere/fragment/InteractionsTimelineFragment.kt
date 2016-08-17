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

import android.content.Context
import android.net.Uri
import edu.tsinghua.hotmobi.model.TimelineType
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.TwidereConstants.NOTIFICATION_ID_INTERACTIONS_TIMELINE
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_EXTRAS
import org.mariotaku.twidere.model.ParameterizedExpression
import org.mariotaku.twidere.model.RefreshTaskParam
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.util.ErrorInfoStore

class InteractionsTimelineFragment : CursorActivitiesFragment() {

    override fun getActivities(param: RefreshTaskParam): Boolean {
        twitterWrapper.getActivitiesAboutMeAsync(param)
        return true
    }

    override val timelineType: String
        @TimelineType
        get() = TimelineType.INTERACTIONS

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_INTERACTIONS

    override val contentUri: Uri
        get() = Activities.AboutMe.CONTENT_URI

    override val notificationType: Int
        get() = NOTIFICATION_ID_INTERACTIONS_TIMELINE

    override val isFilterEnabled: Boolean
        get() = true

    override fun updateRefreshState() {
    }

    override fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<InteractionsTabExtras>(EXTRA_EXTRAS)
            if (extras != null) {
                val expressions = mutableListOf(where)
                val combinedArgs = mutableListOf(*whereArgs)
                if (extras.isMentionsOnly) {
                    expressions.add(Expression.inArgs(Activities.ACTION, 3))
                    combinedArgs.addAll(arrayOf(Activity.Action.MENTION, Activity.Action.REPLY, Activity.Action.QUOTE))
                }
                if (extras.isMyFollowingOnly) {
                    expressions.add(Expression.equals(Activities.HAS_FOLLOWING_SOURCE, 1))
                }
                return ParameterizedExpression(Expression.and(*expressions.toTypedArray()),
                        combinedArgs.toTypedArray())
            }
        }
        return super.processWhere(where, whereArgs)
    }

    override fun onCreateAdapter(context: Context): ParcelableActivitiesAdapter {
        val adapter = ParcelableActivitiesAdapter(context, false)
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<InteractionsTabExtras>(EXTRA_EXTRAS)
            if (extras != null) {
                adapter.followingOnly = extras.isMyFollowingOnly
                adapter.mentionsOnly = extras.isMentionsOnly
            }
        }
        return adapter
    }

    @ReadPositionTag
    override val readPositionTag: String? = ReadPositionTag.ACTIVITIES_ABOUT_ME

}
