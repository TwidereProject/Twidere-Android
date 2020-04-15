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

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.commons.logansquare.JsonStringConverter;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.account.AccountExtras;
import org.mariotaku.twidere.model.account.cred.Credentials;
import org.mariotaku.twidere.model.util.ContentObjectColorConverter;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.util.model.AccountDetailsUtils;

import java.io.IOException;

/**
 * Object holding account info and credentials
 * Created by mariotaku on 2016/12/3.
 */
@ParcelablePlease
@JsonObject
public class AccountDetails implements Parcelable, Comparable<AccountDetails> {

    @JsonField(name = "account", typeConverter = AccountConverter.class)
    public Account account;

    @JsonField(name = "key", typeConverter = UserKeyConverter.class)
    public UserKey key;

    @AccountType
    @JsonField(name = "type")
    public String type;

    @Credentials.Type
    @JsonField(name = "credentials_type")
    public String credentials_type;

    @JsonField(name = "user")
    public ParcelableUser user;

    @ColorInt
    @JsonField(name = "color", typeConverter = ContentObjectColorConverter.class)
    public int color;

    @JsonField(name = "position")
    public int position;

    @JsonField(name = "activated")
    public boolean activated;

    @JsonField(name = "dummy")
    public boolean dummy;

    @JsonField(name = "test")
    public boolean test;

    @JsonField(name = "credentials", typeConverter = JsonStringConverter.class)
    @ParcelableNoThanks
    String credentials_json;

    public Credentials credentials;

    @JsonField(name = "extras", typeConverter = JsonStringConverter.class)
    @ParcelableNoThanks
    String extras_json;
    public AccountExtras extras;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AccountDetailsParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "AccountDetails{" +
                "account=" + account +
                ", dummy=" + dummy +
                ", key=" + key +
                ", credentials=" + credentials +
                ", user=" + user +
                ", color=" + color +
                ", position=" + position +
                ", activated=" + activated +
                ", type='" + type + '\'' +
                ", credentials_type='" + credentials_type + '\'' +
                ", extras=" + extras +
                '}';
    }

    @Override
    public int compareTo(@NonNull AccountDetails that) {
        return this.position - that.position;
    }

    @NonNull
    public static AccountDetails dummy() {
        AccountDetails dummy = new AccountDetails();
        dummy.dummy = true;
        return dummy;
    }

    @OnPreJsonSerialize
    void onPreJsonSerialize() throws IOException {
        if (credentials != null) {
            credentials_json = LoganSquare.serialize(credentials);
        }
        if (extras != null) {
            extras_json = LoganSquare.serialize(extras);
        }
    }

    @OnJsonParseComplete
    void onJsonParseComplete() throws IOException {
        if (credentials_json != null && credentials_type != null) {
            credentials = AccountDetailsUtils.parseCredentials(credentials_json, credentials_type);
        }
        if (extras_json != null && type != null) {
            extras = AccountDetailsUtils.parseAccountExtras(extras_json, type);
        }
    }

    public static final Creator<AccountDetails> CREATOR = new Creator<AccountDetails>() {
        public AccountDetails createFromParcel(Parcel source) {
            AccountDetails target = new AccountDetails();
            AccountDetailsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public AccountDetails[] newArray(int size) {
            return new AccountDetails[size];
        }
    };

    static class AccountConverter implements TypeConverter<Account> {
        @Override
        public Account parse(JsonParser jsonParser) throws IOException {
            if (jsonParser.getCurrentToken() == null) {
                jsonParser.nextToken();
            }
            if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                jsonParser.skipChildren();
                return null;
            }
            String name = null, type = null;
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                switch (fieldName) {
                    case "name": {
                        name = jsonParser.getValueAsString(null);
                        break;
                    }
                    case "type": {
                        type = jsonParser.getValueAsString(null);
                        break;
                    }
                }
                jsonParser.skipChildren();
            }
            if (name != null && type != null) {
                return new Account(name, type);
            }
            return null;
        }

        @Override
        public void serialize(Account object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
            if (writeFieldNameForObject) {
                jsonGenerator.writeFieldName(fieldName);
            }
            if (object == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("name", object.name);
                jsonGenerator.writeStringField("type", object.type);
                jsonGenerator.writeEndObject();
            }
        }
    }
}
