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

package org.mariotaku.twidere.model.account.cred;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/2.
 */

@ParcelablePlease
@JsonObject
public class Credentials implements Parcelable {
    @JsonField(name = "api_url_format")
    public String api_url_format;
    @JsonField(name = "no_version_suffix")
    public boolean no_version_suffix;

    @StringDef({Type.OAUTH, Type.XAUTH, Type.BASIC, Type.EMPTY, Type.OAUTH2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

        String OAUTH = "oauth";
        String XAUTH = "xauth";
        String BASIC = "basic";
        String EMPTY = "empty";
        String OAUTH2 = "oauth2";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "api_url_format='" + api_url_format + '\'' +
                ", no_version_suffix=" + no_version_suffix +
                '}';
    }

    public static final Creator<Credentials> CREATOR = new Creator<Credentials>() {
        public Credentials createFromParcel(Parcel source) {
            Credentials target = new Credentials();
            CredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };
}
