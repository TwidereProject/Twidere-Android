/*
 * 				Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseIntArray;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ErrorInfo;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ErrorInfoStore.DisplayErrorInfo;

public class StatusCodeMessageUtils {

    private static final SparseIntArray TWITTER_ERROR_CODE_MESSAGES = new SparseIntArray();
    private static final SparseIntArray TWITTER_ERROR_CODE_ICONS = new SparseIntArray();

    private static final SparseIntArray HTTP_STATUS_CODE_MESSAGES = new SparseIntArray();
    private static final SparseIntArray HTTP_STATUS_CODE_ICONS = new SparseIntArray();

    static {
        TWITTER_ERROR_CODE_MESSAGES.put(32, R.string.error_twitter_32);
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.PAGE_NOT_FOUND, R.string.error_twitter_34);
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.RATE_LIMIT_EXCEEDED, R.string.error_twitter_88);
        TWITTER_ERROR_CODE_MESSAGES.put(89, R.string.error_twitter_89);
        TWITTER_ERROR_CODE_MESSAGES.put(64, R.string.error_twitter_64);
        TWITTER_ERROR_CODE_MESSAGES.put(130, R.string.error_twitter_130);
        TWITTER_ERROR_CODE_MESSAGES.put(131, R.string.error_twitter_131);
        TWITTER_ERROR_CODE_MESSAGES.put(135, R.string.error_twitter_135);
        TWITTER_ERROR_CODE_MESSAGES.put(136, R.string.error_twitter_136);
        TWITTER_ERROR_CODE_MESSAGES.put(139, R.string.error_twitter_139);
        TWITTER_ERROR_CODE_MESSAGES.put(144, R.string.error_twitter_144);
        TWITTER_ERROR_CODE_MESSAGES.put(161, R.string.error_twitter_161);
        TWITTER_ERROR_CODE_MESSAGES.put(162, R.string.error_twitter_162);
        TWITTER_ERROR_CODE_MESSAGES.put(172, R.string.error_twitter_172);
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.NOT_AUTHORIZED, R.string.error_twitter_179);
        TWITTER_ERROR_CODE_MESSAGES.put(ErrorInfo.STATUS_IS_DUPLICATE, R.string.error_twitter_187);
        TWITTER_ERROR_CODE_MESSAGES.put(193, R.string.error_twitter_193);
        TWITTER_ERROR_CODE_MESSAGES.put(215, R.string.error_twitter_215);
        TWITTER_ERROR_CODE_MESSAGES.put(326, R.string.error_twitter_326);

        TWITTER_ERROR_CODE_ICONS.put(136, R.drawable.ic_info_error_blocked);

        HTTP_STATUS_CODE_MESSAGES.put(407, R.string.error_http_407);
    }

    private StatusCodeMessageUtils() {
    }

    public static boolean containsHttpStatus(final int code) {
        return HTTP_STATUS_CODE_MESSAGES.get(code, -1) != -1;
    }

    static boolean containsTwitterError(final int code) {
        return TWITTER_ERROR_CODE_MESSAGES.get(code, -1) != -1;
    }

    @Nullable
    static String getHttpStatusMessage(final Context context, final int code) {
        if (context == null) return null;
        final int res_id = HTTP_STATUS_CODE_MESSAGES.get(code, -1);
        if (res_id > 0) return context.getString(res_id);
        return null;
    }

    @Nullable
    private static DisplayErrorInfo getHttpStatusInfo(final Context context, final int code) {
        final int messageId = HTTP_STATUS_CODE_MESSAGES.get(code, 0);
        if (messageId == 0) return null;
        final int icon = HTTP_STATUS_CODE_ICONS.get(code, R.drawable.ic_info_error_generic);
        return new DisplayErrorInfo(code, icon, context.getString(messageId));
    }

    @Nullable
    private static DisplayErrorInfo getTwitterErrorInfo(final Context context, final int code) {
        final int messageId = TWITTER_ERROR_CODE_MESSAGES.get(code, 0);
        if (messageId == 0) return null;
        final int icon = TWITTER_ERROR_CODE_ICONS.get(code, R.drawable.ic_info_error_generic);
        return new DisplayErrorInfo(code, icon, context.getString(messageId));
    }

    @Nullable
    public static String getMessage(final Context context, final int statusCode, final int errorCode) {
        if (containsHttpStatus(statusCode)) return getHttpStatusMessage(context, statusCode);
        if (containsTwitterError(errorCode)) return getTwitterErrorMessage(context, errorCode);
        return null;
    }

    @Nullable
    static String getTwitterErrorMessage(final Context context, final int code) {
        if (context == null) return null;
        final int resId = TWITTER_ERROR_CODE_MESSAGES.get(code, -1);
        if (resId > 0) return context.getString(resId);
        return null;
    }


    @Nullable
    private static DisplayErrorInfo getErrorInfo(@NonNull final Context context, final int statusCode,
            @Nullable final ErrorInfo[] errors) {
        int errorCode = -1;
        if (errors != null) for (ErrorInfo error : errors) {
            errorCode = error.getCode();
            if (errorCode > 0) break;
            String message = error.getMessage();
            if (!TextUtils.isEmpty(message)) {
                return new DisplayErrorInfo(0, R.drawable.ic_info_error_generic, message);
            }
        }
        if (containsTwitterError(errorCode)) return getTwitterErrorInfo(context, errorCode);
        if (containsHttpStatus(statusCode)) return getHttpStatusInfo(context, statusCode);
        return null;
    }


    @NonNull
    public static DisplayErrorInfo getMicroBlogErrorInfo(final Context context, final MicroBlogException te) {
        final DisplayErrorInfo errorInfo = getErrorInfo(context, te.getStatusCode(), te.getErrors());
        if (errorInfo != null) return errorInfo;
        return new DisplayErrorInfo(0, R.drawable.ic_info_error_generic, te.getMessage());
    }

    @Nullable
    public static String getMicroBlogErrorMessage(final Context context, final MicroBlogException te) {
        final DisplayErrorInfo errorInfo = getErrorInfo(context, te.getStatusCode(), te.getErrors());
        if (errorInfo != null) return errorInfo.getMessage();
        return te.getMessage();
    }

    public static DisplayErrorInfo getErrorInfo(@NonNull final Context context, @NonNull final Throwable t) {
        if (t instanceof MicroBlogException)
            return getMicroBlogErrorInfo(context, (MicroBlogException) t);
        return new DisplayErrorInfo(0, R.drawable.ic_info_error_generic, t.getMessage());
    }
}
