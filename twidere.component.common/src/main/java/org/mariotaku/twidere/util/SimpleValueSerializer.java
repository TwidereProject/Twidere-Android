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

package org.mariotaku.twidere.util;

import android.util.Log;

import org.mariotaku.twidere.TwidereConstants;

import java.io.IOException;

/**
 * Created by mariotaku on 15/2/13.
 */
public class SimpleValueSerializer {

    private static final char ESCAPE = '\\';
    private static final char DIVIDER_ARRAY_ELEMENT = ';';
    private static final char DIVIDER_OBJECT_FIELD = ',';
    private static final char DIVIDER_KEY_VALUE = '=';

    public static Reader newReader(String str) {
        return new Reader(str);
    }


    private static <T extends SimpleValueSerializable> T[] append(T[] old, int position, T item, Creator<T> creator) {
        T[] appended;
        if (position < old.length) {
            appended = old;
        } else {
            appended = creator.newArray(old.length * 2);
            System.arraycopy(old, 0, appended, 0, old.length);
        }
        appended[position] = item;
        return appended;
    }


    public static String escape(String str) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, j = str.length(); i < j; i++) {
            final char ch = str.charAt(i);
            if (DIVIDER_ARRAY_ELEMENT == ch || ESCAPE == ch) {
                sb.append(ESCAPE);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    @SafeVarargs
    public static <T extends SimpleValueSerializable> String toSerializedString(T... array) {
        final Writer writer = newWriter();
        for (T item : array) {
            writer.beginArrayElement();
            item.write(writer);
        }
        return writer.toString();
    }


    public static <T extends SimpleValueSerializable> T[] fromSerializedString(final String json, Creator<T> creator) {
        long start = System.nanoTime();
        T[] temp = creator.newArray(4);
        try {
            if (json == null) return null;
            final SimpleValueSerializer.Reader reader = SimpleValueSerializer.newReader(json);
            int count = 0;
            while (reader.hasArrayElement()) {
                final T item = creator.create(reader);
                reader.endArrayElement();
                temp = append(temp, count++, item, creator);
            }
            final T[] result = creator.newArray(count);
            System.arraycopy(temp, 0, result, 0, count);
            return result;
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface Creator<T extends SimpleValueSerializable> {
        T create(Reader reader) throws SerializationException;

        T[] newArray(int size);
    }

    public static class SerializationException extends IOException {

    }

    public static Writer newWriter() {
        return new Writer();
    }

    public static interface SimpleValueSerializable {

        void write(Writer writer);
    }

    public static class Writer {

        private int keyValuesCount;
        private final StringBuilder stringBuilder;

        public Writer() {
            stringBuilder = new StringBuilder();
            keyValuesCount = 0;
        }

        public void beginArrayElement() {
            keyValuesCount = 0;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(DIVIDER_ARRAY_ELEMENT);
            }
        }

        public void write(String key, String value) {
            if (keyValuesCount++ > 0) {
                stringBuilder.append(DIVIDER_OBJECT_FIELD);
            }
            stringBuilder.append(escape(key));
            stringBuilder.append(DIVIDER_KEY_VALUE);
            stringBuilder.append(escape(value));
        }

        public String toString() {
            return stringBuilder.toString();
        }

        public void write(String key, long value) {
            write(key, String.valueOf(value));
        }

        public void write(String key, boolean value) {
            write(key, String.valueOf(value));
        }
    }

    public static class Reader {
        private final String s;
        private final int len;
        private char lastToken;
        private int cur;

        public Reader(String s) {
            this.s = s;
            this.len = s.length();
            cur = 0;
            lastToken = '\0';
        }

        public boolean hasKeyValue() {
            return (cur == 0 || (lastToken == '\0' || lastToken == DIVIDER_OBJECT_FIELD)) && cur < len;
        }


        public boolean hasArrayElement() {
            return (cur == 0 || (lastToken == '\0' || lastToken == DIVIDER_ARRAY_ELEMENT)) && cur < len;
        }

        public String nextKey() throws SerializationException {
            if (lastToken != '\0' && lastToken != DIVIDER_OBJECT_FIELD) {
                throw new SerializationException();
            }
            return peek();
        }

        private String peek() {
            StringBuilder sb = new StringBuilder();
            boolean isEscaped = false;
            while (cur < len) {
                char ch = s.charAt(cur++);
                if (isEscaped) {
                    sb.append(ch);
                    isEscaped = false;
                } else if (ch == ESCAPE) {
                    isEscaped = true;
                } else if (ch == DIVIDER_KEY_VALUE || ch == DIVIDER_OBJECT_FIELD || ch == DIVIDER_ARRAY_ELEMENT) {
                    lastToken = ch;
                    break;
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        public void skipValue() throws SerializationException {
            nextString();
        }

        public String nextString() throws SerializationException {
            if (lastToken != DIVIDER_KEY_VALUE) {
                throw new SerializationException();
            }
            return peek();
        }

        public int nextInt() throws SerializationException {
            return Integer.parseInt(nextString());
        }

        public long nextLong() throws SerializationException {
            return Long.parseLong(nextString());
        }

        public void endArrayElement() {
            lastToken = '\0';
        }
    }
}
