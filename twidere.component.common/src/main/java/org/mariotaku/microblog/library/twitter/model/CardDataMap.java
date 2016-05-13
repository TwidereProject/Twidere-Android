/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.ValueMap;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.StringBody;
import org.mariotaku.microblog.library.MicroBlogException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mariotaku on 15/12/30.
 */
public class CardDataMap implements ValueMap {
    protected final Map<String, String> map = new LinkedHashMap<>();

    public void putString(String key, String value) {
        map.put("twitter:string:" + key, value);
    }

    public void putLong(String key, long value) {
        map.put("twitter:long:" + key, String.valueOf(value));
    }

    @Override
    public boolean has(String key) {
        return map.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public String[] keys() {
        final Set<String> keySet = map.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public String toString() {
        return "CardDataMap{" +
                "map=" + map +
                '}';
    }

    public static class BodyConverter implements RestConverter<CardDataMap, Body, MicroBlogException> {
        @Override
        public Body convert(CardDataMap obj) throws ConvertException, IOException, MicroBlogException {
            final StringWriter sw = new StringWriter();
            final JsonGenerator generator = LoganSquare.JSON_FACTORY.createGenerator(sw);
            generator.writeStartObject();
            for (Map.Entry<String, String> entry : obj.map.entrySet()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
            generator.writeEndObject();
            generator.flush();
            return new StringBody(sw.toString(), Charset.defaultCharset());
        }
    }
}
