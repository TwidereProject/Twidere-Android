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

package org.mariotaku.twidere.util.streaming

import org.mariotaku.microblog.library.fanfou.callback.SimpleFanfouUserStreamCallback
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.InternalActivityCreator
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.User
import java.util.*

/**
 * Created by mariotaku on 2017/3/11.
 */

abstract class FanfouTimelineStreamCallback(
        val accountId: String
) : SimpleFanfouUserStreamCallback() {

    override fun onStatusCreation(createdAt: Date, source: User, target: User?, status: Status): Boolean {
        var handled = false
        if (target == null) {
            handled = handled or onHomeTimeline(status)
        }
        if (target?.id == accountId) {
            handled = handled or onActivityAboutMe(InternalActivityCreator.status(accountId, status))
        }
        return handled
    }

    protected abstract fun onHomeTimeline(status: Status): Boolean

    protected abstract fun onActivityAboutMe(activity: Activity): Boolean
}
