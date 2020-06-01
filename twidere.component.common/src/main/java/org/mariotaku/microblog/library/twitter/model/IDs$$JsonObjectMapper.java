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

package org.mariotaku.microblog.library.twitter.model;

import androidx.annotation.Keep;

import com.bluelinelabs.logansquare.JsonMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for IDs object
 * Created by mariotaku on 15/10/21.
 */
@SuppressWarnings("unused")
@Keep
public class IDs$$JsonObjectMapper extends JsonMapper<IDs> {

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
