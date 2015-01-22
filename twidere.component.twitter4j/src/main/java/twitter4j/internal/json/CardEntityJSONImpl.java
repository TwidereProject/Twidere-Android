/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package twitter4j.internal.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import twitter4j.CardEntity;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.internal.util.InternalParseUtil;

/**
 * Created by mariotaku on 14/12/31.
 */
public class CardEntityJSONImpl implements CardEntity {

    private final String name;
    private final Map<String, BindingValue> bindingValues;
    private final User[] users;

    public CardEntityJSONImpl(JSONObject json) throws JSONException, TwitterException {
        this.name = json.getString("name");
        this.bindingValues = BindingValueImpl.valuesOf(json.getJSONObject("binding_values"));
        if (!json.isNull("users")) {
            final JSONObject usersJSON = json.getJSONObject("users");
            final Iterator<String> keys = usersJSON.keys();
            this.users = new UserJSONImpl[usersJSON.length()];
            int idx = 0;
            while (keys.hasNext()) {
                users[idx++] = new UserJSONImpl(usersJSON.getJSONObject(keys.next()));
            }
        } else {
            users = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public User[] gerUsers() {
        return users;
    }

    @Override
    public BindingValue getBindingValue(String name) {
        return bindingValues.get(name);
    }

    @Override
    public BindingValue[] getBindingValues() {
        return bindingValues.values().toArray(new BindingValue[bindingValues.size()]);
    }


    public static abstract class BindingValueImpl implements BindingValue {
        protected final String name, type;

        BindingValueImpl(String name, JSONObject json) throws JSONException {
            this.name = name;
            this.type = json.getString("type");
        }

        @Override
        public final String getName() {
            return name;
        }

        @Override
        public final String getType() {
            return type;
        }

        private static BindingValue valueOf(String name, JSONObject json) throws JSONException {
            final String type = json.optString("type");
            if (TYPE_STRING.equals(type)) {
                return new StringValueImpl(name, json);
            } else if (TYPE_IMAGE.equals(type)) {
                return new ImageValueImpl(name, json);
            } else if (TYPE_USER.equals(type)) {
                return new UserValueImpl(name, json);
            } else if (TYPE_BOOLEAN.equals(type)) {
                return new BooleanValueImpl(name, json);
            }
            throw new UnsupportedOperationException(String.format("Unsupported type %s", type));
        }

        private static HashMap<String, BindingValue> valuesOf(JSONObject json) throws JSONException {
            final Iterator<String> keys = json.keys();
            final HashMap<String, BindingValue> values = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                values.put(key, valueOf(key, json.getJSONObject(key)));
            }
            return values;
        }
    }

    private static final class ImageValueImpl extends BindingValueImpl implements ImageValue {

        private final int width, height;
        private final String url;

        ImageValueImpl(String name, JSONObject json) throws JSONException {
            super(name, json);
            final JSONObject imageValue = json.getJSONObject("image_value");
            this.width = imageValue.getInt("width");
            this.height = imageValue.getInt("height");
            this.url = imageValue.getString("url");
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "ImageValueImpl{" +
                    "name='" + name + '\'' +
                    "width=" + width +
                    ", height=" + height +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    private static final class BooleanValueImpl extends BindingValueImpl implements BooleanValue {

        @Override
        public String toString() {
            return "BooleanValueImpl{" +
                    "value=" + value +
                    '}';
        }

        private final boolean value;

        BooleanValueImpl(String name, JSONObject json) throws JSONException {
            super(name, json);
            this.value = json.getBoolean("boolean_value");
        }

        @Override
        public boolean getValue() {
            return value;
        }


    }

    private static final class StringValueImpl extends BindingValueImpl implements StringValue {

        private final String value;

        StringValueImpl(String name, JSONObject json) throws JSONException {
            super(name, json);
            this.value = json.getString("string_value");
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "StringValueImpl{" +
                    "name='" + name + '\'' +
                    "value='" + value + '\'' +
                    '}';
        }


    }

    private static final class UserValueImpl extends BindingValueImpl implements UserValue {

        private final long userId;

        UserValueImpl(String name, JSONObject json) throws JSONException {
            super(name, json);
            final JSONObject userValue = json.getJSONObject("user_value");
            this.userId = InternalParseUtil.getLong("id_str", userValue);
        }

        @Override
        public long getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return "UserValueImpl{" +
                    "name='" + name + '\'' +
                    "userId='" + userId + '\'' +
                    '}';
        }


    }

    @Override
    public String toString() {
        return "CardEntityJSONImpl{" +
                "name='" + name + '\'' +
                ", bindingValues=" + bindingValues +
                ", users=" + Arrays.toString(users) +
                '}';
    }
}
