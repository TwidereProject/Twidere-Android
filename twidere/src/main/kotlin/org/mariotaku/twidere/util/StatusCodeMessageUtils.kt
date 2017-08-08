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

package org.mariotaku.twidere.util

import android.content.Context
import android.text.TextUtils
import android.util.SparseIntArray

import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.ErrorInfoStore.DisplayErrorInfo

object StatusCodeMessageUtils {

    private val TWITTER_ERROR_CODE_MESSAGES = SparseIntArray()
    private val TWITTER_ERROR_CODE_ICONS = SparseIntArray()

    private val HTTP_STATUS_CODE_MESSAGES = SparseIntArray()
    private val HTTP_STATUS_CODE_ICONS = SparseIntArray()

    init {
        TWITTER_ERROR_CODE_MESSAGES.put(32, R.string.error_twitter_32)
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.PAGE_NOT_FOUND, R.string.error_twitter_34)
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.RATE_LIMIT_EXCEEDED, R.string.error_twitter_88)
        TWITTER_ERROR_CODE_MESSAGES.put(89, R.string.error_twitter_89)
        TWITTER_ERROR_CODE_MESSAGES.put(64, R.string.error_twitter_64)
        TWITTER_ERROR_CODE_MESSAGES.put(130, R.string.error_twitter_130)
        TWITTER_ERROR_CODE_MESSAGES.put(131, R.string.error_twitter_131)
        TWITTER_ERROR_CODE_MESSAGES.put(135, R.string.error_twitter_135)
        TWITTER_ERROR_CODE_MESSAGES.put(136, R.string.error_twitter_136)
        TWITTER_ERROR_CODE_MESSAGES.put(139, R.string.error_twitter_139)
        TWITTER_ERROR_CODE_MESSAGES.put(144, R.string.error_twitter_144)
        TWITTER_ERROR_CODE_MESSAGES.put(161, R.string.error_twitter_161)
        TWITTER_ERROR_CODE_MESSAGES.put(162, R.string.error_twitter_162)
        TWITTER_ERROR_CODE_MESSAGES.put(172, R.string.error_twitter_172)
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.NOT_AUTHORIZED, R.string.error_twitter_179)
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.STATUS_IS_DUPLICATE, R.string.error_twitter_187)
        TWITTER_ERROR_CODE_MESSAGES.put(193, R.string.error_twitter_193)
        TWITTER_ERROR_CODE_MESSAGES.put(215, R.string.error_twitter_215)
        TWITTER_ERROR_CODE_MESSAGES.put(326, R.string.error_twitter_326)
        TWITTER_ERROR_CODE_MESSAGES.put(327, R.string.error_twitter_327)

        TWITTER_ERROR_CODE_ICONS.put(136, R.drawable.ic_info_error_blocked)

        HTTP_STATUS_CODE_MESSAGES.put(407, R.string.error_http_407)
    }

    fun containsHttpStatus(code: Int): Boolean {
        return HTTP_STATUS_CODE_MESSAGES.get(code, -1) != -1
    }

    internal fun containsTwitterError(code: Int): Boolean {
        return TWITTER_ERROR_CODE_MESSAGES.get(code, -1) != -1
    }

    internal fun getHttpStatusMessage(context: Context, code: Int): String? {
        val resId = HTTP_STATUS_CODE_MESSAGES.get(code, -1)
        if (resId > 0) return context.getString(resId)
        return null
    }

    private fun getHttpStatusInfo(context: Context, code: Int): DisplayErrorInfo? {
        val messageId = HTTP_STATUS_CODE_MESSAGES.get(code, 0)
        if (messageId == 0) return null
        val icon = HTTP_STATUS_CODE_ICONS.get(code, R.drawable.ic_info_error_generic)
        return DisplayErrorInfo(code, icon, context.getString(messageId))
    }

    private fun getTwitterErrorInfo(context: Context, code: Int): DisplayErrorInfo? {
        val messageId = TWITTER_ERROR_CODE_MESSAGES.get(code, 0)
        if (messageId == 0) return null
        val icon = TWITTER_ERROR_CODE_ICONS.get(code, R.drawable.ic_info_error_generic)
        return DisplayErrorInfo(code, icon, context.getString(messageId))
    }

    fun getMessage(context: Context, statusCode: Int, errorCode: Int): String? {
        if (containsHttpStatus(statusCode)) return getHttpStatusMessage(context, statusCode)
        if (containsTwitterError(errorCode)) return getTwitterErrorMessage(context, errorCode)
        return null
    }

    internal fun getTwitterErrorMessage(context: Context, code: Int): String? {
        val resId = TWITTER_ERROR_CODE_MESSAGES.get(code, -1)
        if (resId > 0) return context.getString(resId)
        return null
    }


    private fun getErrorInfo(context: Context, statusCode: Int,
            errors: Array<ErrorInfo>?): DisplayErrorInfo? {
        var errorCode = -1
        if (errors != null)
            for (error in errors) {
                errorCode = error.code
                if (errorCode > 0) break
                val message = error.message
                if (!TextUtils.isEmpty(message)) {
                    return DisplayErrorInfo(0, R.drawable.ic_info_error_generic, message)
                }
            }
        if (containsTwitterError(errorCode)) return getTwitterErrorInfo(context, errorCode)
        if (containsHttpStatus(statusCode)) return getHttpStatusInfo(context, statusCode)
        return null
    }


    fun getMicroBlogErrorInfo(context: Context, te: MicroBlogException): DisplayErrorInfo {
        return getErrorInfo(context, te.statusCode, te.errors) ?:
                DisplayErrorInfo(0, R.drawable.ic_info_error_generic, te.message.orEmpty())
    }

    fun getMicroBlogErrorMessage(context: Context, te: MicroBlogException): String? {
        val errorInfo = getErrorInfo(context, te.statusCode, te.errors)
        if (errorInfo != null) return errorInfo.message
        return te.message
    }

    fun getErrorInfo(context: Context, t: Throwable): DisplayErrorInfo {
        if (t is MicroBlogException) return getMicroBlogErrorInfo(context, t)
        return DisplayErrorInfo(0, R.drawable.ic_info_error_generic, t.message.orEmpty())
    }
}
