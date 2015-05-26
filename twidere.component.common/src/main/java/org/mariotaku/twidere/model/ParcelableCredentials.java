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

package org.mariotaku.twidere.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 15/5/26.
 */
@JsonObject
@ParcelablePlease(allFields = false)
public class ParcelableCredentials extends ParcelableAccount {

    public static final Creator<org.mariotaku.twidere.model.ParcelableCredentials> CREATOR = new Creator<org.mariotaku.twidere.model.ParcelableCredentials>() {

        @Override
        public ParcelableCredentials createFromParcel(final Parcel in) {
            return new ParcelableCredentials(in);
        }

        @Override
        public ParcelableCredentials[] newArray(final int size) {
            return new ParcelableCredentials[size];
        }
    };

    public static final int AUTH_TYPE_OAUTH = 0;
    public static final int AUTH_TYPE_XAUTH = 1;
    public static final int AUTH_TYPE_BASIC = 2;
    public static final int AUTH_TYPE_TWIP_O_MODE = 3;


    @ParcelableThisPlease
    @JsonField(name = "auth_type")
    public int auth_type;
    @ParcelableThisPlease
    @JsonField(name = "consumer_key")
    public String consumer_key;
    @ParcelableThisPlease
    @JsonField(name = "consumer_secret")
    public String consumer_secret;
    @ParcelableThisPlease
    @JsonField(name = "basic_auth_username")
    public String basic_auth_username;
    @ParcelableThisPlease
    @JsonField(name = "basic_auth_password")
    public String basic_auth_password;
    @ParcelableThisPlease
    @JsonField(name = "oauth_token")
    public String oauth_token;
    @ParcelableThisPlease
    @JsonField(name = "oauth_token_secret")
    public String oauth_token_secret;
    @ParcelableThisPlease
    @JsonField(name = "api_url_format")
    public String api_url_format;
    @ParcelableThisPlease
    @JsonField(name = "same_oauth_signing_url")
    public boolean same_oauth_signing_url;
    @ParcelableThisPlease
    @JsonField(name = "no_version_suffix")
    public boolean no_version_suffix;

    public ParcelableCredentials() {
    }

    public ParcelableCredentials(final Cursor cursor, final Indices indices) {
        super(cursor, indices);
        auth_type = cursor.getInt(indices.auth_type);
        consumer_key = cursor.getString(indices.consumer_key);
        consumer_secret = cursor.getString(indices.consumer_secret);
        basic_auth_username = cursor.getString(indices.basic_auth_username);
        basic_auth_password = cursor.getString(indices.basic_auth_password);
        oauth_token = cursor.getString(indices.oauth_token);
        oauth_token_secret = cursor.getString(indices.oauth_token_secret);
        api_url_format = cursor.getString(indices.api_url_format);
        same_oauth_signing_url = cursor.getInt(indices.same_oauth_signing_url) == 1;
        no_version_suffix = cursor.getInt(indices.no_version_suffix) == 1;
    }

    public ParcelableCredentials(Parcel in) {
        super(in);
        ParcelableCredentialsParcelablePlease.readFromParcel(this, in);
    }


    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        ParcelableCredentialsParcelablePlease.writeToParcel(this, out, flags);
    }

    @Override
    public String toString() {
        return "AccountWithCredentials{auth_type=" + auth_type + ", consumer_key=" + consumer_key
                + ", consumer_secret=" + consumer_secret + ", basic_auth_password=" + basic_auth_password
                + ", oauth_token=" + oauth_token + ", oauth_token_secret=" + oauth_token_secret
                + ", api_url_format=" + api_url_format + ", same_oauth_signing_url=" + same_oauth_signing_url + "}";
    }
}
