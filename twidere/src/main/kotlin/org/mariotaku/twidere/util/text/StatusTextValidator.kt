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

package org.mariotaku.twidere.util.text

import org.mariotaku.ktextension.mapToIntArray
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.text.twitter.getTweetLength
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey

object StatusTextValidator {

    fun calculateLength(@AccountType accountType: String, accountKey: UserKey?, summary: String?,
            text: String, ignoreMentions: Boolean = false, inReplyTo: ParcelableStatus? = null) = when (accountType) {
        AccountType.TWITTER -> {
            TwitterValidator.getTweetLength(text, ignoreMentions, inReplyTo, accountKey)
        }
        AccountType.MASTODON -> {
            MastodonValidator.getCountableLength(summary, text)
        }
        AccountType.FANFOU -> {
            FanfouValidator.calculateLength(text)
        }
        AccountType.STATUSNET -> {
            text.codePointCount(0, text.length)
        }
        else -> {
            text.codePointCount(0, text.length)
        }
    }

    fun calculateLengths(accounts: Array<AccountDetails>, summary: String?, text: String,
            ignoreMentions: Boolean = false, inReplyTo: ParcelableStatus? = null): IntArray {
        return accounts.mapToIntArray {
            calculateLength(it.type, it.key, summary, text, ignoreMentions, inReplyTo)
        }
    }

    fun calculateLength(accounts: Array<AccountDetails>, summary: String?, text: String,
            ignoreMentions: Boolean = false, inReplyTo: ParcelableStatus? = null): Int {
        return calculateLengths(accounts, summary, text, ignoreMentions, inReplyTo).max() ?: 0
    }

    fun calculateSummaryLength(@AccountType accountType: String,
            summary: String?) = when (accountType) {
        AccountType.MASTODON -> {
            MastodonValidator.getCountableLength(summary, "")
        }
        else -> 0
    }

    fun calculateSummaryLengths(accounts: Array<AccountDetails>, summary: String?): IntArray {
        return accounts.mapToIntArray {
            calculateSummaryLength(it.type, summary)
        }
    }

    fun calculateSummaryLength(accounts: Array<AccountDetails>, summary: String?): Int {
        return calculateSummaryLengths(accounts, summary).max() ?: 0
    }
}
