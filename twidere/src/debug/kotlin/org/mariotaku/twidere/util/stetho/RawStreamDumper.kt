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
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterUserStream
import org.mariotaku.restfu.callback.RawCallback
import org.mariotaku.restfu.http.HttpResponse
import org.mariotaku.twidere.extension.model.getCredentials
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils

/**
 * Created by mariotaku on 2017/3/9.
 */
class RawStreamDumper(val context: Context) : DumperPlugin {

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
        userStream.getUserStreamRaw(object : RawCallback<MicroBlogException> {
            override fun result(result: HttpResponse) {
                dumpContext.stdout.println("Response: ${result.status}")
                dumpContext.stdout.println("Headers:")
                result.headers.toList().forEach {
                    dumpContext.stdout.println("${it.first}: ${it.second}")
                }
                dumpContext.stdout.println()
                result.body.writeTo(dumpContext.stdout)
            }

            override fun error(exception: MicroBlogException) {
                exception.printStackTrace(dumpContext.stderr)
            }

        })
    }

    override fun getName() = "raw_stream"

}