/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package twitter4j.internal.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import twitter4j.TwitterException;
import twitter4j.TwitterResponse;
import twitter4j.http.HTMLEntity;
import twitter4j.http.HttpResponse;

/**
 * A tiny parse utility class.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class InternalParseUtil {
	private static ThreadLocal<Map<String, SimpleDateFormat>> formatMap = new ThreadLocal<Map<String, SimpleDateFormat>>() {
		@Override
		protected Map<String, SimpleDateFormat> initialValue() {
			return new HashMap<String, SimpleDateFormat>();
		}
	};

	private InternalParseUtil() {
		// should never be instantiated
		throw new AssertionError();
	}

	public static boolean getBoolean(final String name, final JSONObject json) {
		final String str = getRawString(name, json);
		if (null == str || "null".equals(str)) return false;
		return Boolean.valueOf(str);
	}

	public static Date getDate(final String name, final JSONObject json) throws TwitterException {
		return getDate(name, json, "EEE MMM d HH:mm:ss z yyyy");
	}

	public static Date getDate(final String name, final JSONObject json, final String format) throws TwitterException {
		final String dateStr = getUnescapedString(name, json);
		if ("null".equals(dateStr) || null == dateStr)
			return null;
		else
			return getDate(dateStr, format);
	}

	public static Date getDate(final String date, final String format) throws TwitterException {
		SimpleDateFormat sdf = formatMap.get().get(format);
		if (null == sdf) {
			sdf = new SimpleDateFormat(format, Locale.ENGLISH);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			formatMap.get().put(format, sdf);
		}
		try {
			return sdf.parse(date);
		} catch (final ParseException pe) {
			throw new TwitterException("Unexpected date format(" + date + ") returned from twitter.com", pe);
		}
	}

	public static double getDouble(final String name, final JSONObject json) {
		final String str2 = getRawString(name, json);
		if (null == str2 || "".equals(str2) || "null".equals(str2))
			return -1;
		else
			return Double.valueOf(str2);
	}

	public static String getHTMLUnescapedString(final String name, final JSONObject json) {
		return HTMLEntity.unescape(getRawString(name, json));
	}

	public static int getInt(final String str) {
		if (null == str || "".equals(str) || "null".equals(str))
			return -1;
		else {
			try {
				return Integer.valueOf(str);
			} catch (final NumberFormatException nfe) {
				// workaround for the API side issue
				// http://twitter4j.org/jira/browse/TFJ-484
				return -1;
			}
		}
	}

	public static int getInt(final String name, final JSONObject json) {
		return getInt(getRawString(name, json));
	}

	public static long getLong(String str) {
		if (null == str || "".equals(str) || "null".equals(str))
			return -1;
		else {
			try {
				final Number number = NumberFormat.getInstance().parse(str);
				return number.longValue();
			} catch (final ParseException e) {
			}
			// some count over 100 will be expressed as "100+"
			if (str.endsWith("+")) {
				str = str.substring(0, str.length() - 1);
				try {
					return Integer.valueOf(str) + 1;
				} catch (final NumberFormatException nfe) {
					// workaround for the API side issue
					// http://twitter4j.org/jira/browse/TFJ-484
					return -1;
				}
			}
			try {
				return Long.valueOf(str);
			} catch (final NumberFormatException nfe) {
				// workaround for the API side issue
				// http://twitter4j.org/jira/browse/TFJ-484
				return -1;
			}
		}
	}

	public static long getLong(final String name, final JSONObject json) {
		return getLong(getRawString(name, json));
	}

	public static String getRawString(final String name, final JSONObject json) {
		try {
			if (json.isNull(name)) return null;
			return json.getString(name);
		} catch (final JSONException e) {
			return null;
		}
	}

	public static String getUnescapedString(final String name, final JSONObject json) {
		final String str = getRawString(name, json);
		if (str == null) return null;
		return str.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
	}

	public static String getURLDecodedString(final String name, final JSONObject json) {
		String returnValue = getRawString(name, json);
		if (returnValue != null) {
			try {
				returnValue = URLDecoder.decode(returnValue, "UTF-8");
			} catch (final UnsupportedEncodingException ignore) {
			}
		}
		return returnValue;
	}

	public static Date parseTrendsDate(final String asOfStr) throws TwitterException {
		Date parsed;
		switch (asOfStr.length()) {
			case 10:
				parsed = new Date(Long.parseLong(asOfStr) * 1000);
				break;
			case 20:
				parsed = getDate(asOfStr, "yyyy-MM-dd'T'HH:mm:ss'Z'");
				break;
			default:
				parsed = getDate(asOfStr, "EEE, d MMM yyyy HH:mm:ss z");
		}
		return parsed;
	}

	public static int toAccessLevel(final HttpResponse res) {
		if (null == res) return -1;
		final String xAccessLevel = res.getResponseHeader("X-Access-Level");
		int accessLevel;
		if (null == xAccessLevel) {
			accessLevel = TwitterResponse.NONE;
		} else {
			// https://dev.twitter.com/pages/application-permission-model-faq#how-do-we-know-what-the-access-level-of-a-user-token-is
			switch (xAccessLevel.length()) {
			// “read” (Read-only)
				case 4:
					accessLevel = TwitterResponse.READ;
					break;
				case 10:
					// “read-write” (Read & Write)
					accessLevel = TwitterResponse.READ_WRITE;
					break;
				case 25:
					// “read-write-directmessages” (Read, Write, & Direct
					// Message)
					accessLevel = TwitterResponse.READ_WRITE_DIRECTMESSAGES;
					break;
				case 26:
					// “read-write-privatemessages” (Read, Write, & Direct
					// Message)
					accessLevel = TwitterResponse.READ_WRITE_DIRECTMESSAGES;
					break;
				default:
					accessLevel = TwitterResponse.NONE;
					// unknown access level;
			}
		}
		return accessLevel;
	}
}
