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

package org.mariotaku.simplerestapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/2/4.
 */
public class Utils {
    public static String[] split(final String str, final String separator) {
        String[] returnValue;
        int index = str.indexOf(separator);
        if (index == -1) {
            returnValue = new String[]{str};
        } else {
            final List<String> strList = new ArrayList<>();
            int oldIndex = 0;
            while (index != -1) {
                final String subStr = str.substring(oldIndex, index);
                strList.add(subStr);
                oldIndex = index + separator.length();
                index = str.indexOf(separator, oldIndex);
            }
            if (oldIndex != str.length()) {
                strList.add(str.substring(oldIndex));
            }
            returnValue = strList.toArray(new String[strList.size()]);
        }

        return returnValue;
    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException ignore) {
        }
    }

    /**
     * @param value string to be encoded
     * @return encoded string
     * @see <a href="http://wiki.oauth.net/TestCases">OAuth / TestCases</a>
     * @see <a
     * href="http://groups.google.com/group/oauth/browse_thread/thread/a8398d0521f4ae3d/9d79b698ab217df2?hl=en&lnk=gst&q=space+encoding#9d79b698ab217df2">Space
     * encoding - OAuth | Google Groups</a>
     * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC 3986 -
     * Uniform Resource Identifier (URI): Generic Syntax - 2.1.
     * Percent-Encoding</a>
     */
    public static String encode(final String value, String encoding) {
        String encoded;
        try {
            encoded = URLEncoder.encode(value, encoding);
        } catch (final UnsupportedEncodingException ignore) {
            return null;
        }
        final StringBuilder buf = new StringBuilder(encoded.length());
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                buf.append("%2A");
            } else if (focus == '+') {
                buf.append("%20");
            } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
                    && encoded.charAt(i + 2) == 'E') {
                buf.append('~');
                i += 2;
            } else {
                buf.append(focus);
            }
        }
        return buf.toString();
    }

    public static void parseGetParameters(final String queryString, final List<Pair<String, String>> params,
                                          final String encoding) {
        final String[] queryStrings = split(queryString, "&");
        try {
            for (final String query : queryStrings) {
                final String[] split = split(query, "=");
                final String key = URLDecoder.decode(split[0], encoding);
                if (split.length == 2) {
                    params.add(new ImmutablePair<>(key, URLDecoder.decode(split[1], encoding)));
                } else {
                    params.add(new ImmutablePair<>(key, ""));
                }
            }
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean copyStream(InputStream is, OutputStream os) throws IOException {
        return copyStream(is, os, 8192);
    }

    public static boolean copyStream(InputStream is, OutputStream os, int bufferSize)
            throws IOException {
        final byte[] bytes = new byte[bufferSize];
        int count;
        while ((count = is.read(bytes, 0, bufferSize)) != -1) {
            os.write(bytes, 0, count);
        }
        os.flush();
        return true;
    }

    @NonNull
    public static <T> T assertNotNull(@Nullable T obj) {
        if (obj == null) throw new NullPointerException();
        return obj;
    }

    private static final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @NonNull
    public static String toString(@NonNull Object value, char delimiter) {
        final Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0, j = Array.getLength(value); i < j; i++) {
                if (i != 0) {
                    sb.append(delimiter);
                }
                sb.append(Array.get(value, i));
            }
            return sb.toString();
        } else {
            return value.toString();
        }
    }
}
