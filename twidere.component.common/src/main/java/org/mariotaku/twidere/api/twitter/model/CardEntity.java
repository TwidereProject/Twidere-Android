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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class CardEntity {

    @JsonField(name = "name")
    String name;

    @JsonField(name = "url")
    String url;

    @JsonField(name = "binding_values")
    Map<String, RawBindingValue> rawBindingValues;
    Map<String, BindingValue> bindingValues;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public User[] getUsers() {
        return new User[0];
    }

    public BindingValue getBindingValue(String key) {
        return bindingValues.get(key);
    }

    public Map<String, BindingValue> getBindingValues() {
        return bindingValues;
    }

    @OnJsonParseComplete
    void onParseComplete() {
        if (rawBindingValues != null) {
            bindingValues = new HashMap<>();
            for (Map.Entry<String, RawBindingValue> entry : rawBindingValues.entrySet()) {
                bindingValues.put(entry.getKey(), entry.getValue().getBindingValue());
            }
        }
    }

    @Override
    public String toString() {
        return "CardEntity{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", bindingValues=" + bindingValues +
                '}';
    }

    public interface BindingValue {

        String TYPE_STRING = "STRING";
        String TYPE_IMAGE = "IMAGE";
        String TYPE_USER = "USER";
        String TYPE_BOOLEAN = "BOOLEAN";

    }

    @JsonObject
    public static class ImageValue implements BindingValue {
        @JsonField(name = "width")
        int width;
        @JsonField(name = "height")
        int height;
        @JsonField(name = "url")
        String url;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public String toString() {
            return "ImageValue{" +
                    "width=" + width +
                    ", height=" + height +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    public static class BooleanValue implements BindingValue {

        public BooleanValue(boolean value) {
            this.value = value;
        }

        private boolean value;

        public boolean getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "BooleanValue{" +
                    "value=" + value +
                    '}';
        }
    }

    public static class StringValue implements BindingValue {
        private final String value;

        public StringValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "StringValue{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    @JsonObject
    public static class UserValue implements BindingValue {

        @JsonField(name = "id")
        long userId;

        public long getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return "UserValue{" +
                    "userId=" + userId +
                    '}';
        }
    }

    @JsonObject
    public static class RawBindingValue {

        @JsonField(name = "type")
        String type;
        @JsonField(name = "boolean_value")
        boolean booleanValue;
        @JsonField(name = "string_value")
        String stringValue;
        @JsonField(name = "image_value")
        ImageValue imageValue;
        @JsonField(name = "user_value")
        UserValue userValue;


        public BindingValue getBindingValue() {
            if (type == null) return null;
            switch (type) {
                case BindingValue.TYPE_BOOLEAN: {
                    return new BooleanValue(booleanValue);
                }
                case BindingValue.TYPE_STRING: {
                    return new StringValue(stringValue);
                }
                case BindingValue.TYPE_IMAGE: {
                    return imageValue;
                }
                case BindingValue.TYPE_USER: {
                    return userValue;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "RawBindingValue{" +
                    "type='" + type + '\'' +
                    ", booleanValue=" + booleanValue +
                    ", stringValue='" + stringValue + '\'' +
                    ", imageValue=" + imageValue +
                    ", userValue=" + userValue +
                    '}';
        }
    }
}
