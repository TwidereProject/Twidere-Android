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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * Created by mariotaku on 15/3/31.
 */
public class Indices {

    private int start, end;

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public Indices(JsonParser reader) throws IOException {
        if (!reader.isExpectedStartArrayToken()) throw new IOException("Malformed indices");
        start = reader.nextIntValue(-1);
        end = reader.nextIntValue(-1);
        if (reader.nextToken() != JsonToken.END_ARRAY) throw new IOException("Malformed indices");
    }


    @Override
    public String toString() {
        return "Index{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }

    public static final TypeConverter<Indices> CONVERTER = new TypeConverter<Indices>() {
        @Override
        public Indices parse(JsonParser jsonParser) throws IOException {
            return new Indices(jsonParser);
        }

        @Override
        public void serialize(Indices object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
            throw new UnsupportedOperationException();
        }
    };
}
