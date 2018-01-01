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

package org.mariotaku.twidere.data.fetcher.users

import android.content.Context
import org.attoparser.config.ParseConfiguration
import org.attoparser.simple.AbstractSimpleMarkupHandler
import org.attoparser.simple.SimpleMarkupParser
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.Twitter
import org.mariotaku.microblog.library.model.Paging
import org.mariotaku.microblog.library.model.mastodon.Account
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList
import org.mariotaku.microblog.library.model.microblog.IDs
import org.mariotaku.microblog.library.model.microblog.User
import org.mariotaku.microblog.library.model.microblog.setIds
import org.mariotaku.microblog.library.twitter.TwitterWeb
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.model.AccountDetails
import java.text.ParseException

class StatusFavoritersFetcher(
        val context: Context,
        private val statusId: String
) : UsersFetcher {
    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging): LinkHeaderList<Account> {
        return mastodon.getStatusFavouritedBy(statusId)
    }

    override fun forTwitter(account: AccountDetails, twitter: Twitter, paging: Paging): List<User> {
        return if (account.extras?.official == true) {
            twitter.getFavoritedBy(statusId, paging)
        } else {
            val web = account.newMicroBlogInstance(context, TwitterWeb::class.java)
            val htmlUsers = web.getFavoritedPopup(statusId).htmlUsers
            val ids = IDs().setIds(parseUserIds(htmlUsers))
            twitter.lookupUsers(ids.iDs)
        }
    }

    @Throws(MicroBlogException::class)
    private fun parseUserIds(html: String): Array<String> {
        val parser = SimpleMarkupParser(ParseConfiguration.htmlConfiguration())
        val userIds = ArrayList<String>()
        val handler = object : AbstractSimpleMarkupHandler() {
            override fun handleOpenElement(elementName: String, attributes: Map<String, String>?,
                    line: Int, col: Int) {
                if (elementName == "div" && attributes != null) {
                    if (attributes["class"]?.split(" ")?.contains("account") == true) {
                        attributes["data-user-id"]?.let { userIds.add(it) }
                    }
                }
            }
        }
        try {
            parser.parse(html, handler)
        } catch (e: ParseException) {
            throw MicroBlogException(e)
        }
        if (userIds.isEmpty()) {
            throw MicroBlogException("Invalid response")
        }
        return userIds.distinct().toTypedArray()
    }

}
