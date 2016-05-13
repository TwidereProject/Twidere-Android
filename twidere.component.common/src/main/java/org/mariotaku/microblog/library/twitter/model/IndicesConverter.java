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

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/**
 * Created by mariotaku on 15/11/11.
 */
public class IndicesConverter implements TypeConverter<Indices> {
    @Override
    public Indices parse(JsonParser jsonParser) throws IOException {
        final int start, end;
        if (!jsonParser.isExpectedStartArrayToken()) throw new IOException("Malformed indices");
        start = jsonParser.nextIntValue(-1);
        end = jsonParser.nextIntValue(-1);
        if (jsonParser.nextToken() != JsonToken.END_ARRAY)
            throw new IOException("Malformed indices");
        return new Indices(start, end);
    }

    @Override
    public void serialize(Indices instance, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {

    }
}
