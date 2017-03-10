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

package org.mariotaku.twidere.util.stetho

import android.accounts.AccountManager
import android.content.Context
import com.facebook.stetho.dumpapp.DumpException
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.extension.model.getCredentials
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ActivityTitleSummaryMessage
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.streaming.TimelineStreamCallback

/**
 * Created by mariotaku on 2017/3/9.
 */
class UserStreamDumper(val context: Context) : DumperPlugin {

    override fun dump(dumpContext: DumperContext) {
        val parser = GnuParser()
        val options = Options()
        options.addRequiredOption("a", "account", true, "Account key")
        options.addOption("t", "timeline", false, "Include timeline")
        options.addOption("i", "interactions", false, "Include interactions")
        val cmdLine = try {
            parser.parse(options, dumpContext.argsAsList.toTypedArray())
        } catch (e: ParseException) {
            throw DumpException(e.message)
        }
        val manager = DependencyHolder.get(context).userColorNameManager
        val includeTimeline = cmdLine.hasOption("timeline")
        val includeInteractions = cmdLine.hasOption("interactions")
        val accountKey = UserKey.valueOf(cmdLine.getOptionValue("account"))
        val am = AccountManager.get(context)
        val account = AccountUtils.findByAccountKey(am, accountKey) ?: return
        val credentials = account.getCredentials(am)
        val userStream = credentials.newMicroBlogInstance(context, account.type,
                cls = TwitterUserStream::class.java)
        dumpContext.stdout.println("Beginning user stream...")
        dumpContext.stdout.flush()
        val callback = object : TimelineStreamCallback(accountKey.id) {
            override fun onException(ex: Throwable): Boolean {
                ex.printStackTrace(dumpContext.stderr)
                dumpContext.stderr.flush()
                return true
            }

            override fun onHomeTimeline(status: Status) {
                if (!includeTimeline && includeInteractions) return
                dumpContext.stdout.println("Home: @${status.user.screenName}: ${status.text.trim('\n')}")
                dumpContext.stdout.flush()
            }

            override fun onActivityAboutMe(activity: Activity) {
                if (!includeInteractions && includeTimeline) return
                val pActivity = ParcelableActivityUtils.fromActivity(activity, accountKey)
                val message = ActivityTitleSummaryMessage.get(context, manager, pActivity, pActivity.sources, 0,
                        true, true)
                if (message != null) {
                    dumpContext.stdout.println("Activity: ${message.title}: ${message.summary}")
                } else {
                    dumpContext.stdout.println("Activity unsupported: ${activity.action}")
                }
                dumpContext.stdout.flush()
            }

            override fun onDirectMessage(directMessage: DirectMessage): Boolean {
                dumpContext.stdout.println("Message: @${directMessage.senderScreenName}: ${directMessage.text.trim('\n')}")
                dumpContext.stdout.flush()
                return true
            }
        }
        try {
            userStream.getUserStream("user", callback)
        } catch (e: Exception) {
            e.printStackTrace(dumpContext.stderr)
        }
    }

    override fun getName() = "user_stream"

}