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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.api.twitter.model.CardEntity;
import org.mariotaku.twidere.api.twitter.model.RateLimitStatus;
import org.mariotaku.twidere.api.twitter.model.User;

import java.util.Map;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class CardEntityImpl implements CardEntity {


    @JsonField(name = "name")
    String name;

    @JsonField(name = "url")
    String url;

    @JsonField(name = "binding_values")
    Map<String, BindingValue> bindingValues;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public User[] getUsers() {
        return new User[0];
    }

    @Override
    public BindingValue getBindingValue(String key) {
        return bindingValues.get(key);
    }

    @Override
    public Map<String, BindingValue> getBindingValues() {
        return bindingValues;
    }

    @JsonObject
    public static class ImageValueImpl implements ImageValue {
        @JsonField(name = "width")
        int width;
        @JsonField(name = "height")
        int height;
        @JsonField(name = "url")
        String url;

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

    }

    public static class BooleanValueImpl implements BooleanValue {

        public BooleanValueImpl(boolean value) {
            this.value = value;
        }

        private boolean value;

        @Override
        public boolean getValue() {
            return value;
        }
    }

    public static class StringValueImpl implements StringValue {
        private final String value;

        public StringValueImpl(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    @JsonObject
    public static class UserValueImpl implements UserValue {

        @JsonField(name = "id")
        long userId;

        @Override
        public long getUserId() {
            return userId;
        }
    }

    @JsonObject
    public static class BindingValueWrapper implements TwitterModelWrapper<BindingValue> {

        @JsonField(name = "type")
        String type;
        @JsonField(name = "boolean_value")
        boolean booleanValue;
        @JsonField(name = "string_value")
        String stringValue;
        @JsonField(name = "image_value")
        ImageValueImpl imageValue;
        @JsonField(name = "user_value")
        UserValueImpl userValue;


        @Override
        public BindingValue getWrapped(Object extra) {
            if (type == null) return null;
            switch (type) {
                case BindingValue.TYPE_BOOLEAN: {
                    return new BooleanValueImpl(booleanValue);
                }
                case BindingValue.TYPE_STRING: {
                    return new StringValueImpl(stringValue);
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
        public void processResponseHeader(RestHttpResponse resp) {

        }

        @Override
        public int getAccessLevel() {
            return 0;
        }

        @Override
        public RateLimitStatus getRateLimitStatus() {
            return null;
        }
    }
}
