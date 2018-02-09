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

package org.mariotaku.twidere.task.status

import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.twitter.model.PinTweetResult
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.StatusPinEvent
import org.mariotaku.twidere.task.AbsAccountRequestTask

/**
 * Created by mariotaku on 2017/4/28.
 */

class PinStatusTask(context: Context, accountKey: UserKey, val id: String) : AbsAccountRequestTask<Any?,
        PinTweetResult, Any?>(context, accountKey) {

    override fun onExecute(account: AccountDetails, params: Any?): PinTweetResult {
        val twitter = account.newMicroBlogInstance(context, MicroBlog::class.java)
        return twitter.pinTweet(id)
    }

    override fun onSucceed(callback: Any?, result: PinTweetResult) {
        super.onSucceed(callback, result)
        Toast.makeText(context, R.string.message_toast_status_pinned, Toast.LENGTH_SHORT).show()
        if (accountKey != null) {
            bus.post(StatusPinEvent(accountKey, true))
        }
    }
}
