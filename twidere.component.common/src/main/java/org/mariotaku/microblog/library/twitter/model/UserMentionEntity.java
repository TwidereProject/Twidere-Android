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
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class UserMentionEntity implements Parcelable {
    @JsonField(name = "indices", typeConverter = IndicesConverter.class)
    Indices indices;
    @JsonField(name = "id")
    String id;
    @JsonField(name = "name")
    String name;
    @JsonField(name = "screen_name")
    String screenName;

    public int getEnd() {
        return indices.getEnd();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public int getStart() {
        return indices.getStart();
    }

    @Override
    public String toString() {
        return "UserMentionEntity{" +
                "indices=" + indices +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserMentionEntityParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserMentionEntity> CREATOR = new Creator<UserMentionEntity>() {
        @Override
        public UserMentionEntity createFromParcel(Parcel source) {
            UserMentionEntity target = new UserMentionEntity();
            UserMentionEntityParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UserMentionEntity[] newArray(int size) {
            return new UserMentionEntity[size];
        }
    };
}
