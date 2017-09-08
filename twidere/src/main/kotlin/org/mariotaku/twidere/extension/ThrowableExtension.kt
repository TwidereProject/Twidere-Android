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

package org.mariotaku.twidere.extension

import android.content.Context
import android.text.format.DateUtils
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.StatusCodeMessageUtils
import java.security.cert.CertPathValidatorException

fun Throwable.getErrorMessage(context: Context): CharSequence = when (this) {
    is MicroBlogException -> getMicroBlogErrorMessage(context)
    is CertPathValidatorException -> context.getString(R.string.message_toast_ssl_tls_error)
    else -> message ?: toString()
}

private fun MicroBlogException.getMicroBlogErrorMessage(context: Context): String {
    val rateLimitStatus = this.rateLimitStatus
    if (isRateLimitExceeded && rateLimitStatus != null) {
        val secUntilReset = rateLimitStatus.secondsUntilReset * 1000L
        val nextResetTime = DateUtils.getRelativeTimeSpanString(System.currentTimeMillis() + secUntilReset)
        return context.getString(R.string.error_message_rate_limit, nextResetTime.trim())
    } else if (isCausedByNetworkIssue) {
        val msg = cause?.message
        if (msg.isNullOrEmpty()) {
            return context.getString(R.string.message_toast_network_error)
        }
        return context.getString(R.string.message_toast_network_error_with_message, msg)
    }
    val msg = when {
        StatusCodeMessageUtils.containsTwitterError(errorCode) -> StatusCodeMessageUtils.getTwitterErrorMessage(context, errorCode)
        StatusCodeMessageUtils.containsHttpStatus(statusCode) -> StatusCodeMessageUtils.getHttpStatusMessage(context, statusCode)
        else -> errorMessage
    }
    return msg ?: message ?: javaClass.simpleName
}
