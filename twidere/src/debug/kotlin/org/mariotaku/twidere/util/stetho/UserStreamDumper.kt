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
import org.mariotaku.microblog.library.twitter.UserStreamCallback
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.twidere.extension.model.getCredentials
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils

/**
 * Created by mariotaku on 2017/3/9.
 */
class UserStreamDumper(val context: Context) : DumperPlugin {

    override fun dump(dumpContext: DumperContext) {
        val parser = GnuParser()
        val options = Options()
        options.addRequiredOption("a", "account", true, "Account key")
        val cmdLine = try {
            parser.parse(options, dumpContext.argsAsList.toTypedArray())
        } catch (e: ParseException) {
            throw DumpException(e.message)
        }

        val accountKey = UserKey.valueOf(cmdLine.getOptionValue("account"))
        val am = AccountManager.get(context)
        val account = AccountUtils.findByAccountKey(am, accountKey) ?: return
        val credentials = account.getCredentials(am)
        val userStream = credentials.newMicroBlogInstance(context, account.type,
                cls = TwitterUserStream::class.java)
        dumpContext.stdout.println("Beginning user stream...")
        dumpContext.stdout.flush()
        val callback = object : UserStreamCallback() {
            override fun onException(ex: Throwable): Boolean {
                ex.printStackTrace(dumpContext.stderr)
                dumpContext.stderr.flush()
                return true
            }

            override fun onStatus(status: Status): Boolean {
                dumpContext.stdout.println("Status: @${status.user.screenName}: ${status.text.trim('\n')}")
                dumpContext.stdout.flush()
                return true
            }

            override fun onDirectMessage(directMessage: DirectMessage): Boolean {
                dumpContext.stdout.println("Message: @${directMessage.senderScreenName}: ${directMessage.text.trim('\n')}")
                dumpContext.stdout.flush()
                return true
            }

            override fun onStatusDeleted(event: DeletionEvent): Boolean {
                dumpContext.stdout.println("Status deleted: ${event.id}")
                dumpContext.stdout.flush()
                return true
            }

            override fun onDirectMessageDeleted(event: DeletionEvent): Boolean {
                dumpContext.stdout.println("Message deleted: ${event.id}")
                dumpContext.stdout.flush()
                return true
            }

            override fun onFriendList(friendIds: Array<String>): Boolean {
                dumpContext.stdout.println("Friends list: ${friendIds.size} in total")
                dumpContext.stdout.flush()
                return true
            }

            override fun onFavorite(source: User, target: User, targetStatus: Status): Boolean {
                dumpContext.stdout.println("Favorited: @${source.screenName} -> ${targetStatus.text.trim('\n')}")
                dumpContext.stdout.flush()
                return true
            }

            override fun onUnhandledEvent(obj: TwitterStreamObject, json: String) {
                dumpContext.stdout.println("Unhandled: ${obj.determine()} = $json")
                dumpContext.stdout.flush()
            }
        }
        try {
            userStream.getUserStream(callback)
        } catch (e: Exception) {
            e.printStackTrace(dumpContext.stderr)
        }
    }

    override fun getName() = "user_stream"

}