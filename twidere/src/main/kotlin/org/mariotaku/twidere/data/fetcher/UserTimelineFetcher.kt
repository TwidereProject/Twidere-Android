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

package org.mariotaku.twidere.data.fetcher

import android.support.v4.util.ArrayMap
import okhttp3.HttpUrl
import org.attoparser.ParseException
import org.attoparser.config.ParseConfiguration
import org.attoparser.simple.AbstractSimpleMarkupHandler
import org.attoparser.simple.SimpleMarkupParser
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.statusnet.StatusNet
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.TimelineOption
import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.alias.MastodonTimelineOption
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.timeline.UserTimelineFilter
import org.mariotaku.twidere.util.JsonSerializer
import java.io.IOException

class UserTimelineFetcher(
        private val userKey: UserKey?,
        private val userScreenName: String?,
        private val profileUrl: String?
) : StatusesFetcher {

    override fun forTwitter(account: AccountDetails, twitter: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        val option = (filter as? UserTimelineFilter)?.toTwitterTimelineOption()
        return when {
            userKey != null -> twitter.getUserTimeline(userKey.id, paging, option)
            userScreenName != null -> twitter.getUserTimelineByScreenName(userScreenName, paging, option)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

    override fun forStatusNet(account: AccountDetails, statusNet: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        if (userKey?.host != account.key.host && profileUrl != null) {
            return statusNet.showStatusNetExternalTimeline(profileUrl, paging)
        }
        return when {
            userKey != null -> statusNet.getUserTimeline(userKey.id, paging, null)
            userScreenName != null -> statusNet.getUserTimelineByScreenName(userScreenName, paging, null)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

    override fun forFanfou(account: AccountDetails, fanfou: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        return when {
            userKey != null -> fanfou.getUserTimeline(userKey.id, paging, null)
            userScreenName != null -> fanfou.getUserTimelineByScreenName(userScreenName, paging, null)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging, filter: TimelineFilter?): List<MastodonStatus> {
        val id = userKey?.id ?: throw MicroBlogException("Only ID are supported at this moment")
        val option = (filter as? UserTimelineFilter)?.toMastodonTimelineOption()
        return mastodon.getStatuses(id, paging, option)
    }

    private fun getMicroBlogUserFavorites(microBlog: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        val option = (filter as? UserTimelineFilter)?.toTwitterTimelineOption()
        return when {
            userKey != null -> microBlog.getUserTimeline(userKey.id, paging, option)
            userScreenName != null -> microBlog.getUserTimelineByScreenName(userScreenName, paging, option)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

    private fun UserTimelineFilter.toTwitterTimelineOption() = TimelineOption().apply {
        setExcludeReplies(!isIncludeReplies)
        setIncludeRetweets(isIncludeRetweets)
    }

    private fun UserTimelineFilter.toMastodonTimelineOption() = MastodonTimelineOption().apply {
        excludeReplies(!isIncludeReplies)
    }


    @Throws(IOException::class)
    private fun StatusNet.showStatusNetExternalTimeline(profileUrl: String, paging: Paging): List<Status> {
        val client = RestAPIFactory.getRestClient(this).restClient
        val requestLink = client.findExternalUserStatusesLink(profileUrl)
        val queries = paging.asMap().map { arrayOf(it.key, it.value?.toString()) }.toTypedArray()
        val restRequest = HttpRequest.Builder().apply {
            method(GET.METHOD)
            url(Endpoint.constructUrl(requestLink, *queries))
        }.build()
        return client.newCall(restRequest).execute().use {
            if (!it.isSuccessful) throw IOException("Server returned ${it.status} response")
            return@use JsonSerializer.parseList(it.body.stream(), Status::class.java)
        }
    }

    private class AtomLinkFindHandler(val profileUrl: String) : AbstractSimpleMarkupHandler() {
        var atomLink: String? = null
        override fun handleStandaloneElement(elementName: String, attributes: Map<String, String>?,
                minimized: Boolean, line: Int, col: Int) {
            if (atomLink != null || elementName != "link" || attributes == null) return
            if (attributes["rel"] == "alternate" && attributes["type"] == "application/atom+xml") {
                val href = attributes["href"] ?: return
                atomLink = HttpUrl.parse(profileUrl)?.resolve(href)?.toString()
            }
        }
    }

    companion object {
        private val statusesLinkCache = ArrayMap<String, String>()
        private fun RestHttpClient.findExternalUserStatusesLink(profileUrl: String): String {
            val cached = statusesLinkCache[profileUrl]
            if (cached != null) return cached
            val parser = SimpleMarkupParser(ParseConfiguration.htmlConfiguration())
            val pageRequest = HttpRequest.Builder().apply {
                method(GET.METHOD)
                url(profileUrl)
            }.build()
            val validAtomSuffix = ".atom"
            val requestLink = newCall(pageRequest).execute().use {
                if (!it.isSuccessful) throw IOException("Server returned ${it.status} response")
                val handler = AtomLinkFindHandler(profileUrl)
                try {
                    parser.parse(SimpleBody.reader(it.body), handler)
                } catch (e: ParseException) {
                    // Ignore
                }
                return@use handler.atomLink
            }?.takeIf { it.endsWith(validAtomSuffix) }?.let {
                it.replaceRange(it.length - validAtomSuffix.length, it.length, ".json")
            } ?: throw IOException("No atom link found for external user")
            statusesLinkCache[profileUrl] = requestLink
            return requestLink
        }
    }
}