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
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.mariotaku.ktextension.subArray
import org.mariotaku.microblog.library.fanfou.FanfouStream
import org.mariotaku.microblog.library.mastodon.MastodonStreaming
import org.mariotaku.microblog.library.mastodon.callback.MastodonUserStreamCallback
import org.mariotaku.microblog.library.mastodon.model.Notification
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.microblog.library.twitter.annotation.StreamWith
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ActivityTitleSummaryMessage
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.dagger.DependencyHolder
import org.mariotaku.twidere.util.streaming.FanfouTimelineStreamCallback
import org.mariotaku.twidere.util.streaming.TwitterTimelineStreamCallback

/**
 * Created by mariotaku on 2017/3/9.
 */
class UserStreamDumperPlugin(val context: Context) : DumperPlugin {

    private val syntax = "$name <account_key> [-ti]"

    override fun dump(dumpContext: DumperContext) {
        val parser = GnuParser()
        val options = Options()
        options.addOption("t", "timeline", false, "Include timeline")
        options.addOption("i", "interactions", false, "Include interactions")
        options.addOption("v", "verbose", false, "Print full object")
        val argsList = dumpContext.argsAsList
        val formatter = HelpFormatter()
        if (argsList.isEmpty()) {
            formatter.printHelp(dumpContext.stderr, syntax, options)
            return
        }
        val cmdLine = parser.parse(options, argsList.subArray(1..argsList.lastIndex))
        val manager = DependencyHolder.get(context).userColorNameManager
        val includeTimeline = cmdLine.hasOption("timeline")
        val includeInteractions = cmdLine.hasOption("interactions")
        val verboseMode = cmdLine.hasOption("verbose")
        val accountKey = UserKey.valueOf(argsList[0])
        val am = AccountManager.get(context)
        val account = AccountUtils.getAccountDetails(am, accountKey, true) ?: return
        when (account.type) {
            AccountType.TWITTER -> {
                beginTwitterStream(account, dumpContext, includeInteractions, includeTimeline,
                        verboseMode, manager)
            }
            AccountType.FANFOU -> {
                beginFanfouStream(account, dumpContext, includeInteractions, includeTimeline,
                        verboseMode, manager)
            }
            AccountType.MASTODON -> {
                beginMastodonStream(account, dumpContext, includeInteractions, includeTimeline,
                        verboseMode, manager)
            }
            else -> {
                dumpContext.stderr.println("Unsupported account type ${account.type}")
                dumpContext.stderr.flush()
            }
        }

    }

    private fun beginMastodonStream(account: AccountDetails, dumpContext: DumperContext,
            includeInteractions: Boolean, includeTimeline: Boolean, verboseMode: Boolean,
            manager: UserColorNameManager) {
        val streaming = account.newMicroBlogInstance(context, cls = MastodonStreaming::class.java)
        dumpContext.stdout.println("Beginning user stream...")
        dumpContext.stdout.flush()
        val callback = object : MastodonUserStreamCallback() {
            override fun onConnected(): Boolean {
                return false
            }

            override fun onException(ex: Throwable): Boolean {
                ex.printStackTrace(dumpContext.stderr)
                dumpContext.stderr.flush()
                return true
            }

            override fun onUpdate(status: Status): Boolean {
                return false
            }

            override fun onNotification(notification: Notification): Boolean {
                return false
            }

            override fun onDelete(id: String): Boolean {
                return false
            }

            override fun onUnhandledEvent(event: String, payload: String) {
                dumpContext.stdout.println(payload)
                dumpContext.stdout.flush()
            }

        }
        streaming.getUserStream(callback)
    }

    private fun beginTwitterStream(account: AccountDetails, dumpContext: DumperContext,
            includeInteractions: Boolean, includeTimeline: Boolean, verboseMode: Boolean,
            manager: UserColorNameManager) {
        val userStream = account.newMicroBlogInstance(context, cls = TwitterUserStream::class.java)
        dumpContext.stdout.println("Beginning user stream...")
        dumpContext.stdout.flush()
        val callback = object : TwitterTimelineStreamCallback(account.key.id) {
            override fun onException(ex: Throwable): Boolean {
                ex.printStackTrace(dumpContext.stderr)
                dumpContext.stderr.flush()
                return true
            }

            override fun onHomeTimeline(status: Status): Boolean {
                if (!includeTimeline && includeInteractions) return true
                if (verboseMode) {
                    dumpContext.stdout.println("Home: @${status.user.screenName}: ${status.toString().trim('\n')}")
                } else {
                    dumpContext.stdout.println("Home: @${status.user.screenName}: ${status.text.trim('\n')}")
                }
                dumpContext.stdout.flush()
                return true
            }

            override fun onActivityAboutMe(activity: Activity): Boolean {
                if (!includeInteractions && includeTimeline) return true
                if (verboseMode) {
                    dumpContext.stdout.println("Activity: @${activity.toString().trim('\n')}")
                } else {
                    val pActivity = activity.toParcelable(account)
                    val message = ActivityTitleSummaryMessage.get(context, manager, pActivity,
                            pActivity.sources_lite, 0, true, true)
                    if (message != null) {
                        dumpContext.stdout.println("Activity: ${message.title}: ${message.summary}")
                    } else {
                        dumpContext.stdout.println("Activity unsupported: ${activity.action}")
                    }
                }
                dumpContext.stdout.flush()
                return true
            }

            override fun onDirectMessage(directMessage: DirectMessage): Boolean {
                if (verboseMode) {
                    dumpContext.stdout.println("Message: @${directMessage.senderScreenName}: ${directMessage.toString().trim('\n')}")
                } else {
                    dumpContext.stdout.println("Message: @${directMessage.senderScreenName}: ${directMessage.text.trim('\n')}")
                }
                dumpContext.stdout.flush()
                return true
            }
        }
        try {
            userStream.getUserStream(StreamWith.USER, callback)
        } catch (e: Exception) {
            e.printStackTrace(dumpContext.stderr)
        }
    }

    private fun beginFanfouStream(account: AccountDetails, dumpContext: DumperContext,
            includeInteractions: Boolean, includeTimeline: Boolean, verboseMode: Boolean,
            manager: UserColorNameManager) {
        val userStream = account.newMicroBlogInstance(context, cls = FanfouStream::class.java)
        dumpContext.stdout.println("Beginning user stream...")
        dumpContext.stdout.flush()
        if (includeTimeline) {
            dumpContext.stderr.println("Timeline only is effectively useless for Fanfou")
            dumpContext.stderr.flush()
        }
        val callback = object : FanfouTimelineStreamCallback(account.key.id) {

            override fun onException(ex: Throwable): Boolean {
                ex.printStackTrace(dumpContext.stderr)
                dumpContext.stderr.flush()
                return true
            }

            override fun onHomeTimeline(status: Status): Boolean {
                return false
            }

            override fun onActivityAboutMe(activity: Activity): Boolean {
                if (!includeInteractions && includeTimeline) return true
                val pActivity = activity.toParcelable(account)
                val message = ActivityTitleSummaryMessage.get(context, manager, pActivity,
                        pActivity.sources_lite, 0, true, true)
                if (message != null) {
                    dumpContext.stdout.println("Activity: ${message.title}: ${message.summary}")
                } else {
                    dumpContext.stdout.println("Activity unsupported: ${activity.action}")
                }
                dumpContext.stdout.flush()
                return true
            }

            override fun onUnhandledEvent(event: String, json: String) {
                dumpContext.stdout.println("Unhandled: $event: $json")
                dumpContext.stdout.flush()
            }
        }
        try {
            userStream.getUserStream(callback)
        } catch (e: Exception) {
            e.printStackTrace(dumpContext.stderr)
        }
    }

    override fun getName() = "userstream"

}