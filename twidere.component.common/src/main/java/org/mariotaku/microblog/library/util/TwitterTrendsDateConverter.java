/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.util;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TwitterTrendsDateConverter implements TypeConverter<Date> {
    private static final Object FORMATTER_LOCK = new Object();

    private static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);

    @Override
    public Date parse(JsonParser jsonParser) throws IOException {
        String dateString = jsonParser.getValueAsString(null);
        if (dateString == null) throw new IOException();
        try {
            synchronized (FORMATTER_LOCK) {
                switch (dateString.length()) {
                    case 10:
                        return new Date(Long.parseLong(dateString) * 1000);
                    case 20:
                        return DATE_FORMAT_1.parse(dateString);
                    default:
                        return parse(dateString, new DateFormat[]{DATE_FORMAT_2, DATE_FORMAT_3});
                }
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void serialize(Date object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) {
        throw new UnsupportedOperationException();
    }

    private static Date parse(String dateString, DateFormat[] formats) throws ParseException {
        for (final DateFormat format : formats) {
            try {
                return format.parse(dateString);
            } catch (ParseException e) {
                // Ignore
            }
        }
        throw new ParseException("Unrecognized date " + dateString, 0);
    }
}
