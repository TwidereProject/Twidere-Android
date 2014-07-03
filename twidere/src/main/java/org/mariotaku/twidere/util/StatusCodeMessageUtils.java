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
import android.util.SparseIntArray;

import org.apache.http.HttpStatus;
import org.mariotaku.twidere.R;

public class StatusCodeMessageUtils {

	public static final int PAGE_NOT_FOUND = 34;
	public static final int RATE_LIMIT_EXCEDDED = 88;
	public static final int NOT_AUTHORIZED = 179;
	public static final int STATUS_IS_DUPLICATE = 187;

	private static final SparseIntArray TWITTER_ERROR_CODE_MESSAGES = new SparseIntArray();

	private static final SparseIntArray HTTP_STATUS_CODE_MESSAGES = new SparseIntArray();

	static {
		TWITTER_ERROR_CODE_MESSAGES.put(32, R.string.error_twitter_32);
		TWITTER_ERROR_CODE_MESSAGES.put(PAGE_NOT_FOUND, R.string.error_twitter_34);
		TWITTER_ERROR_CODE_MESSAGES.put(RATE_LIMIT_EXCEDDED, R.string.error_twitter_88);
		TWITTER_ERROR_CODE_MESSAGES.put(89, R.string.error_twitter_89);
		TWITTER_ERROR_CODE_MESSAGES.put(64, R.string.error_twitter_64);
		TWITTER_ERROR_CODE_MESSAGES.put(130, R.string.error_twitter_130);
		TWITTER_ERROR_CODE_MESSAGES.put(131, R.string.error_twitter_131);
		TWITTER_ERROR_CODE_MESSAGES.put(135, R.string.error_twitter_135);
		TWITTER_ERROR_CODE_MESSAGES.put(161, R.string.error_twitter_161);
		TWITTER_ERROR_CODE_MESSAGES.put(162, R.string.error_twitter_162);
		TWITTER_ERROR_CODE_MESSAGES.put(172, R.string.error_twitter_172);
		TWITTER_ERROR_CODE_MESSAGES.put(NOT_AUTHORIZED, R.string.error_twitter_179);
		TWITTER_ERROR_CODE_MESSAGES.put(STATUS_IS_DUPLICATE, R.string.error_twitter_187);
		TWITTER_ERROR_CODE_MESSAGES.put(193, R.string.error_twitter_193);
		TWITTER_ERROR_CODE_MESSAGES.put(215, R.string.error_twitter_215);
		HTTP_STATUS_CODE_MESSAGES.put(HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED, R.string.error_http_407);
	}

	public static boolean containsHttpStatus(final int code) {
		return HTTP_STATUS_CODE_MESSAGES.get(code, -1) != -1;
	}

	public static boolean containsTwitterError(final int code) {
		return TWITTER_ERROR_CODE_MESSAGES.get(code, -1) != -1;
	}

	public static String getHttpStatusMessage(final Context context, final int code) {
		if (context == null) return null;
		final int res_id = HTTP_STATUS_CODE_MESSAGES.get(code, -1);
		if (res_id > 0) return context.getString(res_id);
		return null;
	}

	public static String getMessage(final Context context, final int statusCode, final int errorCode) {
		if (containsHttpStatus(statusCode)) return getHttpStatusMessage(context, statusCode);
		if (containsTwitterError(errorCode)) return getTwitterErrorMessage(context, errorCode);
		return null;
	}

	public static String getTwitterErrorMessage(final Context context, final int code) {
		if (context == null) return null;
		final int res_id = TWITTER_ERROR_CODE_MESSAGES.get(code, -1);
		if (res_id > 0) return context.getString(res_id);
		return null;
	}
}
