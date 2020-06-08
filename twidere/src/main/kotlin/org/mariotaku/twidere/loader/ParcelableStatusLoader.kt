/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.os.Bundle
import androidx.loader.content.FixedAsyncTaskLoader
import org.mariotaku.ktextension.set
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT
import org.mariotaku.twidere.extension.model.updateExtraInformation
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.DataStoreUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.deleteActivityStatus
import javax.inject.Inject

/**
 * Created by mariotaku on 14/12/5.
 */
class ParcelableStatusLoader(
        context: Context,
        private val omitIntentExtra: Boolean,
        private val extras: Bundle?,
        private val accountKey: UserKey?,
        private val statusId: String?
) : FixedAsyncTaskLoader<SingleResponse<ParcelableStatus>>(context) {

    @Inject
    internal lateinit var userColorNameManager: UserColorNameManager
    @Inject
    internal lateinit var restHttpClient: RestHttpClient

    init {
        GeneralComponent.get(context).inject(this)
    }

    override fun loadInBackground(): SingleResponse<ParcelableStatus> {
        if (accountKey == null || statusId == null) {
            return SingleResponse(IllegalArgumentException())
        }
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
        if (!omitIntentExtra && extras != null) {
            val cache: ParcelableStatus? = extras.getParcelable(IntentConstants.EXTRA_STATUS)
            if (cache != null) {
                val response = SingleResponse(cache)
                response.extras[EXTRA_ACCOUNT] = details
                return response
            }
        }
        if (details == null) return SingleResponse(MicroBlogException("No account"))
        return try {
            val status = DataStoreUtils.findStatus(context, accountKey, statusId)
            status.updateExtraInformation(details)
            val response = SingleResponse(status)
            response.extras[EXTRA_ACCOUNT] = details
            response
        } catch (e: MicroBlogException) {
            if (e.errorCode == ErrorInfo.STATUS_NOT_FOUND) {
                // Delete all deleted status
                val cr = context.contentResolver
                DataStoreUtils.deleteStatus(cr, accountKey, statusId, null)
                cr.deleteActivityStatus(accountKey, statusId, null)
            }
            SingleResponse(e)
        }

    }

    override fun onStartLoading() {
        forceLoad()
    }


}
