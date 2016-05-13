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

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 15/10/21.
 */
public class IDs$$JsonObjectMapper extends JsonMapper<IDs> {

    @SuppressWarnings("TryWithIdenticalCatches")
    @Override
    public IDs parse(JsonParser jsonParser) throws IOException {
        IDs instance = new IDs();
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
            parseIDsArray(instance, jsonParser);
        } else if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                parseField(instance, fieldName, jsonParser);
                jsonParser.skipChildren();
            }
        } else {
            jsonParser.skipChildren();
            return null;
        }
        return instance;
    }

    @Override
    public void serialize(IDs activity, JsonGenerator jsonGenerator, boolean writeStartAndEnd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void parseField(IDs instance, String fieldName, JsonParser jsonParser) throws IOException {
        if ("ids".equals(fieldName)) {
            parseIDsArray(instance, jsonParser);
        } else if ("previous_cursor".equals(fieldName)) {
            instance.previousCursor = jsonParser.getValueAsLong();
        } else if ("next_cursor".equals(fieldName)) {
            instance.nextCursor = jsonParser.getValueAsLong();
        }
    }

    private void parseIDsArray(IDs instance, JsonParser jsonParser) throws IOException {
        List<String> collection1 = new ArrayList<>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            collection1.add(jsonParser.getValueAsString());
        }
        String[] array = new String[collection1.size()];
        int i = 0;
        for (String value : collection1) {
            array[i++] = value;
        }
        instance.ids = array;
    }

}
