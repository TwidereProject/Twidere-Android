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

package org.mariotaku.microblog.library.model.microblog;

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
