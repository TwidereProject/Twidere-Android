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

package org.mariotaku.twidere.model;


import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.ParcelBagger;
import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.microblog.library.twitter.model.CardEntity;
import org.mariotaku.twidere.model.util.UserKeyConverter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/12/31.
 */
@JsonObject
@ParcelablePlease
public final class ParcelableCardEntity implements Parcelable {

    public static final Creator<ParcelableCardEntity> CREATOR = new Creator<ParcelableCardEntity>() {
        @Override
        public ParcelableCardEntity createFromParcel(Parcel source) {
            ParcelableCardEntity target = new ParcelableCardEntity();
            ParcelableCardEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableCardEntity[] newArray(int size) {
            return new ParcelableCardEntity[size];
        }
    };
    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "name")
    public String name;
    @ParcelableThisPlease
    @JsonField(name = "url")
    public String url;
    @ParcelableThisPlease
    @JsonField(name = "users")
    public ParcelableUser[] users;
    @ParcelableThisPlease
    @Bagger(ValueMapConverter.class)
    @JsonField(name = "values")
    public Map<String, ParcelableBindingValue> values;

    public ParcelableCardEntity() {

    }

    @Nullable
    public ParcelableBindingValue getValue(@NonNull String key) {
        if (values == null) return null;
        return values.get(key);
    }

    @Override
    public String toString() {
        return "ParcelableCardEntity{" +
                "account_key=" + account_key +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", users=" + Arrays.toString(users) +
                ", values=" + values +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableCardEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static class ValueMapConverter implements ParcelBagger<Map<String, ParcelableBindingValue>> {

        @Override
        public void write(Map<String, ParcelableBindingValue> map, Parcel out, int flags) {
            if (map != null) {
                final int size = map.size();
                out.writeInt(size);
                for (Map.Entry<String, ParcelableBindingValue> entry : map.entrySet()) {
                    out.writeString(entry.getKey());
                    out.writeParcelable(entry.getValue(), flags);
                }
            } else {
                out.writeInt(-1);
            }
        }

        @Override
        public Map<String, ParcelableBindingValue> read(Parcel in) {
            final int size = in.readInt();
            if (size == -1) return null;
            final Map<String, ParcelableBindingValue> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                final String key = in.readString();
                final ParcelableBindingValue value = in.readParcelable(ParcelableBindingValue.class.getClassLoader());
                map.put(key, value);
            }
            return map;
        }

    }

    /**
     * Created by mariotaku on 15/12/31.
     */
    @ParcelablePlease
    @JsonObject
    public static final class ParcelableBindingValue implements Parcelable {

        public static final Creator<ParcelableBindingValue> CREATOR = new Creator<ParcelableBindingValue>() {
            @Override
            public ParcelableBindingValue createFromParcel(Parcel source) {
                ParcelableBindingValue target = new ParcelableBindingValue();
                ParcelableCardEntity$ParcelableBindingValueParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public ParcelableBindingValue[] newArray(int size) {
                return new ParcelableBindingValue[size];
            }
        };
        @ParcelableThisPlease
        @JsonField(name = "type")
        public String type;
        @ParcelableThisPlease
        @JsonField(name = "value")
        public String value;

        public ParcelableBindingValue() {
        }

        public ParcelableBindingValue(CardEntity.BindingValue value) {
            if (value instanceof CardEntity.ImageValue) {
                this.type = CardEntity.BindingValue.TYPE_IMAGE;
                this.value = ((CardEntity.ImageValue) value).getUrl();
            } else if (value instanceof CardEntity.StringValue) {
                this.type = CardEntity.BindingValue.TYPE_STRING;
                this.value = ((CardEntity.StringValue) value).getValue();
            } else if (value instanceof CardEntity.BooleanValue) {
                this.type = CardEntity.BindingValue.TYPE_BOOLEAN;
                this.value = String.valueOf(((CardEntity.BooleanValue) value).getValue());
            } else if (value instanceof CardEntity.UserValue) {
                this.type = CardEntity.BindingValue.TYPE_USER;
                this.value = String.valueOf(((CardEntity.UserValue) value).getUserId());
            }
        }

        @Override
        public String toString() {
            return value + " (" + type + ")";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableCardEntity$ParcelableBindingValueParcelablePlease.writeToParcel(this, dest, flags);
        }
    }
}
