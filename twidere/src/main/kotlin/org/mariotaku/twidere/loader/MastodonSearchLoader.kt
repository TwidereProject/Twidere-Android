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

package org.mariotaku.twidere.loader

import android.accounts.AccountManager
import android.content.Context
import androidx.loader.content.FixedAsyncTaskLoader
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableHashtag
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils

class MastodonSearchLoader(
        context: Context,
        private val accountKey: UserKey?,
        private val query: String
) : FixedAsyncTaskLoader<List<Any>>(context), Constants {

    override fun loadInBackground(): List<Any> {
        try {
            val am = AccountManager.get(context)
            val account = accountKey?.let {
                AccountUtils.getAccountDetails(am, it, true)
            } ?: throw AccountNotFoundException()
            val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
            val searchResult = mastodon.search(query, true, null)
            return ListResponse(ArrayList<Any>().apply {
                searchResult.accounts?.mapTo(this) {
                    it.toParcelable(account)
                }
                searchResult.hashtags?.mapTo(this) { hashtag ->
                    ParcelableHashtag().also { it.hashtag = hashtag }
                }
                searchResult.statuses?.mapTo(this) {
                    it.toParcelable(account)
                }
            })
        } catch (e: MicroBlogException) {
            return ListResponse(e)
        }
    }

    override fun onStartLoading() {
        forceLoad()
    }

}
