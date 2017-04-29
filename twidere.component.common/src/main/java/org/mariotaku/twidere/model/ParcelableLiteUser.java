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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;

import static org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;

/**
 * Created by mariotaku on 2017/4/28.
 */
@ParcelablePlease
@JsonObject
@CursorObject(valuesCreator = true)
public class ParcelableLiteUser implements Parcelable {

    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    public UserKey account_key;

    @JsonField(name = "id", typeConverter = UserKeyConverter.class)
    @CursorField(value = CachedUsers.USER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey key;

    @JsonField(name = "name")
    @CursorField(CachedUsers.NAME)
    public String name;

    @JsonField(name = "screen_name")
    @CursorField(CachedUsers.SCREEN_NAME)
    public String screen_name;

    @JsonField(name = "profile_image_url")
    @CursorField(CachedUsers.PROFILE_IMAGE_URL)
    public String profile_image_url;

    @JsonField(name = "is_following")
    @CursorField(CachedUsers.IS_FOLLOWING)
    public boolean is_following;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableLiteUserParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableLiteUser> CREATOR = new Creator<ParcelableLiteUser>() {
        public ParcelableLiteUser createFromParcel(Parcel source) {
            ParcelableLiteUser target = new ParcelableLiteUser();
            ParcelableLiteUserParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableLiteUser[] newArray(int size) {
            return new ParcelableLiteUser[size];
        }
    };
}
