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

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.model.util.ParcelMapBagger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/5/7.
 */
@ParcelablePlease
@JsonObject
public class CardEntity implements Parcelable {

    @JsonField(name = "name")
    String name;

    @JsonField(name = "url")
    String url;

    @JsonField(name = "binding_values")
    @ParcelableNoThanks
    Map<String, RawBindingValue> rawBindingValues;
    @Bagger(BindingValueMapBagger.class)
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

    public interface BindingValue extends Parcelable {

        String TYPE_STRING = "STRING";
        String TYPE_IMAGE = "IMAGE";
        String TYPE_USER = "USER";
        String TYPE_BOOLEAN = "BOOLEAN";

    }

    @ParcelablePlease
    @JsonObject
    public static class ImageValue implements BindingValue, Parcelable {
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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            CardEntity$ImageValueParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<ImageValue> CREATOR = new Creator<ImageValue>() {
            @Override
            public ImageValue createFromParcel(Parcel source) {
                ImageValue target = new ImageValue();
                CardEntity$ImageValueParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public ImageValue[] newArray(int size) {
                return new ImageValue[size];
            }
        };
    }

    @ParcelablePlease
    public static class BooleanValue implements BindingValue, Parcelable {

        public BooleanValue(boolean value) {
            this.value = value;
        }

        boolean value;

        BooleanValue() {

        }

        public boolean getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "BooleanValue{" +
                    "value=" + value +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            CardEntity$BooleanValueParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<BooleanValue> CREATOR = new Creator<BooleanValue>() {
            @Override
            public BooleanValue createFromParcel(Parcel source) {
                BooleanValue target = new BooleanValue();
                CardEntity$BooleanValueParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public BooleanValue[] newArray(int size) {
                return new BooleanValue[size];
            }
        };
    }

    @ParcelablePlease
    public static class StringValue implements BindingValue, Parcelable {
        String value;

        public StringValue(String value) {
            this.value = value;
        }

        StringValue() {

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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            CardEntity$StringValueParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<StringValue> CREATOR = new Creator<StringValue>() {
            @Override
            public StringValue createFromParcel(Parcel source) {
                StringValue target = new StringValue();
                CardEntity$StringValueParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public StringValue[] newArray(int size) {
                return new StringValue[size];
            }
        };
    }

    @ParcelablePlease
    @JsonObject
    public static class UserValue implements BindingValue, Parcelable {

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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            CardEntity$UserValueParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<UserValue> CREATOR = new Creator<UserValue>() {
            @Override
            public UserValue createFromParcel(Parcel source) {
                UserValue target = new UserValue();
                CardEntity$UserValueParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public UserValue[] newArray(int size) {
                return new UserValue[size];
            }
        };
    }

    @ParcelablePlease
    @JsonObject
    public static class RawBindingValue implements Parcelable {

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

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            CardEntity$RawBindingValueParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<RawBindingValue> CREATOR = new Creator<RawBindingValue>() {
            @Override
            public RawBindingValue createFromParcel(Parcel source) {
                RawBindingValue target = new RawBindingValue();
                CardEntity$RawBindingValueParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public RawBindingValue[] newArray(int size) {
                return new RawBindingValue[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CardEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<CardEntity> CREATOR = new Creator<CardEntity>() {
        @Override
        public CardEntity createFromParcel(Parcel source) {
            CardEntity target = new CardEntity();
            CardEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public CardEntity[] newArray(int size) {
            return new CardEntity[size];
        }
    };

    public static class BindingValueMapBagger extends ParcelMapBagger<BindingValue> {
        public BindingValueMapBagger() {
            super(BindingValue.class);
        }
    }
}
