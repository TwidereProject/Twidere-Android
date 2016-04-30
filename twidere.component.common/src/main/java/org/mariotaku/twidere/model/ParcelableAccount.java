/*
 * 				Twidere - Twitter client for Android
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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@CursorObject(valuesCreator = true)
@ParcelablePlease(allFields = false)
@JsonObject
public class ParcelableAccount implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "id")
    @CursorField(value = Accounts._ID, excludeWrite = true)
    public long id;

    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = Accounts.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    @CursorField(Accounts.SCREEN_NAME)
    public String screen_name;

    @ParcelableThisPlease
    @JsonField(name = "name")
    @CursorField(Accounts.NAME)
    public String name;

    @Nullable
    @Type
    @ParcelableThisPlease
    @JsonField(name = "account_type")
    @CursorField(Accounts.ACCOUNT_TYPE)
    public String account_type;

    @ParcelableThisPlease
    @JsonField(name = "profile_image_url")
    @CursorField(Accounts.PROFILE_IMAGE_URL)
    public String profile_image_url;

    @ParcelableThisPlease
    @JsonField(name = "profile_banner_url")
    @CursorField(Accounts.PROFILE_BANNER_URL)
    public String profile_banner_url;

    @ParcelableThisPlease
    @JsonField(name = "color")
    @CursorField(Accounts.COLOR)
    public int color;

    @ParcelableThisPlease
    @JsonField(name = "is_activated")
    @CursorField(Accounts.IS_ACTIVATED)
    public boolean is_activated;
    @Nullable
    @ParcelableThisPlease
    @JsonField(name = "account_user")
    @CursorField(value = Accounts.ACCOUNT_USER, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser account_user;

    public boolean is_dummy;

    public static final Creator<ParcelableAccount> CREATOR = new Creator<ParcelableAccount>() {
        @Override
        public ParcelableAccount createFromParcel(Parcel source) {
            ParcelableAccount target = new ParcelableAccount();
            ParcelableAccountParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableAccount[] newArray(int size) {
            return new ParcelableAccount[size];
        }
    };

    ParcelableAccount() {
    }

    public static ParcelableCredentials dummyCredentials() {
        final ParcelableCredentials credentials = new ParcelableCredentials();
        credentials.is_dummy = true;
        return credentials;
    }

    @Override
    public String toString() {
        return "ParcelableAccount{" +
                "id=" + id +
                ", account_key=" + account_key +
                ", screen_name='" + screen_name + '\'' +
                ", name='" + name + '\'' +
                ", account_type='" + account_type + '\'' +
                ", profile_image_url='" + profile_image_url + '\'' +
                ", profile_banner_url='" + profile_banner_url + '\'' +
                ", color=" + color +
                ", is_activated=" + is_activated +
                ", account_user=" + account_user +
                ", is_dummy=" + is_dummy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableAccount account = (ParcelableAccount) o;

        return account_key.equals(account.account_key);

    }

    @Override
    public int hashCode() {
        // Dummy account
        if (account_key == null) return 0;
        return account_key.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableAccountParcelablePlease.writeToParcel(this, dest, flags);
    }

    @AfterCursorObjectCreated
    @OnJsonParseComplete
    void afterObjectCreated() {
        if (account_user != null) {
            account_user.is_cache = true;
            account_user.account_color = color;
        }
    }

    @StringDef({Type.TWITTER, Type.STATUSNET, Type.FANFOU})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        String TWITTER = "twitter";
        String STATUSNET = "statusnet";
        String FANFOU = "fanfou";
    }
}
