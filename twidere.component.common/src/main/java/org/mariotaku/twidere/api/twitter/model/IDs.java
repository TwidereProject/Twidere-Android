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

package org.mariotaku.twidere.api.twitter.model;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * Created by mariotaku on 15/5/10.
 */
public class IDs extends TwitterResponseObject implements TwitterResponse, CursorSupport {

    long previousCursor;
    long nextCursor;
    String[] ids;

    @Override
    public long getNextCursor() {
        return nextCursor;
    }

    @Override
    public long getPreviousCursor() {
        return previousCursor;
    }

    @Override
    public boolean hasNext() {
        return nextCursor != 0;
    }

    @Override
    public boolean hasPrevious() {
        return previousCursor != 0;
    }

    public String[] getIDs() {
        return ids;
    }

    public static class Converter implements TypeConverter<IDs> {

        private static final JsonMapper<IDs> IDS_JSON_MAPPER = LoganSquare.mapperFor(IDs.class);

        @Override
        public IDs parse(JsonParser jsonParser) throws IOException {
            return IDS_JSON_MAPPER.parse(jsonParser);
        }

        @Override
        public void serialize(IDs object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
            if (writeFieldNameForObject) {
                jsonGenerator.writeFieldName(fieldName);
            }
            IDS_JSON_MAPPER.serialize(object, jsonGenerator, true);
        }
    }
}
