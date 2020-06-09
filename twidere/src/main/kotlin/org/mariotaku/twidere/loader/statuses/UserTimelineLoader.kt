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

package org.mariotaku.twidere.loader.statuses

import android.content.Context
import androidx.annotation.WorkerThread
import android.text.TextUtils
import okhttp3.HttpUrl
import org.attoparser.ParseException
import org.attoparser.config.ParseConfiguration
import org.attoparser.simple.AbstractSimpleMarkupHandler
import org.attoparser.simple.SimpleMarkupParser
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.LinkHeaderList
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.TimelineOption
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.alias.MastodonTimelineOption
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.extension.api.tryShowUser
import org.mariotaku.twidere.extension.model.api.mastodon.mapToPaginated
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.api.updateFilterInfoForUserTimeline
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.UserTimelineFilter
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.database.ContentFiltersUtils
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference

class UserTimelineLoader(
        context: Context,
        accountKey: UserKey?,
        private val userKey: UserKey?,
        private val screenName: String?,
        private val profileUrl: String?,
        data: List<ParcelableStatus>?,
        savedStatusesArgs: Array<String>?,
        tabPosition: Int,
        fromUser: Boolean,
        loadingMore: Boolean,
        val loadPinnedStatus: Boolean,
        val timelineFilter: UserTimelineFilter? = null
) : AbsRequestStatusesLoader(context, accountKey, data, savedStatusesArgs, tabPosition, fromUser, loadingMore) {

    private val pinnedStatusesRef = AtomicReference<List<ParcelableStatus>>()

    var pinnedStatuses: List<ParcelableStatus>?
        get() = pinnedStatusesRef.get()
        private set(value) {
            pinnedStatusesRef.set(value)
        }

    @Throws(MicroBlogException::class)
    override fun getStatuses(account: AccountDetails, paging: Paging) = when (account.type) {
        AccountType.MASTODON -> getMastodonStatuses(account, paging).mapToPaginated {
            it.toParcelable(account)
        }
        else -> getMicroBlogStatuses(account, paging).mapMicroBlogToPaginated {
            it.toParcelable(account, profileImageSize = profileImageSize,
                    updateFilterInfoAction = ::updateFilterInfoForUserTimeline)
        }
    }

    @WorkerThread
    override fun shouldFilterStatus(status: ParcelableStatus): Boolean {
        if (timelineFilter != null) {
            if (status.is_retweet && !timelineFilter.isIncludeRetweets) {
                return true
            }
        }
        if (accountKey != null && userKey != null && TextUtils.equals(accountKey.id, userKey.id))
            return false
        return ContentFiltersUtils.isFiltered(context.contentResolver, status, true,
                FilterScope.USER_TIMELINE)
    }

    private fun getMastodonStatuses(account: AccountDetails, paging: Paging): LinkHeaderList<MastodonStatus> {
        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
        val id = userKey?.id ?: throw MicroBlogException("Only ID are supported at this moment")
        val option = MastodonTimelineOption()
        if (timelineFilter != null) {
            option.excludeReplies(!timelineFilter.isIncludeReplies)
        }

        return mastodon.getStatuses(id, paging, option)
    }

    private fun getMicroBlogStatuses(account: AccountDetails, paging: Paging): List<Status> {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        if (loadPinnedStatus && account.type == AccountType.TWITTER && !loadingMore) {
            pinnedStatuses = try {
                val pinnedIds = microBlog.tryShowUser(userKey?.id, screenName, AccountType.TWITTER).pinnedTweetIds
                if (pinnedIds != null && pinnedIds.isNotEmpty()) {
                    microBlog.lookupStatuses(pinnedIds).mapIndexed { idx, status ->
                        val created = status.toParcelable(account, profileImageSize = profileImageSize)
                        created.sort_id = idx.toLong()
                        created.is_pinned_status = true
                        return@mapIndexed created
                    }
                } else {
                    null
                }
            } catch (e: MicroBlogException) {
                null
            }
        }
        val option = TimelineOption()
        if (timelineFilter != null) {
            option.setExcludeReplies(!timelineFilter.isIncludeReplies)
            option.setIncludeRetweets(timelineFilter.isIncludeRetweets)
        }
        when {
            userKey != null -> {
                if (account.type == AccountType.STATUSNET && userKey.host != account.key.host
                    && profileUrl != null) {
                    try {
                        return showStatusNetExternalTimeline(profileUrl, paging)
                    } catch (e: IOException) {
                        throw MicroBlogException(e)
                    }
                }
                return microBlog.getUserTimeline(userKey.id, paging, option)
            }
            screenName != null -> {
                return microBlog.getUserTimelineByScreenName(screenName, paging, option)
            }
            else -> {
                throw MicroBlogException("Invalid user")
            }
        }
    }

    @Throws(IOException::class)
    private fun showStatusNetExternalTimeline(profileUrl: String, paging: Paging): List<Status> {
        val holder = DependencyHolder.get(context)
        val client = holder.restHttpClient
        val parser = SimpleMarkupParser(ParseConfiguration.htmlConfiguration())
        val pageRequest = HttpRequest.Builder().apply {
            method(GET.METHOD)
            url(profileUrl)
        }.build()
        val validAtomSuffix = ".atom"
        val requestLink = client.newCall(pageRequest).execute().use {
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
        } ?: throw IOException("No atom link found fof external user")
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
        fun getMastodonStatuses(mastodon: Mastodon, userKey: UserKey?, screenName: String?, paging: Paging,
                option: MastodonTimelineOption?): LinkHeaderList<MastodonStatus> {
            val id = userKey?.id ?: throw MicroBlogException("Only ID are supported at this moment")
            return mastodon.getStatuses(id, paging, option)
        }
    }
}
