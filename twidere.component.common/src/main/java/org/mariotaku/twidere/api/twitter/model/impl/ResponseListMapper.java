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

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.twidere.api.twitter.model.ResponseList;

import java.io.IOException;

/**
 * Created by mariotaku on 15/12/13.
 */
public class ResponseListMapper<T> extends JsonMapper<ResponseList<T>> {

    private final Class<T> elementCls;

    protected ResponseListMapper(Class<T> elementCls) {
        this.elementCls = elementCls;
    }

    @Override
    public ResponseList<T> parse(JsonParser jsonParser) throws IOException {
        ResponseList<T> list = new ResponseList<>();
        final JsonMapper<T> mapper = LoganSquare.mapperFor(elementCls);
        if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {

                if (jsonParser.getCurrentToken() == null) {
                    jsonParser.nextToken();
                }
                if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                    jsonParser.skipChildren();
                    continue;
                }
                list.add(mapper.parse(jsonParser));
            }
        }
        return list;
    }

    @Override
    public void parseField(ResponseList<T> instance, String fieldName, JsonParser jsonParser) throws IOException {

    }

    @Override
    public void serialize(ResponseList<T> object, JsonGenerator generator, boolean writeStartAndEnd) throws IOException {

    }
}
