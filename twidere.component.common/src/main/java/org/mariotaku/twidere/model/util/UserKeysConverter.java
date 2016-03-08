package org.mariotaku.twidere.model.util;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.twidere.model.UserKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 16/3/8.
 */
public class UserKeysConverter implements TypeConverter<UserKey[]> {
    @Override
    public UserKey[] parse(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            jsonParser.skipChildren();
            return null;
        }
        List<UserKey> list = new ArrayList<>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            list.add(UserKey.valueOf(jsonParser.getValueAsString()));
        }
        return list.toArray(new UserKey[list.size()]);
    }

    @Override
    public void serialize(UserKey[] object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        if (writeFieldNameForObject) {
            jsonGenerator.writeFieldName(fieldName);
        }
        if (object == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeStartArray();
            for (UserKey userKey : object) {
                jsonGenerator.writeString(userKey.toString());
            }
            jsonGenerator.writeEndArray();
        }
    }
}
